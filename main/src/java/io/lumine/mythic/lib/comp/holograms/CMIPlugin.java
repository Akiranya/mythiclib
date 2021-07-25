package io.lumine.mythic.lib.comp.holograms;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import io.lumine.mythic.lib.MythicLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.UUID;

public class CMIPlugin extends HologramSupport {

    @Override
    public void displayIndicator(Location loc, String format, Player player) {
        final CMIHologram hologram = new CMIHologram("MythicLib_" + UUID.randomUUID().toString(), loc);
        hologram.setLines(Collections.singletonList(format));
        if (player != null)
            hologram.hide(player.getUniqueId());
        CMI.getInstance().getHologramManager().addHologram(hologram);
        hologram.update();

        Bukkit.getScheduler().scheduleSyncDelayedTask(MythicLib.plugin, () -> CMI.getInstance().getHologramManager().removeHolo(hologram), 20);
    }
}
