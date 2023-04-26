package io.lumine.mythic.lib.api.stat.modifier;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.api.InstanceModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

/**
 * Stat modifiers do NOT utilize the player modifier UUID
 */
public class StatModifier extends InstanceModifier {
    private final String stat;

    /**
     * Flat stat modifier (simplest modifier you can think about)
     */
    public StatModifier(String key, String stat, double value) {
        this(key, stat, value, ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    /**
     * Stat modifier given by an external mecanic, like a party buff, item set bonuses,
     * skills or abilities... Anything apart from items and armor.
     */
    public StatModifier(String key, String stat, double value, ModifierType type) {
        this(key, stat, value, type, EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    /**
     * Stat modifier given by an item, either a weapon or an armor piece.
     *
     * @param stat   Stat being modified
     * @param key    Player modifier key
     * @param value  Value of stat modifier
     * @param type   Is the modifier flat or multiplicative
     * @param slot   Slot of the item granting the stat modifier
     * @param source Type of the item granting the stat modifier
     */
    public StatModifier(String key, String stat, double value, ModifierType type, EquipmentSlot slot, ModifierSource source) {
        super(key, slot, source, value, type);

        this.stat = stat;
    }

    /**
     * Used to parse a StatModifier from a string in a configuration section.
     * Always returns a modifier with source OTHER. Can be used by MythicCore
     * to handle party buffs, or MMOItems for item set bonuses. Throws IAE
     *
     * @param str The string to be parsed
     */
    public StatModifier(String key, String stat, String str) {
        super(key, EquipmentSlot.OTHER, ModifierSource.OTHER, str);

        this.stat = stat;
    }

    public StatModifier(ConfigObject object) {
        super(object);

        this.stat = object.getString("stat");
    }

    public String getStat() {
        return stat;
    }

    /**
     * Used to add a constant to some existing stat modifier, usually an
     * integer, for instance it is used when a stat trigger is triggered multiple times.
     *
     * @param offset The offset added.
     * @return A new instance of StatModifier with modified value
     */
    public StatModifier add(double offset) {
        return new StatModifier(getKey(), stat, value + offset, type, getSlot(), getSource());
    }

    /**
     * Used to multiply some existing stat modifier by a constant, usually an
     * integer, for instance when MMOCore party modifiers scale with the
     * number of the party member count
     *
     * @param coef The multiplicative constant
     * @return A new instance of StatModifier with modified value
     */
    public StatModifier multiply(double coef) {
        return new StatModifier(getKey(), stat, value * coef, type, getSlot(), getSource());
    }

    @Override
    public void register(MMOPlayerData playerData) {
        playerData.getStatMap().getInstance(stat).addModifier(this);
    }

    @Override
    public void unregister(MMOPlayerData playerData) {
        playerData.getStatMap().getInstance(stat).remove(getKey());
    }
}

