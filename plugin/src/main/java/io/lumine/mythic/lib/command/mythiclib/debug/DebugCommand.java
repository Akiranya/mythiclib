package io.lumine.mythic.lib.command.mythiclib.debug;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import org.bukkit.command.CommandSender;

public class DebugCommand extends CommandTreeNode {
    public DebugCommand(CommandTreeNode parent) {
        super(parent, "debug");

        addChild(new LogsCommand(this));
        addChild(new NBTCommand(this));
        addChild(new StatsCommand(this));
        addChild(new VersionsCommand(this));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
