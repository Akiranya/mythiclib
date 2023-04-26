package io.lumine.mythic.lib.script.mechanic.shaped;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.mechanic.type.DirectionMechanic;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.DoubleFormula;
import org.apache.commons.lang.Validate;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public class ProjectileMechanic extends DirectionMechanic {
    private final DoubleFormula speed, size, lifeSpan, step;
    private final Script onHitBlock, onHitEntity, onTick;
    private final boolean ignorePassable, stopOnBlock;

    /**
     * This determines if this skill is considered a support
     * or offense skill. Depending on the skill type, it changes
     * whether or not that skill can hit certain players.
     */
    private final boolean offense;

    /**
     * Maximum amount of enemies hit per that projectile
     */
    private final int hitLimit;

    private static final double DEFAULT_LIFE_SPAN = 60,
            DEFAULT_SIZE = .2,
            DEFAULT_STEP = .2;

    public ProjectileMechanic(ConfigObject config) {
        super(config);

        onTick = config.contains("tick") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("tick")) : null;
        onHitEntity = config.contains("hit_entity") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("hit_entity")) : null;
        onHitBlock = config.contains("hit_block") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("hit_block")) : null;
        ignorePassable = config.getBoolean("ignore_passable", false);
        offense = config.getBoolean("offense", true);
        hitLimit = config.getInt("hits", 1);
        stopOnBlock = config.getBoolean("stop_on_block", true);

        speed = config.contains("speed") ? new DoubleFormula(config.getString("speed")) : new DoubleFormula(1);
        size = config.contains("size") ? new DoubleFormula(config.getString("size")) : new DoubleFormula(DEFAULT_SIZE);
        step = config.contains("step") ? new DoubleFormula(config.getString("step")) : new DoubleFormula(DEFAULT_STEP);
        lifeSpan = config.contains("life_span") ? new DoubleFormula(config.getString("life_span")) : new DoubleFormula(DEFAULT_LIFE_SPAN);

        // Validate.isTrue(speed > 0, "Speed must be strictly positive");
        // Validate.isTrue(size >= 0, "Size must be positive or null");
        // Validate.isTrue(lifeSpan > 0, "Life span must be strictly positive (don't make it too low)");
    }

    @Override
    public void cast(SkillMetadata meta, Location source, Vector dir) {
        Validate.isTrue(dir.lengthSquared() > 0, "Direction cannot be zero");

        new BukkitRunnable() {

            // Direction is normalized
            final Vector dr = dir.normalize().clone().multiply(.5 * speed.evaluate(meta));
            final double dl = dr.length(), projectileSize = size.evaluate(meta), projLifeSpan = lifeSpan.evaluate(meta), smallest_d = step.evaluate(meta);

            // Location being incremented every second
            Location current = source.clone();

            // Projectile time counter
            int counter = 0;

            // Entity hit counter
            int entityHits = 0;

            public void run() {
                if (counter++ >= projLifeSpan) {
                    cancel();
                    return;
                }

                current.add(dr);

                Predicate<Entity> filter = entity -> MythicLib.plugin.getEntities().canInteract(meta.getCaster().getPlayer(), entity, offense ? InteractionType.OFFENSE_SKILL : InteractionType.SUPPORT_SKILL);
                RayTraceResult result = onHitBlock != null ?
                        current.getWorld().rayTrace(current, dir, dl, FluidCollisionMode.NEVER, ignorePassable, projectileSize, filter)
                        : current.getWorld().rayTraceEntities(current, dir, dl, projectileSize, filter);

                if (onTick != null)
                    for (double j = 0; j < dl; j += smallest_d) {
                        Location intermediate = current.clone().add(dir.clone().multiply(j));
                        onTick.cast(meta.clone(source, intermediate, null, null));
                    }

                if (result == null)
                    return;

                if (onHitBlock != null && result.getHitBlock() != null) {
                    onHitBlock.cast(meta.clone(source, result.getHitPosition().toLocation(current.getWorld()), null, null));
                    if (stopOnBlock)
                        cancel();
                }

                if (onHitEntity != null && result.getHitEntity() != null) {
                    onHitEntity.cast(meta.clone(source, result.getHitPosition().toLocation(current.getWorld()), result.getHitEntity(), null));

                    // Check for entity hits
                    entityHits++;
                    if (entityHits >= hitLimit) cancel();
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
