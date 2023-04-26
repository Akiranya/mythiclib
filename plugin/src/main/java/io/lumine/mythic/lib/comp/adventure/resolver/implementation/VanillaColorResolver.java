package io.lumine.mythic.lib.comp.adventure.resolver.implementation;

import io.lumine.mythic.lib.comp.adventure.argument.AdventureArgumentQueue;
import io.lumine.mythic.lib.comp.adventure.resolver.AdventureTagResolver;
import io.lumine.mythic.lib.util.AdventureUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class VanillaColorResolver implements AdventureTagResolver {

    @Override
    public @Nullable String resolve(@NotNull String tag, @NotNull AdventureArgumentQueue argumentQueue) {
        return AdventureUtils.getByName(tag)
                .map(chatColor -> String.format("%c%c", ChatColor.COLOR_CHAR, chatColor.getChar()))
                .orElse(null);
    }
}
