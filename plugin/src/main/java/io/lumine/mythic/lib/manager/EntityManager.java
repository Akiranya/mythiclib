package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.DamageCheckEvent;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.comp.interaction.TargetRestriction;
import io.lumine.mythic.lib.comp.interaction.relation.RelationshipHandler;
import io.lumine.mythic.lib.util.CustomProjectile;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class EntityManager {
    private final Set<TargetRestriction> restrictions = new HashSet<>();
    private final Set<RelationshipHandler> relHandlers = new HashSet<>();

    /**
     * See {@link TargetRestriction} for more information. This should be
     * called as soon as MythicLib enablesby plugins implementing player sets
     * like parties, friends, factions.... any set that could support friendly fire.
     * <p>
     * This is also helpful to prevent players from interacting with
     * specific invulnerable entities like NPCs
     *
     * @param restriction New restriction for entities
     */
    public void registerRestriction(TargetRestriction restriction) {
        restrictions.add(restriction);
    }

    /**
     * Plugins which create player groups create relations between
     * players. Depending on the
     *
     * @param relationHandler New handler for player relations
     * @see {@link RelationshipHandler}
     */
    public void registerRelationHandler(RelationshipHandler relationHandler) {
        relHandlers.add(relationHandler);
    }

    /**
     * @see {@link CustomProjectile#getCustomData(Entity)}
     * @deprecated
     */
    @Nullable
    @Deprecated
    public CustomProjectile getCustomProjectile(Entity entity) {
        return CustomProjectile.getCustomData(entity);
    }

    /**
     * Registers a custom projectile. This is used for bows, crossbows and tridents.
     *
     * @deprecated Automatically registers on class instanciation
     */
    @Deprecated
    public void registerCustomProjectile(Entity entity, CustomProjectile projectileData) {
    }

    /**
     * Called whenever a player tries to damage OR buff an entity.
     * <p>
     * This should be used by:
     * - plugins which implement friendly fire player sets like parties, guilds, nations, factions....
     * - plugins which implement custom invulnerable entities like NPCs, sentinels....
     *
     * @param source          Player targeting another entity
     * @param target          Entity being targeted
     * @param interactionType Type of interaction, whether it's positive (buff, heal) or negative (offense skill, attack)
     * @return If false, any interaction should be cancelled!
     */
    public boolean canTarget(@NotNull Player source, @NotNull Entity target, @NotNull InteractionType interactionType) {

        // Simple checks
        if (source.equals(target) || target.isDead() || !(target instanceof LivingEntity) || target instanceof ArmorStand)
            return false;

        // Specific plugin checks (Not used anymore)
        final LivingEntity livingTarget = (LivingEntity) target;
        for (TargetRestriction restriction : restrictions)
            if (!restriction.canTarget(source, livingTarget, interactionType))
                return false;

        // Pvp Interaction rules: PvE -> PvP
        if (target instanceof Player) {

            final DamageCheckEvent damageCheckEvent = new DamageCheckEvent(source, target, interactionType);
            Bukkit.getPluginManager().callEvent(damageCheckEvent);

            // Offense
            if (interactionType.isOffense())
                return !damageCheckEvent.isCancelled();

            // Support
            final boolean pvp = !damageCheckEvent.isCancelled();
            for (RelationshipHandler relHandler : relHandlers)
                if (!MythicLib.plugin.getMMOConfig().pvpInteractionRules.isEnabled(pvp, interactionType, relHandler.getRelationship(source, (Player) target)))
                    return false;
        }

        return true;
    }

    @Deprecated
    public void unregisterCustomProjectile(Projectile projectile) {
        projectile.removeMetadata(CustomProjectile.METADATA_KEY, MythicLib.plugin);
    }
}
