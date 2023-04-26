package io.lumine.mythic.lib.comp;

import com.gmail.nossr50.events.skills.rupture.McMMOEntityDamageByRuptureEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * In recent mcMMO builds there is no longer such metadata
 * with key "mcMMO: Custom Damage" although there is an event
 * that you can listen to and use it to initialize the attack
 * metadata.
 *
 * @author Jules
 */
public class McMMOAttackHandler implements Listener {

    /**
     * Runs on priority LOW right before MythicLib's HIGHEST
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void registerAttacks(McMMOEntityDamageByRuptureEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        final StatProvider damager = MMOPlayerData.get(event.getMcMMODamager().getPlayer()).getStatMap().cache(EquipmentSlot.MAIN_HAND);
        final DamageMetadata damageMeta = new DamageMetadata(event.getDamage(), DamageType.SKILL, DamageType.PHYSICAL, DamageType.DOT);
        final AttackMetadata attackMeta = new AttackMetadata(damageMeta, (LivingEntity) event.getEntity(), damager);
        MythicLib.plugin.getDamage().markAsMetadata(attackMeta);
    }
}
