package io.lumine.mythic.lib.script.mechanic.offense;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.entity.Entity;

@MechanicMetadata
public class MultiplyDamageMechanic extends TargetMechanic {
    private final DoubleFormula amount;
    private final DamageType damageType;

    public MultiplyDamageMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("amount");

        amount = new DoubleFormula(config.getString("amount"));
        damageType = config.contains("damage_type") ? DamageType.valueOf(config.getString("damage_type").toUpperCase()) : null;
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        if (damageType == null)
            meta.getAttack().getDamage().multiplicativeModifier(amount.evaluate(meta));
        else
            meta.getAttack().getDamage().multiplicativeModifier(amount.evaluate(meta), damageType);
    }
}