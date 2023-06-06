package io.lumine.mythic.lib.data;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This class is implemented by player datas. A small boolean field
 * is absolutely necessary to keep track if player data has already
 * been loaded or not.
 * <p>
 * This class used to contain a reference to a MMOPlayerData instance.
 * Since it only needs the UUID used to save player data, the only
 * data it really needs is a profile ID.
 *
 * @author jules
 */
public abstract class SynchronizedDataHolder implements OfflineDataHolder {
    private final MMOPlayerData playerData;

    private boolean sync;

    public SynchronizedDataHolder(MMOPlayerData playerData) {
        this.playerData = playerData;
    }

    public MMOPlayerData getMMOPlayerData() {
        return playerData;
    }

    @Override
    @NotNull
    public UUID getUniqueId() {
        return playerData.getUniqueId();
    }

    @NotNull
    public UUID getProfileId() {
        return playerData.getProfileId();
    }

    @NotNull
    public Player getPlayer() {
        return playerData.getPlayer();
    }

    /**
     * @return If the player data should be saved when the player quits.
     */
    public boolean shouldBeSaved() {
        return true;
    }

    /**
     * @return If the synchronized data has already been loaded.
     */
    public boolean isSynchronized() {
        return sync;
    }

    public void markAsSynchronized() {
        Validate.isTrue(!sync, "Data holder already marked synchronized");
        sync = true;
    }
}
