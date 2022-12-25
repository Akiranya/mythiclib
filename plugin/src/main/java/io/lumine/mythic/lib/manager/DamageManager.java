package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.*;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.util.CustomProjectile;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Central piece of the MythicLib damage system. Since ML 1.4.3
 * attack metadatas are stored as entity metadata.
 *
 * @author jules
 */
public class DamageManager implements Listener {

    /**
     * External attack handlers
     */
    private final Set<AttackHandler> handlers = new HashSet<>();

    private static final AttributeModifier NO_KNOCKBACK = new AttributeModifier(UUID.randomUUID(), "noKnockback", 100, AttributeModifier.Operation.ADD_NUMBER);

    private static final String ATTACK_METADATA_TAG = "AttackMetadata";

    /**
     * Attack handlers are used by MythicLib to keep track of details of every
     * attack so that it can apply damage based stats like PvE damage, Magic
     * Damage...
     *
     * @param handler Damage handler being registered
     */
    public void registerHandler(AttackHandler handler) {
        Validate.notNull(handler, "Damage handler cannot be null");

        handlers.add(handler);
    }

    @Deprecated
    public void damage(@NotNull AttackMetadata metadata, @NotNull LivingEntity target) {
        damage(metadata, target, true);
    }

    /**
     * Forces a player to damage an entity with knockback
     *
     * @param metadata The class containing all info about the current attack
     */
    public void registerAttack(@NotNull AttackMetadata metadata) {
        registerAttack(metadata, true, false);
    }

    @Deprecated
    public void damage(@NotNull AttackMetadata metadata, @NotNull LivingEntity target, boolean knockback) {
        damage(metadata, target, knockback, false);
    }

    /**
     * Forces a player to damage an entity with (no) knockback
     *
     * @param metadata  The class containing all info about the current attack
     * @param knockback If the attack should deal knockback
     */
    public void registerAttack(@NotNull AttackMetadata metadata, boolean knockback) {
        registerAttack(metadata, knockback, false);
    }

    @Deprecated
    public void damage(@NotNull AttackMetadata metadata, @NotNull LivingEntity target, boolean knockback, boolean ignoreImmunity) {
        registerAttack(new AttackMetadata(metadata.getDamage(), target, metadata.getAttacker()), knockback, ignoreImmunity);
    }

    /**
     * Deals damage to an entity. Does not do anything if the
     * damage is negative or null.
     *
     * @param attack         The class containing all info about the current attack
     * @param knockback      If the attack should deal knockback
     * @param ignoreImmunity The attack will not produce immunity frames.
     */
    public void registerAttack(@NotNull AttackMetadata attack, boolean knockback, boolean ignoreImmunity) {
        final double damage = attack.getDamage().getDamage();
        if (damage <= 0)
            return;

        Validate.notNull(attack.getTarget(), "Target cannot be null"); // BW compatibility check
        markAsMetadata(attack);
        applyDamage(attack.getDamage().getDamage(), attack.getTarget(), attack.isPlayer() ? ((PlayerMetadata) attack.getAttacker()).getPlayer() : null, knockback, ignoreImmunity);
    }

    private void applyDamage(double damage, @NotNull LivingEntity target, @Nullable Player damager, boolean knockback, boolean ignoreImmunity) {

        // Should knockback be applied
        if (!knockback) {
            final AttributeInstance instance = target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            try {
                instance.addModifier(NO_KNOCKBACK);
                applyDamage(damage, target, damager, true, ignoreImmunity);
            } catch (Exception anyError) {
                MythicLib.plugin.getLogger().log(Level.SEVERE, "An error occured while registering player damage");
                anyError.printStackTrace();
            } finally {
                instance.removeModifier(NO_KNOCKBACK);
            }

            // Should damage immunity be taken into account
        } else if (ignoreImmunity) {
            final int noDamageTicks = target.getNoDamageTicks();
            try {
                target.setNoDamageTicks(0);
                applyDamage(damage, target, damager, true, false);
            } catch (Exception anyError) {
                MythicLib.plugin.getLogger().log(Level.SEVERE, "An error occured while registering player damage");
                anyError.printStackTrace();
            } finally {
                target.setNoDamageTicks(noDamageTicks);
            }

            // Just damage entity
        } else {
            if (damager == null)
                target.damage(damage);
            else
                target.damage(damage, damager);
        }
    }

