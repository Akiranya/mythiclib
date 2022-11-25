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
public class DamageManager implements Listener, AttackHandler {

    /**
     * External attack handlers
     */
    private final Set<AttackHandler> handlers = new HashSet<>();

    private static final AttributeModifier NO_KNOCKBACK = new AttributeModifier(UUID.randomUUID(), "noKnockback", 100, AttributeModifier.Operation.ADD_NUMBER);
    private static final double MINIMUM_DAMAGE = .001;

    private static final String
            ATTACK_METADATA_TAG = "AttackMetadata",
            OFFHAND_ATTACK_TAG = "OffhandAttack";

    public DamageManager() {
        handlers.add(this);
    }

    /**
     * Damage handlers are used by MythicLib to keep track of details of every
     * attack so that it can apply damage based stats like PvE damage, Magic
     * Damage...
     *
     * @param handler Damage handler being registered
     */
    public void registerHandler(AttackHandler handler) {
        Validate.notNull(handler, "Damage handler cannot be null");

        handlers.add(handler);
    }

    @Override
    @Nullable
    public AttackMetadata getAttack(EntityDamageEvent event) {
        for (MetadataValue value : event.getEntity().getMetadata(ATTACK_METADATA_TAG))
            if (value.getOwningPlugin().equals(MythicLib.plugin))
                return (AttackMetadata) value.value();
        return null;
    }

    /**
     * This marks the next EntityDamageEvent to be processed as
     * an offhand melee attack. That way plugins can use player
     * stats from the offhand instead of the main hand.
     * <p>
     * This method has the effect of caching the attack target.
     * <p>
     * Because of RDW we are not guaranteed that an EntityDamageEvent
     * will be called afterwards. Therefore, there is a simple timeout
     * system where if the attack is more than 1s old, it is ignored.
     *
     * @param target Entity being damaged.
     */
    public void registerOffHandAttack(Entity target) {
        target.setMetadata(OFFHAND_ATTACK_TAG, new FixedMetadataValue(MythicLib.plugin, System.currentTimeMillis()));
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
        target.setMetadata(ATTACK_METADATA_TAG, new FixedMetadataValue(MythicLib.plugin, metadata));
        applyDamage(Math.max(metadata.getDamage().getDamage(), MINIMUM_DAMAGE), target, metadata.getPlayer(), knockback, ignoreImmunity);
    }

