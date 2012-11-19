package com.github.CubieX.Arctica;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Arctica extends JavaPlugin
{
    private ArcConfigHandler cHandler = null;
    private ArcEntityListener eListener = null;
    private ArcSchedulerHandler schedHandler = null;
    private ArcCommandHandler comHandler = null;

    static final Logger log = Logger.getLogger("Minecraft");
    static String logPrefix = "[Arctica] "; // Prefix to go in front of all log entries
    static boolean safemode = false;
    static int dps_air = 0;
    static int dps_water = 0;

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

        readConfigValues();
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

    public void readConfigValues()
    {
        if(this.getConfig().getString("safemode").equalsIgnoreCase("true"))
        {
            safemode = true;
        }
        else        
        {
            safemode = false;
        }

        dps_air = this.getConfig().getInt("dps_air");
        if(dps_air > 20) dps_air = 20;

        dps_water = this.getConfig().getInt("dps_water");
        if(dps_water > 20) dps_water = 20;
    }

    // Calculates the factor for damage reduction from players worn armor
    public double getDamageReduceFactor(Player player)
    {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        ItemStack boots = inv.getBoots();
        ItemStack helmet = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack pants = inv.getLeggings();
        double red = 0.0;
        if(helmet.getType() == Material.LEATHER_HELMET)red = red + 0.04;
        else if(helmet.getType() == Material.GOLD_HELMET)red = red + 0.08;
        else if(helmet.getType() == Material.CHAINMAIL_HELMET)red = red + 0.08;
        else if(helmet.getType() == Material.IRON_HELMET)red = red + 0.08;
        else if(helmet.getType() == Material.DIAMOND_HELMET)red = red + 0.12;
        //
        if(boots.getType() == Material.LEATHER_BOOTS)red = red + 0.04;
        else if(boots.getType() == Material.GOLD_BOOTS)red = red + 0.04;
        else if(boots.getType() == Material.CHAINMAIL_BOOTS)red = red + 0.04;
        else if(boots.getType() == Material.IRON_BOOTS)red = red + 0.08;
        else if(boots.getType() == Material.DIAMOND_BOOTS)red = red + 0.12;
        //
        if(pants.getType() == Material.LEATHER_LEGGINGS)red = red + 0.08;
        else if(pants.getType() == Material.GOLD_LEGGINGS)red = red + 0.12;
        else if(pants.getType() == Material.CHAINMAIL_LEGGINGS)red = red + 0.16;
        else if(pants.getType() == Material.IRON_LEGGINGS)red = red + 0.20;
        else if(pants.getType() == Material.DIAMOND_LEGGINGS)red = red + 0.24;
        //
        if(chest.getType() == Material.LEATHER_CHESTPLATE)red = red + 0.12;
        else if(chest.getType() == Material.GOLD_CHESTPLATE)red = red + 0.20;
        else if(chest.getType() == Material.CHAINMAIL_CHESTPLATE)red = red + 0.20;
        else if(chest.getType() == Material.IRON_CHESTPLATE)red = red + 0.24;
        else if(chest.getType() == Material.DIAMOND_CHESTPLATE)red = red + 0.32;
        
        return red;
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
