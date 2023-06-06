package io.lumine.mythic.lib.comp.profile;

import fr.phoenixdevt.profile.event.ProfileSelectEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listener needed for MythicLib to function and
 * hook onto plugins under the Profile API.
 *
 * @author Jules
 */
public class ProfilePluginListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProfileChooseSkillTrigger(ProfileSelectEvent event) {
        MMOPlayerData.get(event.getPlayer().getUniqueId()).triggerSkills(TriggerType.LOGIN, null);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void updateProfileId(ProfileSelectEvent event) {
        MMOPlayerData.get(event.getPlayerData().getUniqueId()).setProfileId(event.getProfile().getUniqueId());
    }
}
