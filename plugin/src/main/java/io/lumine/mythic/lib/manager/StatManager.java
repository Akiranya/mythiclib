package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.SharedStat;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.handler.AttributeStatHandler;
import io.lumine.mythic.lib.api.stat.handler.MovementSpeedStatHandler;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import io.lumine.mythic.lib.util.ConfigFile;
import org.apache.commons.lang.Validate;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

public class StatManager {
    private final Map<String, StatHandler> handlers = new HashMap<>();

    private ConfigurationSection statsConfig;

    public void initialize(boolean clearBefore) {
        if (clearBefore) handlers.clear();
        else UtilityMethods.loadDefaultFile("", "stats.yml");

        statsConfig = new ConfigFile("stats").getConfig();

        // Default stat handlers
        try {
            handlers.put(SharedStat.ARMOR, new AttributeStatHandler(statsConfig, Attribute.GENERIC_ARMOR, SharedStat.ARMOR));
            handlers.put(SharedStat.ARMOR_TOUGHNESS, new AttributeStatHandler(statsConfig, Attribute.GENERIC_ARMOR_TOUGHNESS, SharedStat.ARMOR_TOUGHNESS));
            handlers.put(SharedStat.ATTACK_DAMAGE, new AttributeStatHandler(statsConfig, Attribute.GENERIC_ATTACK_DAMAGE, SharedStat.ATTACK_DAMAGE, true));
            handlers.put(SharedStat.ATTACK_SPEED, new AttributeStatHandler(statsConfig, Attribute.GENERIC_ATTACK_SPEED, SharedStat.ATTACK_SPEED, true));
            handlers.put(SharedStat.KNOCKBACK_RESISTANCE, new AttributeStatHandler(statsConfig, Attribute.GENERIC_KNOCKBACK_RESISTANCE, SharedStat.KNOCKBACK_RESISTANCE));
            handlers.put(SharedStat.MAX_HEALTH, new AttributeStatHandler(statsConfig, Attribute.GENERIC_MAX_HEALTH, SharedStat.MAX_HEALTH));
            handlers.put(SharedStat.MOVEMENT_SPEED, new MovementSpeedStatHandler(statsConfig, "MOVEMENT_SPEED", true));
            handlers.put(SharedStat.SPEED_MALUS_REDUCTION, new MovementSpeedStatHandler(statsConfig, "SPEED_MALUS_REDUCTION", false));
        } catch (Exception exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load default stat handlers: " + exception.getMessage());
            exception.printStackTrace();
        }

        // Load stat handlers
        for (String key : collectKeys())
            try {
                final String stat = UtilityMethods.enumName(key);
                if (handlers.containsKey(stat)) continue;
                handlers.put(stat, new StatHandler(statsConfig, stat));
            } catch (RuntimeException exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load stat handler '" + key + "': " + exception.getMessage());
            }
    }

    private Set<String> collectKeys() {
        final Set<String> keys = new HashSet<>();
        for (String key : statsConfig.getKeys(false))
            keys.addAll(statsConfig.getConfigurationSection(key).getKeys(false));
        return keys;
    }

    @NotNull
    public static String format(String stat, MMOPlayerData player) {
        final @Nullable StatHandler handler = MythicLib.plugin.getStats().handlers.get(stat);
        final double value = handler == null ? player.getStatMap().getStat(stat) : handler.getTotalValue(player.getStatMap().getInstance(stat));
        return (handler == null ? MythicLib.plugin.getMMOConfig().decimal : handler.getDecimalFormat()).format(value);
    }

    @NotNull
    public static String format(String stat, double value) {
        final @Nullable StatHandler handler = MythicLib.plugin.getStats().handlers.get(stat);
        return (handler == null ? MythicLib.plugin.getMMOConfig().decimal : handler.getDecimalFormat()).format(value);
    }

    @Deprecated
    public void runUpdate(StatMap map, String stat) {
        runUpdate(map.getInstance(stat));
    }

    /**
     * Some stats like movement speed, attack damage... are based on vanilla
     * player attributes. Every time a stat modifier is added to a StatInstance
     * in MythicLib, MythicLib needs to perform a further attribute modifier update.
     * This method runs all the updates for the vanilla-attribute-based MythicLib
     * stats.
     *
     * @param map The StatMap of the player who needs update
     */
    public void runUpdates(@NotNull StatMap map) {
        handlers.values().forEach(handler -> handler.runUpdate(map.getInstance(handler.getStat())));
    }

    /**
     * Runs a specific stat update for a specific stat instance
     *
     * @param instance Stat instance that needs updating
     */
    public void runUpdate(@NotNull StatInstance instance) {
        StatHandler handler = handlers.get(instance.getStat());
        if (handler != null) handler.runUpdate(instance);
    }

    @Deprecated
    public double getBaseValue(String stat, StatMap map) {
        final @Nullable StatHandler handler = handlers.get(stat);
        return handler == null ? 0 : handler.getBaseValue(map.getInstance(stat));
    }

    /**
     * @param instance Stat instance
     * @return Base stat value
     */
    public double getBaseValue(StatInstance instance) {
        final @Nullable StatHandler handler = handlers.get(instance.getStat());
        return handler == null ? 0 : handler.getBaseValue(instance);
    }

    @Deprecated
    public double getTotalValue(String stat, StatMap map) {
        final @Nullable StatHandler handler = handlers.get(stat);
        return handler == null ? map.getStat(stat) : handler.getTotalValue(map.getInstance(stat));
    }

    /**
     * @param instance Stat instance
     * @return Final stat value
     */
    public double getTotalValue(StatInstance instance) {
        final @Nullable StatHandler handler = handlers.get(instance.getStat());
        return handler == null ? instance.getTotal() : handler.getTotalValue(instance);
    }

    @Deprecated
    public void registerStat(String stat, StatHandler handler) {
        Validate.notNull(stat, "Stat cannot be null");
        Validate.notNull(handler, "StatHandler cannot be null");

        handlers.put(stat, handler);
    }

    /**
     * Lets the MMO- plugins knows that a specific stat needs an update
     * whenever the value of the player stat changes (due to a MythicLib
     * stat modifier being added/being removed/expiring).
     *
     * @param handler Behaviour of given stat
     */
    public void registerStat(@NotNull StatHandler handler) {
        Validate.notNull(handler, "StatHandler cannot be null");

        handlers.put(handler.getStat(), handler);
    }

    @Nullable
    public StatHandler getStatHandler(String stat) {
        return handlers.get(stat);
    }

    public boolean isRegistered(String stat) {
        return handlers.containsKey(stat);
    }

    @NotNull
    public Set<String> getRegisteredStats() {
        return handlers.keySet();
    }

    public void clearRegisteredStats(Predicate<StatHandler> filter) {
        final Iterator<StatHandler> ite = handlers.values().iterator();
        while (ite.hasNext()) if (filter.test(ite.next())) ite.remove();
    }
}
