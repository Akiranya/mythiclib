package io.lumine.mythic.lib.player.skillmod;

import io.lumine.mythic.lib.api.stat.api.ModifiedInstance;
import io.lumine.mythic.lib.skill.handler.SkillHandler;

public class SkillModifierInstance extends ModifiedInstance<SkillModifier> {
    private final SkillHandler<?> handler;
    private final String parameter;

    public SkillModifierInstance(SkillHandler<?> handler, String parameter) {
        this.handler = handler;
        this.parameter = parameter;
    }

    public SkillHandler<?> getSkill() {
        return handler;
    }

    public String getParameter() {
        return parameter;
    }
}
