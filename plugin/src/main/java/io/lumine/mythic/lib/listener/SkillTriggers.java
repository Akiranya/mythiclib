package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.event.PlayerKillEntityEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.CustomProjectile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SkillTriggers implements Listener {
    public SkillTriggers() {
        Bukkit.getScheduler().runTaskTimer(MythicLib.plugin, () -> MMOPlayerData.forEachOnline(online -> online.getPassiveSkillMap().tickTimerSkills()), 0, 1);
    }

    @EventHandler
    public void killEntity(PlayerKillEntityEvent event) {
        event.getData().triggerSkills(TriggerType.KILL_ENTITY, event.getTarget());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void attack(PlayerAttackEvent event) {
        event.getAttacker().getData().triggerSkills(TriggerType.ATTACK, event.getAttacker(), event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void damagedByEntity(EntityDamageByEntityEvent event) {
        final MMOPlayerData caster;
        if (event.getEntity() instanceof Player && (caster = MMOPlayerData.getOrNull(event.getEntity().getUniqueId())) != null
                && MythicLib.plugin.getEntities().canTarget((Player) event.getEntity(), event.getDamager(), InteractionType.OFFENSE_SKILL))
            caster.triggerSkills(TriggerType.DAMAGED_BY_ENTITY, event.getDamager());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void damaged(EntityDamageEvent event) {
        final MMOPlayerData caster;
        if (event.getEntity() instanceof Player && (caster = MMOPlayerData.getOrNull(event.getEntity().getUniqueId())) != null)
            caster.triggerSkills(TriggerType.DAMAGED, null);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void death(PlayerDeathEvent event) {
        final MMOPlayerData caster;
        if ((caster = MMOPlayerData.getOrNull(event.getEntity().getUniqueId())) != null && caster.isOnline())
            // Check if caster is online as DeluxeCombat calls this event while the player has already logged off
            caster.triggerSkills(TriggerType.DEATH, null);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void login(PlayerJoinEvent event) {
        MMOPlayerData.get(event.getPlayer()).triggerSkills(TriggerType.LOGIN, null);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void shootBow(EntityShootBowEvent event) {
        final MMOPlayerData caster;
        if (event.getEntity() instanceof Player && (caster = MMOPlayerData.getOrNull(event.getEntity().getUniqueId())) != null) {
            final EquipmentSlot actionHand = getShootHand(((Player) event.getEntity()).getInventory());
            caster.triggerSkills(TriggerType.SHOOT_BOW, actionHand, event.getProjectile());

            // Register a runnable to trigger projectile skills
            new CustomProjectile(caster, CustomProjectile.ProjectileType.ARROW, event.getProjectile(), actionHand);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void shootTrident(ProjectileLaunchEvent event) {
        final MMOPlayerData caster;
        if (event.getEntity() instanceof Trident && event.getEntity().getShooter() instanceof Player && (caster = MMOPlayerData.getOrNull(event.getEntity().getUniqueId())) != null) {
            final Player shooter = (Player) event.getEntity().getShooter();
            final EquipmentSlot actionHand = getShootHand(shooter.getInventory());
            caster.triggerSkills(TriggerType.SHOOT_TRIDENT, actionHand, event.getEntity());

            // Register a runnable to trigger projectile skills
            new CustomProjectile(caster, CustomProjectile.ProjectileType.TRIDENT, event.getEntity(), actionHand);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void sneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking())
            MMOPlayerData.get(event.getPlayer()).triggerSkills(TriggerType.SNEAK, null);
    }

    /**
     * @implNote {@link Cancellable#isCancelled()} does not work with PlayerInteractEvent
     *         because there are now two possible ways to cancel the event, either
     *         by canceling the item interaction, either by canceling the block interaction.
     *         <p>
     *         Checking if the event is cancelled points towards the block interaction
     *         and not the item interaction which is NOT what MythicLib is interested in
     * @implNote Scrap this, it's 100% useless to check if the event is cancelled.
     *         It makes sense to trigger skills even if the item or block interactions are canceled
     * @implNote Event priority set to {@link EventPriority#LOW} because MI consumes consumables on
     *         priority NORMAL and item abilities require the held item not to be null in hand
     */
    @EventHandler(priority = EventPriority.LOW)
    public void click(PlayerInteractEvent event) {
        // || event.useItemInHand() == Event.Result.DENY
        if (event.getAction() == Action.PHYSICAL)
            return;

        final MMOPlayerData caster = MMOPlayerData.get(event.getPlayer());
        final boolean left = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK, sneaking = event.getPlayer().isSneaking();
        final TriggerType type = sneaking ? (left ? TriggerType.SHIFT_LEFT_CLICK : TriggerType.SHIFT_RIGHT_CLICK) : (left ? TriggerType.LEFT_CLICK : TriggerType.RIGHT_CLICK);
        caster.triggerSkills(type, EquipmentSlot.fromBukkit(event.getHand()), null);
    }

    /**
     * @return Hand used to shoot a projectile (arrow/trident) based on
     *         what items the player is holding in his two hands
     */
    private EquipmentSlot getShootHand(PlayerInventory inv) {
        final ItemStack main = inv.getItemInMainHand();
        return main != null && isShootable(main.getType()) ? EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND;
    }

    private boolean isShootable(Material mat) {
        return mat == Material.BOW || mat == Material.CROSSBOW || mat == Material.TRIDENT;
    }
}
