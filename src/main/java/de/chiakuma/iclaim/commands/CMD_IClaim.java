package de.chiakuma.iclaim.commands;

import de.chiakuma.iclaim.IClaim;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CMD_IClaim implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("tool"))       player.getInventory().addItem(IClaim.iWand);
                if (args[0].equalsIgnoreCase("wand"))       player.getInventory().addItem(IClaim.iWand);
                if (args[0].equalsIgnoreCase("buy"))        player.sendMessage("iclaim buy");
                if (args[0].equalsIgnoreCase("pos1"))       player.sendMessage("iclaim pos1");
                if (args[0].equalsIgnoreCase("pos2"))       player.sendMessage("iclaim pos2");
                if (args[0].equalsIgnoreCase("info"))       player.sendMessage("iclaim info");
                if (args[0].equalsIgnoreCase("remove"))     player.sendMessage("iclaim remove");
                if (args[0].equalsIgnoreCase("yes"))        player.sendMessage("iclaim yes");
                if (args[0].equalsIgnoreCase("no"))         player.sendMessage("iclaim no");
                if (args[0].equalsIgnoreCase("help"))       player.sendMessage("iclaim help");
            }

            if (args.length > 1)
            {
                if (args[0].equalsIgnoreCase("settings"))
                {
                    if (args[1].equalsIgnoreCase("invite")) player.sendMessage("iclaim settings invite");
                    if (args[1].equalsIgnoreCase("kick"))   player.sendMessage("iclaim settings kick");
                    if (args[1].equalsIgnoreCase("ban"))    player.sendMessage("iclaim settings ban");
                }
                if (args[0].equalsIgnoreCase("sell"))
                {
                    try
                    {
                        int playersPrice = Integer.decode(args[1]);
                        player.sendMessage("iclaim sell " + playersPrice);
                    }
                    catch (NumberFormatException e)
                    {
                        //TODO
                    }
                }
            }

            return true;
        }
        else if (sender instanceof ConsoleCommandSender)
        {
            //TODO
            return true;
        }
        return false;
    }

}
