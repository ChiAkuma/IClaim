package de.chiakuma.iclaim.commands;

import de.chiakuma.iclaim.Plugin_IClaim;
import de.chiakuma.iclaim.utils.SystemMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Claim implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /claim <create|edit|addchunk|removechunk|list> [arguments]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreateCommand(player, args);
            case "edit" -> handleEditCommand(player, args);
            case "addchunk" -> handleAddChunkCommand(player, args);
            case "removechunk" -> handleRemoveChunkCommand(player, args);
            case "list" -> handleListCommand(player);
            default -> SystemMessages.ClaimCommandUsage(player);
        }

        return true;
    }

    private void handleCreateCommand(Player player, String[] args) {
        // Handle create command
        try
        {
            //Create Data-Claim
            Plugin_IClaim.DATABASE.playerExist(player);
            if (Plugin_IClaim.DATABASE.getICMByPlayerClaim(player) != null)
            {
                SystemMessages.ChunkTaken(player);
                return;
            }
            Plugin_IClaim.DATABASE.createClaim(player, args[0]);
            //TODO: create claim funktion...
            //TODO: automatisch in edit modus gehen. und oder direkt die uuid holen

            //Create one chunk-claim on player position
            handleAddChunkCommand(player, null);
        }
        catch (Exception ignored)
        {

        }
    }

    private void handleEditCommand(Player player, String[] args) {
        // Handle edit command
    }

    private void handleAddChunkCommand(Player player, String[] args) {
        // Handle addchunk command
        //Plugin_IClaim.DATABASE.
    }

    private void handleRemoveChunkCommand(Player player, String[] args) {
        // Handle removechunk command
    }

    private void handleListCommand(Player player) {
        // Handle list command
    }
}