    /**
     * This method draws an interface between MythicLib damage mitigation system
     * and Bukkit damage events.
     * <p>
     * In the worst case (unknown damage cause/damage not logged by any plugin) scenario,
     * it just returns a damage metadata with no damage type which is completely fine.
     *
     * @param event The damage event
     * @return The corresponding MythicLib damage metadata.
     * @deprecated Use {@link #findAttack(EntityDamageEvent)} which provides more information
     */
    @NotNull
    @Deprecated
    public DamageMetadata findDamage(EntityDamageEvent event) {
        return findAttack(event).getDamage();
    }

    /**
     * Very important method. Looks for a RegisteredAttack that would have been registered
     * by other plugins ie MMOItems abilities, or MMOCore skills, or any other plugin.
     * <p>
     * If it can't find any plugin that has registered an attack, it checks if it is simply
     * not just a vanilla attack: projectile or melee attacks. If SO it registers this new
     * attack meta within MythicLib to make sure the same attackMeta is provided later on.
     *
     * @param event The attack event
     * @return Null if MythicLib cannot find the attack source, some attack meta otherwise.
     */
    @NotNull
    public AttackMetadata findAttack(EntityDamageEvent event) {
        Validate.isTrue(event.getEntity() instanceof LivingEntity, "Target entity is not living");
        final LivingEntity entity = (LivingEntity) event.getEntity();

        // MythicLib attack registry
        for (MetadataValue value : event.getEntity().getMetadata(ATTACK_METADATA_TAG))
            if (value.getOwningPlugin().equals(MythicLib.plugin))
                return (AttackMetadata) value.value();

        // Attack registries from other plugins
        for (AttackHandler handler : handlers) {
            final AttackMetadata found = handler.getAttack(event);
            if (found != null && !found.hasExpired()) {
                markAsMetadata(found);
                return found;
            }
        }

        // Attacks with a damager
        if (event instanceof EntityDamageByEntityEvent) {

            /*
             * Handles melee attacks. This is used everytime a player left clicks an entity.
             *
             * The attack damage type can vary depending on the context: if it is a bare-firsts
             * attack, final attack has no WEAPON damage type. If the player is holding any
             * other item, it is considered a WEAPON attack.
             *
             * If MythicLib reaches this portion of the code this means that the attack is with
             * the right hand. Left-hand attacks are handled by specific listeners.
             */
            final Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof LivingEntity) {
                final StatProvider attacker = StatProvider.get((LivingEntity) damager, EquipmentSlot.MAIN_HAND, true);
                final AttackMetadata attackMeta = new MeleeAttackMetadata(new DamageMetadata(event.getDamage(), getVanillaDamageTypes((EntityDamageByEntityEvent) event, EquipmentSlot.MAIN_HAND)), entity, attacker);
                markAsMetadata(attackMeta);
                return attackMeta;
            }

            /*
             * Handles projectile attacks; used everytime when a player shoots a trident,
             * a bow, a crossbow or even eggs and snowballs.
             *
             * Notice this is always the same damage type: WEAPON, PHYSICAL, PROJECTILE
             * which means that if MMOCore has a skill which makes players shoot multiple
             * arrows, MythicLib will use the following lines to monitor the attacks
             * and the skill will apply WEAPON damage.
             *
             * Make sure to check the shooter is not the damaged entity. We don't want
             * players to backstab themselves using projectiles.
             */
            else if (damager instanceof Projectile) {

                // First tries to find original CustomProjectile
                final Projectile projectile = (Projectile) damager;
                final @Nullable CustomProjectile projectileData = MythicLib.plugin.getEntities().getCustomProjectile(projectile);
                if (projectileData != null) {
                    final AttackMetadata attackMeta = new ProjectileAttackMetadata(new DamageMetadata(event.getDamage(), DamageType.WEAPON, DamageType.PHYSICAL, DamageType.PROJECTILE),
                            (LivingEntity) event.getEntity(), projectileData.getCaster(), projectile);
                    markAsMetadata(attackMeta);
                    return attackMeta;
                }

                // Try to trace back the player source
                final ProjectileSource source = projectile.getShooter();
                if (source != null && !source.equals(event.getEntity()) && source instanceof LivingEntity) {
                    final StatProvider attacker = StatProvider.get((LivingEntity) source, EquipmentSlot.MAIN_HAND, true);
                    final AttackMetadata attackMeta = new ProjectileAttackMetadata(new DamageMetadata(event.getDamage(), DamageType.WEAPON, DamageType.PHYSICAL, DamageType.PROJECTILE),
                            (LivingEntity) event.getEntity(), attacker, projectile);
                    markAsMetadata(attackMeta);
                    return attackMeta;
                }
            }
        }

