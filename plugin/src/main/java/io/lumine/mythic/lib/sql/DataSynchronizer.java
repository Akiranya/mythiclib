package io.lumine.mythic.lib.sql;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
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
    private final UUID uuid;
    private final String tableName, uuidFieldName;
    private final long start = System.currentTimeMillis();

    private int tries;

    public DataSynchronizer(String tableName, String uuidFieldName, MMODataSource dataSource, UUID uuid) {
        this.tableName = tableName;
        this.uuidFieldName = uuidFieldName;
        this.uuid = uuid;
        this.dataSource = dataSource;
    }

    /**
     * Starts data fetching.
     *
     * @return True if the maximum amounf of tries hasn't been reached yet.
     */
    public boolean fetch() {
        tries++;

        CompletableFuture.runAsync(() -> {

            try {
                final Connection connection = dataSource.getConnection();
                final PreparedStatement prepared = connection.prepareStatement("SELECT * FROM `" + tableName + "` WHERE `" + uuidFieldName + "` = ?;");
                prepared.setString(1, uuid.toString());

                try {
                    UtilityMethods.debug(dataSource.getPlugin(), "SQL", "Trying to load data of " + uuid);
                    final ResultSet result = prepared.executeQuery();

                    // Load data if found
                    if (result.next()) {
                        if (tries > MythicLib.plugin.getMMOConfig().maxSyncTries || result.getInt("is_saved") == 1) {
                            confirmReception(connection);
                            loadData(result);
                            UtilityMethods.debug(dataSource.getPlugin(), "SQL", "Found and loaded data of '" + uuid+"'");
                            UtilityMethods.debug(dataSource.getPlugin(), "SQL", "Time taken: " + (System.currentTimeMillis() - start) + "ms");
                        } else {
                            UtilityMethods.debug(dataSource.getPlugin(), "SQL", "Did not load data of '" + uuid + "' as 'is_saved' is set to 0, trying again in 1s");
                            Bukkit.getScheduler().runTaskLater(MythicLib.plugin, this::fetch, 20);
                        }
                    } else {
                        // Empty player data
                        confirmReception(connection);
                        loadEmptyData();
                        UtilityMethods.debug(dataSource.getPlugin(), "SQL", "Found empty data for '" + uuid+ "', loading default");
                    }

                } catch (Throwable throwable) {
                    MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load player data of " + uuid);
                    throwable.printStackTrace();
                } finally {

                    // Close statement and connection to prevent leaks
                    prepared.close();
                    connection.close();
                }

            } catch (SQLException throwable) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load player data of " + uuid);
                throwable.printStackTrace();
            }
        });

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
        prepared1.setString(1, uuid.toString());
        try {
            prepared1.executeUpdate();
        } catch (Exception exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not confirm data sync of " + uuid);
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
