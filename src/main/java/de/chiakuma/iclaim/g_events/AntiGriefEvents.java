package de.chiakuma.iclaim.g_events;

import de.chiakuma.iclaim.Plugin_IClaim;
import de.chiakuma.iclaim.exceptions.DBMultipleError;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AntiGriefEvents implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e)
    {
        boolean cancelled = true;
        try
        {
            Player p = e.getPlayer();
            String sql = "SELECT * FROM ?PRE?_chunks WHERE xChunkPos=%s AND zChunkPos=%s".formatted(p.getChunk().getX(), p.getChunk().getZ());
            ResultSet rs = Plugin_IClaim.DATABASE.executeQuery(sql);
            rs.last();
            System.out.println("Claims: " + rs.getRow());
            if (rs.getRow() > 0) throw new DBMultipleError("Mehrere claims haben einen Chunk!");
            if (rs.getRow() == 0) cancelled = false;
            if (rs.getRow() < 0) System.out.println("Es ist ein Eintrag als claim vorhanden!");
        }
        catch (NullPointerException | SQLException | DBMultipleError ignored) {}
        e.setCancelled(cancelled);
    }

    public void onBlockPlace(BlockPlaceEvent e)
    {

    }

    public void onBlockBreak(BlockBreakEvent e)
    {

    }

    public void onBlockFluid(BlockFromToEvent e)
    {

    }

    public void onBlockSpread(BlockSpreadEvent e)
    {
        
    }

}
