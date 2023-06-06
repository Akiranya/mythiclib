package io.lumine.mythic.lib.data.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.lumine.mythic.lib.MythicLib;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;

// TODO clear methods and implement CompletableFutures
public class SQLDataSource {
    private final JavaPlugin plugin;
    private final HikariDataSource dataSource;

    public SQLDataSource(JavaPlugin plugin) {
        this.plugin = plugin;

        // Prepare Hikari config
        final ConfigurationSection config = plugin.getConfig().getConfigurationSection("mysql");
        final HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("MMO-hikari");
        hikari.setJdbcUrl("jdbc:mysql://" + config.getString("host", "localhost") + ":" + config.getString("port", "3306") + "/" + config.getString("database", "minecraft"));
        hikari.setUsername(config.getString("user", "mmolover"));
        hikari.setPassword(config.getString("pass", "ILoveAria"));
        hikari.setMaximumPoolSize(config.getInt("maxPoolSize", 10));
        hikari.setMaxLifetime(config.getLong("maxLifeTime", 300000));
        hikari.setConnectionTimeout(config.getLong("connectionTimeOut", 10000));
        hikari.setLeakDetectionThreshold(config.getLong("leakDetectionThreshold", 150000));
        if (config.isConfigurationSection("properties"))
            for (String s : config.getConfigurationSection("properties").getKeys(false))
                hikari.addDataSourceProperty(s, config.getString("properties." + s));

        dataSource = new HikariDataSource(hikari);
    }

    @NotNull
    public JavaPlugin getPlugin() {
        return plugin;
    }

    public void getResult(String sql, Consumer<ResultSet> supplier) {
        execute(connection -> {
            try {
                final PreparedStatement statement = connection.prepareStatement(sql);
                try {
                    supplier.accept(statement.executeQuery());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                statement.close();
            } catch (SQLException exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not open SQL result statement:");
                exception.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> getResultAsync(String sql, Consumer<ResultSet> supplier) {
        return CompletableFuture.runAsync(() -> getResult(sql, supplier));
    }

    public void executeUpdate(String sql) {
        execute(connection -> {
            try {
                final PreparedStatement statement = connection.prepareStatement(sql);
                try {
                    statement.executeUpdate();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                statement.close();
            } catch (SQLException exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not open SQL statement:");
                exception.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> executeUpdateAsync(String sql) {
        return CompletableFuture.runAsync(() -> executeUpdate(sql));
    }

    /**
     * Retrieve a connection from pool and prepare it for
     * use. Connection is closed when consumer is called.
     *
     * @param execute Action to be done with connection
     */
    public void execute(Consumer<Connection> execute) {
        try {
            final Connection connection = dataSource.getConnection();
            try {
                execute.accept(connection);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not open SQL connection:");
            exception.printStackTrace();
        }
    }

    /**
     * Retrieve a connection from pool and prepare it for
     * use. Connection is closed when consumer is called.
     * <p>
     * Called asynchronously.
     *
     * @param execute Action to be done with connection
     */
    public CompletableFuture<Void> executeAsync(Consumer<Connection> execute) {
        return CompletableFuture.runAsync(() -> execute(execute));
    }

    @Deprecated
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null)
            dataSource.close();
    }
}

