package io.lumine.mythic.lib.player;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A class created a specific point in time. It contains information
 * about a player like its cached player stats and the hand used
 * to perform the action, called "action hand".
 *
 * @author jules
 */
public class PlayerMetadata implements StatProvider {
    private final Player player;
    private final MMOPlayerData playerData;
    private final Map<String, Double> playerStats;
    private final EquipmentSlot actionHand;

    public PlayerMetadata(PlayerMetadata parent) {
        Validate.notNull(parent, "Parent cannot be null");

        this.player = parent.player;
        this.playerData = parent.playerData;
        this.playerStats = parent.playerStats;
        this.actionHand = parent.actionHand;
    }

    public PlayerMetadata(StatMap statMap, @NotNull EquipmentSlot actionHand) {
        this.player = statMap.getPlayerData().getPlayer();
        this.playerData = statMap.getPlayerData();
        this.playerStats = new HashMap<>();
        this.actionHand = actionHand;

        Validate.isTrue(Objects.requireNonNull(actionHand).isHand(), "Equipment slot must be a hand");

        // Isolate stat modifiers
        for (StatInstance ins : statMap.getInstances())
            this.playerStats.put(ins.getStat(), ins.getFilteredTotal(actionHand::isCompatible));
    }

    /**
     * @return The cached Player instance. Player instances are cached so
     *         that even if the player logs out, the ability can still be
     *         cast without additional errors
     */
    public Player getPlayer() {
        return player;
    }

    public MMOPlayerData getData() {
        return playerData;
    }

    public EquipmentSlot getActionHand() {
        return actionHand;
    }

    /**
     * @param stat The string key of the stat
     * @return The cached stat value, or the vanilla
     */
    @Override
    public double getStat(String stat) {
        return playerStats.getOrDefault(stat, playerData.getStatMap().getInstance(stat).getBase());
    }

    /**
     * Edits the current cached stat value
     *
     * @param stat  The string key of the stat
     * @param value The value you want to cache
     */
    public void setStat(String stat, double value) {
        playerStats.put(stat, value);
    }

    /**
     * Utility method that makes a player deal damage to a specific
     * entity. This creates the attackMetadata based on the data
     * stored by the CasterMetadata, and calls it using MythicLib
     * damage manager
     *
     * @param target Target entity
     * @param damage Damage dealt
     * @param types  Type of target
     * @return The (modified) attack metadata
     */
    public AttackMetadata attack(LivingEntity target, double damage, DamageType... types) {
        return attack(target, damage, true, types);
    }

    /**
     * Utility method that makes a player deal damage to a specific
     * entity. This creates the attackMetadata based on the data
     * stored by the CasterMetadata, and calls it using MythicLib
     * damage manager
     *
     * @param target    Target entity
     * @param damage    Damage dealt
     * @param knockback Should this attack apply knockback
     * @param types     Type of target
     * @return The (modified) attack metadata
     */
    public AttackMetadata attack(LivingEntity target, double damage, boolean knockback, DamageType... types) {
        final AttackMetadata attackMeta = new AttackMetadata(new DamageMetadata(damage, types), target, this);
        MythicLib.plugin.getDamage().registerAttack(attackMeta, knockback, false);
        return attackMeta;
    }
}
