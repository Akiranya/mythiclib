package io.lumine.mythic.lib.sql;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * This class is used to syncronize player data between
 * servers. This fixes the issue of player data being
 * lost when teleporting to another server.
 * <p>
 * This can be generalized to not only player datas in the future.
 */
public abstract class DataSynchronizer {
    private final MMODataSource dataSource;
    private final MMOPlayerData mmoPlayerData;
    private final String tableName, uuidFieldName;
    private final long start = System.currentTimeMillis();

    private int tries;

    public DataSynchronizer(String tableName, String uuidFieldName, MMODataSource dataSource, UUID uuid) {
        this.tableName = tableName;
        this.uuidFieldName = uuidFieldName;
        this.mmoPlayerData = MMOPlayerData.get(uuid);
        this.dataSource = dataSource;
    }

    /**
     * Tries to fetch data once. If the maximum amount of fetches
     * hasn't been reached yet, it will try again later if no up-to-date
     * data has been retrieved.
     */
    public void tryToFetchData() {

        // Does nothing if player is offline
        if (!mmoPlayerData.isOnline()) {
            UtilityMethods.debug(dataSource.getPlugin(), "SQL", "Stopped data retrieval for '" + mmoPlayerData.getUniqueId() + "' as they went offline");
            return;
        }

        tries++;

        CompletableFuture.runAsync(() -> {

            try {
                final Connection connection = dataSource.getConnection();
                final PreparedStatement prepared = connection.prepareStatement("SELECT * FROM `" + tableName + "` WHERE `" + uuidFieldName + "` = ?;");
                prepared.setString(1, mmoPlayerData.getUniqueId().toString());

                try {
                    UtilityMethods.debug(dataSource.getPlugin(), "SQL", "Trying to load data of " + mmoPlayerData.getUniqueId());
                    final ResultSet result = prepared.executeQuery();

                    // Load data if found
                    if (result.next()) {
                        if (tries > MythicLib.plugin.getMMOConfig().maxSyncTries || result.getInt("is_saved") == 1) {
                            confirmReception(connection);
                            loadData(result);
                            UtilityMethods.debug(dataSource.getPlugin(), "SQL", "Found and loaded data of '" + mmoPlayerData.getUniqueId() + "'");
                            UtilityMethods.debug(dataSource.getPlugin(), "SQL", "Time taken: " + (System.currentTimeMillis() - start) + "ms");
                        } else {
                            UtilityMethods.debug(dataSource.getPlugin(), "SQL", "Did not load data of '" + mmoPlayerData.getUniqueId() + "' as 'is_saved' is set to 0, trying again in 1s");
                            Bukkit.getScheduler().runTaskLater(MythicLib.plugin, this::fetch, 20);
                        }
                    } else {
                        // Empty player data
                        confirmReception(connection);
                        loadEmptyData();
                        UtilityMethods.debug(dataSource.getPlugin(), "SQL", "Found empty data for '" + mmoPlayerData.getUniqueId() + "', loading default...");
                    }

                } catch (Throwable throwable) {
                    MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load player data of " + mmoPlayerData.getUniqueId());
                    throwable.printStackTrace();
                } finally {

                    // Close statement and connection to prevent leaks
                    prepared.close();
                    connection.close();
                }

            } catch (SQLException throwable) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load player data of " + mmoPlayerData.getUniqueId());
                throwable.printStackTrace();
            }
        });
    }

    /**
     * @deprecated Use {@link #tryToFetchData()} instead
     */
    public boolean fetch() {
        tryToFetchData();
        return true;
    }

    /**
     * This confirms the loading of player and switches "is_saved" back to 0
     *
     * @param connection Current SQL connection
     * @throws SQLException Any exception. When thrown, the data will not be loaded.
     */
    private void confirmReception(Connection connection) throws SQLException {

        // Confirm reception of inventory
        final PreparedStatement prepared1 = connection.prepareStatement("INSERT INTO " + tableName + "(`uuid`, `is_saved`) VALUES(?, 0) ON DUPLICATE KEY UPDATE `is_saved` = 0;");
        prepared1.setString(1, mmoPlayerData.getUniqueId().toString());
        try {
            prepared1.executeUpdate();
        } catch (Exception exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not confirm data sync of " + mmoPlayerData.getUniqueId());
            exception.printStackTrace();
        } finally {
            prepared1.close();
        }
    }

    /**
     * Called when the right result set has finally been found.
     *
     * @param result Row found in the database
     */
    public abstract void loadData(ResultSet result) throws SQLException, IOException, ClassNotFoundException;

    /**
     * Called when no data was found.
     */
    public abstract void loadEmptyData();
}
