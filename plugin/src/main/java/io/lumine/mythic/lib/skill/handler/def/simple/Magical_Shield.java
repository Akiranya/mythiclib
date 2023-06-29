package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Magical_Shield extends SkillHandler<SimpleSkillResult> {
    public Magical_Shield() {
        super();

        registerModifiers("power", "radius", "duration");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult(meta.getCaster().getPlayer().isOnGround());
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getParameter("duration");
        double radiusSquared = Math.pow(skillMeta.getParameter("radius"), 2);
        double power = skillMeta.getParameter("power") / 100;

        new MagicalShieldEffect(skillMeta.getCaster().getPlayer(), duration, radiusSquared, power);
    }

    public class MagicalShieldEffect extends BukkitRunnable implements Listener {
        private final Location loc;
        private final double duration, radius, power;

        private int ti = 0;

        public MagicalShieldEffect(Player caster, double duration, double radius, double power) {
            this.loc = caster.getLocation().clone();

            this.duration = duration;
            this.radius = radius;
            this.power = power;

            caster.getWorld().playSound(caster.getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 3, 0);

            runTaskTimer(MythicLib.plugin, 0, 3);
            Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
        }

        private void close() {
            cancel();
            EntityDamageEvent.getHandlerList().unregister(this);
        }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void a(EntityDamageEvent event) {
            if (event.getEntity() instanceof Player && event.getEntity().getWorld().equals(loc.getWorld()) && event.getEntity().getLocation().distanceSquared(loc) < radius)
                event.setDamage(event.getDamage() * (1 - power));
        }

        @Override
        public void run() {
            if (ti++ > duration * 20. / 3.)
                close();

            for (double j = 0; j < Math.PI / 2; j += Math.PI / (28 + random.nextInt(5)))
                for (double i = 0; i < Math.PI * 2; i += Math.PI / (14 + random.nextInt(5)))
                    loc.getWorld().spawnParticle(Particle.REDSTONE,
                            loc.clone().add(2.5 * Math.cos(i + j) * Math.sin(j), 2.5 * Math.cos(j), 2.5 * Math.sin(i + j) * Math.sin(j)), 1,
                            new Particle.DustOptions(Color.FUCHSIA, 1));
        }
    }
}
