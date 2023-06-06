package io.lumine.mythic.lib.data;

import io.lumine.mythic.lib.util.Closeable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A data handler "handles" data, it decides what happens when
 * data is either saved OR loaded from either SQL or YAML. Remember
 * that a {@link SynchronizedDataManager} does not know if it is
 * using SQL or YAML.
 *
 * @author jules
 */
public interface SynchronizedDataHandler<H extends SynchronizedDataHolder, O extends OfflineDataHolder> extends Closeable {

    /**
     * Called once on server startup. This can be used for SQL support
     * to initialize database tables, and make sure they are up to date.
     */
    public void setup();

    /**
     * Called when player data must be saved in configs or database.
     * This method is always called async, no need to wrap requests
     * inside of async runnables.
     *
     * @param playerData Player data to save
     * @param autosave   When logging out, is_saved is switched back to 1. This behaviour
     *                   must be skipped when autosaving otherwise this will mess with
     *                   the database.
     */
    public void saveData(@NotNull H playerData, boolean autosave);

    /**
     * This method is always called SYNC, and therefore must wrap HTTP
     * or SQL requests inside of async CompletableFutures. This completable
     * future should be completed when the data is finished loading.
     *
     * @param playerData Player data to load
     */
    public CompletableFuture<Void> loadData(@NotNull H playerData);

    public O getOffline(@NotNull UUID profileId);
}
