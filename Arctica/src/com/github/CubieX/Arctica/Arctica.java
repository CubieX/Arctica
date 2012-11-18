package com.github.CubieX.Arctica;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class Arctica extends JavaPlugin
{
    private ArcConfigHandler cHandler = null;
    private ArcEntityListener eListener = null;
    private ArcCommandHandler comHandler = null;

    private static final Logger log = Logger.getLogger("Minecraft");
    public static String logPrefix = "[Arctica] "; // Prefix to go in front of all log entries
  
    @Override
    public void onEnable()
    {
        if (!hookToPermissionSystem())
        {
            log.info(String.format("[%s] - Disabled due to no superperms compatible permission system found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }        

        log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is enabled!");

        cHandler = new ArcConfigHandler(this, log);       
        comHandler = new ArcCommandHandler(this, log, cHandler);
        getCommand("arc").setExecutor(comHandler);
        eListener = new ArcEntityListener(this,log);  
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
        
    @Override
    public void onDisable()
    {       
        cHandler = null;       
        comHandler = null;
        eListener = null;
        log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
    }   
}
