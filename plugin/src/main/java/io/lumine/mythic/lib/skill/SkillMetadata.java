package io.lumine.mythic.lib.skill;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.VariableList;
import io.lumine.mythic.lib.script.variable.VariableScope;
import io.lumine.mythic.lib.script.variable.def.*;
import io.lumine.mythic.lib.util.EntityLocationType;
import io.lumine.mythic.lib.util.SkillOrientation;
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
 * Instantiated every time a player casts a skill/script. This
 * contains all the required temporary data like the skill caster
 * and the cached statistics.
 * <p>
 * This also stores variables which can be edited and manipulated by the user.
 */
public class SkillMetadata {
    private final Skill cast;
    private final VariableList vars;

    /**
     * Entity by which the skill was cast
     */
    @NotNull
    private final PlayerMetadata caster;

    /**
     * Location at which the skill was cast
     */
    @NotNull
    private final Location source;

    /**
     * Some skills like projectiles or ray casts cache
     * a target entity which is later used in targeters
     */
    @Nullable
    private final Entity targetEntity;

    /**
     * Some skills like ray casts cache a target
     * location which is later used in targeters
     */
    @Nullable
    private final Location targetLocation;

    @Nullable
    private final AttackMetadata attackSource;

    @Nullable
    public final SkillOrientation orientation;

    public SkillMetadata(Skill cast, @NotNull MMOPlayerData caster) {
        this(cast, caster.getStatMap().cache(EquipmentSlot.MAIN_HAND), new VariableList(VariableScope.SKILL), caster.getPlayer().getLocation(), null, null, null, null);
    }

    @Deprecated
    public SkillMetadata(Skill cast, @NotNull AttackMetadata attackMeta, @NotNull Location source, @Nullable Location targetLocation, @Nullable Entity targetEntity) {
        this(cast, (PlayerMetadata) attackMeta.getAttacker(), new VariableList(VariableScope.SKILL), source, targetLocation, targetEntity, null, attackMeta);
    }

    @Deprecated
    public SkillMetadata(Skill cast, @NotNull PlayerMetadata caster, @NotNull VariableList vars, @Nullable AttackMetadata attackMeta, @NotNull Location source, @Nullable Location targetLocation, @Nullable Entity targetEntity, @Nullable SkillOrientation orientation) {
        this(cast, caster, vars, source, targetLocation, targetEntity, orientation, attackMeta);
    }

    public SkillMetadata(Skill cast, @NotNull Location source, @Nullable Location targetLocation, @Nullable Entity targetEntity, @NotNull AttackMetadata attackMeta) {
        this(cast, (PlayerMetadata) attackMeta.getAttacker(), new VariableList(VariableScope.SKILL), source, targetLocation, targetEntity, null, attackMeta);
    }

    public SkillMetadata(Skill cast, @NotNull PlayerMetadata caster, @NotNull VariableList vars, @NotNull Location source, @Nullable Location targetLocation, @Nullable Entity targetEntity, @Nullable SkillOrientation orientation) {
        this(cast, caster, vars, source, targetLocation, targetEntity, orientation, null);
    }

    /**
     * @param cast           Initial skill being cast. It's used to retrieve skill parameter values
     * @param caster         Cached statistics of the skill caster
     * @param vars           Skill variable list if it already exists
     * @param source         The location at which the skill/mechanic was cast
     * @param targetLocation The skill/mechanic target location
     * @param targetEntity   The skill/mechanic target entity
     * @param orientation    Skill orientation if some rotation is required later on
     * @param attackSource   Attack which triggered the skill
     */
    public SkillMetadata(Skill cast, @NotNull PlayerMetadata caster, @NotNull VariableList vars, @NotNull Location source, @Nullable Location targetLocation, @Nullable Entity targetEntity, @Nullable SkillOrientation orientation, @Nullable AttackMetadata attackSource) {
        this.cast = cast;
        this.caster = caster;
        this.vars = vars;
        this.source = source;
        this.targetLocation = targetLocation;
        this.targetEntity = targetEntity;
        this.orientation = orientation;
        this.attackSource = attackSource;
    }

