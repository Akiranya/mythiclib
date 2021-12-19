package io.lumine.mythic.lib.skill;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.variable.Variable;
import io.lumine.mythic.lib.skill.variable.VariableList;
import io.lumine.mythic.lib.skill.variable.VariableScope;
import io.lumine.mythic.lib.skill.variable.def.EntityVariable;
import io.lumine.mythic.lib.skill.variable.def.PlayerVariable;
import io.lumine.mythic.lib.skill.variable.def.PositionVariable;
import io.lumine.mythic.lib.skill.variable.def.StatsVariable;
import io.lumine.mythic.lib.util.EntityLocationType;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instantiated every time a player casts a skill. This contains
 * all the required temporary data like the skill caster and
 * the cached statistics.
 * <p>
 * This also stores variables which can be edited and manipulated by the user.
 */
public class SkillMetadata {

    @Deprecated
    private final Skill cast;
    private final StatMap.CachedStatMap stats;
    private final VariableList vars;

    /**
     * Some mechanics
     */
    @Nullable
    private final AttackMetadata attackMeta;

    /**
     * Location at which the skill was cast
     */
    @NotNull
    private final Location source;

    /**
     * Some skills like ray casts cache a target location which is
     * later used in targeters
     */
    @Nullable
    private final Location targetLocation;

    /**
     * Some skills like projectiles or ray casts cache a target
     * entity which is later used in targeters
     */
    @Nullable
    private final Entity targetEntity;

    public SkillMetadata(Skill cast, MMOPlayerData caster) {
        this(cast, caster.getStatMap().cache(EquipmentSlot.MAIN_HAND), new VariableList(VariableScope.SKILL), null, caster.getPlayer().getLocation(), null, null);
    }

    /**
     * @param cast           Skill being cast
     * @param caster         Player casting the skill
     * @param attackMeta     Some triggers pass an attackMeta as argument, like DAMAGED or DAMAGE.
     * @param source         The location at which the skill/mechanic was cast
     * @param targetLocation The skill/mechanic target location
     * @param targetEntity   The skill/mechanic target entity
     */
    public SkillMetadata(Skill cast, MMOPlayerData caster, AttackMetadata attackMeta, Location source, Location targetLocation, Entity targetEntity) {
        this(cast, caster.getStatMap().cache(EquipmentSlot.MAIN_HAND), new VariableList(VariableScope.SKILL), attackMeta, source, targetLocation, targetEntity);
    }

    /**
     * @param cast           Skill being cast
     * @param stats          Cached statistics of the skill caster
     * @param vars           Skill variable list if it already exists
     * @param attackMeta     Some triggers pass an attackMeta as argument, like DAMAGED or DAMAGE.
     * @param source         The location at which the skill/mechanic was cast
     * @param targetLocation The skill/mechanic target location
     * @param targetEntity   The skill/mechanic target entity
     */
    public SkillMetadata(Skill cast, StatMap.CachedStatMap stats, VariableList vars, AttackMetadata attackMeta, Location source, Location targetLocation, Entity targetEntity) {
        this.cast = cast;
        this.stats = stats;
        this.vars = vars;
        this.attackMeta = attackMeta;
        this.source = source;
        this.targetLocation = targetLocation;
        this.targetEntity = targetEntity;
    }

    public Skill getCast() {
        return cast;
    }

    public MMOPlayerData getCaster() {
        return stats.getData();
    }

    public VariableList getVariableList() {
        return vars;
    }

    public StatMap.CachedStatMap getStats() {
        return stats;
    }

    public Location getSourceLocation() {
        return source;
    }

    public boolean hasAttackBound() {
        return attackMeta != null;
    }

    @NotNull
    public AttackMetadata getAttack() {
        return Objects.requireNonNull(attackMeta, "Skill has no attack metadata bound");
    }

    @NotNull
    public Entity getTargetEntity() {
        return Objects.requireNonNull(targetEntity, "Skill has no target entity");
    }

    @Nullable
    public Entity getTargetEntityOrNull() {
        return targetEntity;
    }

    @NotNull
    public Location getTargetLocation() {
        return Objects.requireNonNull(targetLocation, "Skill has no target location");
    }

    @Nullable
    public Location getTargetLocationOrNull() {
        return targetLocation;
    }

    /**
     * Analog of {@link #getSkillEntity(boolean)}. Used when a skill requires a
     * location when no targeter is provided
     *
     * @param sourceLocation If the source location should be prioritized
     * @return Target location (and if it exists) OR location of target entity (and if it exists), source location otherwise
     */
    public Location getSkillLocation(boolean sourceLocation) {
        return sourceLocation ? source : targetLocation != null ? targetLocation : targetEntity != null ? EntityLocationType.BODY.getLocation(targetEntity) : source;
    }

