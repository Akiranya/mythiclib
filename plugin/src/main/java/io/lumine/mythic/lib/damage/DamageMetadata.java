package io.lumine.mythic.lib.damage;

import io.lumine.mythic.lib.element.Element;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Contains all the information about damage being dealt
 * during a specific attack.
 * <p>
 * TODO Merge damage types and elements??
 * TODO Turn damage types into strings for external compatibility??
 */
public class DamageMetadata implements Cloneable {
    private final List<DamagePacket> packets = new ArrayList<>();

    @Deprecated
    private boolean weaponCrit, skillCrit;

    @Deprecated
    private final Set<Element> elementalCrit = new HashSet<>();

    /**
     * Used to register an attack with NO initial packet!
     */
    public DamageMetadata() {
    }

    /**
     * Used to register a attack.
     *
     * @param damage The attack damage
     * @param types  The attack damage types
     */
    public DamageMetadata(double damage, DamageType... types) {
        add(damage, types);
    }

    /**
     * Used to register a attack.
     *
     * @param damage  The attack damage
     * @param element If this is an elemental attack
     * @param types   The attack damage types
     */
    public DamageMetadata(double damage, @NotNull Element element, DamageType... types) {
        add(damage, element, types);
    }

    public boolean isWeaponCriticalStrike() {
        return weaponCrit;
    }

    public void registerWeaponCriticalStrike() {
        this.weaponCrit = true;
    }

    public boolean isSkillCriticalStrike() {
        return skillCrit;
    }

    public void registerSkillCriticalStrike() {
        this.skillCrit = true;
    }

    public boolean isElementalCriticalStrike(Element el) {
        return elementalCrit.contains(el);
    }

    public void registerElementalCriticalStrike(Element el) {
        elementalCrit.add(el);
    }

    /**
     * You cannot deal less than 0.01 damage. This is an arbitrary
     * positive constant, as MythicLib and other plugins consider
     * 0-damage events to be fake damage events used to check for
     * the PvP/PvE flag.
     */
    public static final double MINIMAL_DAMAGE = .01;

    public double getDamage() {
        double d = 0;

        for (DamagePacket packet : packets)
            d += packet.getFinalValue();

        return Math.max(MINIMAL_DAMAGE, d);
    }

    /**
     * @param element If null, non-elemental damage will be returned.
     */
    public double getDamage(@Nullable Element element) {
        double d = 0;

        for (DamagePacket packet : packets)
            if (Objects.equals(packet.getElement(), element)) d += packet.getFinalValue();

        return d;
    }

    public double getDamage(DamageType type) {
        double d = 0;

        for (DamagePacket packet : packets)
            if (packet.hasType(type)) d += packet.getFinalValue();

        return d;
    }

    @NotNull
    public Map<Element, Double> mapElementalDamage() {
        final Map<Element, Double> mapped = new HashMap<>();

        for (DamagePacket packet : packets)
            if (packet.getElement() != null)
                mapped.put(packet.getElement(), mapped.getOrDefault(packet.getElement(), 0d) + packet.getFinalValue());

        return mapped;
    }

    @NotNull
    public List<DamagePacket> getPackets() {
        return packets;
    }

    /**
     * @return Set containing all the damage types found
     *         in all the different damage packets.
     */
    @NotNull
    public Set<DamageType> collectTypes() {
        final Set<DamageType> collected = new HashSet<>();

        for (DamagePacket packet : packets)
            for (DamageType type : packet.getTypes())
                collected.add(type);

        return collected;
    }

    /**
     * @return Set containing all the elements found
     *         in all the different damage packets.
     */
    @NotNull
    public Set<Element> collectElements() {
        final Set<Element> collected = new HashSet<>();

        for (DamagePacket packet : packets)
            if (packet.getElement() != null) collected.add(packet.getElement());

        return collected;
    }

    /**
     * @return Iterates through all registered damage packets and
     *         see if any has this damage type.
     */
    public boolean hasType(DamageType type) {
        for (DamagePacket packet : packets)
            if (packet.hasType(type)) return true;

        return false;
    }

    /**
     * @param element If null, will return true if it has non-elemental damage.
     * @return Iterates through all registered damage packets and
     *         see if any has this element.
     */
    public boolean hasElement(@Nullable Element element) {
        for (DamagePacket packet : packets)
            if (Objects.equals(packet.getElement(), element)) return true;

        return false;
    }

