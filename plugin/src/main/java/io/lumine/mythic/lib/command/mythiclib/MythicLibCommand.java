package io.lumine.mythic.lib.command.mythiclib;

import io.lumine.mythic.lib.command.api.CommandTreeRoot;
import io.lumine.mythic.lib.command.mythiclib.debug.DebugCommand;

public class MythicLibCommand extends CommandTreeRoot {
    public MythicLibCommand() {
        super("mythiclib", "mythiclib.admin");

        addChild(new ReloadCommand(this));
        addChild(new CastCommand(this));
        addChild(new DebugCommand(this));
        addChild(new StatModCommand(this));
    }
}