package io.lumine.mythic.lib.comp.mythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Mechanic used to increase the damage for a specific
 * source only. This can be used inside of any skill cast
 * using MMOCore or MMOItems.
 * <p>
 * If the skill trigger is set to DAMAGE for instance, the damage
 * metadata will be saved into a variable so that it can
 * be edited inside of a MM skill using this mechanic.
 * <p>
 * This means that you can have on-hit skills which increase
 * the attack damage by X%. The only thing to make sure on the
 * user end is that the skill trigger is chosen carefully.
 *
 * @author indyuce
 */
@MythicMechanic(
        author = "Indyuce",
        name = "multiplydamage",
        aliases = {"multdamage", "multdmg"},
        description = "Increases damage of current attack by a certain factor"
)
public class MultiplyDamageMechanic implements INoTargetSkill {
    protected final PlaceholderDouble amount;
    protected final DamageType type;
    protected final boolean additive;

    public MultiplyDamageMechanic(MythicLineConfig config) {
        this.amount = PlaceholderDouble.of(config.getString(new String[]{"amount", "a"}, "1", new String[0]));
        String typeFormat = config.getString(new String[]{"type", "t"}, "", new String[0]);
        this.type = typeFormat.isEmpty() ? null : DamageType.valueOf(typeFormat.toUpperCase().replace(" ", "_").replace("-", "_"));
        this.additive = config.getBoolean("additive", false);
    }

    @Override
    public SkillResult cast(SkillMetadata skillMetadata) {

        for (AbstractEntity target : skillMetadata.getEntityTargets()) {
            final @Nullable AttackMetadata attackMeta = MythicLib.plugin.getDamage().getRegisteredAttackMetadata(target.getBukkitEntity());
            if (attackMeta == null)
                continue;

            final double a = this.amount.get(skillMetadata);
            if (additive) {
                if (type == null)
                    attackMeta.getDamage().additiveModifier(a);
                else
                    attackMeta.getDamage().additiveModifier(a, type);
            } else {
                if (type == null)
                    attackMeta.getDamage().multiplicativeModifier(a);
                else
                    attackMeta.getDamage().multiplicativeModifier(a, type);
            }
        }

        return SkillResult.SUCCESS;
    }
}
