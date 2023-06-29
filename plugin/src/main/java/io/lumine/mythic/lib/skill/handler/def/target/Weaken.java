package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.ParabolicProjectile;
import io.lumine.mythic.lib.util.SmallParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class Weaken extends SkillHandler<TargetSkillResult> {
    public Weaken() {
        super();

        registerModifiers("ratio", "duration");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        new ParabolicProjectile(caster.getPlayer().getLocation().add(0, 1, 0), target.getLocation().add(0, target.getHeight() / 2, 0), randomVector(caster.getPlayer()), () -> {
            if (!target.isDead())
                new Weakened(target, skillMeta.getParameter("ratio"), skillMeta.getParameter("duration"));
        }, 2, Particle.SPELL_WITCH);
    }

    private Vector randomVector(Player player) {
        double a = Math.toRadians(player.getEyeLocation().getYaw() + 90);
        a += (random.nextBoolean() ? 1 : -1) * (random.nextDouble() + .5) * Math.PI / 6;
        return new Vector(Math.cos(a), .8, Math.sin(a)).normalize().multiply(.4);
    }

    public static class Weakened implements Listener {
        private final Entity entity;
        private final double c;

        public Weakened(Entity entity, double ratio, double duration) {
            this.entity = entity;
            this.c = 1 + ratio / 100;

            new SmallParticleEffect(entity, Particle.SPELL_WITCH);

            Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
            Bukkit.getScheduler().scheduleSyncDelayedTask(MythicLib.plugin, () -> EntityDamageByEntityEvent.getHandlerList().unregister(this), (int) duration * 20);
        }

        @EventHandler
        public void a(EntityDamageByEntityEvent event) {
            if (event.getEntity().equals(entity)) {
                event.getEntity().getWorld().spawnParticle(Particle.SPELL_WITCH, entity.getLocation().add(0, entity.getHeight() / 2, 0), 16, .5, .5, .5, 0);
                event.setDamage(event.getDamage() * c);
            }
        }
    }
}
