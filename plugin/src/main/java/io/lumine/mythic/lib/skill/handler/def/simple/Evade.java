package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.SmallParticleEffect;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Evade extends SkillHandler<SimpleSkillResult> {
    public Evade() {
        super();

        registerModifiers("duration");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();
        caster.getWorld().playSound(caster.getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 1, 2);
        new SmallParticleEffect(caster, Particle.CLOUD);
        new EvadeSkill(skillMeta.getCaster().getData(), skillMeta.getParameter("duration"));
    }

    private static class EvadeSkill extends BukkitRunnable implements Listener {
        private final MMOPlayerData data;

        public EvadeSkill(MMOPlayerData data, double duration) {
            this.data = data;

            Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
            runTaskTimer(MythicLib.plugin, 0, 1);
            Bukkit.getScheduler().runTaskLater(MythicLib.plugin, this::close, (long) (duration * 20));
        }

        private void close() {
            cancel();
            EntityDamageEvent.getHandlerList().unregister(this);
        }

        @EventHandler(priority = EventPriority.LOW)
        public void a(EntityDamageEvent event) {
            if (!data.isOnline()) return;
            if (event.getEntity().equals(data.getPlayer()))
                event.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void b(PlayerAttackEvent event) {
            if (event.getAttack().getDamage().hasType(DamageType.WEAPON)
                    && event.getAttacker().getData().equals(data))
                close();
        }

        @Override
        public void run() {
            if (!data.isOnline() || data.getPlayer().isDead())
                close();
            else
                data.getPlayer().getWorld().spawnParticle(Particle.CLOUD, data.getPlayer().getLocation().add(0, 1, 0), 0);
        }
    }
}
