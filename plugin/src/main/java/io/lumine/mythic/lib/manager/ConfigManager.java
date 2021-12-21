package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ConfigManager {
    public final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
    public boolean playerAbilityDamage;

    public void reload() {
        playerAbilityDamage = MythicLib.plugin.getConfig().getBoolean("player-ability-damage");
        formatSymbols.setDecimalSeparator(getFirstChar(MythicLib.plugin.getConfig().getString("number-format.decimal-separator")));
    }

    public DecimalFormat newDecimalFormat(String pattern) {
        return new DecimalFormat(pattern, formatSymbols);
    }

    /**
     * Applies the elemental damage formula
     *
     * @param incomingDamage Incoming elemental damage
     * @param defense        Defense which reduces incoming damage
     * @return The final amount of elemental damage taken by an enemy with
     *         a specific elemental defense
     **/
    public double getAppliedElementalDamage(double incomingDamage, double defense) {
        // TODO
        return 0;
    }

    private char getFirstChar(String str) {
        return str == null || str.isEmpty() ? ',' : str.charAt(0);
    }
}
