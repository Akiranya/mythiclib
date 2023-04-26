package io.lumine.mythic.lib.skill;

import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Can be used to cast a skill handler with configurable modifier input
 *
 * @author jules
 */
public class SimpleSkill extends Skill {
    private final SkillHandler<?> handler;
    private final Map<String, Double> modifiers = new HashMap<>();

    public SimpleSkill(TriggerType trigger, SkillHandler<?> handler) {
        super(trigger);

        this.handler = handler;
    }

    @NotNull
    @Override
    public boolean getResult(SkillMetadata skillMeta) {
        return true;
    }

    @Override
    public void whenCast(SkillMetadata skillMeta) {
        // Nothing here
    }

    @Override
    public double getParameter(String path) {
        return modifiers.getOrDefault(path, 0d);
    }

    @Override
    public SkillHandler<?> getHandler() {
        return handler;
    }

    public void registerModifier(String path, double value) {
        modifiers.put(path, value);
    }
}
