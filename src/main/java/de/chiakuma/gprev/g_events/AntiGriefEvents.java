package de.chiakuma.gprev.g_events;

import de.chiakuma.gprev.GriefPrevention;
import de.chiakuma.gprev.exceptions.TrySkip;
import org.bukkit.Chunk;
import org.bukkit.Location;
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
import java.util.UUID;

public class AntiGriefEvents implements Listener
{

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e)
    {
        boolean cancelled = true;
        Player p = e.getPlayer();
        try
        {
            //TODO: nochmal richtiges anti betreten einbauen
            //checking this chunk
            //UUID.fromString(rs.getString("IcmUUID"))
            //6 1 5
            //4 0 3
            //8 2 7

            Chunk toChunk = e.getTo().getChunk();

            ResultSet rs0 = GriefPrevention.DATABASE.getClaim(toChunk.getX(), toChunk.getZ());
            if (rs0 == null)
            {
                cancelled = false;
                throw new TrySkip();
            }

            while (rs0.next()) {
                if (GriefPrevention.DATABASE.ICM_CompareOwner(UUID.fromString(rs0.getString("IcmUUID")), p))
                {
                    cancelled = false;
                    throw new TrySkip();
                }
            }
            Location safeLocation = e.getFrom();
            p.teleport(safeLocation);
            p.sendMessage("You are not allowed to enter this chunk.");
        }
        catch (NullPointerException | SQLException | TrySkip ignored) {}
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
