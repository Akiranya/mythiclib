package io.lumine.mythic.lib.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.lumine.mythic.lib.MythicLib;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;

public abstract class MMODataSource {
    protected final JavaPlugin plugin;
    protected final HikariConfig config = new HikariConfig();
    private HikariDataSource dataSource;

    /**
     * Used to know if SQL is enabled in the config. But connections can be made even
     * if it not enabled. (e.g /mmocore transferdata).
     */
    private boolean enabled;

    protected MMODataSource(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup(FileConfiguration fileConfig) {
        if (fileConfig.isConfigurationSection("mysql")) {
            ConfigurationSection cfg = fileConfig.getConfigurationSection("mysql");
            enabled = cfg.getBoolean("enabled");

            config.setPoolName("MMO-hikari");

            String sb = "jdbc:mysql://" + cfg.getString("host", "localhost") + ":" + cfg.getString("port", "3306") + "/"
                    + cfg.getString("database", "minecraft");

            config.setJdbcUrl(sb);
            config.setUsername(cfg.getString("user", "mmolover"));
            config.setPassword(cfg.getString("pass", "ILoveAria"));
            config.setMaximumPoolSize(cfg.getInt("maxPoolSize", 10));
            config.setMaxLifetime(cfg.getLong("maxLifeTime", 300000));
            config.setConnectionTimeout(cfg.getLong("connectionTimeOut", 10000));
            config.setLeakDetectionThreshold(cfg.getLong("leakDetectionThreshold", 150000));

            if (cfg.isConfigurationSection("properties"))
                for (String s : cfg.getConfigurationSection("properties").getKeys(false)) {
                    config.addDataSourceProperty(s, cfg.getString("properties." + s));
                }
            dataSource = new HikariDataSource(config);
            load();
        }
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    protected abstract void load();

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
            }
            connection.close();
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

    public boolean isEnabled() {
        return enabled;
    }
}

