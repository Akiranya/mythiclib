package io.lumine.mythic.lib.script.condition.generic;

import bsh.EvalError;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

/**
 * Checks if the specified algebraic expression returns true
 */
public class BooleanCondition extends Condition {
    private final String formula;

    public BooleanCondition(ConfigObject config) {
        super(config);

        config.validateKeys("formula");

        formula = config.getString("formula");
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        try {
            return (boolean)MythicLib.plugin.getFormulaParser().eval(meta.parseString(formula));
        } catch (EvalError e) {
            return false;
        }
    }
}