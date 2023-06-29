package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

public class Bunny_Mode extends SkillHandler<SimpleSkillResult> {
    public Bunny_Mode() {
        super();

        registerModifiers("duration", "jump-force", "speed");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getParameter("duration") * 20;
        double y = skillMeta.getParameter("jump-force");
        double xz = skillMeta.getParameter("speed");

        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            final BunnyModeEffect handler = new BunnyModeEffect(caster, duration);
            int j = 0;

            public void run() {
                if (j++ > duration) {
                    handler.close(3 * 20);
                    cancel();
                    return;
                }

                if (caster.getLocation().add(0, -.5, 0).getBlock().getType().isSolid()) {
                    caster.setVelocity(caster.getEyeLocation().getDirection().setY(0).normalize().multiply(.8 * xz).setY(0.5 * y / xz));
                    caster.getWorld().playSound(caster.getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 2, 1);
                    for (double a = 0; a < Math.PI * 2; a += Math.PI / 12)
                        caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation(), 0, Math.cos(a), 0, Math.sin(a),
                                .2);
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }

    public class BunnyModeEffect extends TemporaryListener {
        private final Player player;

        public BunnyModeEffect(Player player, double duration) {
            super(EntityDamageEvent.getHandlerList());

            this.player = player;

            close((long) (duration * 20));
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void a(EntityDamageEvent event) {
            if (event.getEntity().equals(player) && event.getCause() == DamageCause.FALL)
                event.setCancelled(true);
        }

        @Override
        public void whenClosed() {
            // Nothing
        }
    }
}
