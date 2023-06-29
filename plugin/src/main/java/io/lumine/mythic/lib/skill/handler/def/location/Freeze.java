package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class Freeze extends SkillHandler<LocationSkillResult> {
    public Freeze() {
        super();

        registerModifiers("duration", "amplifier", "radius");
    }

    @NotNull
    @Override
    public LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        int duration = (int) (skillMeta.getParameter("duration") * 20);
        int amplifier = (int) (skillMeta.getParameter("amplifier") - 1);
        double radiusSquared = Math.pow(skillMeta.getParameter("radius"), 2);

        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.add(0, .1, 0), 0);
        loc.getWorld().spawnParticle(Particle.SNOW_SHOVEL, loc, 48, 0, 0, 0, .2);
        loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 24, 0, 0, 0, .2);
        loc.getWorld().playSound(loc, VersionSound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.toSound(), 2, 1);

        for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
            if (entity.getLocation().distanceSquared(loc) < radiusSquared && UtilityMethods.canTarget(caster, entity)) {
                ((LivingEntity) entity).removePotionEffect(PotionEffectType.SLOW);
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, amplifier));
            }
    }
}
