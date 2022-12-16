package io.lumine.mythic.lib.comp.adventure.tag.implementation;

import io.lumine.mythic.lib.comp.adventure.resolver.implementation.AdventureColorResolver;
import io.lumine.mythic.lib.comp.adventure.tag.AdventureTag;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class AdventureColorTag extends AdventureTag {

    public AdventureColorTag() {
        super("color", new AdventureColorResolver(), true,true);
    }
}
