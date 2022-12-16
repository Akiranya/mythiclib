package io.lumine.mythic.lib.comp.adventure.tag.implementation;

import io.lumine.mythic.lib.comp.adventure.resolver.implementation.GradientResolver;
import io.lumine.mythic.lib.comp.adventure.tag.AdventureTag;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class GradientTag extends AdventureTag {

    public GradientTag() {
        super("gradient", new GradientResolver(), false, true);
    }
}
