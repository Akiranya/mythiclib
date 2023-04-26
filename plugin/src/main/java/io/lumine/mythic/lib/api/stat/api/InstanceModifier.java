package io.lumine.mythic.lib.api.stat.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.apache.commons.lang.Validate;

import java.text.DecimalFormat;

public abstract class InstanceModifier extends PlayerModifier {
    protected final double value;
    protected final ModifierType type;

    public InstanceModifier(String key, EquipmentSlot slot, ModifierSource source, double value, ModifierType type) {
        super(key, slot, source);
        this.value = value;
        this.type = type;
    }

    public InstanceModifier(String key, EquipmentSlot slot, ModifierSource source, String str) {
        super(key, slot, source);
        Validate.notNull(str, "String cannot be null");
        Validate.notEmpty(str, "String cannot be empty");

        type = str.toCharArray()[str.length() - 1] == '%' ? ModifierType.RELATIVE : ModifierType.FLAT;
        value = Double.parseDouble(type == ModifierType.RELATIVE ? str.substring(0, str.length() - 1) : str);
    }

    public InstanceModifier(ConfigObject object) {
        super(object.getString("key"), EquipmentSlot.OTHER, ModifierSource.OTHER);
        value = object.getDouble("value");
        type = object.getBoolean("multiplicative", false) ? ModifierType.RELATIVE : ModifierType.FLAT;
    }

    public ModifierType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return MythicLib.plugin.getMMOConfig().decimal.format(value) + (type == ModifierType.RELATIVE ? "%" : "");
    }
}
