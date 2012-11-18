package com.github.CubieX.Arctica;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class Arctica extends JavaPlugin
{
    private ArcConfigHandler cHandler = null;
    private ArcEntityListener eListener = null;
    private ArcSchedulerHandler schedHandler = null;
    private ArcCommandHandler comHandler = null;

    private static final Logger log = Logger.getLogger("Minecraft");
    public static String logPrefix = "[Arctica] "; // Prefix to go in front of all log entries
  
    //************************************************
    static String usedConfigVersion = "1"; // Update this every time the config file version changes, so the plugin knows, if there is a suiting config present
    //************************************************
    
    @Override
    public void onEnable()
    {
        cHandler = new ArcConfigHandler(this, log);       
        
        if(!checkConfigFileVersion())
        {
            log.severe(logPrefix + "Outdated or corrupted config file. Please delete your current config file, so Assignment can create a new one!");
            log.severe(logPrefix + "will be disabled now. Config file is outdated or corrupted.");
            disablePlugin();
            return;
        }
        
        if (!hookToPermissionSystem())
        {
            log.info(String.format("[%s] - Disabled due to no superperms compatible permission system found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }        

        log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is enabled!");
        
        comHandler = new ArcCommandHandler(this, log, cHandler);
        getCommand("arc").setExecutor(comHandler);
        schedHandler = new ArcSchedulerHandler(this);
        eListener = new ArcEntityListener(this,log);  
        
        schedHandler.startCleanupScheduler_SyncRep();
    }

    private boolean hookToPermissionSystem()
    {
        if ((getServer().getPluginManager().getPlugin("PermissionsEx") == null) &&
                (getServer().getPluginManager().getPlugin("bPermissions") == null) &&
                (getServer().getPluginManager().getPlugin("zPermissions") == null) &&
                (getServer().getPluginManager().getPlugin("PermissionsBukkit") == null))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    private boolean checkConfigFileVersion()
    {
        boolean res = false;

        if(this.getConfig().isSet("config_version"))
        {
            String configVersion = this.getConfig().getString("config_version");

            if(configVersion.equals(usedConfigVersion))
            {
                res = true;
            }  
        }

        return (res);
    }
    
    void disablePlugin()
    {
        getServer().getPluginManager().disablePlugin(this);        
    }
        
    @Override
    public void onDisable()
    {               
        eListener = null;
        cHandler = null;       
        schedHandler = null;
        comHandler = null;
        log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
    }   
}
