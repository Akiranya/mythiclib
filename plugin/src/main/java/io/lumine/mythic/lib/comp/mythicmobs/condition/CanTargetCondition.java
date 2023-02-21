package io.lumine.mythic.lib.comp.mythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityComparisonCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.core.utils.annotations.MythicCondition;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

@MythicCondition(author = "Indyuce", name = "mmocantarget", aliases = {}, description = "Whether the caster can interact with the target")
public class CanTargetCondition extends SkillCondition implements IEntityComparisonCondition {
    private final InteractionType interaction;

    public CanTargetCondition(String line, MythicLineConfig mlc) {
        super(line);

        String interactionName = mlc.getString(new String[]{"interact", "type", "name", "interaction", "interactionType", "interactionName"}, "OFFENSE_SKILL");
        this.interaction = InteractionType.valueOf(UtilityMethods.enumName(interactionName));
    }

    @Override
    public boolean check(AbstractEntity entity, AbstractEntity target) {
        Validate.isTrue(entity instanceof AbstractPlayer, "First entity must be a player");
        return MythicLib.inst().getEntities().canInteract((Player) entity.getBukkitEntity(), target.getBukkitEntity(), interaction);
    }
}