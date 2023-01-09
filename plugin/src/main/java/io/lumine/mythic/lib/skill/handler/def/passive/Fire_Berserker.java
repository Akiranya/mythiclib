package io.lumine.mythic.lib.skill.handler.def.passive;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.AttackSkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Fire_Berserker extends SkillHandler<AttackSkillResult> implements Listener {
    public Fire_Berserker() {
        super(false);

        registerModifiers("extra");
    }

    @Override
    public AttackSkillResult getResult(SkillMetadata meta) {
        return new AttackSkillResult(meta);
    }

    @Override
    public void whenCast(AttackSkillResult result, SkillMetadata skillMeta) {
        skillMeta.getAttack().getDamage().multiplicativeModifier(1 + skillMeta.getModifier("extra") / 100);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(PlayerAttackEvent event) {
        MMOPlayerData data = event.getAttacker().getData();
        if (event.getAttacker().getPlayer().getFireTicks() <= 0)
            return;

        PassiveSkill skill = data.getPassiveSkillMap().getSkill(this);
        if (skill == null)
            return;

        skill.getTriggeredSkill().cast(new TriggerMetadata(event.getAttacker(), event.getEntity()));
    }
}
