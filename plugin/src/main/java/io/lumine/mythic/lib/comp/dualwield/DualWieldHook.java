package io.lumine.mythic.lib.comp.dualwield;

import com.ranull.dualwield.event.OffHandAttackEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.MeleeAttackMetadata;
import io.lumine.mythic.lib.manager.DamageManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

public class DualWieldHook implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void a(OffHandAttackEvent event) {
        if (!(event.getDamager() instanceof LivingEntity))
            return;

        final StatProvider attacker = StatProvider.get((LivingEntity) event.getDamager(), EquipmentSlot.OFF_HAND, true);
        final AttackMetadata attackMeta = new MeleeAttackMetadata(new DamageMetadata(event.getDamage(), MythicLib.plugin.getDamage().getVanillaDamageTypes(event, EquipmentSlot.OFF_HAND)), (LivingEntity) event.getEntity(), attacker);
        MythicLib.plugin.getDamage().markAsMetadata(attackMeta);
    }
}
