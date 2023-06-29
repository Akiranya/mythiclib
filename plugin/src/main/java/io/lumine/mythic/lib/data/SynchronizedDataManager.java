package io.lumine.mythic.lib.data;

import fr.phoenixdevt.profile.ProfileDataModule;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.SynchronizedDataLoadEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.comp.profile.ProfilePluginHook;
import io.lumine.mythic.lib.util.Closeable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A general player data manager which implements
 * - player data caching on login
 * - support for both YAML and SQL
 * - better SQL data synchronization between servers
 * - profile-based data saving for MMOProfiles
 *
 * @param <H> Type of player data being cached on login
 * @param <O> This is used to manipulate player data when players
 *            are offline
 * @author jules
 */
public abstract class SynchronizedDataManager<H extends SynchronizedDataHolder, O extends OfflineDataHolder> {
    private final JavaPlugin owning;
    private final Map<UUID, H> activeData = Collections.synchronizedMap(new HashMap<>());

    /**
     * Profile plugins behave differently on data storage:
     * - they use the player UUID directly instead of using profile IDs
     * - this option has to be passed to all of the data handlers
     */
    private final boolean profilePlugin;

    @NotNull
    private SynchronizedDataHandler<H, O> dataHandler;

    public SynchronizedDataManager(@NotNull JavaPlugin owning, @NotNull SynchronizedDataHandler<H, O> dataHandler) {
        this(owning, dataHandler, false);
    }

    public SynchronizedDataManager(@NotNull JavaPlugin owning, @NotNull SynchronizedDataHandler<H, O> dataHandler, boolean profilePlugin) {
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
    public JavaPlugin getOwningPlugin() {
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
     *
     * On server shutdown (/restart) pending async methods must be
     * completed before the program stops, otherwise data saving
     * tasks are lost, deleting the players' progressions.
     */
    public void saveAll(boolean autosave) {
        for (H holder : getLoaded())
            if (holder.isSynchronized()) {
                if (!autosave && holder instanceof Closeable) ((Closeable) holder).close();
                getDataHandler().saveData(holder, autosave);
            }

        // Execute pending async tasks (supports /restart and /stop)
        if (!autosave) for (BukkitTask pending : Bukkit.getScheduler().getPendingTasks())
            if (pending.getOwner().equals(owning))
                ((Runnable) pending).run();
    }

    private static final Listener FICTIVE_LISTENER = new Listener() {
    };

    /**
     * This method is called when the plugin enables and does three things:
     * - initialize player data of currently connected players. This makes the plugin
     * support the /reload command.
     * - register the join and quit events which are required to load and unload data
     * at the right time. By manipulating the event priority, you can choose which
     * plugin load their data first.
     * MythicLib > MMOProfiles > MMOCore > MMOItems/MMOInventory
     * - enable auto-save if found in the configuration file
     *
     * @param joinEventPriority Event priority when logging in
     * @param quitEventPriority Event priority when logging off
     */
    public void initialize(@NotNull EventPriority joinEventPriority, @NotNull EventPriority quitEventPriority) {

        // Setup online player data
        Bukkit.getOnlinePlayers().forEach(this::setup);

        // Auto-save
        if (owning.getConfig().getBoolean("auto-save.enabled")) new AutoSaveRunnable(this);

        // Load data on login
        Bukkit.getPluginManager().registerEvent(PlayerJoinEvent.class, FICTIVE_LISTENER, joinEventPriority, (listener, event) -> setup(((PlayerJoinEvent) event).getPlayer()), owning);

        // Profile events if profile module is installed
        if (!profilePlugin && MythicLib.plugin.hasProfiles())
            new ProfilePluginHook(this, FICTIVE_LISTENER, joinEventPriority, quitEventPriority);

            // Save data on logout
        else
            Bukkit.getPluginManager().registerEvent(PlayerQuitEvent.class, FICTIVE_LISTENER, quitEventPriority, (listener, event) -> unregisterSafely(get(((PlayerQuitEvent) event).getPlayer())), owning);
    }

    /**
     * Called when the data manager is being auto saved.
     */
    public void whenAutoSaved() {
        // Nothing by default
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
     */
    public H setup(@NotNull Player player) {

        // Get data or compute it if non existent (more resilient)
        final @Nullable H playerData = activeData.computeIfAbsent(player.getUniqueId(), uuid -> newPlayerData(MMOPlayerData.get(player.getUniqueId())));

        // Schedule data loading
        if (!playerData.isSynchronized() && (profilePlugin || !MythicLib.plugin.hasProfiles()))
            dataHandler.loadData(playerData).thenRun(() -> Bukkit.getScheduler().runTask(owning, () -> {
                playerData.markAsSynchronized();
                Bukkit.getPluginManager().callEvent(new SynchronizedDataLoadEvent(this, playerData));
            }));

        return playerData;
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
        if (playerData.isSynchronized() && playerData.shouldBeSaved())
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

    /**
     * @return An object of type {@link ProfileDataModule} which is an object
     * that cannot be referenced inside of that class to avoid import issues.
     */
    public abstract Object newProfileDataModule();

    public boolean isLoaded(UUID uuid) {
        return activeData.containsKey(uuid);
    }

    public Collection<H> getLoaded() {
        return activeData.values();
    }
}
