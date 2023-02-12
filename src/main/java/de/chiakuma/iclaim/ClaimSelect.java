package de.chiakuma.iclaim;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class ClaimSelect implements Runnable
{
    Player player;
    Location pos1, last_pos1;
    Location pos2, last_pos2;
    
    boolean running = true;
    public ClaimSelect(Player player)
    {
        this.player = player;
    }
    
    public void setLocation(int p1_p2, Location loc)
    {
        this.resetGhostBlocks(pos1, pos2);
        if (p1_p2 > 3) return;
        if (p1_p2 == 1)
        {
            this.last_pos1 = this.pos1;
            this.pos1 = loc;
        }
        if (p1_p2 == 2)
        {
            this.last_pos2 = this.pos2;
            this.pos2 = loc;
        }
    }
    
    public void resetLastGhostBlocks()
    {
        this.resetGhostBlocks(last_pos1, last_pos2);
    }
    
    public void resetGhostBlocks(Location pos1, Location pos2)
    {
        for (int x = 0; x < 10; x++)
        {
            for (int z = 0; z < 10; z++)
            {
                if (pos1 != null)
                {
                    Location xpos = new Location(player.getWorld(), (pos1.getX() -5) + x, pos1.getY(), (pos1.getZ() -5) + z);
                    placeSelectorGhostBlock(xpos, xpos.getBlock().getType());
                }
                if (pos2 != null)
                {
                    Location zpos = new Location(player.getWorld(), (pos2.getX() - 5) + x, pos2.getY(), (pos2.getZ() - 5) + z);
                    placeSelectorGhostBlock(zpos, zpos.getBlock().getType());
                }
                
            
            }
        }
    }
    
    public void placeSelectorGhostBlock(Location pos, Material mat)
    {
        BlockData bd = Bukkit.createBlockData(mat);
        player.sendBlockChange(pos, bd);
    }
    
    public void placeSelectorGhostBlocks()
    {
        if (pos1 != null) this.placeSelectorGhostBlock(pos1, Material.DIAMOND_BLOCK);
        if (pos2 != null) this.placeSelectorGhostBlock(pos2, Material.DIAMOND_BLOCK);
        if (pos1 != null && pos2 != null)
        {
            int x_shift = 0;
            if (pos1.getX() < pos2.getX()) x_shift = 1;
            else if (pos1.getX() > pos2.getX()) x_shift = -1;
            
            Location x1 = pos1.clone();
            x1.setX(pos1.getX() + x_shift);
            this.placeSelectorGhostBlock(x1, Material.GOLD_BLOCK);
    
            Location x2 = pos2.clone();
            x2.setX(pos2.getX() - x_shift);
            this.placeSelectorGhostBlock(x2, Material.GOLD_BLOCK);
    
            int z_shift = 0;
            if (pos1.getZ() < pos2.getZ()) z_shift = 1;
            else if (pos1.getZ() > pos2.getZ()) z_shift = -1;
    
            Location z1 = pos1.clone();
            z1.setZ(pos1.getZ() + z_shift);
            this.placeSelectorGhostBlock(z1, Material.GOLD_BLOCK);
    
            Location z2 = pos2.clone();
            z2.setZ(pos2.getZ() - z_shift);
            this.placeSelectorGhostBlock(z2, Material.GOLD_BLOCK);
            
            this.placeSelectorGhostBlock(pos1, Material.GLOWSTONE);
            this.placeSelectorGhostBlock(pos2, Material.GLOWSTONE);
        }
    }
    
    public void placeEdgeParticles()
    {
        int x_shift = 0, z_shift = 0;
        if (pos1 != null && pos2 != null)
        {
            if (pos1.getX() < pos2.getX()) x_shift = 1;
            else if (pos1.getX() > pos2.getX()) x_shift = -1;
            if (pos1.getZ() < pos2.getZ()) z_shift = 1;
            else if (pos1.getZ() > pos2.getZ()) z_shift = -1;
        }
        
        if (x_shift == 0 | z_shift == 0) return;
        for (int x = 0; Math.abs(x) < Math.floor(Math.abs(pos1.getX() - pos2.getX())) +2; x += x_shift)
        {
            for (int y = -50; y < 100; y++)
            {
                if (x == 0 | x % 4 == 0 && y % 4 == 0)
                {
                    Location c1 = new Location(pos1.getWorld(), pos1.getX() + x, pos1.getY() + y, pos1.getZ());
                    player.spawnParticle(Particle.CLOUD, c1, 0, 0.0, 0.0, 0.0);
        
                    int czs = z_shift;
                    if (z_shift == -1) czs = 0;
                    Location c2 = new Location(pos1.getWorld(), pos1.getX() + x, pos1.getY() + y, pos2.getZ() + czs);
                    player.spawnParticle(Particle.CLOUD, c2, 0, 0.0, 0.0, 0.0);
                }
            }
        }
    
        for (int z = 0; Math.abs(z) < Math.floor(Math.abs(pos1.getZ() - pos2.getZ())) +2; z += z_shift)
        {
            for (int y = -50; y < 100; y++)
            {
                if (z == 0 | z % 4 == 0 && y % 4 == 0)
                {
                    Location c1 = new Location(pos1.getWorld(), pos1.getX(), pos1.getY() +y, pos1.getZ() + z);
                    player.spawnParticle(Particle.CLOUD, c1, 0, 0.0, 0.0, 0.0);
        
                    int cxs = x_shift;
                    if (x_shift == -1) cxs = 0;
                    Location c2 = new Location(pos1.getWorld(), pos2.getX() + cxs, pos1.getY() +y, pos1.getZ() + z);
                    player.spawnParticle(Particle.CLOUD, c2, 0, 0.0, 0.0, 0.0);
                }
            }
        }
    
        //500-510 = -10 | 500 -10 = 490
    }
    
    public Location rayCastPos()
    {
        RayTraceResult rtr = player.rayTraceBlocks(100);
        return rtr.getHitBlock().getLocation();
    }
    
    @Override
    public void run()
    {
        int x = 0;
        while (running)
        {
            try
            {
                Thread.sleep(250);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            x++;
            this.resetLastGhostBlocks();
            this.placeSelectorGhostBlocks();
            if (x % 4 == 0) this.placeEdgeParticles();
        }
    }
}
