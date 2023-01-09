package io.lumine.mythic.lib.comp.adventure.argument;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;

/**
 * mythiclib
 * 22/12/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
@ApiStatus.Internal
public class EmptyArgumentQueue extends AdventureArgumentQueue{

    public EmptyArgumentQueue() {
        super(Collections.emptyList());
    }
}
