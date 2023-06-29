package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.damage.ProjectileAttackMetadata;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

/**
 * The one thing about projectiles is that they create some type of
 * delay between the attack action (when shooting the trident or
 * the arrow) and the actual damage. This means the player stats
 * and abilities need to be cached on item use.
 * <p>
 * This class triggers the right skills when a projectile flies into
 * the air/hits the ground or an entity.
 * <p>
 * This is used for any type of projectile: arrows, tridents, eggs,
 * snowballs.
 *
 * @author indyuce
 */
public class CustomProjectile extends TemporaryListener {
    private final int entityId;
    private final BukkitRunnable runnable;
    private final ProjectileType projectileType;

    /**
     * Used to cache the caster skills. If the skills are not cached, the player skill
     * list can actually be edited (hand swapping or external plugins) while the projectile
     * is still in midair which will change the projectile behaviour. The very same
     * glitch is being fixed by {@link PlayerMetadata}
     */
    private final Iterable<PassiveSkill> cachedSkills;
    private final PlayerMetadata caster;

    public static final String METADATA_KEY = "CustomProjectile";

    /**
     * Used to trigger skills related to projectiles (either arrows or tridents). This
     * class is instanciated when a player shoots a projectile and triggers TICK, HIT
     * and LAND skills
     *
     * @param caster         Player triggering the skills
     * @param projectileType See {@link ProjectileType}
     * @param projectile     Type of projectile being shot
     * @param hand           Hand being used to shoot the projectile
     */
    public CustomProjectile(MMOPlayerData caster, ProjectileType projectileType, Entity projectile, EquipmentSlot hand) {
        super(ProjectileHitEvent.getHandlerList(), EntityDeathEvent.getHandlerList(), PlayerQuitEvent.getHandlerList(), PlayerAttackEvent.getHandlerList());

        this.entityId = projectile.getEntityId();
        this.projectileType = projectileType;

        // Cache important stuff
        this.caster = caster.getStatMap().cache(hand);
        this.cachedSkills = caster.getPassiveSkillMap().isolateModifiers(hand);

        // Trigger skills
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                caster.triggerSkills(projectileType.getTickTrigger(), CustomProjectile.this.caster, cachedSkills, projectile);
            }
        };
        runnable.runTaskTimer(MythicLib.plugin, 0, 1);

        // Register
        projectile.setMetadata(METADATA_KEY, new FixedMetadataValue(MythicLib.plugin, this));
    }

    public PlayerMetadata getCaster() {
        return caster;
    }

    @EventHandler
    public void unregisterOnHit(ProjectileHitEvent event) {
        if (event.getEntity().getEntityId() == entityId)
            // Close with delay to make sure skills are triggered on hit/land
            Bukkit.getScheduler().runTask(MythicLib.plugin, () -> close());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void triggerHit(PlayerAttackEvent event) {
        if (event.getAttack() instanceof ProjectileAttackMetadata && ((ProjectileAttackMetadata) event.getAttack()).getProjectile().getEntityId() == entityId)
            caster.getData().triggerSkills(projectileType.getHitTrigger(), caster, cachedSkills, event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void triggerLand(ProjectileHitEvent event) {

        // Make sure the projectile landed on a block
        if (event.getHitBlock() != null && event.getEntity().getEntityId() == entityId)
            caster.getData().triggerSkills(projectileType.getLandTrigger(), caster, cachedSkills, event.getEntity());
    }

    @EventHandler
    public void unregisterOnDeath(EntityDeathEvent event) {
        if (event.getEntity().getEntityId() == entityId)
            close();
    }

    @EventHandler
    public void unregisterOnLogout(PlayerQuitEvent event) {
        if (event.getPlayer().getUniqueId().equals(caster.getData().getUniqueId()))
            close();
    }

    @Override
    public void whenClosed() {
        runnable.cancel();
    }

    public enum ProjectileType {
        ARROW,
        TRIDENT;

        private final TriggerType tick, hit, land;

        ProjectileType() {
            tick = new TriggerType(name() + "_TICK");
            hit = new TriggerType(name() + "_HIT");
            land = new TriggerType(name() + "_LAND");
        }

        public TriggerType getTickTrigger() {
            return tick;
        }

        public TriggerType getHitTrigger() {
            return hit;
        }

        public TriggerType getLandTrigger() {
            return land;
        }
    }

    @Nullable
    public static CustomProjectile getCustomData(Entity proj) {
        for (MetadataValue mv : proj.getMetadata(METADATA_KEY))
            if (mv.getOwningPlugin().equals(MythicLib.plugin))
                return (CustomProjectile) mv.value();
        return null;
    }
}
