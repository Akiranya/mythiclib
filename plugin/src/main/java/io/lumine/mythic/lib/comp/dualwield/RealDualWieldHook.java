package io.lumine.mythic.lib.comp.dualwield;

import com.evill4mer.RealDualWield.Api.PlayerDamageEntityWithOffhandEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.MeleeAttackMetadata;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class RealDualWieldHook implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void a(PlayerDamageEntityWithOffhandEvent event) {
        final StatProvider attacker = MMOPlayerData.get(event.getPlayer()).getStatMap().cache(EquipmentSlot.OFF_HAND);
        final AttackMetadata attackMeta = new MeleeAttackMetadata(new DamageMetadata(event.getDamage(), MythicLib.plugin.getDamage().getVanillaDamageTypes(event.getPlayer(), EntityDamageEvent.DamageCause.ENTITY_ATTACK, EquipmentSlot.OFF_HAND)), (LivingEntity) event.getEntity(), attacker);
        MythicLib.plugin.getDamage().markAsMetadata(attackMeta);
    }
}
