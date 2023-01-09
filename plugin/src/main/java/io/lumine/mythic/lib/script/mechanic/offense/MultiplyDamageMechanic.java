package io.lumine.mythic.lib.script.mechanic.offense;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.entity.Entity;

@MechanicMetadata
public class MultiplyDamageMechanic extends TargetMechanic {
    private final DoubleFormula amount;
    private final DamageType damageType;
    private final boolean additive;

    public MultiplyDamageMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("amount");

        amount = new DoubleFormula(config.getString("amount"));
        damageType = config.contains("damage_type") ? DamageType.valueOf(config.getString("damage_type").toUpperCase()) : null;
        additive = config.getBoolean("additive", false);
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        if (additive) {
            if (damageType == null)
                meta.getAttack().getDamage().additiveModifier(amount.evaluate(meta));
            else
                meta.getAttack().getDamage().additiveModifier(amount.evaluate(meta), damageType);
        } else {
            if (damageType == null)
                meta.getAttack().getDamage().multiplicativeModifier(amount.evaluate(meta));
            else
                meta.getAttack().getDamage().multiplicativeModifier(amount.evaluate(meta), damageType);
        }
    }
}
