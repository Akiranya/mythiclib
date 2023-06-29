package io.lumine.mythic.lib.script.mechanic.offense;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@MechanicMetadata
public class MultiplyDamageMechanic extends TargetMechanic {
    private final DoubleFormula amount;
    private final DamageType damageType;
    private final boolean additive;

    /**
     * Cannot save the Element object reference since skills
     * load BEFORE elements. This also permits the elements to
     * be modified without having to reload skills which reduces
     * MythicLib module load inter-dependency.
     */
    @Nullable
    private final String elementName;

    public MultiplyDamageMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("amount");

        amount = new DoubleFormula(config.getString("amount"));
        damageType = config.contains("damage_type") ? DamageType.valueOf(config.getString("damage_type").toUpperCase()) : null;
        additive = config.getBoolean("additive", false);

        // Elemental attack?
        elementName = config.contains("element") ? UtilityMethods.enumName(config.getString("element")) : null;
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        final @Nullable Element element = elementName != null ? Objects.requireNonNull(MythicLib.plugin.getElements().get(elementName), "Could not find element with ID '" + elementName + "'") : null;

        if (additive) {
            if (element != null)
                meta.getAttackSource().getDamage().additiveModifier(amount.evaluate(meta), element);
            else if (damageType != null)
                meta.getAttackSource().getDamage().additiveModifier(amount.evaluate(meta), damageType);
            else
                meta.getAttackSource().getDamage().additiveModifier(amount.evaluate(meta));
        } else {
            if (element != null)
                meta.getAttackSource().getDamage().multiplicativeModifier(amount.evaluate(meta), element);
            else if (damageType != null)
                meta.getAttackSource().getDamage().multiplicativeModifier(amount.evaluate(meta), damageType);
            else
                meta.getAttackSource().getDamage().multiplicativeModifier(amount.evaluate(meta));
        }
    }
}
