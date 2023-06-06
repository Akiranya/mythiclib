package io.lumine.mythic.lib.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class AutoSaveRunnable extends BukkitRunnable {
    private final SynchronizedDataManager<?, ?> manager;
    private final boolean log;

    /**
     * Minimum interval arbitrarily set to 60 seconds
     */
    private static final long MINIMUM_INTERVAL = 60;

    public AutoSaveRunnable(SynchronizedDataManager<?, ?> manager) {
        this.manager = manager;
        final ConfigurationSection config = manager.getOwningPlugin().getConfig().getConfigurationSection("auto-save");
        log = config.getBoolean("log", false);
        final long timer = Math.max(MINIMUM_INTERVAL, config.getLong("interval", 60 * 30)) * 20;
        runTaskTimerAsynchronously(manager.getOwningPlugin(), timer, timer);
    }

    @Override
    public void run() {
        if (log)
            manager.getOwningPlugin().getLogger().log(Level.INFO, "Autosaving player data, might take a while...");
        manager.saveAll(true);
        manager.whenAutoSaved();
    }
}
