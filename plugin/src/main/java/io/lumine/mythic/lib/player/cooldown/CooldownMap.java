package io.lumine.mythic.lib.player.cooldown;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CooldownMap {
    private final Map<String, CooldownInfo> map = new HashMap<>();

    /**
     * Sets current cooldown to the maximum value
     * of the current and input cooldown values.
     *
     * @param obj      The skill or action
     * @param cooldown Initial skill or action cooldown
     * @return The newly registered cooldown info
     */
    public CooldownInfo applyCooldown(CooldownObject obj, double cooldown) {
        return applyCooldown(obj.getCooldownPath(), cooldown);
    }

    /**
     * Sets current cooldown to the maximum value o
     * of the current and input cooldown values.
     *
     * @param path     The skill or action path, must be completely unique
     * @param cooldown Initial skill or action cooldown
     * @return The newly registered cooldown info
     */
    public CooldownInfo applyCooldown(String path, double cooldown) {
        @Nullable CooldownInfo current = map.get(path);
        if (current != null && current.getRemaining() >= cooldown * 1000)
            return current;

        current = new CooldownInfo(cooldown);
        map.put(path, current);
        return current;
    }

    /**
     * @return Finds the cooldown info for a specific action or skill
     */
    @Nullable
    public CooldownInfo getInfo(CooldownObject obj) {
        return getInfo(obj.getCooldownPath());
    }

    /**
     * @return Finds the cooldown info for a specific action or skill
     */
    @Nullable
    public CooldownInfo getInfo(String path) {
        return map.get(path);
    }

    /**
     * @param obj The skill or action
     * @return Retrieves the remaining cooldown in seconds
     */
    public double getCooldown(CooldownObject obj) {
        return getCooldown(obj.getCooldownPath());
    }

    /**
     * @param path The skill or action path, must be completely unique
     * @return Retrieves the remaining cooldown in seconds
     */
    public double getCooldown(String path) {
        final @Nullable CooldownInfo info = map.get(path);
        return info == null ? 0 : (double) info.getRemaining() / 1000;
    }

    /**
     * @param obj The skill or action
     * @return If the mechanic can be used by the player
     */
    public boolean isOnCooldown(CooldownObject obj) {
        return isOnCooldown(obj.getCooldownPath());
    }

    /**
     * @param path The skill or action path, must be completely unique
     * @return If the mechanic can be used by the player
     */
    public boolean isOnCooldown(String path) {
        final @Nullable CooldownInfo found = map.get(path);
        return found != null && !found.hasEnded();
    }
}
