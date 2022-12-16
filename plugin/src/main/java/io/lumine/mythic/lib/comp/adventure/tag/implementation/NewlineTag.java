package io.lumine.mythic.lib.comp.adventure.tag.implementation;

import io.lumine.mythic.lib.comp.adventure.tag.AdventureTag;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class NewlineTag extends AdventureTag {

    public NewlineTag() {
        super("newline", (src, argumentQueue) -> "\n", true, false,"br");
    }
}
