package de.chiakuma.iclaim.g_events;

import de.chiakuma.iclaim.ClaimSelect;
import de.chiakuma.iclaim.IClaim;
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
        if (!IClaim.perPlayerInstances.containsKey(e.getPlayer()))
        {
            //TODO: instance for every player. Not starting until its really needed, cause performance
            ClaimSelect instance = new ClaimSelect(e.getPlayer());
            IClaim.perPlayerInstances.put(e.getPlayer().getUniqueId(), instance);
            
            Thread t = new Thread(instance);
            t.start();
        }
        String f1 = e.getPlayer().getName();
        String f2 = e.getPlayer().getUniqueId().toString();
        ResultSet result = IClaim.database.executeQuery("SELECT * FROM IClaim_players WHERE (playerUUID=\"" + f2 + "\")");
        
        boolean hasContent = false;
        try
        {
            hasContent = result.last();
        }
        catch (SQLException ignored) {}
        if (!hasContent)
        {
            IClaim.database.executeQuery("INSERT INTO IClaim_players (playerName, playerUUID) VALUES (\"%s\", \"%s\")".formatted(f1, f2));
            e.getPlayer().sendMessage("you are NOT in the database");
        }
        
    }
    
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e)
    {
    
    }
}
