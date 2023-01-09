package io.lumine.mythic.lib.command;

import io.lumine.mythic.lib.gui.AttributeExplorer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Deprecated
public class ExploreAttributesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("/exploreattributes is deprecated. Use instead /ml debug attributes");

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "> Only players may use this command.");
            return true;
        }

        Player target = args.length > 0 ? Bukkit.getPlayer(args[0]) : (Player) sender;
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "> Could not find player called " + args[0] + ".");
            return true;
        }

        new AttributeExplorer((Player) sender, target).open();
        return true;
    }
}
