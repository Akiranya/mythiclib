package io.lumine.mythic.lib.comp.placeholder;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.DefenseFormula;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A mythic placeholder that just passes
 * on to PAPI to do all the parsing.
 *
 * @author Gunging
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "mythiclib";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Indyuce";
    }

    @Override
    public @NotNull String getVersion() {
        return MythicLib.plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {

        // All placeholders are related to players
        if (player == null)
            return null;

        if (params.startsWith("defense_damage_reduction")) {
            final double defenseStat = MMOPlayerData.get(player).getStatMap().getStat("DEFENSE");
            final double damageReduction = 100 - new DefenseFormula(false).getAppliedDamage(defenseStat, 100);
            return MythicLib.plugin.getMMOConfig().decimal.format(damageReduction);
        }

        if (params.startsWith("stat_")) {
            final String stat = UtilityMethods.enumName(params.substring(5));
            return MythicLib.plugin.getStats().format(stat, MMOPlayerData.get(player));
        }

        if (params.startsWith("cooldown_")) {
            final String key = UtilityMethods.enumName(params.substring(9));
            return MythicLib.plugin.getMMOConfig().decimal.format(MMOPlayerData.get(player).getCooldownMap().getCooldown(key));
        }

        return null;
    }
}
