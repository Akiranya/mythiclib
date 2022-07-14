package io.lumine.mythic.lib.skill.custom.mechanic.variable.vector;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.custom.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.skill.custom.mechanic.variable.VariableMechanic;
import io.lumine.mythic.lib.skill.custom.variable.Variable;
import io.lumine.mythic.lib.skill.custom.variable.def.PositionVariable;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.Position;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.apache.commons.lang.Validate;

@MechanicMetadata
public class SetXMechanic extends VariableMechanic {
    private final DoubleFormula coordinate;

    public SetXMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("x");

        coordinate = new DoubleFormula(config.getString("x"));
    }

    @Override
    public void cast(SkillMetadata meta) {

        Variable targetVar = meta.getVariable(getVariableName());
        Validate.isTrue(targetVar instanceof PositionVariable, "Variable '" + getVariableName() + "' is not a vector");
        Position target = (Position) targetVar.getStored();

        target.setX(coordinate.evaluate(meta));
    }
}
