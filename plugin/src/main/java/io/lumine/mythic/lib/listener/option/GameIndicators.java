package io.lumine.mythic.lib.listener.option;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.IndicatorDisplayEvent;
import io.lumine.mythic.lib.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Random;

public abstract class GameIndicators implements Listener {
    private final String format;
    private final DecimalFormat decFormat;
    private final double radialVelocity, gravity, initialUpwardVelocity, entityHeightPercentage, yOffset;

    protected static final Random random = new Random();

    /**
     * Hologram life span in ticks
     */
    private static final int HOLOGRAM_LIFE_SPAN = 7;

    public GameIndicators(ConfigurationSection config) {
        decFormat = MythicLib.plugin.getMMOConfig().newDecimalFormat(config.getString("decimal-format"));
        format = config.getString("format");
        radialVelocity = config.getDouble("radial-velocity", 1);
        gravity = config.getDouble("gravity", 1);
        initialUpwardVelocity = config.getDouble("initial-upward-velocity", 1);
        entityHeightPercentage = config.getDouble("entity-height-percent", .75);
        yOffset = config.getDouble("y-offset", .1);
    }

    public String formatNumber(double d) {
        return decFormat.format(d);
    }

    public String getRaw() {
        return format;
    }

    /**
     * Displays a message using a hologram around an entity.
     * <p>
     * Since 1.3.4 holograms are not provided internally by
     * MythicLib with different providers and priorities. This
     * chooses the best provider depending on what plugins the
     * user has installed.
     *
     * @param entity  Entity used to find the hologram initial position.
     * @param message Message to display
     * @param dir     Average direction of the hologram indicator
     */
    public void displayIndicator(Entity entity, String message, @NotNull Vector dir, IndicatorDisplayEvent.IndicatorType type) {
        IndicatorDisplayEvent called = new IndicatorDisplayEvent(entity, message, type);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled())
            return;

        Location loc = entity.getLocation().add((random.nextDouble() - .5) * 1.2, yOffset + entity.getHeight() * entityHeightPercentage, (random.nextDouble() - .5) * 1.2);
        displayIndicator(loc, called.getMessage(), dir);
    }

    private void displayIndicator(Location loc, String message, @NotNull Vector dir) {
        // Use individual holo to hide the temporary armor stand
        Hologram holo = Hologram.create(loc, MythicLib.plugin.parseColors(Collections.singletonList(message)));

        // Parabola trajectory
        new BukkitRunnable() {
            double v = 6 * initialUpwardVelocity; // Initial upward velocity
            int i = 0; // Counter

            private final double acc = -10 * gravity; // Downwards acceleration
            private final double dt = 3d / 20d; // Delta_t used to integrate acceleration and velocity

            @Override
            public void run() {

                if (i == 0)
                    dir.multiply(2 * radialVelocity);

                // Remove hologram when reaching end of life
                if (i++ >= HOLOGRAM_LIFE_SPAN) {
                    holo.despawn();
                    cancel();
                    return;
                }

                v += acc * dt;
                loc.add(dir.getX() * dt, v * dt, dir.getZ() * dt);
                holo.updateLocation(loc);
            }
        }.runTaskTimer(MythicLib.plugin, 0, 3);
    }
}
