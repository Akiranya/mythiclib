package io.lumine.mythic.lib.command.mythiclib.debug;

import io.lumine.mythic.lib.api.event.DamageCheckEvent;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Used to test some code sections. It should be kept empty
 * so that users don't randomly just perform that command
 *
 * @author jules
 */
public class TestCommand extends CommandTreeNode {
    public TestCommand(CommandTreeNode parent) {
        super(parent, "test");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {


        Player player = (Player) sender;
        for (Entity ent : player.getNearbyEntities(10, 10, 10)) {

            player.sendMessage("Testing for>> " + ent.getEntityId() + " " + ent.getType().name());

            DamageCheckEvent called = new DamageCheckEvent(player, ent, InteractionType.OFFENSE_SKILL);
            Bukkit.getPluginManager().callEvent(called);
            player.sendMessage("===> " + called.isCancelled());


        }

        return CommandResult.SUCCESS;
    }
}
