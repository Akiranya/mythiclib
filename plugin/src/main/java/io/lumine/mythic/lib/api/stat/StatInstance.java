package io.lumine.mythic.lib.api.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.api.ModifiedInstance;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.util.Closeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class StatInstance extends ModifiedInstance<StatModifier> {
    @NotNull
    private final StatMap map;
    @NotNull
    private final String stat;

    public StatInstance(@NotNull StatMap map, @NotNull String stat) {
        this.map = map;
        this.stat = stat;
    }

    @NotNull
    public StatMap getMap() {
        return map;
    }

    @NotNull
    public String getStat() {
        return stat;
    }

    public double getBase() {
        return MythicLib.plugin.getStats().getBaseValue(this);
    }

    @Nullable
    public StatHandler findHandler() {
        return MythicLib.plugin.getStats().getStatHandler(stat);
    }

    /**
     * @return The final stat value taking into account the default stat value
     *         as well as the stat modifiers. The relative stat modifiers are
     *         applied afterwards, onto the sum of the base value + flat
     *         modifiers.
     */
    public double getTotal() {
        return getFilteredTotal(EquipmentSlot.MAIN_HAND::isCompatible, mod -> mod);
    }

    /**
     * @param filter Filters stat modifications taken into account for the calculation
     * @return The final stat value taking into account the default stat value
     *         as well as the stat modifiers. The relative stat modifiers are
     *         applied afterwards, onto the sum of the base value + flat
     *         modifiers.
     */
    public double getFilteredTotal(Predicate<StatModifier> filter) {
        return getFilteredTotal(filter, mod -> mod);
    }

    /**
     * @param modification A modification to any stat modifier before taking it into
     *                     account in stat calculation. This can be used for instance to
     *                     reduce debuffs, by checking if a stat modifier has a negative
     *                     value and returning a modifier with a reduced absolute value
     * @return The final stat value taking into account the default stat value
     *         as well as the stat modifiers. The relative stat modifiers are
     *         applied afterwards, onto the sum of the base value + flat
     *         modifiers.
     */
    public double getTotal(Function<StatModifier, StatModifier> modification) {
        return getFilteredTotal(EquipmentSlot.MAIN_HAND::isCompatible, modification);
    }

    /**
     * @param filter       Filters stat modifications taken into account for the calculation
     * @param modification A modification to any stat modifier before taking it into
     *                     account in stat calculation. This can be used for instance to
     *                     reduce debuffs, by checking if a stat modifier has a negative
     *                     value and returning a modifier with a reduced absolute value
     * @return The final stat value taking into account the default stat value
     *         as well as the stat modifiers. The relative stat modifiers are
     *         applied afterwards, onto the sum of the base value + flat
     *         modifiers.
     */
    public double getFilteredTotal(Predicate<StatModifier> filter, Function<StatModifier, StatModifier> modification) {
        final @NotNull StatHandler handler = findHandler();
        final double base = handler == null ? 0 : handler.getBaseValue(this);
        final double total = getFilteredTotal(base, filter, modification);
        return handler == null ? total : handler.clampValue(total);
    }

    /**
     * @param key The string key of the external modifier source or plugin
     * @return Attribute with the given key, or <code>null</code> if not found
     */
    @Nullable
    public StatModifier getModifier(String key) {
        return modifiers.get(key);
    }

    /**
     * Registers a stat modifier and run the required player stat updates
     *
     * @param modifier The stat modifier being registered
     */
    @Override
    public void addModifier(StatModifier modifier) {
        final ModifierPacket packet = new ModifierPacket(this);
        packet.addModifier(modifier);
        packet.runUpdate();
    }

    /**
     * Iterates through registered stat modifiers and unregisters them if a
     * certain condition based on their string key is met
     *
     * @param condition Condition on the modifier key, if it should be
     *                  unregistered or not
     */
    @Override
    public void removeIf(Predicate<String> condition) {
        final ModifierPacket packet = new ModifierPacket(this);
        packet.removeIf(condition);
        packet.runUpdate();
    }

    /**
     * Removes a stat modifier with a specific key
     *
     * @param key The string key of the external stat modifier source or plugin
     */
    @Override
    public void remove(String key) {
        final ModifierPacket packet = new ModifierPacket(this);
        packet.remove(key);
        packet.runUpdate();
    }

    public ModifierPacket newPacket() {
        return new ModifierPacket(this);
    }

    /**
     * Allows to first add as many modifiers as needed and only THEN update the
     * stat instance to avoid sending too many udpates at one time which can
     * be performance heavy for attribute based stats.
     * <p>
     * Since MythicLib 1.3 the use of a modifier packet is mandatory to add,
     * filter or remove modifiers from the modifier map.
     *
     * @author indyuce
     */
    public class ModifierPacket {
        private final StatInstance instance;

        /**
         * Set to true if some update is required. This is a small
         * performance improvement as it reduces useless stat updates.
         */
        private boolean updateRequired;

        public ModifierPacket(StatInstance instance) {
            this.instance = instance;
        }

        /**
         * Registers a stat modifier and run the required player stat updates.
         * If a modifier with the same key already exists, it is then unregistered
         * and closed if required.
         *
         * @param modifier The stat modifier being registered
         */
        public void addModifier(StatModifier modifier) {
            final StatModifier current = modifiers.put(modifier.getKey(), modifier);
            if (current != null && current instanceof Closeable) ((Closeable) current).close();
            updateRequired = true;
        }

        /**
         * Removes a stat modifier with a specific key
         *
         * @param key The string key of the external stat modifier source or plugin
         */
        public void remove(String key) {

            // Find and remove current value
            final StatModifier mod = modifiers.remove(key);
            if (mod == null) return;

            /*
             * Closing modifier is really important with temporary stats because
             * otherwise the runnable will try to remove the key from the map even
             * though the attribute was cancelled before hand
             */
            if (mod instanceof Closeable) ((Closeable) mod).close();

            updateRequired = true;
        }

        /**
         * Iterates through registered stat modifiers and unregisters them
         * if a certain condition based on their string key is met
         *
         * @param condition Condition on the modifier key, if it should be unregistered or not
         */
        public void removeIf(Predicate<String> condition) {
            for (Iterator<Map.Entry<String, StatModifier>> iterator = modifiers.entrySet().iterator(); iterator.hasNext(); ) {
                final Map.Entry<String, StatModifier> entry = iterator.next();
                if (condition.test(entry.getKey())) {

                    final StatModifier modifier = entry.getValue();
                    if (modifier instanceof Closeable) ((Closeable) modifier).close();

                    iterator.remove();
                    updateRequired = true;
                }
            }
        }

        /**
         * Only runs an update if absolutely necessary
         */
        public void runUpdate() {
            final StatHandler handler;
            if (updateRequired && (handler = findHandler()) != null) handler.runUpdate(instance);
        }
    }
}

