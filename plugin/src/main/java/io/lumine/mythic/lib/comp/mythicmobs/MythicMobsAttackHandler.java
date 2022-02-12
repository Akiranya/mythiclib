package io.lumine.mythic.lib.comp.mythicmobs;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackHandler;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.xikage.mythicmobs.adapters.AbstractPlayer;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * This class should never be used under good circumstances. This class
 * only helps MythicLib take into account damage dealt using the default
 * MythicMobs <code>damage</code> mechanic.
 * <p>
 * This mechanic does NOT take into account the player stat snapshot as
 * stats are cached when checking for the player attack. It's fine most of
 * the time though.
 * <p>
 * This mechanic does NOT take into account damage types either. This messes
 * with on-hit effects like elemental damage, critical strikes, other stats too.
 */
public class MythicMobsAttackHandler implements AttackHandler {

    @Override
    @Nullable
    public AttackMetadata getAttack(EntityDamageEvent event) {
        Optional<Object> opt = BukkitAdapter.adapt(event.getEntity()).getMetadata("skill-damage");
        if (!opt.isPresent())
            return null;

        io.lumine.xikage.mythicmobs.skills.damage.DamageMetadata metadata = (io.lumine.xikage.mythicmobs.skills.damage.DamageMetadata) opt.get();
        if (!(metadata.getDamager().getEntity() instanceof AbstractPlayer))
            return null;

        DamageMetadata result = new DamageMetadata(metadata.getAmount(), DamageType.MAGIC, DamageType.SKILL);
        return new AttackMetadata(result, MMOPlayerData.get(metadata.getDamager().getEntity().getUniqueId()).getStatMap().cache(EquipmentSlot.MAIN_HAND));
    }
}
