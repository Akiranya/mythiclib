package io.lumine.mythic.lib.player.modifier;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.Closeable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public abstract class ModifierMap<T extends PlayerModifier> {
    private final MMOPlayerData playerData;
    private final Map<UUID, T> modifiers = new HashMap<>();

    public ModifierMap(MMOPlayerData playerData) {
        this.playerData = playerData;
    }

    @NotNull
    public MMOPlayerData getPlayerData() {
        return playerData;
    }

    @NotNull
    public Collection<T> getModifiers() {
        return modifiers.values();
    }

    @NotNull
    public Iterable<T> isolateModifiers(EquipmentSlot hand) {
        final ArrayList<T> isolated = new ArrayList<>();

        for (T modifier : getModifiers())
            if (hand.isCompatible(modifier))
                isolated.add(modifier);

        return isolated;
    }

    @Nullable
    public T addModifier(T modifier) {
        return modifiers.put(modifier.getUniqueId(), modifier);
    }

    @Nullable
    public T removeModifier(UUID uuid) {
        final @Nullable T removed = modifiers.remove(uuid);
        if (removed != null && removed instanceof Closeable)
            ((Closeable) removed).close();
        return removed;
    }

    public void removeModifiers(String key) {
        final Iterator<T> iterator = modifiers.values().iterator();
        T skill;
        while (iterator.hasNext()) {
            skill = iterator.next();
            if (skill.getKey().equals(key)) {
                iterator.remove();
                if (skill instanceof Closeable)
                    ((Closeable) skill).close();
            }
        }
    }
}
