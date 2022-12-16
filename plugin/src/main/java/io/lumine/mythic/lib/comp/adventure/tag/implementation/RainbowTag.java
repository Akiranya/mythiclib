package io.lumine.mythic.lib.comp.adventure.tag.implementation;

import io.lumine.mythic.lib.comp.adventure.resolver.implementation.RainbowResolver;
import io.lumine.mythic.lib.comp.adventure.tag.AdventureTag;

/**
 * mythiclib
 * 01/12/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class RainbowTag extends AdventureTag {

    public RainbowTag() {
        super("rainbow", new RainbowResolver(), false, true);
    }
}
