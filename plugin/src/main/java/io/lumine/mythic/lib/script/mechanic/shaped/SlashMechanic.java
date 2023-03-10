package io.lumine.mythic.lib.script.mechanic.shaped;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.script.targeter.location.DefaultDirectionTargeter;
import io.lumine.mythic.lib.script.targeter.location.DefaultLocationTargeter;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Performs what looks like a sword slash in
 * front of the caster/target entity/target location.
 */
public class SlashMechanic extends Mechanic {
    private final double length, angle, distance;
    private final long points, timeInterval, pointsPerTick;

    private final LocationTargeter sourceLocation, targetLocation;

    private final Script onTick, onEnd;

    public SlashMechanic(ConfigObject config) {
        sourceLocation = config.contains("source") ? MythicLib.plugin.getSkills().loadLocationTargeter(config.getObject("source")) : new DefaultLocationTargeter();
        targetLocation = config.contains("target") ? MythicLib.plugin.getSkills().loadLocationTargeter(config.getObject("target")) : new DefaultDirectionTargeter();

        config.validateKeys("tick");

        onTick = MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("tick"));
        onEnd = config.contains("end") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("end")) : null;

        distance = config.getDouble("distance", 3);
        length = config.getDouble("length", 4);
        angle = config.getDouble("angle", -30);

        points = config.getInt("points", 20);
        timeInterval = config.getInt("time_interval", 1);
        pointsPerTick = config.getInt("points_per_tick", 1);

        Validate.isTrue(length > 0, "Length must be strictly positive");
        Validate.isTrue(points > 0, "Points must be strictly positive");
        Validate.isTrue(timeInterval > 0, "Time interval must be strictly positive");
        Validate.isTrue(pointsPerTick > 0, "Points per tick must be strictly positive");
    }

    @Override
    public void cast(SkillMetadata meta) {

        // This better not be empty
        Location source = this.sourceLocation.findTargets(meta).get(0);

        for (Location loc : targetLocation.findTargets(meta))
            cast(meta, source, loc.clone().subtract(source).toVector());
    }

    public void cast(SkillMetadata meta, Location source, Vector dir) {
        Validate.isTrue(dir.lengthSquared() > 0, "Direction cannot be zero");

        Vector radialAxis = dir.clone().normalize();
        Vector slashDirection = dir.clone().setY(0).rotateAroundY(-Math.PI / 2).rotateAroundAxis(radialAxis, Math.toRadians(angle)).normalize();

        new BukkitRunnable() {

            // Tick counter
            int counter = 0;

            Location current = source.clone().add(slashDirection.clone().multiply(-length / 2));
            Vector incremented = slashDirection.clone().multiply(length / points);

            public void run() {
                for (int i = 0; i < pointsPerTick; i++) {

                    // Move the current slash location
                    current.add(incremented);

                    // Add some curvature using f(x) = sqrt(1 - x??) to find the real location
                    double x = Math.abs(counter - points / 2) * 2d / points;
                    Location intermediate = current.clone().add(dir.clone().multiply(distance * Math.sqrt(1 - x * x)));
                    onTick.cast(meta.clone(source, intermediate, null, null));

                    if (counter++ >= points) {
                        cancel();
                        if (onEnd != null)
                            onEnd.cast(meta.clone(source, intermediate, null, null));
                        return;
                    }
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, timeInterval);
    }
}
