package io.lumine.mythic.lib.comp.mythicmobs.mechanic;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.INoTargetSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderDouble;
import org.apache.commons.lang.Validate;

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
public class MultiplyDamageMechanic extends SkillMechanic implements INoTargetSkill {
    protected final PlaceholderDouble amount;
    protected final DamageType type;

    public MultiplyDamageMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);

        this.amount = PlaceholderDouble.of(config.getString(new String[]{"amount", "a"}, "1", new String[0]));
        String typeFormat = config.getString(new String[]{"type", "t"}, "", new String[0]);
        this.type = typeFormat.isEmpty() ? null : DamageType.valueOf(typeFormat.toUpperCase().replace(" ", "_").replace("-", "_"));
    }

    @Override
    public boolean cast(SkillMetadata skillMetadata) {
        Validate.isTrue(skillMetadata.getVariables().has("MMOAttack"), "No attack meta is provided");
        AttackMetadata attackMeta = (AttackMetadata) skillMetadata.getVariables().get("MMOAttack").get();

        double a = this.amount.get(skillMetadata.getCaster());
        if (type == null)
            attackMeta.getDamage().multiplicativeModifier(a);
        else
            attackMeta.getDamage().multiplicativeModifier(a, type);

        return true;
    }
}
