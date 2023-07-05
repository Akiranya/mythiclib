package io.lumine.mythic.lib.comp.formula;

import bsh.EvalError;
import bsh.Interpreter;
import io.lumine.mythic.lib.MythicLib;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class FormulaParser {
    private final Interpreter interpreter;
    private final List<String> mathFunctions = Arrays.asList("pow", "sqrt", "sin", "cos", "tan", "asin", "acos", "atan", "atan2", "exp", "log", "random", "abs", "max", "min");

    public FormulaParser() {
        interpreter = new bsh.Interpreter();
        try {
            interpreter.eval("import java.lang.Math;");
        } catch (EvalError error) {
            throw new RuntimeException(error);
        }
    }

    @NotNull
    public Object eval(String str) throws EvalError {
        for (String function : mathFunctions)
            str = str.replace(function + "(", "Math." + function + "(");
        return interpreter.eval(str);
    }

    @NotNull
    public Object eval(OfflinePlayer player, String str) throws EvalError {
        return eval(MythicLib.plugin.getPlaceholderParser().parse(player, str));
    }
}
