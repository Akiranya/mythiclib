package io.lumine.mythic.lib.comp.mythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.damage.DamagingMechanic;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.result.MythicMobsSkillResult;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;

@MythicMechanic(
        author = "Indyuce",
        name = "mmodamage",
        aliases = {"mmod", "mmodmg"},
        description = "Deals damage to the target (compatible with MMO plugins)"
)
public class MMODamageMechanic extends DamagingMechanic implements ITargetedEntitySkill {
    protected final PlaceholderDouble amount;
    protected final boolean ignoreMMOAttack;

    /**
     * Can be empty if no damage type is registered.
     * <p>
     * It IS possible but any attack should be at least physical or magical.
     * It should also be either a weapon/skill/unarmed attack.
     */
    protected final DamageType[] types;

    /**
     * Cannot save the Element object reference since skills
     * load BEFORE elements. This also permits the elements to
     * be modified without having to reload skills which reduces
     * MythicLib module load inter-dependency.
     */
    @Nullable
    private final String elementName;

    @Deprecated
    public MMODamageMechanic(SkillExecutor manager, String file, MythicLineConfig mlc) {
        this(manager, new File(file), mlc.getLine(), mlc);
    }

    public MMODamageMechanic(SkillExecutor manager, File file, String line, MythicLineConfig mlc) {
        super(manager, file, line, mlc);

        this.amount = PlaceholderDouble.of(mlc.getString(new String[]{"amount", "a"}, "1", new String[0]));
        String typesString = mlc.getString(new String[]{"type", "t", "types"}, null, new String[0]);
        this.ignoreMMOAttack = mlc.getBoolean(new String[]{"ignoreMMOAttack", "immo"}, false);
        this.elementName = mlc.getString(new String[]{"element", "el", "e"}, null);
        this.types = (typesString == null || typesString.isEmpty() || "NONE".equalsIgnoreCase(typesString)) ? new DamageType[0] : toDamageTypeArray(typesString);
    }

    @NotNull
    private DamageType[] toDamageTypeArray(String typesString) {
        String[] split = typesString.split("\\,");
        DamageType[] array = new DamageType[split.length];

        for (int i = 0; i < array.length; i++)
            array[i] = DamageType.valueOf(UtilityMethods.enumName(split[i]));

        return array;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {

        if (target.isDead() || !(target.getBukkitEntity() instanceof LivingEntity) || data.getCaster().isUsingDamageSkill() || target.getHealth() <= 0)
            return SkillResult.INVALID_TARGET;

        // Find damageMeta
        final double damage = amount.get(data, target) * data.getPower();
        final @Nullable Element element = elementName != null ? Objects.requireNonNull(MythicLib.plugin.getElements().get(UtilityMethods.enumName(elementName)), "Could not find element with ID '" + elementName + "'") : null;

        final AttackMetadata currentAttack;
        if (!ignoreMMOAttack && (currentAttack = MythicLib.plugin.getDamage().getRegisteredAttackMetadata(target.getBukkitEntity())) != null) {
            if (element == null)
                currentAttack.getDamage().add(damage, types);
            else
                currentAttack.getDamage().add(damage, element, types);
            return SkillResult.SUCCESS;
        }

        // Find attacker
        final Entity attackerBukkit = data.getCaster().getEntity().getBukkitEntity();
        final @Nullable StatProvider attacker = data.getVariables().has(MythicMobsSkillResult.MMO_SKILLMETADATA_TAG) ?
                ((io.lumine.mythic.lib.skill.SkillMetadata) data.getVariables().get(MythicMobsSkillResult.MMO_SKILLMETADATA_TAG).get()).getCaster()
                : attackerBukkit instanceof LivingEntity ? StatProvider.get((LivingEntity) attackerBukkit, EquipmentSlot.MAIN_HAND, true) : null;

        // Find damage
        final DamageMetadata damageMeta = element == null ? new DamageMetadata(damage, types) : new DamageMetadata(damage, element, types);
        final AttackMetadata attackMeta = new AttackMetadata(damageMeta, (LivingEntity) target.getBukkitEntity(), attacker);

        // Register damage in ML and apply damage
        MythicLib.plugin.getDamage().markAsMetadata(attackMeta);
        try {
            doDamage(data, target, damage);
            MythicLogger.debug(MythicLogger.DebugLevel.MECHANIC, "+ MMODamageMechanic fired for {0} with {1} power", new Object[]{damage, data.getPower()});
        } catch (Exception exception) {
            MythicLib.plugin.getLogger().log(Level.SEVERE, "Caught an exception (4) while damaging entity '" + target.getUniqueId() + "':");
            exception.printStackTrace();
        } finally {
            MythicLib.plugin.getDamage().unmarkAsMetadata(attackMeta);
        }

        return SkillResult.SUCCESS;
    }
}
