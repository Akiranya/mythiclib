package io.lumine.mythic.lib.skill.handler.def.mmocore;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.SmallParticleEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

public class Minor_Healings extends SkillHandler<TargetSkillResult> {
    public Minor_Healings() {
        super();

        registerModifiers("heal");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return meta.getCaster().getPlayer().isSneaking() ? new TargetSkillResult(meta.getCaster().getPlayer()) : new TargetSkillResult(meta, InteractionType.SUPPORT_SKILL);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        UtilityMethods.heal(target, skillMeta.getModifier("heal"));
        new SmallParticleEffect(target, Particle.HEART, 1);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2, 2);
    }
}
