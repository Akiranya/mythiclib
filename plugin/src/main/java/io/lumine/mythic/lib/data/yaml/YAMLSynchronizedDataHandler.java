package io.lumine.mythic.lib.data.yaml;

import io.lumine.mythic.lib.data.OfflineDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataHandler;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.util.ConfigFile;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class YAMLSynchronizedDataHandler<H extends SynchronizedDataHolder, O extends OfflineDataHolder> implements SynchronizedDataHandler<H, O> {
    private final Plugin owning;
    private final boolean profilePlugin;

    public YAMLSynchronizedDataHandler(Plugin owning) {
        this(owning, false);
    }

    /**
     * @param owning        Plugin saving data
     * @param profilePlugin See {@link SynchronizedDataManager#SynchronizedDataManager(JavaPlugin, SynchronizedDataHandler, boolean)}
     */
    public YAMLSynchronizedDataHandler(Plugin owning, boolean profilePlugin) {
        this.owning = Objects.requireNonNull(owning, "Plugin cannot be null");
        this.profilePlugin = profilePlugin;
    }

    @Override
    public void saveData(H playerData, boolean autosave) {
        // TODO config section is uselessly loaded into memory
        final ConfigFile configFile = getUserFile(playerData);
        saveInSection(playerData, configFile.getConfig());
        configFile.save();
    }

    public abstract void saveInSection(H playerData, ConfigurationSection config);

    @Override
    public CompletableFuture<Void> loadData(H playerData) {
        return CompletableFuture.runAsync(() -> {
            try {
                loadFromSection(playerData, getUserFile(playerData).getConfig());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public abstract void loadFromSection(H playerData, ConfigurationSection config);

    private ConfigFile getUserFile(H data) {
        final UUID effectiveUUID = profilePlugin ? data.getUniqueId() : data.getProfileId();
        return new ConfigFile(owning, "/userdata", effectiveUUID.toString());
    }
}
