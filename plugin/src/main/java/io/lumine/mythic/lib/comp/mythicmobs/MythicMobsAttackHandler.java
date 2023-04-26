package io.lumine.mythic.lib.comp.mythicmobs;

import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.AttackHandler;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.element.Element;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * This class is the interface between MythicMobs -> MMO damage systems.
 * <p>
 * The user is strongly advised to use the mmodamage mechanic instead:
 * - No player stat caching (fine most of the time)
 * - Bare bones damage type/element support
 *
 * @author jules
 */
public class MythicMobsAttackHandler implements AttackHandler {

    @Override
    @Nullable
    public AttackMetadata getAttack(EntityDamageEvent event) {
        final Optional<Object> opt = BukkitAdapter.adapt(event.getEntity()).getMetadata("skill-damage");
        if (!opt.isPresent())
            return null;

        // Retrieve damage info
        final io.lumine.mythic.api.skills.damage.DamageMetadata mythic = (io.lumine.mythic.api.skills.damage.DamageMetadata) opt.get();

        // Find damager
        final Entity damagerBukkit = mythic.getDamager().getEntity().getBukkitEntity();
        final @Nullable StatProvider damager = damagerBukkit instanceof LivingEntity ? StatProvider.get((LivingEntity) damagerBukkit, EquipmentSlot.MAIN_HAND, true) : null;

        // Find damage metadata and apply element if found
        final DamageMetadata damageMeta = new DamageMetadata(mythic.getAmount(), MythicLib.plugin.getDamage().getVanillaDamageTypes(mythic.getDamageCause()));
        if (mythic.getElement() != null)
            try {
                final @Nullable Element element = MythicLib.plugin.getElements().get(UtilityMethods.enumName(mythic.getElement()));
                Validate.notNull(element);
                for (DamagePacket packet : damageMeta.getPackets())
                    packet.setElement(element);
            } catch (Exception exception) {
                // Do not apply element
            }

        return new AttackMetadata(damageMeta, (LivingEntity) event.getEntity(), damager);
    }
}
