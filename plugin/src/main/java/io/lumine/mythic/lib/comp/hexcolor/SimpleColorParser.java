package io.lumine.mythic.lib.comp.hexcolor;

import io.lumine.mythic.lib.comp.adventure.AdventureParser;
import net.md_5.bungee.api.ChatColor;

/**
 * @deprecated Use {@link AdventureParser} instead.
 */
@Deprecated(forRemoval = true)
public class SimpleColorParser implements ColorParser {

    @Override
    public String parseColorCodes(String format) {
        return ChatColor.translateAlternateColorCodes('&', format);
    }
}

