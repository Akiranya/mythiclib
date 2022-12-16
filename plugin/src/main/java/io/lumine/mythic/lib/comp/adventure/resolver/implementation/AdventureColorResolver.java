package io.lumine.mythic.lib.comp.adventure.resolver.implementation;

import io.lumine.mythic.lib.util.AdventureUtils;
import io.lumine.mythic.lib.comp.adventure.argument.AdventureArgumentQueue;
import io.lumine.mythic.lib.comp.adventure.resolver.AdventureTagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class AdventureColorResolver implements AdventureTagResolver {

    @Override
    public @Nullable String resolve(@NotNull String src, @NotNull AdventureArgumentQueue args) {
        return args.hasNext() ?
                AdventureUtils.getByName(args.peek().value())
                        .map(c -> "" + c)
                        .orElse(AdventureUtils.getByHex(args.pop().value())
                                .map(c -> "" + c)
                                .orElse(null))
                : null;
    }
}
