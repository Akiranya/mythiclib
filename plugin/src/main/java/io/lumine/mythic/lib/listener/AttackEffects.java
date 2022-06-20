package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.stat.SharedStat;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.listener.event.PlayerAttackEventListener;
import io.lumine.mythic.lib.player.cooldown.CooldownType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Random;

public class AttackEffects implements Listener {

    // Critical strike configs
    private double weaponCritCoef, skillCritCoef, maxWeaponCritChance, maxSkillCritChance, weaponCritCooldown, skillCritCooldown;

    private static final Random RANDOM = new Random();

    public AttackEffects() {
        reload();
    }

    public void reload() {
        weaponCritCoef = MythicLib.plugin.getConfig().getDouble("critical-strikes.weapon.coefficient", 2);
        skillCritCoef = MythicLib.plugin.getConfig().getDouble("critical-strikes.skill.coefficient", 1.5);

        maxWeaponCritChance = MythicLib.plugin.getConfig().getDouble("critical-strikes.weapon.max-chance", 80);
        maxSkillCritChance = MythicLib.plugin.getConfig().getDouble("critical-strikes.skill.max-chance", 80);

        weaponCritCooldown = MythicLib.plugin.getConfig().getDouble("critical-strikes.weapon.cooldown", 3);
        skillCritCooldown = MythicLib.plugin.getConfig().getDouble("critical-strikes.skill.cooldown", 3);
    }

    public double getMaxWeaponCritChance() {
        return maxWeaponCritChance;
    }

    /**
     * See how easy it is to just listen to any player
     * attack and apply on-hit attack effects now??
     *
     * @see {@link PlayerAttackEventListener}
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHitAttackEffects(PlayerAttackEvent event) {
        StatProvider stats = event.getAttack();

        // Apply specific damage increase
        for (DamageType type : DamageType.values())
            event.getDamage().additiveModifier(stats.getStat(type.getOffenseStat()) / 100, type);

        // Apply undead damage
        if (MythicLib.plugin.getVersion().getWrapper().isUndead(event.getEntity()))
            event.getDamage().additiveModifier(stats.getStat("UNDEAD_DAMAGE") / 100);

        // Apply PvP or PvE damage, one of the two anyways.
        event.getDamage().additiveModifier(stats.getStat(event.getEntity() instanceof Player ? "PVP_DAMAGE" : "PVE_DAMAGE") / 100);

        // Weapon critical strikes
        if ((event.getDamage().hasType(DamageType.WEAPON) || event.getDamage().hasType(DamageType.UNARMED))
                && RANDOM.nextDouble() <= Math.min(stats.getStat("CRITICAL_STRIKE_CHANCE"), maxWeaponCritChance) / 100
                && !event.getData().isOnCooldown(CooldownType.WEAPON_CRIT)) {
            event.getData().applyCooldown(CooldownType.WEAPON_CRIT, weaponCritCooldown);

            // Works for both weapon and unarmed damage
            double damageMultiplicator = weaponCritCoef + stats.getStat("CRITICAL_STRIKE_POWER") / 100;
            event.getDamage().multiplicativeModifier(damageMultiplicator, DamageType.WEAPON);
            event.getDamage().multiplicativeModifier(damageMultiplicator, DamageType.UNARMED);

            event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
            applyCritEffects(event.getEntity(), Particle.CRIT, 32, .4f);
        }

        // Skill critical strikes
        if (event.getDamage().hasType(DamageType.SKILL)
                && RANDOM.nextDouble() <= Math.min(stats.getStat("SKILL_CRITICAL_STRIKE_CHANCE"), maxSkillCritChance) / 100
                && !event.getData().isOnCooldown(CooldownType.SKILL_CRIT)) {
            event.getData().applyCooldown(CooldownType.SKILL_CRIT, skillCritCooldown);
            event.getDamage().multiplicativeModifier(skillCritCoef + stats.getStat("SKILL_CRITICAL_STRIKE_POWER") / 100, DamageType.SKILL);
            event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 2);
            applyCritEffects(event.getEntity(), Particle.TOTEM, 16, .4f);
        }

        // Apply spell vamp and lifesteal
        double heal = (event.getAttack().getDamage().getDamage(DamageType.WEAPON) * event.getAttack().getStat(SharedStat.LIFESTEAL)
                + event.getAttack().getDamage().getDamage(DamageType.SKILL) * event.getAttack().getStat(SharedStat.SPELL_VAMPIRISM)) / 100;
        if (heal > 0)
            UtilityMethods.heal(event.getPlayer(), heal);
    }

    private void applyCritEffects(Entity entity, Particle particle, int amount, double speed) {
        Location loc = entity.getLocation().add(0, entity.getHeight() / 2, 0);
        double offset = entity.getBoundingBox().getWidthX() / 2;
        entity.getWorld().spawnParticle(particle, loc, amount, offset, offset, offset, speed);
    }
}
