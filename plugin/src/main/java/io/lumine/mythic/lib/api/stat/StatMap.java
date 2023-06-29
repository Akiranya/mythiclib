package io.lumine.mythic.lib.api.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.player.PlayerMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatMap implements StatProvider {
    private final MMOPlayerData data;
    private final Map<String, StatInstance> stats = new ConcurrentHashMap<>();

    public StatMap(MMOPlayerData player) {
        this.data = player;
    }

    /**
     * @return The StatMap owner ie the corresponding MMOPlayerData
     */
    public MMOPlayerData getPlayerData() {
        return data;
    }

    /**
     * @param stat The string key of the stat
     * @return The value of the stat after applying stat modifiers
     */
    @Override
    public double getStat(String stat) {
        return getInstance(stat).getTotal();
    }

    /**
     * StatInstances are completely flushed when the server restarts
     *
     * @param id The string key of the stat
     * @return The corresponding StatInstance, which can be manipulated to add
     *         (temporary?) stat modifiers to a player, remove modifiers or
     *         calculate stat values in various ways.
     */
    @NotNull
    public StatInstance getInstance(String id) {
        return stats.computeIfAbsent(id, stat -> new StatInstance(this, stat));
    }

    /**
     * @return The StatInstances that have been manipulated so far since the
     *         player has logged in. StatInstances are completely flushed when
     *         the server restarts
     */
    public Collection<StatInstance> getInstances() {
        return stats.values();
    }

    /**
     * Some stats like movement speed, attack damage.. are based on vanilla
     * player attributes. Every time a stat modifier is added to a StatInstance
     * in MythicLib, MythicLib needs to perform a further attribute modifier update.
     * This method runs all the updates for the vanilla-attribute-based MythicLib
     * stats.
     * <p>
     * Performance heavy method
     */
    @Deprecated
    public void updateAll() {
        MythicLib.plugin.getStats().runUpdates(this);
    }

    /***
     * Runs a specific stat update for a specific StatMap
     *
     * @param stat
     *            The string key of the stat which needs update
     */
    @Deprecated
    public void update(String stat) {
        final @Nullable StatHandler handler = MythicLib.plugin.getStats().getStatHandler(stat);
        if (handler != null) handler.runUpdate(getInstance(stat));
    }

    /**
     * @param castHand The casting hand matters a lot! Should MythicLib take into account
     *                 the 'Skill Damage' due to the offhand weapon, when casting a
     *                 skill with mainhand?
     * @return Some actions require the player stats to be temporarily saved.
     *         When a player casts a projectile skill, there's a brief delay
     *         before it hits the target: the stat values taken into account
     *         correspond to the stat values when the player cast the skill (not
     *         when it finally hits the target). This cache technique fixes a
     *         huge game breaking glitch
     */
    public PlayerMetadata cache(EquipmentSlot castHand) {
        return new PlayerMetadata(this, castHand);
    }
}
