package io.lumine.mythic.lib.api.stat.api;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.modifier.Closeable;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class ModifiedInstance<T extends InstanceModifier> {
    protected final Map<String, T> modifiers = new ConcurrentHashMap<>();

    /**
     * @return The final modified value taking, into account the default value
     * as well as all of the modifiers. %-based modifiers are applied
     * afterwards, onto the sum of the base value + flat modifiers.
     */
    public double getTotal(double base) {
        return getFilteredTotal(base, EquipmentSlot.MAIN_HAND::isCompatible, mod -> mod);
    }

    /**
     * @param filter Filters modifiers taken into account for the final value computation
     * @return The final modified value taking, into account the default value
     * as well as all of the modifiers. %-based modifiers are applied
     * afterwards, onto the sum of the base value + flat modifiers.
     */
    public double getFilteredTotal(double base, Predicate<T> filter) {
        return getFilteredTotal(base, filter, mod -> mod);
    }

    /**
     * @param modification A modification to any modifier before taking it into account
     *                     in the final calculation.
     *                     This can be used for instance to reduce debuffs, by checking if
     *                     a stat modifier has a negative value and returning a modifier
     *                     with a reduced absolute value.
     * @return The final modified value taking, into account the default value
     * as well as all of the modifiers. %-based modifiers are applied
     * afterwards, onto the sum of the base value + flat modifiers.
     */
    public double getTotal(double base, Function<T, T> modification) {
        return getFilteredTotal(base, EquipmentSlot.MAIN_HAND::isCompatible, modification);
    }

    /**
     * @param filter       Filters modifiers taken into account for the final value computation
     * @param modification A modification to any modifier before taking it into account
     *                     in the final calculation.
     *                     This can be used for instance to reduce debuffs, by checking if
     *                     a stat modifier has a negative value and returning a modifier
     *                     with a reduced absolute value.
     * @return The final modified value taking, into account the default value
     * as well as all of the modifiers. %-based modifiers are applied
     * afterwards, onto the sum of the base value + flat modifiers.
     */
    public double getFilteredTotal(double d, Predicate<T> filter, Function<T, T> modification) {

        for (T mod : modifiers.values())
            if (mod.getType() == ModifierType.FLAT && filter.test(mod))
                d += modification.apply(mod).getValue();

        for (T mod : modifiers.values())
            if (mod.getType() == ModifierType.RELATIVE && filter.test(mod))
                d *= 1 + modification.apply(mod).getValue() / 100;

        return d;
    }

    /**
     * @param key The string key of the external modifier source or plugin
     * @return Attribute with the given key, or <code>null</code> if not found
     */
    @Nullable
    public T getModifier(String key) {
        return modifiers.get(key);
    }

    public void addModifier(T mod) {
        modifiers.put(mod.getKey(), mod);
    }

    /**
     * Removes the modifier associated to the given key.
     */
    public void remove(String key) {
        modifiers.remove(key);
    }

    /**
     * Iterates through registered modifiers and unregisters them if a
     * certain condition based on their string key is met.
     *
     * @param condition Condition on the modifier key
     */
    public void removeIf(Predicate<String> condition) {
        for (Iterator<Map.Entry<String, T>> iterator = modifiers.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, T> entry = iterator.next();
            if (condition.test(entry.getKey())) {

                T modifier = entry.getValue();
                if (modifier instanceof Closeable)
                    ((Closeable) modifier).close();

                iterator.remove();
            }
        }
    }

    /**
     * @return All registered modifiers
     */
    public Collection<T> getModifiers() {
        return modifiers.values();
    }

    /**
     * @return All string keys of currently registered modifiers
     */
    public Set<String> getKeys() {
        return modifiers.keySet();
    }

    /**
     * @param key The string key of the external modifier source or plugin
     * @return If a modifier is registered with this key
     */
    public boolean contains(String key) {
        return modifiers.containsKey(key);
    }
}
