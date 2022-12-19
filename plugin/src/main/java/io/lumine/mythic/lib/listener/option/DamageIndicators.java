package io.lumine.mythic.lib.listener.option;


import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.event.IndicatorDisplayEvent;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamagePacket;
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

import java.util.*;

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

        // Calculate holograms, take into account DAMAGE MODIFIERS (bug fix)
        final List<String> holos = new ArrayList<>();
        final Map<IndicatorType, Double> mappedDamage = mapDamage(event.getDamage());
        @Deprecated final double modifierDue = (event.toBukkit().getFinalDamage() - event.toBukkit().getDamage()) / mappedDamage.size();
        mappedDamage.forEach((type, val) -> holos.add(type.computeFormat(val + modifierDue)));

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

    private Map<IndicatorType, Double> mapDamage(DamageMetadata damageMetadata) {
        final Map<IndicatorType, Double> mapped = new HashMap<>();

        for (DamagePacket packet : damageMetadata.getPackets()) {
            final IndicatorType type = new IndicatorType(damageMetadata, packet);
            mapped.put(type, mapped.getOrDefault(type, 0d) + packet.getFinalValue());
        }

        return mapped;
    }

    private class IndicatorType {

        /**
         * If it's not skill damage, then it's either PHYSICAL or
         * UNARMED or no primary damage type at all. It makes sense
         * to group these three categories
         */
        final boolean skill, weapon;

        final @Nullable Element element;

        /**
         * Rule for being a critical strike: it's either a crit for
         * the primary damage type, or the element.
         */
        final boolean crit;

        IndicatorType(DamageMetadata damageMetadata, DamagePacket packet) {
            skill = packet.hasType(DamageType.SKILL);
            weapon = !skill && (packet.hasType(DamageType.WEAPON) || packet.hasType(DamageType.UNARMED));
            element = packet.getElement();
            crit = (skill ? damageMetadata.isSkillCriticalStrike() : damageMetadata.isWeaponCriticalStrike()) || (element != null && damageMetadata.isElementalCriticalStrike(element));
        }

        private String computeIcon() {
            final StringBuilder build = new StringBuilder();

            // Append damage type
            if (skill)
                build.append(crit ? skillIconCrit : skillIcon);
            else if (weapon)
                build.append(crit ? weaponIconCrit : weaponIcon);

            // Append element
            if (element != null)
                build.append(element.getColor() + element.getLoreIcon());

            return build.toString();
        }

        private String computeFormat(double damage) {
            @Nullable final CustomFont indicatorFont = crit && fontCrit != null ? fontCrit : font;
            @NotNull final String formattedDamage = indicatorFont == null ? formatNumber(damage) : indicatorFont.format(formatNumber(damage));

            return MythicLib.plugin.getPlaceholderParser().parse(null, getRaw()
                    .replace("{icon}", computeIcon())
                    .replace("{value}", formattedDamage));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IndicatorType that = (IndicatorType) o;
            return skill == that.skill && Objects.equals(element, that.element);
        }

        @Override
        public int hashCode() {
            return Objects.hash(skill, element);
        }
    }
}
