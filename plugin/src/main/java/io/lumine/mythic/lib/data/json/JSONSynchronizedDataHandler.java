package io.lumine.mythic.lib.data.json;

import com.google.gson.JsonObject;
import com.sk89q.worldguard.util.profile.cache.MySQLCache;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.data.OfflineDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataHandler;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.util.Jsonable;
import io.lumine.mythic.lib.util.config.JsonFile;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public abstract class JSONSynchronizedDataHandler<H extends SynchronizedDataHolder & Jsonable, O extends OfflineDataHolder> implements SynchronizedDataHandler<H, O> {
    private final Plugin owning;
    private final boolean profilePlugin;

    public JSONSynchronizedDataHandler(Plugin owning) {
        this(owning, false);
    }

    /**
     * @param owning        Plugin saving data
     * @param profilePlugin See {@link SynchronizedDataManager#SynchronizedDataManager(Plugin, SynchronizedDataHandler, boolean)}
     */
    public JSONSynchronizedDataHandler(Plugin owning, boolean profilePlugin) {
        this.owning = Objects.requireNonNull(owning, "Plugin cannot be null");
        this.profilePlugin = profilePlugin;
    }

    @Override
    public void saveData(H playerData, boolean autosave) {
        // TODO json object is uselessly loaded into memory
        final JsonFile file = getUserFile(playerData);
        file.setContent(playerData.toJson());
        file.save();
    }

    @Override
    public CompletableFuture<Void> loadData(H playerData) {
        return CompletableFuture.runAsync(() -> {
            try {
                loadFromObject(playerData, getUserFile(playerData).getContent());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public abstract void loadFromObject(H playerData, JsonObject json);

    private JsonFile getUserFile(H data) {
        final UUID effectiveUUID = profilePlugin ? data.getUniqueId() : data.getProfileId();
        return new JsonFile(owning, "/userdata", effectiveUUID.toString());
    }
}