        // Attacks with NO damager
        final @NotNull AttackMetadata vanillaAttack = new AttackMetadata(new DamageMetadata(event.getDamage(), getVanillaDamageTypes(event)), entity, null);
        markAsMetadata(vanillaAttack);
        return vanillaAttack;
    }

    /**
     * Registers the attackMetadata inside of the entity metadata.
     * This does NOT apply any damage to the target entity.
     *
     * @param attackMeta Attack metadata being registered
     */
    public void markAsMetadata(AttackMetadata attackMeta) {
        attackMeta.getTarget().setMetadata(ATTACK_METADATA_TAG, new FixedMetadataValue(MythicLib.plugin, attackMeta));
    }

    /**
     * @param event Attack event
     * @return The damage types of a vanilla attack
     */
    @NotNull
    public DamageType[] getVanillaDamageTypes(EntityDamageEvent event) {
        return getVanillaDamageTypes(event.getCause());
    }

    /**
     * @param cause Cause of the attack
     * @return The damage types of a vanilla attack
     */
    @NotNull
    public DamageType[] getVanillaDamageTypes(EntityDamageEvent.DamageCause cause) {
        switch (cause) {
            case MAGIC:
            case DRAGON_BREATH:
                return new DamageType[]{DamageType.MAGIC};
            case POISON:
            case WITHER:
                return new DamageType[]{DamageType.MAGIC, DamageType.DOT};
            case FIRE_TICK:
            case MELTING:
                return new DamageType[]{DamageType.PHYSICAL, DamageType.DOT};
            case FALL:
            case THORNS:
            case CONTACT:
            case ENTITY_EXPLOSION:
            case ENTITY_SWEEP_ATTACK:
            case FALLING_BLOCK:
            case FLY_INTO_WALL:
            case BLOCK_EXPLOSION:
            case ENTITY_ATTACK:
            case SUFFOCATION:
            case CRAMMING:
            case DROWNING:
                return new DamageType[]{DamageType.PHYSICAL};
            case PROJECTILE:
                return new DamageType[]{DamageType.PHYSICAL, DamageType.PROJECTILE};
            default:
                return new DamageType[0];
        }
    }

    /**
     * @param event Attack event
     * @param hand  Hand used to perform the attack
     * @return The damage types of a vanilla melee entity attack
     */
    @NotNull
    public DamageType[] getVanillaDamageTypes(EntityDamageByEntityEvent event, EquipmentSlot hand) {
        Validate.isTrue(event.getDamager() instanceof LivingEntity, "Not an entity attack");
        return getVanillaDamageTypes((LivingEntity) event.getDamager(), event.getCause(), hand);
    }

    /**
     * @param damager Entity attacking
     * @param cause   Cause of the attack
     * @param hand    Hand used to perform the attack
     * @return The damage types of a vanilla melee entity attack
     */
    @NotNull
    public DamageType[] getVanillaDamageTypes(@NotNull LivingEntity damager, @NotNull EntityDamageEvent.DamageCause cause, @NotNull EquipmentSlot hand) {

        // Not an entity attack
        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
            return new DamageType[]{DamageType.PHYSICAL};

        // Physical attack with bare fists.
        if (isAir(damager.getEquipment().getItem(hand.toBukkit())))
            return new DamageType[]{DamageType.UNARMED, DamageType.PHYSICAL};

        // Weapon attack
        if (isWeapon(damager.getEquipment().getItem(hand.toBukkit()).getType()))
            return new DamageType[]{DamageType.WEAPON, DamageType.PHYSICAL};

        // Hitting with a random item
        return new DamageType[]{DamageType.PHYSICAL};
    }

    /**
     * This method is used to unregister MythicLib custom damage after everything
     * was calculated, hence MONITOR priority. As a safe practice, it does NOT
     * ignore cancelled damage events.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void unregisterCustomAttacks(EntityDamageByEntityEvent event) {

        // Ignore fake events from RDW/mcMMO/...
        if (event.getDamage() == 0)
            return;

        event.getEntity().removeMetadata(DamageManager.ATTACK_METADATA_TAG, MythicLib.plugin);
    }

    // Purely arbitrary but works decently
    private boolean isWeapon(Material mat) {
        return mat.getMaxDurability() > 0;
    }

    private boolean isAir(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }
}
