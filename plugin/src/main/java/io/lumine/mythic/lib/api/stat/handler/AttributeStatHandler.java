package io.lumine.mythic.lib.api.stat.handler;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * TODO remove stat updates and create an interface between MythicLib stat modifiers and Minecraft attribute modifiers
 */
public class AttributeStatHandler extends StatHandler {
    private final Attribute attribute;
    private final String stat;
    private final boolean meleeWeaponStat;

    public AttributeStatHandler(@NotNull ConfigurationSection config, @NotNull Attribute attribute, @NotNull String stat) {
        this(config, attribute, stat, false);
    }

    /**
     * Statistics like Atk Damage, Atk Speed, Max Health...
     * which are based on vanilla player attributes.
     *
     * @param attribute       The corresponding vanilla player attribute
     * @param stat            The stat identifier
     * @param meleeWeaponStat When set to true, stat modifiers from ranged weapons won't
     *                        be taken into account. This is only the case for Attack Damage
     *                        and Attack Speed
     */
    public AttributeStatHandler(ConfigurationSection config, @NotNull Attribute attribute, @NotNull String stat, boolean meleeWeaponStat) {
        super(config, stat);

        this.attribute = attribute;
        this.stat = stat;
        this.meleeWeaponStat = meleeWeaponStat;
    }

    @Override
    public void runUpdate(@NotNull StatInstance instance) {
        final AttributeInstance attrIns = instance.getMap().getPlayerData().getPlayer().getAttribute(attribute);
        removeModifiers(attrIns);

        /*
         * The first two boolean checks make sure that ranged
         * weapons do not register their attack damage.
         *
         * The last two checks guarantee that weapons
         * held in off hand don't register any of their stats.
         */
        final double mmo = instance.getFilteredTotal(mod -> (!meleeWeaponStat || mod.getSource() != ModifierSource.RANGED_WEAPON) && (!mod.getSource().isWeapon() || mod.getSlot() != EquipmentSlot.OFF_HAND));

        /*
         * Calculate the stat base value. Since it can be changed by
         * external plugins, it's better to calculate it once and cache the result.
         */
        final double base = instance.getMap().getPlayerData().getPlayer().getAttribute(attribute).getBaseValue();

        /*
         * Only add an attribute modifier if the very final stat
         * value is different from the main one to save calculations.
         */
        if (mmo != base)
            attrIns.addModifier(new AttributeModifier("mythiclib.main", mmo - base, AttributeModifier.Operation.ADD_NUMBER));
    }

    @Override
    public double getBaseValue(@NotNull StatInstance instance) {
        return super.getBaseValue(instance) + instance.getMap().getPlayerData().getPlayer().getAttribute(attribute).getBaseValue();
    }

    @Override
    public double getTotalValue(@NotNull StatInstance instance) {
        return instance.getMap().getPlayerData().getPlayer().getAttribute(attribute).getValue();
    }
}
