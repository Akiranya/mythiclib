package io.lumine.mythic.lib.skill.handler.def.passive;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.util.ParabolicProjectile;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Power_Mark extends SkillHandler<AttackSkillResult> implements Listener {
    public Power_Mark() {
        super(false);

        registerModifiers("duration", "stun", "ratio");
    }

    @Override
    public AttackSkillResult getResult(SkillMetadata meta) {
        return new AttackSkillResult(meta);
    }

    @Override
    public void whenCast(AttackSkillResult result, SkillMetadata skillMeta) {
        new PowerMark(skillMeta.getCaster(), skillMeta, skillMeta.getTargetEntity().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(PlayerAttackEvent event) {
        if (!event.getAttack().getDamage().hasType(DamageType.WEAPON))
            return;

        PassiveSkill skill = event.getAttacker().getData().getPassiveSkillMap().getSkill(this);
        if (skill == null)
            return;

        skill.getTriggeredSkill().cast(new TriggerMetadata(event.getAttacker(), event.getEntity()));
    }

    public class PowerMark extends BukkitRunnable implements Listener {
        private final PlayerMetadata caster;
        private final Location loc;

        private final double duration;
        private final double ratio;
        private double stun;

        private double accumulate;
        private int j;

        public PowerMark(PlayerMetadata caster, SkillMetadata skillMeta, Location loc) {
            this.caster = caster;
            this.loc = loc;

            loc.getWorld().playSound(loc, Sound.BLOCK_END_PORTAL_FRAME_FILL, 2, 1);

            duration = skillMeta.getModifier("duration");
            ratio = skillMeta.getModifier("ratio") / 100;
            stun = skillMeta.getModifier("stun");

            runTaskTimer(MythicLib.plugin, 0, 1);
            Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
        }

        private void unregister() {
            PlayerAttackEvent.getHandlerList().unregister(this);
            cancel();
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void stackDamage(PlayerAttackEvent event) {
            if (!event.isCancelled() && j < 20 * (duration - 2) && radiusCheck(event.getEntity().getLocation()) && event.getAttacker().getPlayer().equals(caster.getPlayer())) {
                accumulate += event.getAttack().getDamage().getDamage() * ratio;
                new ParabolicProjectile(event.getEntity().getLocation().add(0, event.getEntity().getHeight() / 2, 0), loc, () -> loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1), Color.PURPLE);
            }
        }

        private boolean radiusCheck(Location loc) {
            return loc.getWorld().equals(this.loc.getWorld()) && loc.distanceSquared(this.loc) < 16;
        }

        @Override
        public void run() {
            if (j++ > duration * 20) {
                unregister();

                for (double a = 0; a < Math.PI * 2; a += Math.PI * 2 / 17)
                    new ParabolicProjectile(loc, loc.clone().add(6 * Math.cos(a), 0, 6 * Math.sin(a)), Particle.SPELL_WITCH);

                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 0);
                loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.clone().add(0, 1, 0), 16, 2, 2, 2, 0);
                loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc.clone().add(0, 1, 0), 24, 0, 0, 0, .3f);

                stun += Math.log(Math.max(1, accumulate - 10)) / 8;

                for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                    if (entity.getLocation().distanceSquared(loc) < 25 && UtilityMethods.canTarget(caster.getPlayer(), entity)) {
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (stun * 20), 10, false, false));
                        caster.attack((LivingEntity) entity, accumulate, DamageType.SKILL, DamageType.MAGIC);
                        entity.setVelocity(format(entity.getLocation().subtract(loc).toVector().setY(0)).setY(.3));
                    }
                return;
            }

            if (j % 2 == 0 && j > 20 * (duration - 2))
                loc.getWorld().playSound(loc, VersionSound.BLOCK_NOTE_BLOCK_PLING.toSound(), 1, (float) (1 + (j - 20 * (duration - 2)) / 40));

            double a = (double) j / 16;
            double r = Math.sqrt(Math.min(duration * 2 - (double) j / 10, 4)) * 2;
            for (double k = 0; k < Math.PI * 2; k += Math.PI * 2 / 5)
                loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc.clone().add(r * Math.cos(k + a), 0, r * Math.sin(k + a)), 0);
        }
    }

    private Vector format(Vector vec) {
        return vec.length() < .01 ? new Vector(0, 0, 0) : vec.normalize();
    }
}
