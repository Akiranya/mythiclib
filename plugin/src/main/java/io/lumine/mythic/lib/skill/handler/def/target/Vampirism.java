package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Vampirism extends SkillHandler<AttackSkillResult> {
    public Vampirism() {
        super();

        registerModifiers("drain");
    }

    @Override
    public AttackSkillResult getResult(SkillMetadata meta) {
        return new AttackSkillResult(meta);
    }

    @Override
    public void whenCast(AttackSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            final Location loc = target.getLocation();
            double ti = 0;
            double dis = 0;

            public void run() {
                for (int j1 = 0; j1 < 4; j1++) {
                    ti += .75;
                    dis += ti <= 10 ? .15 : -.15;

                    for (double j = 0; j < Math.PI * 2; j += Math.PI / 4)
                        loc.getWorld().spawnParticle(Particle.REDSTONE,
                                loc.clone().add(Math.cos(j + (ti / 20)) * dis, 0, Math.sin(j + (ti / 20)) * dis), 1,
                                new Particle.DustOptions(Color.RED, 1));
                }
                if (ti >= 17)
                    cancel();
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITCH_DRINK, 1, 2);
        UtilityMethods.heal(caster, skillMeta.getAttack().getDamage().getDamage() * skillMeta.getModifier("drain") / 100);
    }
}
