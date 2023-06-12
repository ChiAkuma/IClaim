package de.chiakuma.gprev;

import de.chiakuma.gprev.commands.CMD_Claim;
import de.chiakuma.gprev.g_events.AntiGriefEvents;
import de.chiakuma.gprev.g_events.ConnectionEvent;
import de.chiakuma.gprev.utils.DatabaseTools;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class GriefPrevention extends JavaPlugin
{
    //================================================================================
    // Plugin global variables
    //================================================================================
    public static JavaPlugin PLUGIN;
    public static ITEM_Wand iWand = new ITEM_Wand();
    public static DatabaseTools DATABASE;

    //================================================================================
    // Per Player Data
    //================================================================================
    public static HashMap<UUID, PlayerData> perPlayerInstances = new HashMap<>();
    
    
    @Override
    public void onLoad()
    {
        PLUGIN = this;
    }

    @Override
    public void onEnable()
    {
        // Plugin startup logic
        //Reading / creating config
        //TODO
        //SQL Connection
        DATABASE = new DatabaseTools("mariadb", "12.5.0.2", 3306, "minecraft", "minecraft", "minecraft");
        
        //Events
        getServer().getPluginManager().registerEvents(new ITEM_Wand(), this);
        getServer().getPluginManager().registerEvents(new ConnectionEvent(), this);
        getServer().getPluginManager().registerEvents(new AntiGriefEvents(), this);


        //Commands
        //this.getCommand("iclaim").setExecutor(new CMD_IClaim());
        this.getCommand("claim").setExecutor(new CMD_Claim());

    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic

    }
}
