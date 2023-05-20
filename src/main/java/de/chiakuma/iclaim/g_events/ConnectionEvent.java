package de.chiakuma.iclaim.g_events;

import de.chiakuma.iclaim.Plugin_IClaim;
import de.chiakuma.iclaim.PlayerData;
import de.chiakuma.iclaim.exceptions.DBMultipleError;
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
        if (!Plugin_IClaim.perPlayerInstances.containsKey(e.getPlayer().getUniqueId()))
        {
            //TODO: instance for every player. Not starting until its really needed, cause performance
            PlayerData instance = new PlayerData(e.getPlayer());
        }
        String f1 = e.getPlayer().getName();
        String f2 = e.getPlayer().getUniqueId().toString();
        ResultSet result = Plugin_IClaim.DATABASE.executeQuery("SELECT * FROM ?PRE?_players WHERE playerUUID='%s'".formatted(f2));
        
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
            Plugin_IClaim.DATABASE.executeQuery("INSERT INTO ?PRE?_players (playerName, playerUUID) VALUES (\"%s\", \"%s\")".formatted(f1, f2));
            //e.getPlayer().sendMessage("you are NOT in the database");
            //TODO: First join message. Teleport to Tutorial? whatever
        }
        
    }
    
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e)
    {
    
    }
}
