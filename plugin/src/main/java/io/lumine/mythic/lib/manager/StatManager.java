package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.stat.SharedStat;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.handler.AttributeStatHandler;
import io.lumine.mythic.lib.api.stat.handler.MovementSpeedStatHandler;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import io.lumine.mythic.lib.util.ConfigFile;
import org.apache.commons.lang.Validate;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

public class StatManager {
    private final Map<String, StatHandler> handlers = new HashMap<>();
    private final Map<String, DecimalFormat> decimalFormats = new HashMap<>();

    private DecimalFormat defaultDecimalFormat;

    public StatManager() {

        // Default stat handlers
        handlers.put(SharedStat.ARMOR, new AttributeStatHandler(Attribute.GENERIC_ARMOR, SharedStat.ARMOR));
        handlers.put(SharedStat.ARMOR_TOUGHNESS, new AttributeStatHandler(Attribute.GENERIC_ARMOR_TOUGHNESS, SharedStat.ARMOR_TOUGHNESS));

        handlers.put(SharedStat.ATTACK_DAMAGE, new AttributeStatHandler(Attribute.GENERIC_ATTACK_DAMAGE, SharedStat.ATTACK_DAMAGE, true));
        handlers.put(SharedStat.ATTACK_SPEED, new AttributeStatHandler(Attribute.GENERIC_ATTACK_SPEED, SharedStat.ATTACK_SPEED, true));
        handlers.put(SharedStat.KNOCKBACK_RESISTANCE, new AttributeStatHandler(Attribute.GENERIC_KNOCKBACK_RESISTANCE, SharedStat.KNOCKBACK_RESISTANCE));
        handlers.put(SharedStat.MAX_HEALTH, new AttributeStatHandler(Attribute.GENERIC_MAX_HEALTH, SharedStat.MAX_HEALTH));

        handlers.put(SharedStat.MOVEMENT_SPEED, new MovementSpeedStatHandler(true));
        handlers.put(SharedStat.SPEED_MALUS_REDUCTION, new MovementSpeedStatHandler(false));
    }

    public void initialize(boolean clearBefore) {
        if (clearBefore)
            decimalFormats.clear();
        else
            UtilityMethods.loadDefaultFile("", "stats.yml");

        // Load decimal formats
        defaultDecimalFormat = MythicLib.plugin.getMMOConfig().newDecimalFormat("0.#");
        FileConfiguration config = new ConfigFile("stats").getConfig();
        for (String key : config.getConfigurationSection("decimal-format").getKeys(false))
            try {
                final String stat = UtilityMethods.enumName(key);
                decimalFormats.put(stat, MythicLib.plugin.getMMOConfig().newDecimalFormat(config.getString("decimal-format." + key)));
            } catch (RuntimeException exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load decimal format of '" + key + "': " + exception.getMessage());
            }
    }

    @NotNull
    public static String format(String stat, double value) {
        final StatManager manager = MythicLib.plugin.getStats();
        return Objects.requireNonNullElse(manager.decimalFormats.get(stat), manager.defaultDecimalFormat).format(value);
    }

    /**
     * Some stats like movement speed, attack damage.. are based on vanilla
     * player attributes. Every time a stat modifier is added to a StatInstance
     * in MythicLib, MythicLib needs to perform a further attribute modifier update.
     * This method runs all the updates for the vanilla-attribute-based MythicLib
     * stats.
     *
     * @param map The StatMap of the player who needs update
     */
    public void runUpdates(StatMap map) {
        handlers.values().forEach(update -> update.runUpdate(map));
    }

    /**
     * Runs a specific stat update for a specific StatMap
     *
     * @param map  The StatMap of the player who needs update
     * @param stat The string key of the stat which needs update
     */
    public void runUpdate(StatMap map, String stat) {
        StatHandler handler = handlers.get(stat);
        if (handler != null)
            handler.runUpdate(map);
    }

    /**
     * @param stat The string key of the stat
     * @return The base value of this stat, or 0 if it does not have any
     */
    public double getBaseValue(String stat, StatMap map) {
        StatHandler handler = handlers.get(stat);
        return handler == null ? 0 : handler.getBaseValue(map);
    }

    /**
     * @param stat The string key of the stat
     * @return The total value of this stat
     */
    public double getTotalValue(String stat, StatMap map) {
        StatHandler handler = handlers.get(stat);
        return handler == null ? map.getStat(stat) : handler.getTotalValue(map);
    }

    /**
     * Lets MMOCore knows that a specific stat needs an update whenever the
     * value of the player stat changes (due to a MythicLib stat modifier being
     * added/being removed/expiring).
     *
     * @param stat    The string key of the stat
     * @param handler Behaviour of given stat
     */
    public void registerStat(String stat, StatHandler handler) {
        Validate.notNull(stat, "Stat cannot be null");
        Validate.notNull(handler, "StatHandler cannot be null");

        handlers.put(stat, handler);
    }

    @Nullable
    public StatHandler getStatHandler(String id) {
        return handlers.get(id);
    }

    public boolean isRegistered(String stat) {
        return handlers.containsKey(stat);
    }

    public Set<String> getRegisteredStats() {
        return handlers.keySet();
    }

    public void clearRegisteredStats(Predicate<StatHandler> filter) {
        final Iterator<StatHandler> ite = handlers.values().iterator();
        while (ite.hasNext())
            if (filter.test(ite.next()))
                ite.remove();
    }
}
