package de.chiakuma.iclaim;

import de.chiakuma.iclaim.commands.CMD_IClaim;
import de.chiakuma.iclaim.g_events.ConnectionEvent;
import de.chiakuma.iclaim.utils.DatabaseTools;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public final class IClaim extends JavaPlugin
{

    public static JavaPlugin PLUGIN;
    public static IClaimWand iWand = new IClaimWand();
    public static DatabaseTools database;
    
    public static HashMap<UUID, ClaimSelect> perPlayerInstances = new HashMap<>();
    
    
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
        database = new DatabaseTools("mariadb", "12.5.0.2", 3306, "minecraft", "minecraft", "minecraft");
        
        //Events
        getServer().getPluginManager().registerEvents(new IClaimWand(), this);
        getServer().getPluginManager().registerEvents(new ConnectionEvent(), this);
        
        //Commands
        this.getCommand("iclaim").setExecutor(new CMD_IClaim());

    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic

    }
}
