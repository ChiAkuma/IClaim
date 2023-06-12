package de.chiakuma.gprev.g_events;

import de.chiakuma.gprev.GriefPrevention;
import de.chiakuma.gprev.PlayerData;
import de.chiakuma.gprev.exceptions.DBMultipleError;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionEvent implements Listener
{
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        if (!GriefPrevention.perPlayerInstances.containsKey(e.getPlayer().getUniqueId()))
        {
            //TODO: instance for every player. Not starting until its really needed, cause performance
            PlayerData instance = new PlayerData(e.getPlayer());
        }
        String f1 = e.getPlayer().getName();
        String f2 = e.getPlayer().getUniqueId().toString();
        ResultSet result = GriefPrevention.DATABASE.executeQuery("SELECT * FROM ?PRE?_players WHERE UUID='%s'".formatted(f2));
        
        boolean hasContent = false;
        try
        {
            hasContent = result.last();
            //String x = result.getString(1) + " " + result.getString(2) + " " + result.getString(3);
            //e.getPlayer().sendMessage("Test: " + x + " == " + result.getRow());
            if (result.getRow() > 1) throw new DBMultipleError("Mehrere players pro UUID existieren. Darf nicht passieren");
        }
        catch (SQLException ignored) {} catch (DBMultipleError ex) {
            ex.printStackTrace();
        }
        if (!hasContent)
        {
            GriefPrevention.DATABASE.executeQuery("INSERT INTO ?PRE?_players (Name, UUID) VALUES (\"%s\", \"%s\")".formatted(f1, f2));
            //e.getPlayer().sendMessage("you are NOT in the database");
            //TODO: First join message. Teleport to Tutorial? whatever
        }

        Chunk toChunk = e.getPlayer().getLocation().getChunk();
        int chunkX = toChunk.getX();
        int chunkZ = toChunk.getZ();

        //TODO: Add Chunk restricted function to DatabaseTools
        if (isChunkRestricted(chunkX, chunkZ)) {
            Location safeLocation = e.getPlayer().getWorld().getSpawnLocation();
            e.getPlayer().teleport(safeLocation);
            e.getPlayer().sendMessage("You logged in within a restricted chunk. You have been teleported to the spawn.");
        }
        
    }
    
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e)
    {
    
    }
}
