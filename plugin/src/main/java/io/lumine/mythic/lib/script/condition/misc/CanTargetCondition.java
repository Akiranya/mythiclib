package io.lumine.mythic.lib.script.condition.misc;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

/**
 * Checks if the skill caster can damage the skill target
 */
public class CanTargetCondition extends Condition {
    private final InteractionType interactionType;

    public CanTargetCondition(ConfigObject config) {
        super(config);

        interactionType = config.contains("interaction_type") ? InteractionType.valueOf(UtilityMethods.enumName(config.getString("interaction_type"))) : InteractionType.OFFENSE_SKILL;
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        return MythicLib.plugin.getEntities().canInteract(meta.getCaster().getPlayer(), meta.getTargetEntity(), interactionType);
    }
}