    /**
     * Analog of {@link #getSkillLocation(boolean)}. Used when a skill requires an
     * entity when no targeter is provided
     *
     * @param caster If the skill caster should be prioritized
     * @return Target entity if prioritized (and if it exists), skill caster otherwise
     */
    public Entity getSkillEntity(boolean caster) {
        return caster || targetEntity == null ? getCaster().getPlayer() : targetEntity;
    }

    /**
     * Keeps the same skill caster and variables. Used when
     * casting subskills with different targets. This has the
     * effect of keeping every skill data, put aside targets.
     *
     * @return New skill metadata for other subskills
     */
    public SkillMetadata clone(Location source, Location targetLocation, Entity targetEntity) {
        return new SkillMetadata(cast, stats, vars, attackMeta, source, targetLocation, targetEntity);
    }

    /**
     * Finds the initial variable and dives into its
     * subvariables to parse some expression.
     * <p>
     * Possible options:
     * - var.custom_variable.subvariable
     * - caster.subvariable
     * - target.subvariable
     *
     * @param name Something like "var.custom_variable.subvariable1.subvariable2"
     * @return The (sub) variable found
     */
    public Variable getVariable(String name) {

        // Find initial variable
        String[] args = name.split("\\.");
        Variable var;
        int i = 1;

        switch (args[0]) {

            // Skill source location
            case "source":
                var = new PositionVariable("temp", source);
                break;

            // Skill target location
            case "targetLocation":
                var = new PositionVariable("temp", getTargetLocation());
                break;

            // Skill caster
            case "caster":
                var = new PlayerVariable("temp", getCaster().getPlayer());
                break;

            // Cached stat map
            case "stat":
                var = new StatsVariable("temp", stats);
                break;

            // Skill target
            case "target":
                Validate.notNull(targetEntity, "Skill has no target");
                var = targetEntity instanceof Player ? new PlayerVariable("temp", (Player) targetEntity) : new EntityVariable("temp", targetEntity);
                break;

            // Custom variable
            case "var":
                Validate.isTrue(args.length > 1, "Custom variable name not specified");
                var = getCustomVariable(args[i++]);
                break;

            default:
                throw new IllegalArgumentException("Could not match variable type to '" + args[0] + "', perhaps you meant 'var." + args[0] + "'?");
        }

        // Dives into the variable tree to find the subvariable
        for (; i < args.length; i++)
            var = var.getVariable(args[i]);

        return var;
    }

    /**
     * Finds a CUSTOM variable with a certain name.
     * <p>
     * Scope priority (from most to least restrictive):
     * - SKILL
     * - PLAYER
     * - SERVER (not implemented yet)
     *
     * @param name Variable name
     * @return Variable found
     */
    @Nullable
    public Variable getCustomVariable(String name) {

        // Prioritize SKILL scope
        Variable var = vars.getVariable(name);
        if (var != null)
            return var;

        // Check for PLAYER scope
        var = getCaster().getSkillVariableList().getVariable(name);
        return Objects.requireNonNull(var, "Could not find custom variable with name '" + name + "'");
    }

    private static final Pattern INTERNAL_PLACEHOLDER_PATTERN = Pattern.compile("<.*?>");

    public String parseString(String str) {

        // Parse any placeholders and apply color codes
        String format = MythicLib.plugin.getPlaceholderParser().parse(getCaster().getPlayer(), str);

        // Internal placeholders
        Matcher match = INTERNAL_PLACEHOLDER_PATTERN.matcher(format);
        while (match.find()) {
            String placeholder = format.substring(match.start() + 1, match.end() - 1);
            format = format.replace("<" + placeholder + ">", getVariable(placeholder).toString());
            match = INTERNAL_PLACEHOLDER_PATTERN.matcher(format);
        }

        return format;
    }

    /**
     * Utility method that makes a player deal damage to a specific
     * entity.
     * <p>
     * This method either creates a new attackMetadata based on this
     * metadata, or uses the existing one if any is bound.
     *
     * @param target Target entity
     * @param damage Damage dealt
     * @param types  Type of target
     * @return The (modified) attack metadata
     */
    public AttackMetadata attack(LivingEntity target, double damage, DamageType... types) {
        if (attackMeta != null) {
            attackMeta.getDamage().add(damage, types);
            return attackMeta;
        }

        AttackMetadata attackMeta = new AttackMetadata(new DamageMetadata(damage, types), getStats());
        MythicLib.plugin.getDamage().damage(attackMeta, target);
        return attackMeta;
    }
}