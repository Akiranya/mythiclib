package io.lumine.mythic.lib.data.sql;

import io.lumine.mythic.lib.data.OfflineDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataHandler;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class SQLSynchronizedDataHandler<H extends SynchronizedDataHolder, O extends OfflineDataHolder, S extends SQLDataSynchronizer> implements SynchronizedDataHandler<H, O> {
    private final SQLDataSource dataSource;

    public SQLSynchronizedDataHandler(SQLDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SQLDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public CompletableFuture<Void> loadData(H playerData) {
        return CompletableFuture.runAsync(() -> {
            try {
                newDataSynchronizer(playerData).synchronize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    /**
     * @deprecated Not implemented yet
     */
    @Deprecated
    @Override
    public abstract void saveData(@NotNull H playerData, boolean autosave);

    @Override
    public void close() {
        getDataSource().close();
    }

    public abstract SQLDataSynchronizer newDataSynchronizer(H playerData);
}
