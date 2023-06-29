package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Sky_Smash extends SkillHandler<SimpleSkillResult> {
    public Sky_Smash() {
        super();

        registerModifiers("damage", "knock-up");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double damage = skillMeta.getParameter("damage");
        double knockUp = skillMeta.getParameter("knock-up");

        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, .5f);
        caster.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 254));
        Location loc = caster.getEyeLocation().add(caster.getEyeLocation().getDirection().multiply(3));
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
        loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 16, 0, 0, 0, .1);

        for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
            if (UtilityMethods.canTarget(caster, entity) && entity.getLocation().distanceSquared(loc) < 10) {
                skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.PHYSICAL);
                Location loc1 = caster.getEyeLocation().clone();
                loc1.setPitch(-70);
                entity.setVelocity(loc1.getDirection().multiply(1.2 * knockUp));
            }
    }
}
