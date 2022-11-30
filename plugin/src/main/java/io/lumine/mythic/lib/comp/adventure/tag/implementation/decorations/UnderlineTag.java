package io.lumine.mythic.lib.comp.adventure.tag.implementation.decorations;

import io.lumine.mythic.lib.comp.adventure.tag.AdventureTag;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class UnderlineTag extends AdventureTag {

    public UnderlineTag() {
        super("underline", (src, argumentQueue) -> "§n", true, "u");
    }

}
