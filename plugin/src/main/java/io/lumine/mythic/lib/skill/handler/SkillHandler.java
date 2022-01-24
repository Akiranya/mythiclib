package io.lumine.mythic.lib.skill.handler;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

/**
 * The interface that groups up:
 * - custom MM skills
 * - custom MythicLib skills
 * - custom SkillAPI skills
 * - default MythicLib skills
 * <p>
 * MythicLib has a huge default collection of "skills" however
 * these cannot be cast yet because they still lack important
 * properties which cannot be setup in MythicLib because they
 * are specific to the plugins using the ML skills:
 * - the ability configurable name and description (different setups for MI and MMOCore)
 * - default values for modifiers (also different formulas for MI and MMOCore)
 * <p>
 * MythicLib stores {@link SkillHandler}, which are skills substracted from
 * all of this data which is SPECIFIC to the plugin using the skills.
 * Other plugins like MMOCore and MMOItems store {@link Skill} instances.
 *
 * @param <T> Skill result class being used by that skill behaviour
 * @author jules
 */
public abstract class SkillHandler<T extends SkillResult> {
    private final String id;
    private final Set<String> modifiers = new HashSet<>();
    private final boolean triggerable;

    protected static final Random random = new Random();

    /**
     * Used by default MythicLib skill handlers
     */
    public SkillHandler() {
        this(true);
    }

    /**
     * Used by default MythicLib skill handlers
     *
     * @param triggerable If the skill can be triggered
     */
    public SkillHandler(boolean triggerable) {
        this.id = formatId(getClass().getSimpleName());
        this.triggerable = triggerable;

        registerModifiers("cooldown", "mana", "stamina");
    }

    /**
     * Used to register a custom skill handler
     *
     * @param id Skill handler identifier
     */
    public SkillHandler(@NotNull String id) {
        this.id = formatId(id);
        this.triggerable = true;

        registerModifiers("cooldown", "mana", "stamina");
    }

    /**
     * Used to register a custom skill handler
     *
     * @param config Configuration section to load the skill handler from
     * @param id     Skill handler identifier
     */
    public SkillHandler(@NotNull ConfigurationSection config, @NotNull String id) {
        this.id = formatId(id);
        this.triggerable = true;

        // Register custom modifiers
        if (config.contains("modifiers")) { registerModifiers(config.getStringList("modifiers")); }

        // Default modifiers
        registerModifiers("cooldown", "mana", "stamina");
    }

    private String formatId(String str) {
        return str.toUpperCase().replace("-", "_").replace(" ", "_").replaceAll("[^A-Z_]", "");
    }

    public String getId() {
        return id;
    }

    public String getLowerCaseId() {
        return id.toLowerCase().replace("_", "-");
    }

    public void registerModifiers(String... mods) {
        registerModifiers(Arrays.asList(mods));
    }

    public void registerModifiers(Collection<String> mods) {
        modifiers.addAll(mods);
    }

    /**
     * This field is set to true to handle passive skills
     * which are fully handled by MythicLib without the use
     * of {@link TriggerType}.
     *
     * @return If it should be triggered when calling {@link MMOPlayerData#triggerSkills(TriggerType, Entity)}
     */
    public boolean isTriggerable() {
        return triggerable;
    }

    /**
     * Skill modifiers are specific numeric values that
     * determine how powerful a skill is. Modifiers can be
     * the skill damage, cooldown, duration if it applies
     * some potion effect, etc.
     * <p>
     * MythicLib does NOT store modifier default values/
     * formulas that scale with the player class level. Rather
     * it only stores what modifiers the skill has because
     * it's a necessary information for skill handlers.
     *
     * @return The set of all possible modifiers of that skill
     */
    public Set<String> getModifiers() {
        return modifiers;
    }

    /**
     * Skill results are used to check if a skill can be cast.
     * <p>
     * This method evaluates MythicMobs custom conditions,
     * checks if the caster has an entity in their line of
     * sight, if he is on the ground...
     * <p>
     * Runs first before {@link Skill#getResult(SkillMetadata)}
     *
     * @param meta Info of skill being cast
     * @return Skill result
     */
    @NotNull
    public abstract T getResult(SkillMetadata meta);

    /**
     * This is where the actual skill effects are applied.
     * <p>
     * Runs last, after {@link Skill#whenCast(SkillMetadata)}
     *
     * @param result    Skill result
     * @param skillMeta Info of skill being cast
     */
    public abstract void whenCast(T result, SkillMetadata skillMeta);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillHandler<?> that = (SkillHandler<?>) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
