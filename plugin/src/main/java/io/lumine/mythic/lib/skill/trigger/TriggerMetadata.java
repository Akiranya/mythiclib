package io.lumine.mythic.lib.skill.trigger;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.script.variable.VariableList;
import io.lumine.mythic.lib.script.variable.VariableScope;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TriggerMetadata extends PlayerMetadata {

    @Nullable
    private final Entity target;

    @Nullable
    private final AttackMetadata attack;

    /**
     * @deprecated Use {@link #TriggerMetadata(PlayerAttackEvent)}
     */
    @Deprecated
    public TriggerMetadata(@NotNull AttackMetadata attack, @Nullable Entity target) {
        this((PlayerMetadata) attack.getAttacker(), target, attack);
    }

    public TriggerMetadata(@NotNull PlayerAttackEvent attackEvent) {
        this(attackEvent.getAttacker(), attackEvent.getEntity(), attackEvent.getAttack());
    }

    public TriggerMetadata(@NotNull PlayerMetadata caster) {
        this(caster, null, null);
    }

    /**
     * Instantiated every time a player performs an action linked
     * to a skill trigger. This is used to temporarily cache the
     * player stats and save the info needed to cast some skills
     *
     * @param caster Player triggering the skills
     * @param target Potential skill target
     * @param attack Attack which triggered the skill
     */
    public TriggerMetadata(@NotNull PlayerMetadata caster, @Nullable Entity target, @Nullable AttackMetadata attack) {
        super(caster);

        this.target = target;
        this.attack = attack;
    }

    @Nullable
    public Entity getTarget() {
        return target;
    }

    @Nullable
    public AttackMetadata getAttack() {
        return attack;
    }

    /**
     * Called when casting a skill. This transfers all the useful
     * information into a skill metadata
     *
     * @param cast Skill being cast
     * @return Skill cast information containing all the previous information
     */
    @NotNull
    public SkillMetadata toSkillMetadata(Skill cast) {
        return new SkillMetadata(cast, this, new VariableList(VariableScope.SKILL), getPlayer().getLocation(), null, target, null, attack);
    }
}
