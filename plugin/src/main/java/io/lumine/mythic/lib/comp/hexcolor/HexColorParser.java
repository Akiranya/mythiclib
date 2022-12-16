package io.lumine.mythic.lib.comp.hexcolor;

import io.lumine.mythic.lib.comp.adventure.AdventureParser;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @deprecated Use {@link AdventureParser} instead.
 */
@Deprecated(forRemoval = true)
public class HexColorParser implements ColorParser {

    public static final Pattern HEX_PATTERN = Pattern.compile("&(#[A-Fa-f0-9]{6})");
    public static final char COLOR_CHAR = org.bukkit.ChatColor.COLOR_CHAR;

    @Override
    @Contract("null -> null")
    @Nullable
    public String parseColorCodes(@Nullable String format) {
        if (format == null) return null;

        Matcher matcher = HEX_PATTERN.matcher(format);
        StringBuilder buffer = new StringBuilder(format.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }
}
