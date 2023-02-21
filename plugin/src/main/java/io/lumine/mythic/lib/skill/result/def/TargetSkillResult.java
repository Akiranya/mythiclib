package io.lumine.mythic.lib.skill.result.def;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.util.RayTrace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class TargetSkillResult implements SkillResult {
    private final LivingEntity target;

    public TargetSkillResult(SkillMetadata skillMeta) {
        this(skillMeta, InteractionType.OFFENSE_SKILL);
    }

    public TargetSkillResult(SkillMetadata skillMeta, InteractionType interactType) {
        this(skillMeta, 50, interactType);
    }

    public TargetSkillResult(SkillMetadata skillMeta, double range, InteractionType interactType) {
        Player caster = skillMeta.getCaster().getPlayer();
        this.target = skillMeta.hasTargetEntity() && MythicLib.plugin.getEntities().canInteract(caster, skillMeta.getTargetEntityOrNull(), interactType) ? (LivingEntity) skillMeta.getTargetEntityOrNull() :
                new RayTrace(caster, range, entity -> MythicLib.plugin.getEntities().canInteract(caster, entity, interactType)).getHit();
    }

    public TargetSkillResult(@Nullable LivingEntity target) {
        this.target = target;
    }

    public LivingEntity getTarget() {
        return target;
    }

    @Override
    public boolean isSuccessful(SkillMetadata skillMeta) {
        return target != null;
    }
}
