package io.lumine.mythic.lib.comp.profile;

import fr.phoenixdevt.profile.ProfileDataModule;
import fr.phoenixdevt.profile.event.ProfileCreateEvent;
import fr.phoenixdevt.profile.event.ProfileRemoveEvent;
import fr.phoenixdevt.profile.placeholder.PlaceholderRequest;
import io.lumine.mythic.lib.UtilityMethods;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Used for simple data plugins like MMOItems or MMOInventory,
 * featuring profile data support but which do not have any
 * placeholders or profile-based features.
 *
 * @author Jules
 */
public class DefaultProfileDataModule implements ProfileDataModule, Listener {
    private final JavaPlugin plugin;
    private final String id;

    public DefaultProfileDataModule(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.id = UtilityMethods.enumName(plugin.getName()).toLowerCase();
    }

    @Override
    public JavaPlugin getOwningPlugin() {
        return plugin;
    }

    @Override
    public boolean hasPlaceholders() {
        return false;
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public void processPlaceholderRequest(PlaceholderRequest placeholderRequest) {
        throw new RuntimeException("Not supported");
    }

    @EventHandler
    public void onProfileCreate(ProfileCreateEvent event) {
        event.validate(this);
    }

    @EventHandler
    public void onProfileDelete(ProfileRemoveEvent event) {
        event.validate(this);
    }
}
