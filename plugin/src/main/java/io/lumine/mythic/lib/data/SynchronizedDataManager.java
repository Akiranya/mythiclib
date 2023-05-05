package io.lumine.mythic.lib.data;

import fr.phoenixdevt.profile.event.ProfileChooseEvent;
import fr.phoenixdevt.profile.event.ProfileUnloadEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.AsyncSynchronizedDataLoadEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.Closeable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public abstract class SynchronizedDataManager<H extends SynchronizedDataHolder, O extends OfflineDataHolder> {
    private final Plugin owning;
    private final Map<UUID, H> activeData = Collections.synchronizedMap(new HashMap<>());

    /**
     * Profile plugins behave differently on data storage:
     * - they use the player UUID directly instead of using profile IDs
     * - this option has to be passed to all of the data handlers
     */
    private final boolean profilePlugin;

    @NotNull
    private SynchronizedDataHandler<H, O> dataHandler;

    public SynchronizedDataManager(@NotNull Plugin owning, @NotNull SynchronizedDataHandler<H, O> dataHandler) {
        this(owning, dataHandler, false);
    }

    public SynchronizedDataManager(@NotNull Plugin owning, @NotNull SynchronizedDataHandler<H, O> dataHandler, boolean profilePlugin) {
        this.owning = Objects.requireNonNull(owning, "Plugin cannot be null");
        this.dataHandler = Objects.requireNonNull(dataHandler, "Data handler cannot be null");
        this.profilePlugin = profilePlugin;
    }

    public void setDataHandler(@NotNull SynchronizedDataHandler<H, O> dataHandler) {
        this.dataHandler = Objects.requireNonNull(dataHandler, "Data handler cannot be null");

        dataHandler.setup();
    }

    public SynchronizedDataHandler<H, O> getDataHandler() {
        return dataHandler;
    }

    @NotNull
    public Plugin getOwningPlugin() {
        return owning;
    }

    @NotNull
    public H get(OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    /**
     * Gets the player data, or throws an exception if not found.
     * The player data should be loaded when the player logs in
     * so it's really bad practice to setup the player data if it's not loaded.
     *
     * @param uuid Player UUID
     * @return Player data, if it's loaded
     */
    @NotNull
    public H get(UUID uuid) {
        return Objects.requireNonNull(activeData.get(uuid), "Player data is not loaded");
    }

    @Nullable
    public H getOrNull(OfflinePlayer player) {
        return getOrNull(player.getUniqueId());
    }


    @Nullable
    public H getOrNull(UUID uuid) {
        return activeData.get(uuid);
    }

    /**
     * Offline player data is used to handle processes like friend removal
     * which can still occur if one of the two players is offline.
     * <p>
     * Unlike {@link #get(UUID)} this method never returns a null instance
     *
     * @param uuid Player unique id
     * @return Offline player data
     */
    @NotNull
    public O getOffline(UUID uuid) {
        return isLoaded(uuid) ? (O) activeData.get(uuid) : dataHandler.getOffline(uuid);
    }

    /**
     * Saves all currently loaded data. It is either used on server
     * shutdown, which requires to save all the data of currently
     * connected players, or when performing frequent autosaves.
     */
    public void saveAll(boolean autosave) {
        for (H holder : getLoaded())
            if (holder.isSynchronized()) getDataHandler().saveData(holder, autosave);
    }

    /**
     * Setups all player datas for online players. This method is
     * mainly used on server reloads.
     */
    public void setupAll() {
        Bukkit.getOnlinePlayers().forEach(this::setup);
    }

    /**
     * Called when a player logs in, loading the player data inside the map.
     * <p>
     * For YAML configs or SQL databases, data is loaded sync as not to overload
     * the main thread with SQL requests. Therefore, the object returned by that
     * function is always empty.
     *
     * @param player Player UUID (not profile)
     * @return The empty player data, which will be loaded in a near future.
     * @deprecated
     */
    public H setup(@NotNull Player player) {

        // Load player data if it already exists (should never happen)
        final @Nullable H current = activeData.get(player.getUniqueId());
        if (current != null) return current;

        // Initialize player data and schedule loading
        final H newData = newPlayerData(MMOPlayerData.get(player.getUniqueId()));
        if (profilePlugin || MythicLib.plugin.getProfileModule().loadsDataOnLogin())
            dataHandler.loadData(newData).thenAccept(unused -> {
                newData.markAsSynchronized();
                Bukkit.getPluginManager().callEvent(new AsyncSynchronizedDataLoadEvent(this, newData));
            });

        // Update data map and return
        activeData.put(player.getUniqueId(), newData);
        return newData;
    }

    /**
     * Safely unregisters the player data from the map.
     * This saves the player data either through SQL or YAML,
     * then closes the player data and clears it from the data map.
     *
     * @param playerData PLayer data to unregister
     */
    public void unregisterSafely(H playerData) {

        // Save data async if required
        if (playerData.isSynchronized())
            Bukkit.getScheduler().runTaskAsynchronously(owning, () -> dataHandler.saveData(playerData, false));

        // Close and unregister data instantly if no error occured
        if (playerData instanceof Closeable) ((Closeable) playerData).close();
        activeData.remove(playerData.getUniqueId());
    }

    /**
     * @param playerData Data of player who just logged in
     * @return A new instance of player data
     */
    public abstract H newPlayerData(@NotNull MMOPlayerData playerData);

    public boolean isLoaded(UUID uuid) {
        return activeData.containsKey(uuid);
    }

    public Collection<H> getLoaded() {
        return activeData.values();
    }

    private static final Listener FICTIVE_LISTENER = new Listener() {
    };

    /**
     * This method registers the join and quit events which are required in order
     * to load and unload data at the right time. By manipulating the event priority,
     * you can choose which plugin load their data first. For instance,
     * <p>
     * MythicLib > MMOProfiles > MMOCore > MMOItems/MMOInventory
     *
     * @param joinEventPriority Event priority when logging in
     * @param quitEventPriority Event priority when logging off
     */
    public void registerEvents(@NotNull EventPriority joinEventPriority, @NotNull EventPriority quitEventPriority) {

        // Load data on login
        Bukkit.getPluginManager().registerEvent(PlayerJoinEvent.class, FICTIVE_LISTENER, joinEventPriority, (listener, event) -> setup(((PlayerJoinEvent) event).getPlayer()), owning);

        // Profile event if profile module is installed
        if (!profilePlugin && !MythicLib.plugin.getProfileModule().loadsDataOnLogin()) {

            // Load data on profile select
            Bukkit.getPluginManager().registerEvent(ProfileChooseEvent.class, FICTIVE_LISTENER, joinEventPriority, (listener, event) -> {
                final ProfileChooseEvent ev = (ProfileChooseEvent) event;
                final @NotNull H data = get(ev.getPlayer());
                dataHandler.loadData(data).thenAccept(unused -> {
                    data.markAsSynchronized();
                    ev.markAsLoaded(getOwningPlugin());
                    Bukkit.getPluginManager().callEvent(new AsyncSynchronizedDataLoadEvent(this, data));
                });
            }, owning);

            // Save data on profile unload
            Bukkit.getPluginManager().registerEvent(ProfileUnloadEvent.class, FICTIVE_LISTENER, quitEventPriority, (listener, event) -> unregisterSafely(get(((ProfileUnloadEvent) event).getPlayer())), owning);
        } else

            // Save data on logout
            Bukkit.getPluginManager().registerEvent(PlayerQuitEvent.class, FICTIVE_LISTENER, quitEventPriority, (listener, event) -> unregisterSafely(get(((PlayerQuitEvent) event).getPlayer())), owning);
    }
}
