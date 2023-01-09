package io.lumine.mythic.lib.comp.interaction.relation;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface RelationshipHandler {

    @NotNull
    public Relationship getRelationship(Player source, Player target);
}
