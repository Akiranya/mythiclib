package io.lumine.mythic.lib.player.skillmod;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SkillModifierMap {
    private final MMOPlayerData playerData;

    /**
     * This map enables to calculate the skill buffs associated to a particular skill and a particular skillModifier
     * without having to parse other modifers. In particular this is done every time a skill is cast.
     */
    private final Map<SkillParameterIdentifier, SkillModifierInstance> instances = new HashMap<>();

    public SkillModifierMap(MMOPlayerData playerData) {
        this.playerData = playerData;
    }

    /**
     * @return The StatMap owner ie the corresponding MMOPlayerData
     */
    public MMOPlayerData getPlayerData() {
        return playerData;
    }


    /**
     * @return The StatInstances that have been manipulated so far since the
     *         player has logged in. StatInstances are completely flushed when
     *         the server restarts
     */
    public Collection<SkillModifierInstance> getInstances() {
        return instances.values();
    }

    @NotNull
    public SkillModifierInstance getInstance(SkillHandler<?> handler, String skill) {
        final SkillParameterIdentifier id = new SkillParameterIdentifier(handler, skill);
        return instances.computeIfAbsent(id, ident -> new SkillModifierInstance(ident.handler, ident.parameter));
    }
}
