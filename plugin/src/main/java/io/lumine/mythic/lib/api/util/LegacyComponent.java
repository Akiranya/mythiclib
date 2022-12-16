package io.lumine.mythic.lib.api.util;

import io.lumine.mythic.lib.comp.adventure.AdventureParser;
import io.lumine.mythic.lib.util.AdventureUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A utility class for converting legacy text to Adventure components.
 *
 * @deprecated Use {@link AdventureParser} instead.
 * @see AdventureParser
 * @see AdventureUtils#parse(String)
 */
@Deprecated(forRemoval = true)
public class LegacyComponent {

    /**
     * Used to parse text for display names by adding a
     * white color code by default if no color is added
     *
     * @return
     */
    @NotNull
    public static Component parse(@NotNull String text) {
        return simpleParse(text).colorIfAbsent(NamedTextColor.WHITE);
    }

    /**
     * Used to parse lore lines (does NOT add a white color code)
     */
    @NotNull
    public static Component simpleParse(@NotNull String text) {
        Component component = MiniMessage.miniMessage().deserialize(translateLegacyColorCodes(text));

        if (component.decorations().get(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET)
            component = component.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

        return component;
    }

    private static final Pattern LEGACY_HEX_COLOR = Pattern.compile("<HEX([a-fA-F0-9]{6})>");
    private static final Pattern COMPILED_LEGACY_HEX_COLOR = Pattern.compile("§[xX](§[a-fA-F0-9]){6}");
    private static final Pattern LEGACY_COLOR = Pattern.compile("(&|§)[a-fA-F0-9kKlLmMnNoOrR]");
    private static final Map<Character, String> NEW_COLORS = new HashMap<>();

    static {

        // Colors
        NEW_COLORS.put('0', "black");
        NEW_COLORS.put('1', "dark_blue");
        NEW_COLORS.put('2', "dark_green");
        NEW_COLORS.put('3', "dark_aqua");
        NEW_COLORS.put('4', "dark_red");
        NEW_COLORS.put('5', "dark_purple");
        NEW_COLORS.put('6', "gold");
        NEW_COLORS.put('7', "gray");
        NEW_COLORS.put('8', "dark_gray");
        NEW_COLORS.put('9', "blue");
        NEW_COLORS.put('a', "green");
        NEW_COLORS.put('b', "aqua");
        NEW_COLORS.put('c', "red");
        NEW_COLORS.put('d', "light_purple");
        NEW_COLORS.put('e', "yellow");
        NEW_COLORS.put('f', "white");

        // Decorations
        NEW_COLORS.put('k', "obfuscated");
        NEW_COLORS.put('l', "bold");
        NEW_COLORS.put('m', "strikethrough");
        NEW_COLORS.put('n', "underlined");
        NEW_COLORS.put('o', "italic");
        NEW_COLORS.put('r', "reset");
    }

    /**
     * @return String with & and § color codes translated into MiniMessage format.
     * @deprecated It is now preferred to use the MiniMessage format.
     */
    @Deprecated
    private static String translateLegacyColorCodes(String text) {

        // Replace legacy color codes
        text = LEGACY_COLOR.matcher(text).replaceAll(result -> {
            final char legacyColorCode = Character.toLowerCase(result.group().charAt(1));
            return new StringBuilder("<").append(Objects.requireNonNullElse(NEW_COLORS.get(legacyColorCode), "cc_err")).append(">").toString();
        });

        // Replace §x§1§1§2§2§3§3 compiled color codes
        text = COMPILED_LEGACY_HEX_COLOR.matcher(text).replaceAll(result -> "<#" + result.group().replaceAll("[§xX]", "") + ">");


        // Replace <HEX112233> color codes
        return LEGACY_HEX_COLOR.matcher(text).replaceAll(result -> "<#" + result.group().substring(4));
    }
}