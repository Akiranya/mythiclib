package io.lumine.mythic.lib.script.variable.def;

import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.VariableMetadata;
import io.lumine.mythic.lib.script.variable.VariableRegistry;
import org.jetbrains.annotations.NotNull;

@VariableMetadata(name = "statMap")
public class StatsVariable extends Variable<StatProvider> {
    public static final VariableRegistry<StatsVariable> VARIABLE_REGISTRY = new VariableRegistry<StatsVariable>() {

        @NotNull
        @Override
        public Variable accessVariable(@NotNull StatsVariable statsVariable, @NotNull String name) {
            return new DoubleVariable("temp", statsVariable.getStored().getStat(name.toUpperCase()));
        }

        @Override
        public boolean hasVariable(String name) {
            return true;
        }
    };

    public StatsVariable(String name, StatProvider provider) {
        super(name, provider);
    }

    @Override
    public VariableRegistry getVariableRegistry() {
        return VARIABLE_REGISTRY;
    }

    @Override
    public String toString() {
        return getStored() == null ? "None" : "StatProvider";
    }
}