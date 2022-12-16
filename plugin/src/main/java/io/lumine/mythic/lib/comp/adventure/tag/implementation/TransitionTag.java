package io.lumine.mythic.lib.comp.adventure.tag.implementation;

import io.lumine.mythic.lib.comp.adventure.resolver.implementation.TransitionResolver;
import io.lumine.mythic.lib.comp.adventure.tag.AdventureTag;

/**
 * mythiclib
 * 05/12/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class TransitionTag extends AdventureTag {

    public TransitionTag() {
        super("transition", new TransitionResolver(), false, true);
    }
}
