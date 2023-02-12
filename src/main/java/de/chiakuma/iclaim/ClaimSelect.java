package de.chiakuma.iclaim;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.Objects;

public class ClaimSelect implements Runnable
{
    Player player;
    Location pos1, last_pos1;
    Location pos2, last_pos2;
    
    boolean running = true;
    
    /**
     * Constructor
     * @param player the player the selector is tied to
     */
    public ClaimSelect(Player player)
    {
        this.player = player;
    }
    
    /**
     *
     * @param p1_p2 Selects if Pos1 | Pos2 will be set
     * @param loc Location to be set
     */
    public void setLocation(int p1_p2, Location loc)
    {
        //Resets all Ghost Blocks
        this.resetGhostBlocks(pos1, pos2);
        if (p1_p2 > 3) return;
        if (p1_p2 == 1)
        {
            //saves last location and then updates position
            this.last_pos1 = this.pos1;
            this.pos1 = loc;
        }
        if (p1_p2 == 2)
        {
            //saves last location and then updates position
            this.last_pos2 = this.pos2;
            this.pos2 = loc;
        }
    }
    
    /**
     * Resets all ghost blocks on the locations of last positions
     */
    public void resetLastGhostBlocks()
    {
        //Calls resetGhostBlocks with last position variables
        this.resetGhostBlocks(last_pos1, last_pos2);
    }
    
    /**
     * Resets Ghost blocks on locations
     * Resets 5 Blocks around the position to the material the server knows
     * @param pos1 Location 1
     * @param pos2 Location 2
     */
    public void resetGhostBlocks(Location pos1, Location pos2)
    {
        for (int x = 0; x < 10; x++)
        {
            for (int z = 0; z < 10; z++)
            {
                if (pos1 != null)
                {
                    //Location with offset -5 | -5 +10 = +5
                    Location xpos = new Location(player.getWorld(), (pos1.getX() -5) + x, pos1.getY(), (pos1.getZ() -5) + z);
                    placeSelectorGhostBlock(xpos, xpos.getBlock().getType());
                }
                if (pos2 != null)
                {
                    //Location with offset -5 | -5 +10 = +5
                    Location zpos = new Location(player.getWorld(), (pos2.getX() - 5) + x, pos2.getY(), (pos2.getZ() - 5) + z);
                    placeSelectorGhostBlock(zpos, zpos.getBlock().getType());
                }
                
            
            }
        }
    }
    
    /**
     * places one ghost block
     * @param pos Location to set the ghost block
     * @param mat Material to be set
     */
    public void placeSelectorGhostBlock(Location pos, Material mat)
    {
        BlockData bd = Bukkit.createBlockData(mat);
        //Sending player ghost blocks
        player.sendBlockChange(pos, bd);
    }
    
    /**
     * Places ghost blocks in their respective formation<br />
     * # = Glowstone | + = GoldBlock | . = AIR<br />
     * <br />
     * Places diamond block if just one selector (pos1, pos2) is set<br />
     * <br />
     * this is the format of the gold, glowstone blocks<br />
     * Pos1<br />
     * |#+..........|<br />
     * |+...........|<br />
     * |............|<br />
     * |...........+|<br />
     * |..........+#| Pos2
     */
    public void placeSelectorGhostBlocks()
    {
        //Sets Diamond blocks on the positions if the`re not null
        if (pos1 != null) this.placeSelectorGhostBlock(pos1, Material.DIAMOND_BLOCK);
        if (pos2 != null) this.placeSelectorGhostBlock(pos2, Material.DIAMOND_BLOCK);
        //Sets Gold and Glowstone in the formation shown above in every rotation
        //only if both positions are set (replaces the diamond blocks in the process)
        if (pos1 != null && pos2 != null)
        {
            //Finds the direction for X where the gold block should be
            int x_shift = 0;
            if (pos1.getX() < pos2.getX()) x_shift = 1;
            else if (pos1.getX() > pos2.getX()) x_shift = -1;
    
            //places the right gold block for pos1
            Location x1 = pos1.clone();
            x1.setX(pos1.getX() + x_shift);
            this.placeSelectorGhostBlock(x1, Material.GOLD_BLOCK);
            
            //places gold block on the left for pos2
            Location x2 = pos2.clone();
            x2.setX(pos2.getX() - x_shift);
            this.placeSelectorGhostBlock(x2, Material.GOLD_BLOCK);
            
            //Finds the direction for the Z axis
            int z_shift = 0;
            if (pos1.getZ() < pos2.getZ()) z_shift = 1;
            else if (pos1.getZ() > pos2.getZ()) z_shift = -1;
            
            //places the bottom gold block for pos1
            Location z1 = pos1.clone();
            z1.setZ(pos1.getZ() + z_shift);
            this.placeSelectorGhostBlock(z1, Material.GOLD_BLOCK);
            
            //places the top gold block for pos2
            Location z2 = pos2.clone();
            z2.setZ(pos2.getZ() - z_shift);
            this.placeSelectorGhostBlock(z2, Material.GOLD_BLOCK);
            
            //Sets the Glowstone blocks on the position itself
            //pos1
            this.placeSelectorGhostBlock(pos1, Material.GLOWSTONE);
            //pos2
            this.placeSelectorGhostBlock(pos2, Material.GLOWSTONE);
        }
    }
    
