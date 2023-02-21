package io.lumine.mythic.lib.sql;

import io.lumine.mythic.lib.MythicLib;
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

    /**
     * Maximum amount of tries before
     */
    private static final int MAX_TRIES = 3;

    public DataSynchronizer(String tableName, String uuidFieldName, MMODataSource dataSource, UUID uuid) {
        this.tableName = tableName;
        this.uuidFieldName = uuidFieldName;
        this.uuid = uuid;
        this.dataSource = dataSource;
    }

    /**
     * Tries to fetch data
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
                    MythicLib.debug("SQL", "Trying to load data of " + uuid);
                    final ResultSet result = prepared.executeQuery();

                    // Load data if found
                    if (result.next()) {
                        if (tries > MAX_TRIES || result.getInt("is_saved") == 1) {
                            confirmReception(connection);
                            loadData(result);
                            MythicLib.debug("SQL", "Found and loaded data of " + uuid);
                            MythicLib.debug("SQL", "Time taken: " + (System.currentTimeMillis() - start) + "ms");
                        } else {
                            MythicLib.debug("SQL", "Could not load data of " + uuid + " because is_saved is set to 0, trying again in 1s");
                            Bukkit.getScheduler().runTaskLater(MythicLib.plugin, this::fetch, 20);
                        }
                    } else
                        // Empty inventory
                        confirmReception(connection);

                } catch (Throwable throwable) {
                    MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load player inventory of " + uuid);
                    throwable.printStackTrace();
                } finally {

                    // Close statement and connection to prevent leaks
                    prepared.close();
                    connection.close();
                }

            } catch (SQLException throwable) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load player inventory of " + uuid);
                throwable.printStackTrace();
            }
        });

        return true;
    }

    /**
     * @deprecated Not used
     */
    @Deprecated
    public void confirmSaving(Connection connection) throws SQLException {
        final PreparedStatement prepared1 = connection.prepareStatement("INSERT INTO " + tableName + "(`uuid`, `is_saved`) VALUES(?, 0) ON DUPLICATE KEY UPDATE `is_saved` = 1;");
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
     * This switches
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
