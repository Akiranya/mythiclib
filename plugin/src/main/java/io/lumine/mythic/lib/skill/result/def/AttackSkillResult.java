package io.lumine.mythic.lib.skill.result.def;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.SkillResult;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AttackSkillResult implements SkillResult {

    @Nullable
    private final LivingEntity target;

    public AttackSkillResult(SkillMetadata skillMeta) {
        this.target = skillMeta.hasAttackBound()
                && MythicLib.plugin.getEntities().canTarget(skillMeta.getCaster().getPlayer(), skillMeta.getAttack().getTarget(), InteractionType.OFFENSE_SKILL) ?
                skillMeta.getAttack().getTarget() : null;
    }

    @NotNull
    public LivingEntity getTarget() {
        return Objects.requireNonNull(target, "Skill is unsuccessful");
    }

    @Override
    public boolean isSuccessful(SkillMetadata skillMeta) {
        return target != null;
    }
}
