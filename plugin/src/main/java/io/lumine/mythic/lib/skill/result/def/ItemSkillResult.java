package io.lumine.mythic.lib.skill.result.def;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Skill that requires to throw the item in hand. This takes as input
 * the player's main hand item and also takes the direction where he's looking
 */
public class ItemSkillResult extends VectorSkillResult {

    @Nullable
    private final ItemStack item;

    @Nullable
    private final Material defaultMaterial;

    public ItemSkillResult(SkillMetadata skillMeta) {
        this(skillMeta, null);
    }

    public ItemSkillResult(SkillMetadata skillMeta, @Nullable Material defaultMaterial) {
        super(skillMeta);

        this.defaultMaterial = defaultMaterial;
        final EquipmentSlot actionHand = skillMeta.getCaster().getActionHand().toBukkit();
        item = skillMeta.getCaster().getPlayer().getInventory().getItem(actionHand);
    }

    @NotNull
    public ItemStack getItem() {
        return UtilityMethods.isAir(item) ? new ItemStack(defaultMaterial) : item;
    }

    @Override
    public boolean isSuccessful(SkillMetadata skillMeta) {
        return super.isSuccessful(skillMeta) && (!UtilityMethods.isAir(item) || defaultMaterial != null);
    }
}