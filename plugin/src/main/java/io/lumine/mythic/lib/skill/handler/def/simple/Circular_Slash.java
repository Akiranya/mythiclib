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
import org.bukkit.util.Vector;

public class Circular_Slash extends SkillHandler<SimpleSkillResult> {
    public Circular_Slash() {
        super();

        registerModifiers("damage", "radius", "knockback");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double damage = skillMeta.getParameter("damage");
        double radius = skillMeta.getParameter("radius");
        double knockback = skillMeta.getParameter("knockback");

        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, .5f);
        caster.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 254));
        for (Entity entity : caster.getNearbyEntities(radius, radius, radius)) {
            if (UtilityMethods.canTarget(caster, entity)) {
                skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.PHYSICAL);
                Vector v1 = entity.getLocation().toVector();
                Vector v2 = caster.getLocation().toVector();
                double y = .5;
                Vector v3 = v1.subtract(v2).multiply(.5 * knockback).setY(knockback == 0 ? 0 : y);
                entity.setVelocity(v3);
            }
        }
        double step = 12 + (radius * 2.5);
        for (double j = 0; j < Math.PI * 2; j += Math.PI / step) {
            Location loc = caster.getLocation().clone();
            loc.add(Math.cos(j) * radius, .75, Math.sin(j) * radius);
            loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 0);
        }
        caster.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, caster.getLocation().add(0, 1, 0), 0);
    }
}
