package io.lumine.mythic.lib.listener.option;


import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.event.IndicatorDisplayEvent;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.util.CustomFont;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Since 1.3.4 damage holograms are split into multiple damage types which
 * lets the user know what type of damage their are dealing.
 * Available "damage types" (elements are not damage types) are:
 * - physical damage
 * - magic damage
 * - elemental damage
 *
 * @author jules
 */
public class DamageIndicators extends GameIndicators {
    private final String skillIcon, weaponIcon, skillIconCrit, weaponIconCrit;
    private final boolean splitHolograms;

    @Nullable
    private final CustomFont font, fontCrit;

    public DamageIndicators(ConfigurationSection config) {
        super(config);

        this.skillIcon = config.getString("icon.skill.normal");
        this.weaponIcon = config.getString("icon.weapon.normal");
        this.skillIconCrit = config.getString("icon.skill.crit");
        this.weaponIconCrit = config.getString("icon.weapon.crit");
        this.splitHolograms = config.getBoolean("split-holograms");

        // Custom fonts
        if (config.getBoolean("custom-font.enabled")) {
            font = new CustomFont(config.getConfigurationSection("custom-font.normal"));
            fontCrit = new CustomFont(config.getConfigurationSection("custom-font.crit"));
        } else {
            font = null;
            fontCrit = null;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void a(AttackEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity) || event.getEntity() instanceof ArmorStand)
            return;

        // Display no indicator around vanished player
        if (entity instanceof Player && UtilityMethods.isVanished((Player) entity))
            return;

        final @NotNull DamageMetadata damage = event.getDamage();

        // Calculate holograms
        final List<String> holos = new ArrayList<>();

        // Physical/Unarmed damage
        final double weapon = damage.getDamage(DamageType.WEAPON) + damage.getDamage(DamageType.UNARMED);
        if (weapon > 0)
            holos.add(computeFormat(damage.isWeaponCriticalStrike() ? weaponIconCrit : weaponIcon, weapon, damage.isWeaponCriticalStrike()));

        // Magic damage
        final double skill = damage.getDamage(DamageType.SKILL);
        if (skill > 0)
            holos.add(computeFormat(damage.isSkillCriticalStrike() ? skillIconCrit : skillIcon, skill, damage.isSkillCriticalStrike()));

        // Elemental damage
        final Map<Element, Double> elementalDamage = damage.mapElementalDamage();
        elementalDamage.forEach((el, dmg) -> holos.add(computeFormat(el.getColor() + el.getLoreIcon(), dmg, damage.isElementalCriticalStrike(el))));

        // Display multiple indicators
        if (splitHolograms)
            for (String holo : holos)
                displayIndicator(entity, holo, getDirection(event.toBukkit()), IndicatorDisplayEvent.IndicatorType.DAMAGE);

            // Only display one indicator
        else {
            String joined = String.join(" ", holos);
            displayIndicator(entity, joined, getDirection(event.toBukkit()), IndicatorDisplayEvent.IndicatorType.DAMAGE);
        }
    }

    private String computeFormat(String icon, double damage, boolean crit) {
        @Nullable final CustomFont font = crit && fontCrit != null ? fontCrit : this.font;
        final String formattedDamage = font == null ? formatNumber(damage) : font.format(formatNumber(damage));

        return MythicLib.plugin.getPlaceholderParser().parse(null, getRaw()
                .replace("{icon}", icon)
                .replace("{value}", formattedDamage));
    }

    /**
     * If MythicLib can find a damager, display the hologram
     * in a cone which direction is the damager-target line.
     *
     * @param event Damage event
     * @return Direction of the hologram
     */
    private Vector getDirection(EntityDamageEvent event) {

        if (event instanceof EntityDamageByEntityEvent) {
            Vector dir = event.getEntity().getLocation().toVector().subtract(((EntityDamageByEntityEvent) event).getDamager().getLocation().toVector()).setY(0);
            if (dir.lengthSquared() > 0) {

                // Calculate angle of attack
                double a = Math.atan2(dir.getZ(), dir.getX());

                // Random angle offset
                a += Math.PI / 2 * (random.nextDouble() - .5);

                return new Vector(Math.cos(a), 0, Math.sin(a));
            }
        }

        double a = random.nextDouble() * Math.PI * 2;
        return new Vector(Math.cos(a), 0, Math.sin(a));
    }
}
