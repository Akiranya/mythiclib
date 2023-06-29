package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class TNT_Throw extends SkillHandler<VectorSkillResult> {
    public TNT_Throw() {
        super();

        registerModifiers("force");
    }

    @Override
    public VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        Vector vec = result.getTarget().multiply(2 * skillMeta.getParameter("force"));
        TNTPrimed tnt = (TNTPrimed) caster.getWorld().spawnEntity(caster.getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
        tnt.setFuseTicks(80);
        tnt.setVelocity(vec);
        new CancelTeamDamage(caster, tnt);
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1, 0);
        caster.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, caster.getLocation().add(0, 1, 0), 12, 0, 0, 0, .1);
    }

    /**
     * Used to cancel team damage and other things
     */
    public class CancelTeamDamage extends TemporaryListener {
        private final Player player;
        private final TNTPrimed tnt;

        public CancelTeamDamage(Player player, TNTPrimed tnt) {
            super(EntityDamageByEntityEvent.getHandlerList());

            this.player = player;
            this.tnt = tnt;

            close(100);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void a(EntityDamageByEntityEvent event) {
            if (event.getDamager().equals(tnt) && !UtilityMethods.canTarget(player, event.getEntity()))
                event.setCancelled(true);
        }

        @Override
        public void whenClosed() {
            // Nothing
        }
    }
}