    /**
     * Deals damage to an entity.
     *
     * @param attack         The class containing all info about the current attack
     * @param knockback      If the attack should deal knockback
     * @param ignoreImmunity The attack will not produce immunity frames.
     */
    public void registerAttack(@NotNull AttackMetadata attack, boolean knockback, boolean ignoreImmunity) {
        Validate.notNull(attack.getTarget(), "Target cannot be null"); // BW compatibility check
        attack.getTarget().setMetadata(ATTACK_METADATA_TAG, new FixedMetadataValue(MythicLib.plugin, attack));
        applyDamage(Math.max(attack.getDamage().getDamage(), MINIMUM_DAMAGE), attack.getTarget(), attack.isPlayer() ? ((PlayerMetadata) attack.getAttacker()).getPlayer() : null, knockback, ignoreImmunity);
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
     * This method is used to unregister MythicLib custom damage after everything
     * was calculated, hence MONITOR priority. As a safe practice, it does NOT
     * ignore cancelled damage events.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void unregisterCustomAttacks(EntityDamageByEntityEvent event) {

        // Ignore fake events from RDW/mcMMO/...
        if (event.getDamage() == 0)
            return;

        event.getEntity().removeMetadata(ATTACK_METADATA_TAG, MythicLib.plugin);
        event.getEntity().removeMetadata(OFFHAND_ATTACK_TAG, MythicLib.plugin);
    }

    /**
     * This method draws an interface between MythicLib damage mitigation system
     * and Bukkit damage events.
     * <p>
     * Unlike {@link #getAttack(EntityDamageEvent)} which can return a null object
     * if MythicLib cannot find an attack source, this method NEVER returns null. In
     * the worst case (unknown damage cause/damage not logged by any plugin) scenario,
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

        /*
         * Checks in the MythicLib attack registry. This is used by MMOItems skills,
         * MMOCore skills, scripts, items or any plugin using the ML damage system.
         */
        for (AttackHandler handler : handlers) {
            final AttackMetadata found = handler.getAttack(event);
            if (found != null && !found.hasExpired())
                return found;
        }

        // Players damaging Citizens NPCs are not registered
        if (entity.hasMetadata("NPC"))
            return null;

        // Attacks with a damager
        if (event instanceof EntityDamageByEntityEvent) {

            /*
             * Handles melee attacks. This is used everytime a player left clicks an entity.
             *
             * The attack damage type can vary depending on the context: if it is a bare-firsts
             * attack, final attack has no WEAPON damage type. If the player is holding any
             * other item, it is considered a WEAPON attack.
             */
            final Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof LivingEntity) {
                final EquipmentSlot hand = isBeingOffHandAttacked(event.getEntity()) ? EquipmentSlot.OFF_HAND : EquipmentSlot.MAIN_HAND;
                final StatProvider attacker = StatProvider.generate((LivingEntity) damager, hand);
                final AttackMetadata attackMeta = new MeleeAttackMetadata(new DamageMetadata(event.getDamage(), getDamageTypes((EntityDamageByEntityEvent) event, hand)), entity, attacker, hand);
                event.getEntity().setMetadata(ATTACK_METADATA_TAG, new FixedMetadataValue(MythicLib.plugin, attackMeta));
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
                    final AttackMetadata attackMeta = new ProjectileAttackMetadata(new DamageMetadata(event.getDamage(), DamageType.WEAPON, DamageType.PROJECTILE),
                            (LivingEntity) event.getEntity(), projectileData.getCaster(), projectile);
                    event.getEntity().setMetadata(ATTACK_METADATA_TAG, new FixedMetadataValue(MythicLib.plugin, attackMeta));
                    return attackMeta;
                }

                // Try to trace back the player source
                final ProjectileSource source = projectile.getShooter();
                if (source != null && !source.equals(event.getEntity()) && source instanceof LivingEntity) {
                    final StatProvider attacker = StatProvider.generate((LivingEntity) source, EquipmentSlot.MAIN_HAND);
                    final AttackMetadata attackMeta = new ProjectileAttackMetadata(new DamageMetadata(event.getDamage(), DamageType.WEAPON, DamageType.PROJECTILE),
                            (LivingEntity) event.getEntity(), attacker, projectile);
                    event.getEntity().setMetadata(ATTACK_METADATA_TAG, new FixedMetadataValue(MythicLib.plugin, attackMeta));
                    return attackMeta;
                }
            }
        }

        // Attacks with NO damager
        final @NotNull AttackMetadata vanillaAttack = new AttackMetadata(getVanillaDamageMetadata(event), entity, null);
        event.getEntity().setMetadata(ATTACK_METADATA_TAG, new FixedMetadataValue(MythicLib.plugin, vanillaAttack));
        return vanillaAttack;
    }

    @NotNull
    private DamageMetadata getVanillaDamageMetadata(EntityDamageEvent event) {
        switch (event.getCause()) {
            case MAGIC:
            case DRAGON_BREATH:
                return new DamageMetadata(event.getDamage(), DamageType.MAGIC);
            case POISON:
            case WITHER:
                return new DamageMetadata(event.getDamage(), DamageType.MAGIC, DamageType.DOT);
            case FIRE_TICK:
            case MELTING:
                return new DamageMetadata(event.getDamage(), DamageType.PHYSICAL, DamageType.DOT);
            case FALL:
            case THORNS:
            case CONTACT:
            case ENTITY_EXPLOSION:
            case ENTITY_SWEEP_ATTACK:
            case FALLING_BLOCK:
            case FLY_INTO_WALL:
            case BLOCK_EXPLOSION:
            case ENTITY_ATTACK:
                return new DamageMetadata(event.getDamage(), DamageType.PHYSICAL);
            case PROJECTILE:
                return new DamageMetadata(event.getDamage(), DamageType.PROJECTILE);
            default:
                return new DamageMetadata(event.getDamage());
        }
    }

    /**
     * @param event The attack event
     * @return The damage types of a vanilla melee entity attack
     */
    private DamageType[] getDamageTypes(EntityDamageByEntityEvent event, EquipmentSlot hand) {
        Validate.isTrue(event.getDamager() instanceof LivingEntity, "Not an entity attack");

        // Physical attack with bare fists.
        final LivingEntity damager = (LivingEntity) event.getDamager();
        if (isAir(damager.getEquipment().getItem(hand.toBukkit())))
            return new DamageType[]{DamageType.UNARMED, DamageType.PHYSICAL};

        // Weapon attack
        if (isWeapon(damager.getEquipment().getItem(hand.toBukkit()).getType()))
            return new DamageType[]{DamageType.WEAPON, DamageType.PHYSICAL};

        // Hitting with a random item
        return new DamageType[]{DamageType.PHYSICAL};
    }

    // Purely arbitrary but works decently
    private boolean isWeapon(Material mat) {
        return mat.getMaxDurability() > 0;
    }

    private boolean isAir(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    /**
     * @see {@link #registerOffHandAttack(Entity)}
     */
    private static final long OFFHAND_ATTACK_TIMEOUT = 100;

    private boolean isBeingOffHandAttacked(Entity entity) {
        for (MetadataValue value : entity.getMetadata(OFFHAND_ATTACK_TAG))
            if (value.getOwningPlugin().equals(MythicLib.plugin))
                return System.currentTimeMillis() - (long) value.value() < OFFHAND_ATTACK_TIMEOUT;
        return false;
    }
}
