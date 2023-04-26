package io.lumine.mythic.lib.util;

import bsh.EvalError;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Instead of directly using a double in a skill, we rather use a string
 * where internal or PAPI placeholders are parsed before finally
 * evaluating the formula.
 * <p>
 * This represents 90% of the total skill system configurability
 */
public class DoubleFormula {
    @Nullable
    private final String value;
    private final double trivialValue;
    private final boolean trivial;

    public static final DoubleFormula ZERO = new DoubleFormula(0);

    public DoubleFormula(@NotNull String str) {
        this.value = str;

        double trivialValue = 0;
        boolean trivial;

        try {
            trivialValue = Double.valueOf(str);
            trivial = true;
        } catch (IllegalArgumentException exception) {
            trivial = false;
        }

        this.trivialValue = trivialValue;
        this.trivial = trivial;
    }

    /**
     * A mere double
     */
    public DoubleFormula(double trivialValue) {
        this.trivial = true;
        this.trivialValue = trivialValue;
        this.value = null;
    }

    @SneakyThrows
    public double evaluate(SkillMetadata meta) {
        try {
            return trivial ? trivialValue : (double) MythicLib.plugin.getInterpreter().eval(meta.parseString(value));
        } catch (EvalError exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not evaluate '" + value + "' while casting skill '" + meta.getCast().getHandler().getId() + "': " + exception.getMessage());
            return 0;
        }
    }
}
