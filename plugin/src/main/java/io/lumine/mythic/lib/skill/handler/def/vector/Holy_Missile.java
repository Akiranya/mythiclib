package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Holy_Missile extends SkillHandler<VectorSkillResult> {
    public Holy_Missile() {
        super();

        registerModifiers("damage", "duration");
    }

    @Override
    public VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        double duration = skillMeta.getParameter("duration") * 10;
        double damage = skillMeta.getParameter("damage");

        caster.getWorld().playSound(caster.getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 1);
        new BukkitRunnable() {
            final Vector vec = result.getTarget().multiply(.45);
            final Location loc = caster.getLocation().clone().add(0, 1.3, 0);
            double ti = 0;

            public void run() {
                if (ti++ > duration)
                    cancel();

                loc.getWorld().playSound(loc, VersionSound.BLOCK_NOTE_BLOCK_HAT.toSound(), 2, 1);
                List<Entity> entities = UtilityMethods.getNearbyChunkEntities(loc);
                for (int j = 0; j < 2; j++) {
                    loc.add(vec);
                    if (loc.getBlock().getType().isSolid())
                        cancel();

                    for (double i = -Math.PI; i < Math.PI; i += Math.PI / 2) {
                        Vector v = new Vector(Math.cos(i + ti / 4), Math.sin(i + ti / 4), 0);
                        v = UtilityMethods.rotate(v, loc.getDirection());
                        loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 0, v.getX(), v.getY(), v.getZ(), .08);
                    }

                    for (Entity entity : entities)
                        if (UtilityMethods.canTarget(caster, loc, entity)) {
                            loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 1);
                            loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 32, 0, 0, 0, .2);
                            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
                            skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC);
                            cancel();
                            return;
                        }
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}

