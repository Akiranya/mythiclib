package io.lumine.mythic.lib.command.mythiclib;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.api.stat.modifier.TemporaryStatModifier;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class StatModCommand extends CommandTreeNode {
    public StatModCommand(CommandTreeNode parent) {
        super(parent, "statmod");

        addParameter(Parameter.PLAYER);
        addParameter(new Parameter("<STAT_NAME>", (tree, list) -> list.add("ATTACK_DAMAGE")));
        addParameter(new Parameter("<value>", (tree, list) -> list.add("10")));
        addParameter(new Parameter("(duration)", (tree, list) -> {
            for (int j = 1; j < 5; j++)
                list.add(String.valueOf(20 * j));
        }));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 4)
            return CommandResult.THROW_USAGE;

        final @Nullable Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return CommandResult.FAILURE;
        }

        final String statName = UtilityMethods.enumName(args[2]);
        final MMOPlayerData playerData = MMOPlayerData.get(target);
        final ModifierType type = args[3].toCharArray()[args[3].length() - 1] == '%' ? ModifierType.RELATIVE : ModifierType.FLAT;
        final double value = Double.parseDouble(type == ModifierType.RELATIVE ? args[3].substring(0, args[3].length() - 1) : args[3]);
        final long duration = args.length > 4 ? Math.max(1, Long.parseLong(args[4])) : 0;

        if (duration <= 0)
            new StatModifier(UUID.randomUUID().toString(), statName, value, type, EquipmentSlot.OTHER, ModifierSource.OTHER).register(playerData);
        else
            new TemporaryStatModifier(UUID.randomUUID().toString(), statName, value, type, EquipmentSlot.OTHER, ModifierSource.OTHER).register(playerData, duration);
        return CommandResult.SUCCESS;
    }
}
