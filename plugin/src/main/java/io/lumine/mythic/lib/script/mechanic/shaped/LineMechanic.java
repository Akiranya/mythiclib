package io.lumine.mythic.lib.script.mechanic.shaped;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.script.targeter.location.ConstantLocationTargeter;
import io.lumine.mythic.lib.script.targeter.location.DefaultLocationTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Draws a helix of particles around the target
 */
public class LineMechanic extends Mechanic {
   // private final DoubleFormula points;
    private final LocationTargeter source, target;

    private final Script onStart, onTick, onEnd;

    public LineMechanic(ConfigObject config) {
        config.validateKeys("source", "target");

        source = MythicLib.plugin.getSkills().loadLocationTargeter(config.getObject("source"));
        target = MythicLib.plugin.getSkills().loadLocationTargeter(config.getObject("target"));

        onStart = config.contains("start") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("start")) : null;
        onTick = MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("tick"));
        onEnd = config.contains("end") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("end")) : null;
    }

    @Override
    public void cast(SkillMetadata meta) {
 /*
        // This better not be empty
        Vector dir = direction.findTargets(meta).get(0).toVector();

        for (Location loc : targetLocation.findTargets(meta))
            cast(meta, loc, dir); */
    }

    public void cast(SkillMetadata meta, Location source, Vector dir) {
    /*    Validate.isTrue(dir.lengthSquared() > 0, "Direction cannot be zero");

        final double yaw_i = UtilityMethods.getYawPitch(dir)[0] - yawSpread / 2;
        new BukkitRunnable() {

            // Tick counter
            int counter = 0;

            public void run() {
                for (int i = 0; i < pointsPerTick; i++) {
                    if (counter++ >= points) {
                        cancel();
                        if (onEnd != null)
                            onEnd.cast(meta);
                        return;
                    }

                    final double yaw = Math.toRadians(yaw_i + ((double) counter / points) * yawSpread);
                    final double y = ((double) counter / points) * height, r = radius.evaluate(meta);

                    for (int j = 0; j < helixes; j++) {
                        final double angle = yaw + Math.PI * 2 / helixes * j;
                        Location loc = source.clone().add(r * Math.cos(angle), y, r * Math.sin(angle));
                        onTick.cast(meta.clone(source, loc, null, null));
                    }
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, timeInterval); */
    }
}
