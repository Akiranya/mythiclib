package io.lumine.mythic.lib.comp.profile;

import fr.phoenixdevt.profile.ProfileDataModule;
import fr.phoenixdevt.profile.ProfileProvider;
import fr.phoenixdevt.profile.event.ProfileChooseEvent;
import fr.phoenixdevt.profile.event.ProfileUnloadEvent;
import io.lumine.mythic.lib.api.event.SynchronizedDataLoadEvent;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * This is used to hook plugins which support profile-based data.
 * - MMOCore
 * - MMOItems
 * - MMOInventory
 * <p>
 * This class tells these plugins to load data when a profile is being
 * selected by a player. By default, player data loads on login so this
 * has to be changed to support profiles.
 *
 * @author jules
 */
public class ProfilePluginHook {

    public <H extends SynchronizedDataHolder> ProfilePluginHook(SynchronizedDataManager<H, ?> manager, Listener fictiveListener, EventPriority joinEventPriority, EventPriority quitEventPriority) {

        // Register data holder
        final ProfileProvider profilePlugin = Bukkit.getServicesManager().getRegistration(ProfileProvider.class).getProvider();
        final ProfileDataModule module = manager.newProfileDataModule();
        profilePlugin.registerModule(module);
        manager.getOwningPlugin().getLogger().log(Level.INFO, "Hooked onto Profiles");

        // Load data on profile select
        Bukkit.getPluginManager().registerEvent(ProfileChooseEvent.class, fictiveListener, joinEventPriority, (listener, evt) -> {
            final ProfileChooseEvent event = (ProfileChooseEvent) evt;
            final @NotNull H data = manager.get(event.getPlayer());
            if (data.isSynchronized()) event.validate(module); // More resilience
            else
                manager.getDataHandler().loadData(data).thenRun(() -> Bukkit.getScheduler().runTask(manager.getOwningPlugin(), () -> {
                    event.validate(module);
                    data.markAsSynchronized();
                    Bukkit.getPluginManager().callEvent(new SynchronizedDataLoadEvent(manager, data));
                }));
        }, manager.getOwningPlugin());

        // TODO Remove data on profile removal

        // Save data on profile unload
        Bukkit.getPluginManager().registerEvent(ProfileUnloadEvent.class, fictiveListener, quitEventPriority, (listener, event) -> manager.unregisterSafely(manager.get(((ProfileUnloadEvent) event).getPlayer())), manager.getOwningPlugin());
    }
}
