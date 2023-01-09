package io.lumine.mythic.lib.comp.flags;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FlagHandler {
    private final List<FlagPlugin> flagPlugins = new ArrayList<>();

    public void registerPlugin(@NotNull FlagPlugin plugin) {
        flagPlugins.add(Objects.requireNonNull(plugin, "Flag plugin cannot be null"));
    }

    @Nullable
    public <T extends FlagPlugin> T getHandler(Class<T> clazz) {
        for (FlagPlugin plugin : flagPlugins)
            if (plugin.getClass().equals(clazz))
                return (T) plugin;
        return null;
    }

    /**
     * PVP is enabled by default.
     *
     * @return If PvP is toggled on at a specific location.
     */
    public boolean isPvpAllowed(Location loc) {
        for (FlagPlugin plugin : flagPlugins)
            if (!plugin.isPvpAllowed(loc))
                return false;
        return true;
    }

    /**
     * @return If some flag is toggled on for some player given their location.
     */
    public boolean isFlagAllowed(Player player, CustomFlag flag) {
        for (FlagPlugin plugin : flagPlugins)
            if (plugin.isFlagAllowed(player, flag) != flag.getDefault())
                return !flag.getDefault();
        return flag.getDefault();
    }

    /**
     * @return If some flag is toggled on at a specific location.
     */
    public boolean isFlagAllowed(Location loc, CustomFlag flag) {
        for (FlagPlugin plugin : flagPlugins)
            if (plugin.isFlagAllowed(loc, flag) != flag.getDefault())
                return !flag.getDefault();
        return flag.getDefault();
    }
}
