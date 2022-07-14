package io.lumine.mythic.lib.skill.custom.variable.def;

import io.lumine.mythic.lib.player.cooldown.CooldownMap;
import io.lumine.mythic.lib.skill.custom.variable.Variable;
import io.lumine.mythic.lib.skill.custom.variable.VariableMetadata;
import io.lumine.mythic.lib.skill.custom.variable.VariableRegistry;
import org.jetbrains.annotations.NotNull;

@VariableMetadata(name = "cooldownMap")
public class CooldownsVariable extends Variable<CooldownMap> {
    public static final VariableRegistry<CooldownsVariable> VARIABLE_REGISTRY = new VariableRegistry<>() {

        @NotNull
        @Override
        public Variable accessVariable(@NotNull CooldownsVariable cdVariable, @NotNull String name) {
            return new DoubleVariable("temp", cdVariable.getStored().getCooldown(name));
        }

        @Override
        public boolean hasVariable(String name) {
            return true;
        }

    };

    public CooldownsVariable(String name, CooldownMap map) {
        super(name, map);
    }

    @Override
    public VariableRegistry getVariableRegistry() {
        return VARIABLE_REGISTRY;
    }

    @Override
    public String toString() {
        return getStored() == null ? "None" : "CooldownMap";
    }
}