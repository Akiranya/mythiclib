package io.lumine.mythic.lib.api.player;

import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.listener.PlayerListener;
import io.lumine.mythic.lib.player.CooldownMap;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MMOPlayerData {
    private final UUID uuid;

    private Player player;

    /**
     * Last time the player either logged in or logged out.
     */
    private long lastLogActivity;

    // Data saved till next server restart
    private final CooldownMap basicCooldowns = new CooldownMap();
    private final StatMap stats = new StatMap(this);

    private static final Map<UUID, MMOPlayerData> data = new HashMap<>();

    private MMOPlayerData(Player player) {
        this.uuid = player.getUniqueId();

        this.player = player;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * @return The player's StatMap which can be used by any other plugins to
     *         apply stat modifiers to ANY MMOItems/MMOCore/external stats,
     *         calculate stat values, etc.
     */
    public StatMap getStatMap() {
        return stats;
    }

    /**
     * @return The last time, in millis, the player logged in or out
     * @deprecated Use {@link #getLastLogActivity()} instead
     */
    @Deprecated
    public long getLastLogin() {
        return getLastLogActivity();
    }

    /**
     * @return The last time, in millis, the player logged in or out
     */
    public long getLastLogActivity() {
        return lastLogActivity;
    }

    /**
     * This method simply checks if the cached Player instance is null
     * because MMOLib uncaches it when the player leaves for memory purposes.
     *
     * @return If the player is currently online.
     */
    public boolean isOnline() {
        return player != null;
    }

    /**
     * Throws an IAE if the player is currently not online
     * OR if the Player instance was not cached in yet.
     * <p>
     * MythicLib updates the Player instance on event priority LOW
     * using {@link PlayerJoinEvent} here: {@link PlayerListener}
     *
     * @return Returns the corresponding Player instance.
     */
    public Player getPlayer() {
        Validate.notNull(player, "Player is offline");
        return player;
    }

    /**
     * Caches a new Player instance and refreshes the last log activity
     *
     * @param player Player instance to cache
     */
    public void updatePlayer(Player player) {
        this.player = player;
        this.lastLogActivity = System.currentTimeMillis();
    }

    /**
     * Used when damage mitigation or a crit occurs to apply cooldown
     *
     * @param cd    The type of mitigation
     * @param value Mitigation cooldown in seconds
     */
    public void applyCooldown(CooldownType cd, double value) {
        basicCooldowns.applyCooldown(cd.name(), value);
    }

    /**
     * @param cd Cooldown type
     * @return If the mecanic is currently on cooldown for the player
     */
    public boolean isOnCooldown(CooldownType cd) {
        return basicCooldowns.isOnCooldown(cd.name());
    }

    /**
     * Cooldown maps centralize cooldowns in MythicLib for easier use.
     * Can be used for item cooldows, item abilities, MMOCore player
     * skills or any other external plugin
     *
     * @return The main player's cooldown map
     */
    public CooldownMap getCooldownMap() {
        return basicCooldowns;
    }

    /**
     * Called everytime a player enters the server. If the
     * resource data is not initialized yet, initializes it.
     * <p>
     * This is called async using {@link AsyncPlayerPreLoginEvent} which does
     * not provide a Player instance, meaning the cached Player instance is NOT
     * loaded yet. It is only loaded when the player logs in using {@link PlayerJoinEvent}
     *
     * @param player Player whose data should be initialized
     */
    public static MMOPlayerData setup(Player player) {
        MMOPlayerData found = data.get(player.getUniqueId());

        // Not loaded yet
        if (found == null) {
            MMOPlayerData playerData = new MMOPlayerData(player);
            data.put(player.getUniqueId(), playerData);
            return playerData;
        }

        return found;
    }

    /**
     * This essentially checks if a player logged in since the last time the
     * server started/was reloaded.
     *
     * @param uuid The player UUID to check
     * @return If the MMOPlayerData instance is loaded for a specific
     *         player
     */
    public static boolean isLoaded(UUID uuid) {
        return data.containsKey(uuid);
    }

    public static MMOPlayerData get(@NotNull OfflinePlayer player) {
        return data.get(player.getUniqueId());
    }

    public static MMOPlayerData get(UUID uuid) {
        return Objects.requireNonNull(data.get(uuid), "Player data not loaded");
    }

    /**
     * @return Currently loaded MMOPlayerData instances. This can be used to
     *         apply things like resource regeneration or other runnable based
     *         tasks instead of looping through online players and having to
     *         resort to a map-lookup-based get(Player) call
     */
    public static Collection<MMOPlayerData> getLoaded() {
        return data.values();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MMOPlayerData)) return false;

        MMOPlayerData that = (MMOPlayerData) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}

