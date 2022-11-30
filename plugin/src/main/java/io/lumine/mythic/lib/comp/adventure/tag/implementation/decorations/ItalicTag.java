package io.lumine.mythic.lib.comp.adventure.tag.implementation.decorations;

import io.lumine.mythic.lib.comp.adventure.argument.AdventureArgumentQueue;
import io.lumine.mythic.lib.comp.adventure.resolver.AdventureTagResolver;
import io.lumine.mythic.lib.comp.adventure.tag.AdventureTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class ItalicTag extends AdventureTag {

    public ItalicTag() {
        super("italic", new BoldResolver(), "i");
    }

    public static class BoldResolver implements AdventureTagResolver {

        @Override
        public @Nullable String resolve(@NotNull String src, @NotNull AdventureArgumentQueue argumentQueue) {
            return "§o";
        }
    }
}
