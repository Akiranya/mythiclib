package io.lumine.mythic.lib.comp.hexcolor;

import io.lumine.mythic.lib.comp.adventure.AdventureParser;

/**
 * @deprecated Use {@link AdventureParser} instead.
 */
@Deprecated
public interface ColorParser {

    /**
     * @param format
     *            The string from which to parse color codes
     * @return String with parsed color codes
     */
    String parseColorCodes(String format);
}
