package de.chiakuma.iclaim.commands;

import de.chiakuma.iclaim.Plugin_IClaim;
import de.chiakuma.iclaim.PlayerData;
import de.chiakuma.iclaim.exceptions.DBMultipleError;
import de.chiakuma.iclaim.utils.SystemMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CMD_IClaim implements CommandExecutor {
    //TODO: ChatGPTs ansatz einrichten: Name des chats: claim system
    //TODO neue implementierung des SQL Codes in die neue implementierung der klasse hinzufügen

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            PlayerData playerData = Plugin_IClaim.perPlayerInstances.get(player.getUniqueId());
            if (args.length == 1) {
                //================================================================================
                // Commands for getting the Wand/Tool
                //================================================================================
                if (args[0].equalsIgnoreCase("tool"))           player.getInventory().addItem(Plugin_IClaim.iWand);
                if (args[0].equalsIgnoreCase("wand"))           player.getInventory().addItem(Plugin_IClaim.iWand);

                //================================================================================
                // Commands that need more arguments
                // More info defined below
                //================================================================================
                if (args[0].equalsIgnoreCase("create"))         player.sendMessage("/iclaim create <ClaimName>");
                if (args[0].equalsIgnoreCase("edit"))           player.sendMessage("/iclaim edit <ClaimName>");

                //================================================================================
                // See which claim is selected
                //================================================================================
                if (args[0].equalsIgnoreCase("selected")
                        || args[0].equalsIgnoreCase("sld"))     player.sendMessage("/iclaim selected:sld");

                //================================================================================
                // Adds a chunk to currently edited claim
                //================================================================================
                if (args[0].equalsIgnoreCase("add"))
                {
                    if (playerData.currentClaimEdit != null)
                    {
                        ResultSet rs = Plugin_IClaim.DATABASE.executeQuery("SELECT * FROM ?PRE?_claims WHERE claimUUID='" + playerData.currentClaimEdit + "'");
                        try {
                            rs.last();
                            if (rs.getRow() > 1) throw new DBMultipleError("Mehrere claims pro claimUUID existieren. Darf nicht passieren");
                            String SQL = "INSERT INTO ?PRE?chunks (claimID, xChunkPos, zChunkPos) VALUES (%s, %s, %s)".formatted(rs.getInt("claimID"), player.getChunk().getX(), player.getChunk().getZ());
                            Plugin_IClaim.DATABASE.executeQuery(SQL);
                        } catch (SQLException e) {
                            SystemMessages.SQLError(player);
                        } catch (DBMultipleError e) {
                            //TODO: Sending error to server admin
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        player.sendMessage("Benutze vorher /iclaim edit <ClaimName>");
                    }
                    player.sendMessage("/iclaim add");
                }

                //================================================================================
                // Removes a chunk from currently edited claim
                //================================================================================
                if (args[0].equalsIgnoreCase("remove"))         player.sendMessage("/iclaim remove");
                if (args[0].equalsIgnoreCase("buy"))            player.sendMessage("/iclaim buy");
                if (args[0].equalsIgnoreCase("info"))           player.sendMessage("/iclaim info");
                if (args[0].equalsIgnoreCase("remove"))         player.sendMessage("/iclaim remove");
                if (args[0].equalsIgnoreCase("yes"))            player.sendMessage("/iclaim yes");
                if (args[0].equalsIgnoreCase("no"))             player.sendMessage("/iclaim no");
                if (args[0].equalsIgnoreCase("help"))           player.sendMessage("/iclaim help");
            }

            if (args.length > 1)
            {
                //================================================================================
                // Create a new claim
                //================================================================================
                if (args[0].equalsIgnoreCase("create"))
                {
                    try
                    {
                        //get if player is in database
                        String sql = "SELECT * FROM ?PRE?_players WHERE playerUUID='%s'".formatted(player.getUniqueId());
                        ResultSet res = Plugin_IClaim.DATABASE.executeQuery(sql);
                        res.last();
                        player.sendMessage(res.getRow() + "");
                        if (res.getRow() > 1) throw new DBMultipleError("Mehrere players pro UUID existieren. Darf nicht passieren");
                        UUID pid = UUID.fromString(res.getString("playerUUID"));
                        res.close();

                        //get claim by name and check if it does exist already if not send a message
                        ResultSet res1 = Plugin_IClaim.DATABASE.executeQuery("SELECT * FROM ?PRE?_claims WHERE claimName='%s'".formatted(args[1]));
                        res1.last();
                        player.sendMessage(res1.getRow() + "");
                        if (res1.getRow() > 0)
                        {
                            player.sendMessage("Es gibt schon ein claim mit diesem Namen! Bitte anderen verwenden");
                            throw new DBMultipleError("Mehrere claims pro NAME existieren. Darf nicht passieren");
                        }
                        res1.close();

                        //create the claim
                        Plugin_IClaim.DATABASE.executeQuery("INSERT INTO ?PRE?_claims(playerUUID, claimUUID, claimName, claimSize) VALUES('%s', '%s', '%s', 0)".formatted(pid, UUID.randomUUID().toString(), args[1]));
                    }
                    catch (SQLException | DBMultipleError e) { e.printStackTrace(); }
                }

                //================================================================================
                // Edit a claim and its properties
                //================================================================================
                if (args[0].equalsIgnoreCase("edit"))
                {
                    try {
                        //Get if there is a claim with this name/id
                        String sql = "SELECT playerID FROM ?PRE?claims WHERE claimName='%s'".formatted(args[1]);
                        ResultSet res = Plugin_IClaim.DATABASE.executeQuery(sql);
                        res.last();
                        int pid = res.getInt("playerID");
                        if (res.getRow() > 1) throw new DBMultipleError("Mehrere claims pro NAME existieren. Darf nicht passieren");

                        //check if the owner is the player
                        ResultSet res1 = Plugin_IClaim.DATABASE.executeQuery("SELECT playerUUID FROM ?PRE?_players WHERE playerID=%s".formatted(pid));
                        res1.last();
                        UUID uid = UUID.fromString(res1.getString("playerUUID"));
                        if (!player.getUniqueId().equals(uid))
                        {
                            player.sendMessage("You are not the owner of this Claim!");
                        }

                        //sende nachricht wenn es nicht der selbe claim ist
                        //TODO: testen ob sqlexception passiert. Wenn ja getString ist nicht der richtige typ für UUID
                        if (playerData.currentClaimEdit != null && UUID.fromString(res.getString("claimUUID")).equals(playerData.currentClaimEdit))
                            player.sendMessage("Du bearbeitest den Claim: %s nicht mehr".formatted(res.getString("claimName")));
                        res.close();

                        playerData.currentClaimEdit = UUID.fromString(res.getString("claimUUID"));
                    }
                    catch (SQLException | DBMultipleError e) { e.printStackTrace(); }
                }
                if (args[0].equalsIgnoreCase("settings"))
                {
                    if (args[1].equalsIgnoreCase("invite"))     player.sendMessage("/iclaim settings invite");
                    if (args[1].equalsIgnoreCase("kick"))       player.sendMessage("/iclaim settings kick");
                    if (args[1].equalsIgnoreCase("ban"))        player.sendMessage("/iclaim settings ban");
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