    @NotNull
    public Skill getCast() {
        return cast;
    }

    @NotNull
    public VariableList getVariableList() {
        return vars;
    }

    @NotNull
    public PlayerMetadata getCaster() {
        return caster;
    }

    @NotNull
    public Location getSourceLocation() {
        return source.clone();
    }

    /**
     * @deprecated See {@link #getAttackSource()}
     */
    @Deprecated
    public boolean hasAttackBound() {
        return hasAttackSource();
    }

    /**
     * Looks into the target entity metadata for an AttackMetadata.
     * If it finds one then it has to be from the skill caster.
     *
     * @return Eventual attack currently being dealt to the entity.
     * @deprecated See {@link #getAttackSource()}
     */
    @NotNull
    @Deprecated
    public AttackMetadata getAttack() {
        return getAttackSource();
    }

    public boolean hasAttackSource() {
        return attackSource != null;
    }

    /**
     * @return The attack which triggered the skill.
     */
    @NotNull
    public AttackMetadata getAttackSource() {
        return Objects.requireNonNull(attackSource, "Skill was not triggered by any attack");
    }

    /**
     * @deprecated Skill modifiers are now called "parameters"
     */
    @Deprecated
    public double getModifier(String param) {
        return getParameter(param);
    }

    /**
     * Retrieves a specific skill parameter value
     *
     * @param param Skill parameter path
     * @return Skill parameter final value, taking into account skill mods
     */
    public double getParameter(String param) {
        return caster.getData().getSkillModifierMap().getInstance(cast.getHandler(), param).getTotal(cast.getParameter(param));
    }

    @NotNull
    public Entity getTargetEntity() {
        return Objects.requireNonNull(targetEntity, "Skill has no target entity");
    }

    @Nullable
    public Entity getTargetEntityOrNull() {
        return targetEntity;
    }

    public boolean hasTargetEntity() {
        return targetEntity != null;
    }

    @NotNull
    public Location getTargetLocation() {
        return Objects.requireNonNull(targetLocation, "Skill has no target location").clone();
    }

    @Nullable
    public Location getTargetLocationOrNull() {
        return targetLocation == null ? null : targetLocation.clone();
    }

    public boolean hasTargetLocation() {
        return targetLocation != null;
    }

    @NotNull
    public SkillOrientation getOrientation() {
        return Objects.requireNonNull(orientation, "Skill has no orientation");
    }

    @Nullable
    public SkillOrientation getOrientationOrNull() {
        return orientation;
    }

    public boolean hasOrientation() {
        return orientation != null;
    }

    /**
     * Analog of {@link #getSkillEntity(boolean)}. Used when a skill requires a
     * location when no targeter is provided
     *
     * @param sourceLocation If the source location should be prioritized
     * @return Target location (and if it exists) OR location of target entity (and if it exists), source location otherwise
     */
    @NotNull
    public Location getSkillLocation(boolean sourceLocation) {
        return sourceLocation ? source.clone() : targetLocation != null ? targetLocation.clone() : targetEntity != null ? EntityLocationType.BODY.getLocation(targetEntity) : source.clone();
    }

    /**
     * Analog of {@link #getSkillLocation(boolean)}. Used when a skill requires an
     * entity when no targeter is provided
     *
     * @param caster If the skill caster should be prioritized
     * @return Target entity if prioritized (and if it exists), skill caster otherwise
     */
    @NotNull
    public Entity getSkillEntity(boolean caster) {
        return caster || targetEntity == null ? getCaster().getPlayer() : targetEntity;
    }

    /**
     * Analog of {@link #getSkillEntity(boolean)} or {@link #getSkillLocation(boolean)}
     * being used when a location targeter requires an orientation in order
     * to potentially orient locations.
     *
     * @return Skill orientation if not null. If it is, it tries to create
     *         one using the skill target and source location if it is not null.
     *         Throws a NPE if the metadata has neither an orientation nor a target location.
     */
    @NotNull
    public SkillOrientation getSkillOrientation() {
        return orientation != null ? orientation : new SkillOrientation(Objects.requireNonNull(targetLocation, "Skill has no orientation").clone(), targetLocation.clone().subtract(source).toVector());
    }

