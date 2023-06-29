package io.lumine.mythic.lib.api.stat.handler;

import io.lumine.mythic.lib.api.stat.SharedStat;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This fixes an issue where, when the player's max health decreases
 * due to any stat update, the player's health remains stuck to a value
 * that is strictly greater than the player's current max health.
 * <p>
 * The health would only update when the player takes damage to go down
 * back to a legal value.
 * <p>
 * This class makes sure to set the player's health back to a legal value
 * whenever the player's max health is updated.
 *
 * @author indyuce
 */
public class MaxHealthStatHandler extends AttributeStatHandler {
    public MaxHealthStatHandler(@NotNull ConfigurationSection config) {
        super(config, Attribute.GENERIC_MAX_HEALTH, SharedStat.MAX_HEALTH, false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void runUpdate(@NotNull StatInstance instance) {

        // Do everything like normal
        super.runUpdate(instance);

        // Fix player health
        final Player player = instance.getMap().getPlayerData().getPlayer();
        final double max = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        final double fixed = Math.max(0, Math.min(max, player.getHealth()));
        player.setHealth(fixed);
    }
}