    /**
     * Registers a new damage packet.
     *
     * @param value Damage dealt by another source, this could be an on-hit
     *              skill increasing the damage of the current attack.
     * @param types The damage types of the packet being registered
     * @return The same modified damage metadata
     */
    @NotNull
    public DamageMetadata add(double value, @NotNull DamageType... types) {
        packets.add(new DamagePacket(value, types));
        return this;
    }

    /**
     * Registers a new elemental damage packet.
     *
     * @param value   Damage dealt by another source, this could be an on-hit
     *                skill increasing the damage of the current attack.
     * @param element The element
     * @param types   The damage types of the packet being registered
     * @return The same modified damage metadata
     */
    @NotNull
    public DamageMetadata add(double value, @Nullable Element element, @NotNull DamageType... types) {
        packets.add(new DamagePacket(value, element, types));
        return this;
    }

    /**
     * Register a multiplicative damage modifier in all damage packets.
     * <p>
     * This is used for critical strikes which modifier should
     * NOT stack up with damage boosting statistics.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     * @return The same damage metadata
     */
    @NotNull
    public DamageMetadata multiplicativeModifier(double coefficient) {
        for (DamagePacket packet : packets)
            packet.multiplicativeModifier(coefficient);
        return this;
    }

    /**
     * Registers a multiplicative damage modifier
     * which applies to any damage packet
     *
     * @param multiplier From 0 to infinity, 1 increases damage by 100%.
     *                   This can be negative as well
     * @return The same damage metadata
     */
    @NotNull
    public DamageMetadata additiveModifier(double multiplier) {
        for (DamagePacket packet : packets)
            packet.additiveModifier(multiplier);
        return this;
    }

    /**
     * Register a multiplicative damage modifier for a specific damage type.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     * @param damageType  Specific damage type
     * @return The same damage metadata
     */
    @NotNull
    public DamageMetadata multiplicativeModifier(double coefficient, @NotNull DamageType damageType) {
        for (DamagePacket packet : packets)
            if (packet.hasType(damageType)) packet.multiplicativeModifier(coefficient);
        return this;
    }

    /**
     * Register a multiplicative damage modifier for a specific element.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     * @param element     If null, non-elemental damage will be considered
     * @return The same damage metadata
     */
    @NotNull
    public DamageMetadata multiplicativeModifier(double coefficient, @Nullable Element element) {
        for (DamagePacket packet : packets)
            if (Objects.equals(packet.getElement(), element)) packet.multiplicativeModifier(coefficient);
        return this;
    }

    /**
     * Registers a multiplicative damage modifier which only
     * applies to a specific damage type
     *
     * @param multiplier From 0 to infinity, 1 increases damage by 100%.
     *                   This can be negative as well
     * @param damageType Specific damage type
     * @return The same damage metadata
     */
    @NotNull
    public DamageMetadata additiveModifier(double multiplier, @NotNull DamageType damageType) {
        for (DamagePacket packet : packets)
            if (packet.hasType(damageType)) packet.additiveModifier(multiplier);
        return this;
    }

    /**
     * Register an additive damage modifier for a specific element.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     * @param element     If null, non-elemental damage will be considered
     * @return The same damage metadata
     */
    @NotNull
    public DamageMetadata additiveModifier(double coefficient, @NotNull Element element) {
        for (DamagePacket packet : packets)
            if (Objects.equals(packet.getElement(), element)) packet.additiveModifier(coefficient);
        return this;
    }

    @Override
    public DamageMetadata clone() {
        DamageMetadata clone = new DamageMetadata();

        for (DamagePacket packet : packets)
            clone.packets.add(packet.clone());

        return clone;
    }

    @Override
    public String toString() {

        StringBuilder damageTypes = new StringBuilder("\u00a73Damage Meta{");

        boolean packetAppended = false;
        for (DamagePacket packet : packets) {
            if (packetAppended) {
                damageTypes.append("\u00a73;");
            }
            packetAppended = true;

            // Damage
            damageTypes.append(packet);
        }

        // Yeah
        return damageTypes.append("\u00a73}").toString();
    }
}
