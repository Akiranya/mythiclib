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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Cursed_Beam extends SkillHandler<VectorSkillResult> {
    public Cursed_Beam() {
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

        double duration = skillMeta.getModifier("duration");

        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
        new BukkitRunnable() {
            final Vector dir = result.getTarget().multiply(.3);
            final Location loc = caster.getEyeLocation().clone();
            final double r = 0.4;
            int ti = 0;

            public void run() {
                ti++;
                if (ti > 50)
                    cancel();

                List<Entity> entities = UtilityMethods.getNearbyChunkEntities(loc);
                for (double j = 0; j < 4; j++) {
                    loc.add(dir);
                    for (double i = 0; i < Math.PI * 2; i += Math.PI / 6) {
                        Vector vec = UtilityMethods.rotate(new Vector(r * Math.cos(i), r * Math.sin(i), 0), loc.getDirection());
                        loc.add(vec);
                        loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 0);
                        loc.add(vec.multiply(-1));
                    }

                    for (Entity target : entities)
                        if (UtilityMethods.canTarget(caster, loc, target)) {
                            effect(target);
                            double damage = skillMeta.getModifier("damage");
                            loc.getWorld().playSound(loc, VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 2, .7f);

                            for (Entity entity : entities)
                                if (UtilityMethods.canTarget(caster, entity) && loc.distanceSquared(entity.getLocation().add(0, 1, 0)) < 9) {
                                    skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC);
                                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (duration * 20), 0));
                                }
                            cancel();
                            return;
                        }
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }

    private void effect(Entity ent) {
        new BukkitRunnable() {
            final Location loc2 = ent.getLocation();
            double y = 0;

            public void run() {
                for (int i = 0; i < 3; i++) {
                    y += .05;
                    for (int j = 0; j < 2; j++) {
                        double xz = y * Math.PI * .8 + (j * Math.PI);
                        loc2.getWorld().spawnParticle(Particle.SPELL_WITCH, loc2.clone().add(Math.cos(xz) * 2.5, y, Math.sin(xz) * 2.5), 0);
                    }
                }
                if (y >= 3)
                    cancel();
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
