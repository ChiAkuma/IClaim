package de.chiakuma.iclaim;

import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerData implements Runnable
{
    //================================================================================
    //region Per Player Data:
    //================================================================================
    public UUID currentClaimEdit = null;
    public int currentSelectMode = 0;
    //endregion
    //================================================================================

    //================================================================================
    //region Thread specific variables
    //================================================================================
    Thread thread = new Thread(this);
    UUID threadRandomID;
    boolean isRunning = false;
    boolean stopSign = false;
    //endregion
    //================================================================================

    public PlayerData(Player player)
    {
        //Register this new PlayerData
        Plugin_IClaim.perPlayerInstances.putIfAbsent(player.getUniqueId(), this);
        //Defining the Thread
        this.threadRandomID = UUID.randomUUID();
        this.thread.setName("PlayerData_" + threadRandomID);
        //Starting the Thread
        //TODO: use thread if needed
        //this.thread.start();
    }

    public void restartThread()
    {
        this.stopSign = true;
        if (!this.isRunning) this.thread.start();
    }

    public void startThread()
    {
        if (!this.isRunning) this.thread.start();
    }

    public void closeThread()
    {
        this.stopSign = true;
    }

    public void killThread()
    {
        this.stopSign = true;
        this.thread.interrupt();
    }

    @Override
    public void run()
    {
        this.isRunning = true;
        while (!this.stopSign)
        {
            //Good sleep amount for better controlling performance
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) { }
            //Some processing things for every Player
            //TODO: if needed
            //TODO: yes | no abfrage hier zeitlich begrenzen (Timer für abfragen)
        }
        this.isRunning = false;
    }
}
