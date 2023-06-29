package io.lumine.mythic.lib.util;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class CustomFont {
    private final char[] characters = new char[10];
    private final char separator, middle;

    public CustomFont(ConfigurationSection config) {
        for (int i = 0; i < 10; i++)
            characters[i] = config.getString(String.valueOf(i), "?").charAt(0);
        separator = config.getString("dot", "?").charAt(0);
        middle = config.contains("inter") ? config.getString("inter").charAt(0) : 0;
    }

    @NotNull
    public String format(String raw) {
        final StringBuilder builder = new StringBuilder();
        for (char c : raw.toCharArray()) {
            final int index = c - '0';
            if (middle != 0 && builder.length() != 0)
                builder.append(middle);
            builder.append(index < 0 || index > 9 ? separator : characters[index]);
        }

        return builder.toString();
    }
}
