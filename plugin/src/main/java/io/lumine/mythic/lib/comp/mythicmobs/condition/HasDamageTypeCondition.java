package io.lumine.mythic.lib.comp.mythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.core.utils.annotations.MythicCondition;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@MythicCondition(author = "Gunging", name = "mmodamagetype", aliases = {}, description = "If the skill is of this damage type. ")
public class HasDamageTypeCondition extends SkillCondition implements ISkillMetaCondition {
    protected final boolean exact;
    protected final Set<DamageType> types = new HashSet<>();

    public HasDamageTypeCondition(@NotNull MythicLineConfig mlc) {
        super(mlc.getLine());

        // Require exact damage types
        this.exact = mlc.getBoolean("exact", false);

        // Read types being sought
        String typesString = mlc.getString(new String[]{"type", "t", "types"}, null);
        if (typesString != null && !typesString.isEmpty() && !typesString.equalsIgnoreCase("NONE"))
            for (String str : typesString.replace("<&cm>", ",").split(","))
                this.types.add(DamageType.valueOf(UtilityMethods.enumName(str)));
    }

    @Override
    public boolean check(SkillMetadata skillMetadata) {

        /*
         * Making it a Validate statement would cause MythicMob skills to crash
         * and stop execution if the condition was used through a means that
         * produced no attack meta, where at least I personally would use this
         * very condition to check if the skill had no damage meta at all.
         */
        final Set<DamageType> attackDamageTypes = new HashSet<>();

        // Collect all damage types
        for (AbstractEntity target : skillMetadata.getEntityTargets()) {
            final @Nullable AttackMetadata opt = MythicLib.plugin.getDamage().getRegisteredAttackMetadata(target.getBukkitEntity());
            if (opt != null)
                attackDamageTypes.addAll(opt.getDamage().collectTypes());
        }

        // Exact match
        if (exact)
            return attackDamageTypes.equals(this.types);

        // Must contain ALL specified damage types
        for (DamageType damageType : this.types)
            if (!attackDamageTypes.contains(damageType))
                return false;

        return true;
    }
}
