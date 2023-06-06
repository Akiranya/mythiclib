package io.lumine.mythic.lib.command.api;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ToggleableCommand {

    @NotNull
    public String getMainLabel();

    @NotNull
    public String getDescription();

    @NotNull
    public List<String> getAliases();

    @NotNull
    public String getConfigPath();

    public boolean isEnabled();
}
