package io.lumine.mythic.lib.api.stat.handler;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import org.apache.commons.lang.Validate;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Used to handle complex stat behaviours, including updates
 * ran whenever a player stat changes or stat base values.
 * <p>
 * TODO merge with StatInstance? So much confusion possible
 *
 * @author indyuce
 */
public class StatHandler {
    private final boolean hasMinValue, hasMaxValue;
    private final double baseValue, minValue, maxValue;
    private final DecimalFormat decimalFormat;
    private final String stat;

    public StatHandler(@NotNull ConfigurationSection config, @NotNull String stat) {
        this.stat = stat;
        final String[] splitBounds = (" " + config.getString("min-max-values." + this.stat, "=") + " ").split("=");
        Validate.isTrue(splitBounds.length == 2, "Could not find unique = separator symbol");
        final String cleanMin = splitBounds[0].replace(" ", "");
        final String cleanMax = splitBounds[1].replace(" ", "");
        hasMinValue = !cleanMin.isEmpty();
        hasMaxValue = !cleanMax.isEmpty();
        minValue = hasMinValue ? Double.parseDouble(cleanMin) : 0;
        maxValue = hasMaxValue ? Double.parseDouble(cleanMax) : 0;
        baseValue = config.getDouble("base-stat-value." + this.stat);
        decimalFormat = config.contains("decimal-format." + this.stat) ? MythicLib.plugin.getMMOConfig().newDecimalFormat(config.getString("decimal-format." + this.stat)) : MythicLib.plugin.getMMOConfig().decimal;
    }

    @NotNull
    public String getStat() {
        return stat;
    }

    @NotNull
    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    /**
     * Update ran whenever a player equips an item, or something happens that makes
     * the player's stat value changes. This is important eg for attribute based stats
     * like Max Health, because the player's spigot Max Health attribute must be updated.
     *
     * @param instance Stat instance of player that needs updating
     */
    public void runUpdate(@NotNull StatInstance instance) {
        // Nothing to do
    }

    /**
     * This is an import class for vanilla attribute based statistics like Max Health.
     * MythicLib can't use 20 as base stat value because this can be edited by other
     * plugins. It must retrieve the actual player's base attribute value
     *
     * @param instance Stat instance collecting the stat value
     * @return The player's base stat value
     */
    public double getBaseValue(@NotNull StatInstance instance) {
        return baseValue;
    }

    /**
     * Used for attribute-based statistics. These stats already in Minecraft therefore
     * the final value doesn't match the value returned by {@link StatMap#getStat(String)}
     *
     * @param instance Stat instance collecting the stat value
     * @return The player's total stat value
     */
    public double getTotalValue(@NotNull StatInstance instance) {
        return instance.getTotal();
    }

    /**
     * This clips a stat value in the bounds configured in the config sections.
     *
     * @param clamped Value to be clipped
     */
    public double clampValue(double clamped) {
        if (hasMaxValue && clamped > maxValue) clamped = maxValue;
        if (hasMinValue && clamped < minValue) clamped = minValue;
        return clamped;
    }

    /**
     * Used by attribute based stats like Max Healh or Attack Damage. Clears
     * attribute modifiers due to MythicLib ie modifiers which names start
     * with "mmolib." or "mythiclib." or "mmoitems."
     *
     * @param ins The attribute instance to clean from undesired modifiers
     */
    public void removeModifiers(AttributeInstance ins) {
        for (AttributeModifier attribute : ins.getModifiers())

            /*
             * 'mmoitems.' is not used as an attribute modifier name prefix
             * anymore but old modifiers still have it so we need to clear these.
             * Same with 'mmolib.'
             */
            if (attribute.getName().startsWith("mmolib.") || attribute.getName().startsWith("mmoitems.") || attribute.getName().startsWith("mythiclib."))
                ins.removeModifier(attribute);
    }
}
