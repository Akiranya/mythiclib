package io.lumine.mythic.lib.comp.flags;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ResidenceFlags implements FlagPlugin {
    public ResidenceFlags() {
        for (CustomFlag flag : CustomFlag.values())
            FlagPermissions.addFlag(flag.getPath());
    }

    @Override
    public boolean isPvpAllowed(Location loc) {
        final ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
        return res == null || res.getPermissions().has(Flags.pvp, true);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFlagAllowed(Player player, CustomFlag flag) {
        final ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(player);
        return res == null || res.getPermissions().playerHas(player, flag.getPath(), flag.getDefault());
    }

    @Override
    public boolean isFlagAllowed(Location loc, CustomFlag flag) {
        final ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
        return res == null || res.getPermissions().has(flag.getPath(), flag.getDefault(), true);
    }
}