package io.lumine.mythic.lib.comp.placeholder;

import io.lumine.mythic.lib.MythicLib;
import org.bukkit.OfflinePlayer;

public interface PlaceholderParser {

    /**
     * Placeholder parsers ALREADY compute color codes! No need
     * to use {@link MythicLib#parseColors(String)} when using this method.
     *
     * @return String with parsed placeholders AND color codes
     */
    String parse(OfflinePlayer player, String string);
}
