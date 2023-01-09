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
    private final PlayerMetadata caster;
    private final VariableList vars;

    /**
     * Location at which the skill was cast
     */
    @NotNull
    private final Location source;

    /**
     * Some skills like ray casts cache a target
     * location which is later used in targeters
     */
    @Nullable
    private final Location targetLocation;

    /**
     * Some skills like projectiles or ray casts cache
     * a target entity which is later used in targeters
     */
    @Nullable
    private final Entity targetEntity;

    @Nullable
    public final SkillOrientation orientation;

    public SkillMetadata(Skill cast, @NotNull MMOPlayerData caster) {
        this(cast, caster.getStatMap().cache(EquipmentSlot.MAIN_HAND), new VariableList(VariableScope.SKILL), null, caster.getPlayer().getLocation(), null, null, null);
    }

    /**
     * @deprecated AttackMetadata no longer extends PlayerMetadata
     */
    @Deprecated
    public SkillMetadata(Skill cast, @NotNull AttackMetadata attackMeta, @NotNull Location source, @Nullable Location targetLocation, @Nullable Entity targetEntity) {
        this(cast, (PlayerMetadata) attackMeta.getAttacker(), new VariableList(VariableScope.SKILL), attackMeta, source, targetLocation, targetEntity, null);
    }

    /**
     * @deprecated SkillMetadatas no longer take as arguments attackMetadatas.
     */
    @Deprecated
    public SkillMetadata(Skill cast, @NotNull PlayerMetadata caster, @NotNull VariableList vars, @Nullable AttackMetadata attackMeta, @NotNull Location source, @Nullable Location targetLocation, @Nullable Entity targetEntity, @Nullable SkillOrientation orientation) {
        this(cast, caster, vars, source, targetLocation, targetEntity, orientation);
    }

        /**
         * @param cast           Initial skill being cast. It's used to retrieve skill modifiers
         * @param caster         Cached statistics of the skill caster
         * @param vars           Skill variable list if it already exists
         * @param source         The location at which the skill/mechanic was cast
         * @param targetLocation The skill/mechanic target location
         * @param targetEntity   The skill/mechanic target entity
         * @param orientation    Skill orientation if some rotation is required later on
         */
    public SkillMetadata(Skill cast, @NotNull PlayerMetadata caster, @NotNull VariableList vars, @NotNull Location source, @Nullable Location targetLocation, @Nullable Entity targetEntity, @Nullable SkillOrientation orientation) {
        this.cast = cast;
        this.caster = caster;
        this.vars = vars;
        this.source = source;
        this.targetLocation = targetLocation;
        this.targetEntity = targetEntity;
        this.orientation = orientation;
    }

    public Skill getCast() {
        return cast;
    }

    public VariableList getVariableList() {
        return vars;
    }

    public PlayerMetadata getCaster() {
        return caster;
    }

    public Location getSourceLocation() {
        return source;
    }

    @Deprecated
    public boolean hasAttackBound() {
        return targetEntity != null && MythicLib.plugin.getDamage().getRegisteredAttackMetadata(targetEntity) != null;
    }

    /**
     * Retrieves a specific skill modifier using
     * the cached instance of {@link Skill}
     *
     * @param path Modifier path
     * @return Modifier value
     */
    public double getModifier(String path) {
        return cast.getModifier(path);
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
        return Objects.requireNonNull(targetLocation, "Skill has no target location");
    }

    @Nullable
    public Location getTargetLocationOrNull() {
        return targetLocation;
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
        return sourceLocation ? source : targetLocation != null ? targetLocation : targetEntity != null ? EntityLocationType.BODY.getLocation(targetEntity) : source;
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
     * Looks into the target entity metadata for an AttackMetadata.
     * If it finds one then it has to be from the skill caster.
     *
     * @return Eventual attack currently being dealt to the entity.
     */
    @NotNull
    public AttackMetadata getAttack() {
        return Objects.requireNonNull(MythicLib.plugin.getDamage().getRegisteredAttackMetadata(getTargetEntity()), "Skill has no attack bound");
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
        return orientation != null ? orientation : new SkillOrientation(Objects.requireNonNull(targetLocation, "Skill has no orientation"), targetLocation.subtract(source).toVector());
    }

    /**
     * Keeps the same skill caster and variables. Used when
     * casting subskills with different targets. This has the
     * effect of keeping every skill data, put aside targets.
     *
     * @return New skill metadata for other subskills
     */
    @NotNull
    public SkillMetadata clone(Location source, Location targetLocation, Entity targetEntity, SkillOrientation orientation) {
        return new SkillMetadata(cast, caster, vars, source, targetLocation, targetEntity, orientation);
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

            // Skill caster
            case "attack":
                var = new AttackMetadataVariable("temp", getAttack());
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
        if (var != null)
            return var;

        // Check for PLAYER scope
        var = getCaster().getData().getVariableList().getVariable(name);
        if (var != null)
            return var;

        // Check for SERVER scope
        return Objects.requireNonNull(SERVER_VARIABLE_LIST.getVariable(name), "Could not find custom variable with name '" + name + "'");
    }

    private static final Pattern INTERNAL_PLACEHOLDER_PATTERN = Pattern.compile("<.*?>");

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
     */
    @NotNull
    public AttackMetadata attack(@NotNull LivingEntity target, double damage, DamageType... types) {
        final @Nullable AttackMetadata opt = MythicLib.plugin.getDamage().getRegisteredAttackMetadata(targetEntity);
        if (opt != null) {
            opt.getDamage().add(damage, types);
            return opt;
        }

        return caster.attack(target, damage, types);
    }
}
