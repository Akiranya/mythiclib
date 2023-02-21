package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

// TODO use in default skill handlers
public class CustomProjectileHandler {
    private final Set<Integer> untargetable = new HashSet<>();
    private final PlayerMetadata caster;
    private final InteractionType interactionType;

    private static final double BOUNDING_BOX_EXPANSION = .2;

    /**
     * Utility methods for when casting projectiles. This also provides
     * a cache for checked entities to improve performance
     *
     * @param caster Player casting the projectile
     */
    public CustomProjectileHandler(PlayerMetadata caster) {
        this(caster, InteractionType.OFFENSE_SKILL);
    }

    /**
     * Utility methods for when casting projectiles. This also provides
     * a cache for checked entities to improve performance
     *
     * @param caster          Player casting the projectile
     * @param interactionType Type of interaction for the projectile.
     *                        By default, set to {@link InteractionType#OFFENSE_SKILL}
     */
    public CustomProjectileHandler(PlayerMetadata caster, InteractionType interactionType) {
        this.caster = caster;
        this.interactionType = interactionType;
    }

    /**
     * Called whenever a player tries to damage OR buff an entity.
     * The result is cached by this class to reduce useless computations.
     * No bounding box checks.
     *
     * @param target The entity being hit
     * @return If the entity can be damaged, by a specific player, at a specific spot
     */
    public boolean canTarget(@NotNull Entity target) {
        return canTarget(null, target);
    }

    /**
     * Called whenever a player tries to damage OR buff an entity.
     * The result is cached by this class to reduce useless computations.
     *
     * @param target Entity being targeted
     * @return If false, any interaction should be cancelled!
     */
    public boolean canTarget(@Nullable Location loc, @NotNull Entity target) {

        // Check for bounding box
        if (loc != null && !target.getBoundingBox().expand(BOUNDING_BOX_EXPANSION).contains(loc.toVector()))
            return false;

        if (untargetable.contains(target.getEntityId()))
            return false;

        // Interaction type check
        if (!MythicLib.plugin.getEntities().canInteract(caster.getPlayer(), target, interactionType)) {
            untargetable.add(target.getEntityId());
            return false;
        }

        return true;
    }

    /**
     * Finds a potential target for the projectile. Looks
     * through entities registered in nearby chunks
     *
     * @param loc Projectile current location
     * @return If it exists, a potential entity target
     */
    @Nullable
    public LivingEntity findTarget(@NotNull Location loc) {
        for (Entity ent : UtilityMethods.getNearbyChunkEntities(loc))
            if (canTarget(loc, ent))
                return (LivingEntity) ent;
        return null;
    }
}
