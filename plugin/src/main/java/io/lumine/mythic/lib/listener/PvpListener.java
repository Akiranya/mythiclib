package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import javax.annotation.Nullable;

public class PvpListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void a(EntityDamageByEntityEvent event) {
        if (!UtilityMethods.isRealPlayer(event.getEntity()))
            return;

        final @Nullable Player source = UtilityMethods.getPlayerDamager(event);
        if (source == null)
            return;

        // Apply PvpInteractionRules
        final Player target = (Player) event.getEntity();
        if (!MythicLib.plugin.getEntities().checkPvpInteractionRules(source, target, InteractionType.OFFENSE_ACTION, true))
            event.setCancelled(true);
    }
}
