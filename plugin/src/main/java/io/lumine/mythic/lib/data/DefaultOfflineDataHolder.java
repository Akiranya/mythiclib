package io.lumine.mythic.lib.data;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Used when plugins don't handle offline player data.
 */
public class DefaultOfflineDataHolder implements OfflineDataHolder {
    private final UUID uuid;

    public DefaultOfflineDataHolder(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return uuid;
    }
}
