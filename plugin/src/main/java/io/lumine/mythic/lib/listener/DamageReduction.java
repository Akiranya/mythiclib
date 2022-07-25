package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.matheclipse.commons.parser.client.eval.DoubleEvaluator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;

public class DamageReduction implements Listener {

    /**
     * Since MythicMobs is a soft depend, this event triggers
     * correctly, fixing a bug with MythicMobs skill mechanics.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void damageMitigation(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || event.getEntity().hasMetadata("NPC"))
            return;

        // Find the damageMeta. ML doesn't need an attackMeta here.
        MMOPlayerData data = MMOPlayerData.get((OfflinePlayer) event.getEntity());
        DamageMetadata damageMeta = MythicLib.plugin.getDamage().findDamage(event);

        // Applies specific damage reduction
        for (SpecificDamageReductionType type : SpecificDamageReductionType.values())
            type.applyReduction(data.getStatMap(), damageMeta, event);

        // Applies damage reduction for existing damage types
        for (DamageType damageType : DamageType.values())
            damageMeta.multiplicativeModifier(1 - data.getStatMap().getStat(damageType + "_DAMAGE_REDUCTION") / 100, damageType);

        // Applies the Defense stat
        double defense = data.getStatMap().getStat("DEFENSE");
        double damage = damageMeta.getDamage();
        if (defense > 0)
            damage = new DefenseFormula(defense).getAppliedDamage(damage);

        // Finally apply damage
        event.setDamage(damage);
    }

    /**
     * Used for calculating damage mitigation due to the defense stat.
     */
    public class DefenseFormula {
        private final double defense;

        public DefenseFormula(double defense) {
            this.defense = defense;
        }

        public double getAppliedDamage(double damage) {
            String formula = MythicLib.plugin.getConfig().getString("defense-application", "#damage# * (1 - (#defense# / (#defense# + 100)))");
            formula = formula.replace("#defense#", String.valueOf(defense));
            formula = formula.replace("#damage#", String.valueOf(damage));

            try {
                return Math.max(0, new DoubleEvaluator().evaluate(formula));
            } catch (RuntimeException exception) {

                /**
                 * Formula won't evaluate if hanging #'s or unparsed placeholders. Send a
                 * friendly warning to console and just return the default damage.
                 */
                MythicLib.inst().getLogger()
                        .log(Level.WARNING, "Could not evaluate defense formula, please check config.");
                return damage;
            }
        }
    }

    private static final Set<EntityDamageEvent.DamageCause> FIRE_DAMAGE_CAUSES
            = new HashSet<>(Arrays.asList(EntityDamageEvent.DamageCause.FIRE, EntityDamageEvent.DamageCause.FIRE_TICK, EntityDamageEvent.DamageCause.LAVA, EntityDamageEvent.DamageCause.MELTING));

    /**
     * Damage reduction types which do NOT depend on an
     * existing MythicLib damage type
     */
    public enum SpecificDamageReductionType {

        // Damage reduction, always applies
        ENVIRONMENTAL("DAMAGE_REDUCTION", event -> true),

        // Vanilla damage types
        PVP(event -> event instanceof EntityDamageByEntityEvent && getDamager((EntityDamageByEntityEvent) event) instanceof Player),
        PVE(event -> event instanceof EntityDamageByEntityEvent && !(getDamager((EntityDamageByEntityEvent) event) instanceof Player)),
        FIRE(event -> FIRE_DAMAGE_CAUSES.contains(event.getCause())),
        FALL(event -> event.getCause() == EntityDamageEvent.DamageCause.FALL);

        /**
         * The corresponding item stat that will be used to
         * apply damage reduction. For instance, ENVIRONMENTAL calls
         * DAMAGE_REDUCTION and MAGIC calls MAGIC_DAMAGE_REDUCTION
         */
        @NotNull
        private final String stat;

        /**
         * When this field is not null, if it does return true, it will reduce
         * all the damage from every damage packet. This is used for vanilla
         * damage types, like {@link #FALL} or {@link #FIRE} or even {@link #ENVIRONMENTAL}
         */
        @NotNull
        private final Predicate<EntityDamageEvent> apply;

        SpecificDamageReductionType(String stat, Predicate<EntityDamageEvent> apply) {
            this.stat = stat;
            this.apply = Objects.requireNonNull(apply);
        }

        SpecificDamageReductionType(Predicate<EntityDamageEvent> apply) {
            this.stat = name() + "_DAMAGE_REDUCTION";
            this.apply = Objects.requireNonNull(apply);
        }

        public void applyReduction(StatMap statMap, DamageMetadata damageMeta, EntityDamageEvent event) {
            if (apply.test(event))
                damageMeta.multiplicativeModifier(1 - statMap.getStat(stat) / 100);
        }
    }

    /**
     * Tries to find the entity who dealt the damage in some attack event. Main issue is that
     * if it is a ranged attack like a trident or an arrow, we have to find back the shooter.
     */
    private static LivingEntity getDamager(EntityDamageByEntityEvent event) {

        // Check direct damager
        if (event.getDamager() instanceof LivingEntity)
            return (LivingEntity) event.getDamager();

        /*
         * Checks projectile and add damage type, which supports every vanilla
         * projectile like snowballs, tridents and arrows
         */
        if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof LivingEntity)
                return (LivingEntity) proj.getShooter();
        }

        return null;
    }
}
