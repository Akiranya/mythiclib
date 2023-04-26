package io.lumine.mythic.lib.player.modifier;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;

import java.util.Objects;
import java.util.UUID;

/**
 * Player modifiers were defined in 1.2 as a generalization of
 * stat modifiers. These can be any property that may be
 * temporarily assigned to a player including the following:
 * - stat modifier
 * - potion effect
 * - any triggered skill
 * - skill modifier
 *
 * @author Jules
 */
public abstract class PlayerModifier {
    private final ModifierSource source;
    private final EquipmentSlot slot;

    /**
     * Identifier given to skills to differentiate between them.
     * Every plugin like MMOItems has a key to be able to manipulate
     * the triggers that were registered on the player at any time.
     * <p>
     * Unlike the UUID, this key is NOT ALWAYS unique in the case
     * of modifier instances.
     */
    private final String key;

    private final UUID uniqueId = UUID.randomUUID();

    public PlayerModifier(String key, EquipmentSlot slot, ModifierSource source) {
        this.key = key;
        this.slot = slot;
        this.source = source;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getKey() {
        return key;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public ModifierSource getSource() {
        return source;
    }

    public abstract void register(MMOPlayerData playerData);

    public abstract void unregister(MMOPlayerData playerData);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerModifier that = (PlayerModifier) o;
        return uniqueId.equals(that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
}
