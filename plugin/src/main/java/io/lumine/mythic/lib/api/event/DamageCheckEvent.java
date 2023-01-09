package io.lumine.mythic.lib.api.event;

import io.lumine.mythic.lib.comp.interaction.InteractionType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class DamageCheckEvent extends EntityDamageByEntityEvent {
    private final InteractionType interactionType;

    /**
     * This is the fake event used by MythicLib to
     * determine if a player can hit ANY entity
     *
     * @param damager Player damaging the entity
     * @param target  Entity being attacked
     */
    public DamageCheckEvent(@NotNull Player damager, @NotNull Entity target, @NotNull InteractionType interactionType) {
        super(damager, target, DamageCause.ENTITY_ATTACK, 0);

        this.interactionType = interactionType;
    }

    public InteractionType getInteractionType() {
        return interactionType;
    }
}
