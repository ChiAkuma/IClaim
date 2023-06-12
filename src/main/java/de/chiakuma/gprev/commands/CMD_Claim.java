package de.chiakuma.gprev.commands;

import de.chiakuma.gprev.GriefPrevention;
import de.chiakuma.gprev.utils.SystemMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

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
        //TODO: too few arguments message here--
        if (args.length < 1) return;
        try
        {
            //Create Data-Claim
            //look for player in database
            GriefPrevention.DATABASE.playerExist(player);
            //get if this chunk is already claimed
            if (GriefPrevention.DATABASE.getICMByPlayerClaim(player) != null)
            {
                //if its already claimed its taken and sending message to player
                SystemMessages.ChunkTaken(player);
                return;
            }
            //if all is ok the icm will be created
            UUID IcmUUID = GriefPrevention.DATABASE.createICM(player, args[1]);
            //TODO: create claim funktion...
            //TODO: automatisch in edit modus gehen. und oder direkt die uuid holen

            //and if the icm is created the chunk will get claimed automatically
            System.out.println("Claim got added? " + GriefPrevention.DATABASE.addClaimToICM(player.getChunk().getX(), player.getChunk().getZ(), IcmUUID));
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

    }

    private void handleRemoveChunkCommand(Player player, String[] args) {
        // Handle removechunk command
    }

    private void handleListCommand(Player player) {
        // Handle list command
    }
}
