package io.lumine.mythic.lib.skill.handler.def.simple;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.NoClipItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;

public class Present_Throw extends SkillHandler<SimpleSkillResult> {
    private final ItemStack present = VersionMaterial.PLAYER_HEAD.toItem();

    public Present_Throw() {
        super();

        registerModifiers("damage", "radius", "force");

        try {
            ItemMeta presentMeta = present.getItemMeta();
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTcyNmQ5ZDA2MzJlNDBiZGE1YmNmNjU4MzliYTJjYzk4YTg3YmQ2MTljNTNhZGYwMDMxMGQ2ZmM3MWYwNDJiNSJ9fX0="));

            Field profileField = presentMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(presentMeta, profile);

            present.setItemMeta(presentMeta);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not apply 'Present Throw' head texture");
        }
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double damage = skillMeta.getModifier("damage");
        double radiusSquared = Math.pow(skillMeta.getModifier("radius"), 2);

        Player caster = skillMeta.getCaster().getPlayer();

        final NoClipItem item = new NoClipItem(caster.getLocation().add(0, 1.2, 0), present);
        item.getEntity().setVelocity(caster.getEyeLocation().getDirection().multiply(1.5 * skillMeta.getModifier("force")));

        /*
         * when items are moving through the air, they loose a percent of their
         * velocity proportionally to their coordinates in each axis. this means
         * that if the trajectory is not affected, the ratio of x/y will always
         * be the same. check for any change of that ratio to check for a
         * trajectory change
         */
        final double trajRatio = item.getEntity().getVelocity().getX() / item.getEntity().getVelocity().getZ();
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1, 0);
        new BukkitRunnable() {
            int ti = 0;

            public void run() {
                if (ti++ > 70 || item.getEntity().isDead()) {
                    item.close();
                    cancel();
                }

                double currentTrajRatio = item.getEntity().getVelocity().getX() / item.getEntity().getVelocity().getZ();
                item.getEntity().getWorld().spawnParticle(Particle.SPELL_INSTANT, item.getEntity().getLocation().add(0, .1, 0), 0);
                if (item.getEntity().isOnGround() || Math.abs(trajRatio - currentTrajRatio) > .1) {
                    item.getEntity().getWorld().spawnParticle(Particle.FIREWORKS_SPARK, item.getEntity().getLocation().add(0, .1, 0), 128, 0, 0, 0, .25);
                    item.getEntity().getWorld().playSound(item.getEntity().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_TWINKLE.toSound(), 2, 1.5f);
                    for (Entity entity : UtilityMethods.getNearbyChunkEntities(item.getEntity().getLocation()))
                        if (entity.getLocation().distanceSquared(item.getEntity().getLocation()) < radiusSquared && UtilityMethods.canTarget(caster, entity))
                            skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC);
                    item.close();
                    cancel();
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
