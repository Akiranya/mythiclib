package io.lumine.mythic.lib.skill.result;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillShot;
import com.sucy.skill.api.skills.TargetSkill;
import com.sucy.skill.api.target.TargetHelper;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

public class SkillAPISkillResult implements SkillResult {
    private final PlayerData skillPlayerData;
    private final Skill skill;
    private final int level;

    @Nullable
    private LivingEntity target;

    public SkillAPISkillResult(SkillMetadata skillMeta, Skill skill) {
        this.skillPlayerData = SkillAPI.getPlayerData(skillMeta.getCaster().getPlayer());
        this.skill = skill;
        this.level = (int) skillMeta.getParameter("level");
    }

    public Skill getSkill() {
        return skill;
    }

    public int getLevel() {
        return level;
    }

    @NotNull
    public LivingEntity getTarget() {
        return Objects.requireNonNull(target, "Skill has no target");
    }

    @Override
    public boolean isSuccessful(SkillMetadata skillMeta) {

        // Dead players can't cast skills
        if (skillMeta.getCaster().getPlayer().isDead())
            return false;

        // Skill Shots
        if (skill instanceof SkillShot)
            return true;

            // Target Skills
        else if (skill instanceof TargetSkill) {
            target = skillMeta.hasTargetEntity() && skillMeta.getTargetEntityOrNull() instanceof LivingEntity ? (LivingEntity) skillMeta.getTargetEntity() : TargetHelper.getLivingTarget(skillPlayerData.getPlayer(), skill.getRange(level));

            // Must have a target
            if (target == null)
                return false;
        }

        return false;
    }
}
