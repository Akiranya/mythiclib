package io.lumine.mythic.lib.skill.custom.targeter.location;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.custom.targeter.LocationTargeter;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a circle around target location. It can be oriented
 * and can appear at source or target location.
 */
@Orientable
public class CircleLocationTargeter extends LocationTargeter {
    private final boolean orient, source;
    private final DoubleFormula radius, amount;

    public CircleLocationTargeter(ConfigObject config) {
        super(config);

        config.validateKeys("radius", "amount");

        orient = config.getBoolean("orient", false);
        source = config.getBoolean("source", false);
        radius = new DoubleFormula(config.getString("radius"));
        amount = new DoubleFormula(config.getString("amount"));
    }

    @Override
    public List<Location> findTargets(SkillMetadata meta) {

        Location source = meta.getSkillLocation(this.source);
        int amount = (int) this.amount.evaluate(meta);
        Validate.isTrue(amount >= 0, "Amount cannot be negative");
        double rad = this.radius.evaluate(meta);
        double step = Math.PI * 2 / (double) amount;

        List<Location> targets = new ArrayList<>();

        Vector axis = orient ? meta.getTargetLocation().clone().subtract(meta.getSourceLocation()).toVector() : null;
        double[] coords = UtilityMethods.getYawPitch(axis);

        for (int i = 0; i < amount; i++) {
            Vector vec = new Vector(rad * Math.cos(i * step), 0, rad * Math.sin(i * step));
            if (orient)
                vec = UtilityMethods.rotate(vec, coords[0], coords[1]);
            targets.add(source.clone().add(vec));
        }

        return targets;
    }
}
