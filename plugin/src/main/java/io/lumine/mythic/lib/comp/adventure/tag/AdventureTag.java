package io.lumine.mythic.lib.comp.adventure.tag;

import io.lumine.mythic.lib.comp.adventure.resolver.AdventureTagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 * <p>
 * This class is holding the tag information and resolver.
 */
public abstract class AdventureTag {

    private final String name;
    private final List<String> aliases = new ArrayList<>();
    private final AdventureTagResolver resolver;
    private final boolean backwardsCompatible;
    private final boolean color;

    public AdventureTag(@NotNull String name, @NotNull AdventureTagResolver resolver, boolean backwardsCompatible, boolean color, @NotNull String... aliases) {
        this.name = name;
        this.resolver = resolver;
        this.backwardsCompatible = backwardsCompatible;
        this.aliases.addAll(Arrays.asList(aliases));
        this.color = color;
    }

    public AdventureTagResolver resolver() {
        return resolver;
    }

    public String name() {
        return name;
    }

    public boolean backwardsCompatible() {
        return backwardsCompatible;
    }

    public List<String> aliases() {
        return aliases;
    }

    public boolean color() {
        return color;
    }
}
