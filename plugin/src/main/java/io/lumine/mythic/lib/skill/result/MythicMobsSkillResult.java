package io.lumine.mythic.lib.skill.result;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.GenericCaster;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillMetadataImpl;
import io.lumine.mythic.core.skills.SkillTriggers;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.MythicMobsSkillHandler;
import io.lumine.mythic.lib.util.RayTrace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class MythicMobsSkillResult implements SkillResult {
    private final MythicMobsSkillHandler behaviour;
    private final SkillMetadataImpl mmSkillMeta;

    public MythicMobsSkillResult(SkillMetadata skillMeta, MythicMobsSkillHandler behaviour) {
        this.behaviour = behaviour;

        // TODO Support trigger/caster difference?
        AbstractEntity trigger = BukkitAdapter.adapt(skillMeta.getCaster().getPlayer());
        SkillCaster caster = new GenericCaster(trigger);

        HashSet<AbstractEntity> targetEntities = new HashSet<>();
        HashSet<AbstractLocation> targetLocations = new HashSet<>();

        // Add target entity
        if (skillMeta.hasTargetEntity())
            targetEntities.add(BukkitAdapter.adapt(skillMeta.getTargetEntityOrNull()));

            /*
             * If none is found, provide a default entity target. This takes
             * the entity is the player is looking at if there is any. This
             * is purely for simplicity so that skills cast within MythicLib
             * match the /mm test cast command.
             */
        else {
            final Player player = skillMeta.getCaster().getPlayer();
            final RayTrace res = new RayTrace(player, 32, entity -> !entity.equals(player) && entity instanceof LivingEntity);
            if (res.hasHit())
                targetEntities.add(BukkitAdapter.adapt(res.getHit()));
        }

        // Add target location
        if (skillMeta.hasTargetLocation())
            targetLocations.add(BukkitAdapter.adapt(skillMeta.getTargetLocationOrNull()));

        mmSkillMeta = new SkillMetadataImpl(SkillTriggers.API, caster, trigger, BukkitAdapter.adapt(skillMeta.getCaster().getPlayer().getEyeLocation()), targetEntities, targetLocations, 1);

        // Stats & cast skill are cached inside a variable
        mmSkillMeta.getVariables().putObject(MMO_SKILLMETADATA_TAG, skillMeta);
    }

    public static final String MMO_SKILLMETADATA_TAG = "MMOSkillMetadata";

    @Override
    public boolean isSuccessful(SkillMetadata skillMeta) {
        return behaviour.getSkill().isUsable(mmSkillMeta);
    }

    public SkillMetadataImpl getMythicMobsSkillMetadata() {
        return mmSkillMeta;
    }

    @Deprecated
    public SkillMetadataImpl getMythicMobskillMetadata() {
        return getMythicMobsSkillMetadata();
    }
}
