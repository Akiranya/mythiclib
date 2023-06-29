package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Stun extends SkillHandler<TargetSkillResult> {
    public Stun() {
        super();

        registerModifiers("duration");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();

        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 2);
        target.getWorld().playEffect(target.getLocation(), Effect.STEP_SOUND, 42);
        target.getWorld().playEffect(target.getLocation().add(0, 1, 0), Effect.STEP_SOUND, 42);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (skillMeta.getParameter("duration") * 20), 254));
    }
}