    /**
     * Places all the particles for the selector
     */
    public void placeEdgeParticles()
    {
        //The Particles are on the side of the block and needs some shifting
        //This will get the shifting directions where we correct the particle position
        int x_shift = 0, z_shift = 0;
        if (pos1 != null && pos2 != null)
        {
            if (pos1.getX() < pos2.getX()) x_shift = 1;
            else if (pos1.getX() > pos2.getX()) x_shift = -1;
            if (pos1.getZ() < pos2.getZ()) z_shift = 1;
            else if (pos1.getZ() > pos2.getZ()) z_shift = -1;
        }
        
        //If no shifting position is found the shifting variables are not set, so we abort and break here
        if (x_shift == 0 | z_shift == 0) return;
        //Loops through the blocks between the selection points and sets the particles
        //For x axis. So 2 Walls
        //pos1 x = 10 | pos2 x = 30
        //:(x now is 5; 5 < 22; x += direction of the xshift)
        //So it will loop 22 times through and places particles
        //Edge case: pos1 x = 30 | pos2 x = 10 ||| Is also 20 ;)
        for (int x = 0; Math.abs(x) < Math.floor(Math.abs(pos1.getX() - pos2.getX())) +2; x += x_shift)
        {
            //Loops the height through and set particles
            //Shifts the loop start to -50 so 50 blocks down. And places particles to 100 blocks up, so 150 total
            for (int y = -50; y < 100; y++)
            {
                //When x is 0, so at the start it will place a particle. On every 4th x-axis block and every 4th height axis block
                if (x == 0 | x % 4 == 0 && y % 4 == 0)
                {
                    //Places particle with x and y added
                    Location c1 = new Location(pos1.getWorld(), pos1.getX() + x, pos1.getY() + y, pos1.getZ());
                    player.spawnParticle(Particle.CLOUD, c1, 0, 0.0, 0.0, 0.0);
                    
                    //Places particle with x, y and z shift added
                    int czs = z_shift;
                    if (z_shift == -1) czs = 0;
                    Location c2 = new Location(pos1.getWorld(), pos1.getX() + x, pos1.getY() + y, pos2.getZ() + czs);
                    player.spawnParticle(Particle.CLOUD, c2, 0, 0.0, 0.0, 0.0);
                }
            }
        }
    
        //Loops through the blocks between the selection points and sets the particles for the z axis. The last 2 walls
        for (int z = 0; Math.abs(z) < Math.floor(Math.abs(pos1.getZ() - pos2.getZ())) +2; z += z_shift)
        {
            //Loops the height through and set particles
            //Shifts the loop start to -50 so 50 blocks down. And places particles to 100 blocks up, so 150 total
            for (int y = -50; y < 100; y++)
            {
                //When z is 0, so at the start it will place a particle. On every 4th z-axis block and every 4th height axis block
                if (z == 0 | z % 4 == 0 && y % 4 == 0)
                {
                    //Places particle with y and z added
                    Location c1 = new Location(pos1.getWorld(), pos1.getX(), pos1.getY() +y, pos1.getZ() + z);
                    player.spawnParticle(Particle.CLOUD, c1, 0, 0.0, 0.0, 0.0);
    
                    //Places particle with x shift, y and z added
                    int cxs = x_shift;
                    if (x_shift == -1) cxs = 0;
                    Location c2 = new Location(pos1.getWorld(), pos2.getX() + cxs, pos1.getY() +y, pos1.getZ() + z);
                    player.spawnParticle(Particle.CLOUD, c2, 0, 0.0, 0.0, 0.0);
                }
            }
        }
    
        //500-510 = -10 | 500 -10 = 490
    }
    
    /**
     * Gets a ray traced Location
     * @return -"-
     */
    public Location rayCastPos()
    {
        RayTraceResult rtr = player.rayTraceBlocks(100);
        assert rtr != null;
        return Objects.requireNonNull(rtr.getHitBlock()).getLocation();
    }
    
    //Thread to run in background
    //Adds and removes all the ghost blocks
    @Override
    public void run()
    {
        //Keep a count for later usage
        int x = 0;
        while (running)
        {
            try
            {
                //250 milliseconds pause. So 4 times a second
                Thread.sleep(250);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            x++;
            
            //Resets ghost blocks on the last position variables
            this.resetLastGhostBlocks();
            //Places ghost blocks in their respective formation
            this.placeSelectorGhostBlocks();
            //And finally spawns particles every second
            if (x % 4 == 0) this.placeEdgeParticles();
        }
    }
}
