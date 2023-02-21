package io.lumine.mythic.lib.api.stat.handler;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

public class MovementSpeedStatHandler implements StatHandler {
    private final boolean moveSpeed;

    public MovementSpeedStatHandler(boolean moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void runUpdate(StatMap stats) {
        final AttributeInstance attrIns = stats.getPlayerData().getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        removeModifiers(attrIns);

        /*
         * Calculate speed malus reduction (capped at 80%)
         */
        final double coef = 1 - Math.min(.8, Math.max(0, stats.getInstance("SPEED_MALUS_REDUCTION").getTotal() / 100));

        /*
         * This guarantees that weapons held in off
         * hand don't register any of their stats.
         */
        final StatInstance statIns = stats.getInstance("MOVEMENT_SPEED");
        final double mmo = statIns.getFilteredTotal(mod -> !mod.getSource().isWeapon() || mod.getSlot() != EquipmentSlot.OFF_HAND, mod -> mod.getValue() < 0 ? mod.multiply(coef) : mod);

        /*
         * Calculate the stat base value. Since it can be changed by
         * external plugins, it's better to calculate it once and cache the result.
         */
        final double base = stats.getPlayerData().getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();

        /*
         * Only add an attribute modifier if the very final stat
         * value is different from the main one to save calculations.
         */
        if (mmo != base)
            attrIns.addModifier(new AttributeModifier("mythiclib.main", mmo - base, AttributeModifier.Operation.ADD_NUMBER));
    }

    @Override
    public double getTotalValue(StatMap map) {
        return moveSpeed ? map.getPlayerData().getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue() : map.getStat("SPEED_MALUS_REDUCTION");
    }

    @Override
    public double getBaseValue(StatMap map) {
        return moveSpeed ? map.getPlayerData().getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() : 0;
    }
}
