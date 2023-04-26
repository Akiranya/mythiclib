package io.lumine.mythic.lib.player.skillmod;

import io.lumine.mythic.lib.skill.handler.SkillHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SkillParameterIdentifier {
    public final SkillHandler<?> handler;
    public final String parameter;

    public SkillParameterIdentifier(@NotNull SkillHandler<?> handler, @NotNull String parameter) {
        this.handler = handler;
        this.parameter = parameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillParameterIdentifier that = (SkillParameterIdentifier) o;
        return handler.equals(that.handler) && parameter.equals(that.parameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handler, parameter);
    }
}