    /**
     * Keeps the same skill caster and variables. Used when casting subskills with different
     * targets. This has the effect of keeping every skill data, put aside targets.
     * <p>
     * Data that is kept on cloning:
     * - skill being cast
     * - skill caster
     * - variable list
     * - attack source
     * <p>
     * Data being replaced on cloning:
     * - source location
     * - target entity
     * - target location
     * - skill orientation (not used yet)
     *
     * @return New skill metadata for other subskills
     */
    @NotNull
    public SkillMetadata clone(@NotNull Location source, @Nullable Location targetLocation, @Nullable Entity targetEntity, @Nullable SkillOrientation orientation) {
        return new SkillMetadata(cast, caster, vars, source, targetLocation, targetEntity, orientation, attackSource);
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
    public Variable getReference(String name) {

        // Find initial variable
        final String[] args = name.split("\\.");
        Variable var;
        int i = 1;

        switch (args[0]) {

            // Access modifiers
            case "modifier":
                Validate.isTrue(args.length > 1, "Please specify a modifier name");
                var = new DoubleVariable("temp", getParameter(args[i++]));
                break;

            // Skill source location
            case "source":
                var = new PositionVariable("temp", source.clone());
                break;

            // Skill target location
            case "targetLocation":
                var = new PositionVariable("temp", getTargetLocation());
                break;

            // Skill caster
            case "caster":
                var = new PlayerVariable("temp", getCaster().getPlayer());
                break;

            // Skill caster
            case "attack":
                var = new AttackMetadataVariable("temp", getAttackSource());
                break;

            // Cached stat map
            case "stat":
                var = new StatsVariable("temp", caster);
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
                throw new IllegalArgumentException("Could not match variable type to '" + args[0] + "', did you mean 'var." + args[0] + "'?");
        }

        // Dives into the variable tree to find the subvariable
        for (; i < args.length; i++)
            var = var.getVariable(args[i]);

        return var;
    }

    public static final VariableList SERVER_VARIABLE_LIST = new VariableList(VariableScope.SERVER);

    /**
     * Finds a CUSTOM variable with a certain name.
     * <p>
     * Scope priority (from most to least restrictive):
     * - SKILL
     * - PLAYER
     * - SERVER
     *
     * @param name Variable name
     * @return Variable found
     */
    @NotNull
    public Variable getCustomVariable(String name) {

        // Prioritize SKILL scope
        Variable var = vars.getVariable(name);
        if (var != null) return var;

        // Check for PLAYER scope
        var = getCaster().getData().getVariableList().getVariable(name);
        if (var != null) return var;

        // Check for SERVER scope
        return Objects.requireNonNull(SERVER_VARIABLE_LIST.getVariable(name), "Could not find custom variable with name '" + name + "'");
    }

    private static final Pattern INTERNAL_PLACEHOLDER_PATTERN = Pattern.compile("<[^&|<>]*?>");

    @NotNull
    public String parseString(String str) {

        // Parse any placeholders and apply color codes
        str = MythicLib.plugin.getPlaceholderParser().parse(getCaster().getPlayer(), str);

        // Internal placeholders
        Matcher match = INTERNAL_PLACEHOLDER_PATTERN.matcher(str);
        while (match.find()) {
            final String placeholder = str.substring(match.start() + 1, match.end() - 1);
            str = str.replace("<" + placeholder + ">", getReference(placeholder).toString());
            match = INTERNAL_PLACEHOLDER_PATTERN.matcher(str);
        }

        return str;
    }

    /**
     * Have the skill caster damage an entity. Either creates a new
     * instance of AttackMeta based on this metadata, or uses the
     * existing one if any is bound.
     *
     * @param target Target entity
     * @param damage Damage dealt
     * @param types  Type of target
     * @return The (modified) attack metadata
     * @deprecated Use {@link PlayerMetadata#attack(LivingEntity, double, DamageType...)} instead
     */
    @NotNull
    @Deprecated
    public AttackMetadata attack(@NotNull LivingEntity target, double damage, DamageType... types) {
        return caster.attack(target, damage, types);
    }
}
