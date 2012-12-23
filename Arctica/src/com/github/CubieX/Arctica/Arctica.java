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
    static final String logPrefix = "[Arctica] "; // Prefix to go in front of all log entries
    static boolean debug = false;
    static boolean safemode = false;
    
    static int damageApplyPeriod = 10; // seconds to apply the cold damage (cyclic)
    static double baseDamageInAir = 0.0;
    static double extraDamageInAirWhenOutside = 0.0;
    static double baseDamageInWater = 0.0;    
    static double extraDamageInWaterWhenOutside = 0.0;
    static final double warmthBonusFactor = 0.7; // a factor of 0.7 means, damage taken from cold will be reduced by 70%.
    static final double torchBonusFactor = 0.25; // bonus when holding a torch. Reduces cold damage by 25%.
    
    static final int checkRadius = 20; // how far should the plugin check for crafted blocks? (used for "Player is outside" check)     
    static final int maxMapHeight = 255; // TOP check will fail above this height
    static final int horizontalWarmBlockSearchRadius = 5;
    static final int verticalWarmBlockSearchRadius = 3;
    
    // TODO make configurable??
    
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

        comHandler = new ArcCommandHandler(this, log, cHandler);
        getCommand("arc").setExecutor(comHandler);
        schedHandler = new ArcSchedulerHandler(this);
        eListener = new ArcEntityListener(this,log);  

        readConfigValues();

        schedHandler.startColdDamageScheduler_SyncRep();        

        log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is enabled!");
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
        debug = this.getConfig().getBoolean("debug");
        
        safemode = this.getConfig().getBoolean("safemode");

        damageApplyPeriod = this.getConfig().getInt("damageApplyPeriod");
        if(Arctica.damageApplyPeriod > 60) damageApplyPeriod = 60;
        if(Arctica.damageApplyPeriod < 5) damageApplyPeriod = 5;

        baseDamageInAir = (double)this.getConfig().getInt("baseDamageInAir");
        if(Arctica.baseDamageInAir > 20) baseDamageInAir = 20;
        if(Arctica.baseDamageInAir < 0) baseDamageInAir = 0;
        
        baseDamageInWater = (double)this.getConfig().getInt("baseDamageInWater");
        if(baseDamageInWater > 20) baseDamageInWater = 20;
        if(baseDamageInWater < 0) baseDamageInWater = 0;

        extraDamageInAirWhenOutside = (double)this.getConfig().getInt("extraDamageInAirWhenOutside");
        if(extraDamageInAirWhenOutside > 20) extraDamageInAirWhenOutside = 20;
        if(extraDamageInAirWhenOutside < 0) extraDamageInAirWhenOutside = 0;

        extraDamageInWaterWhenOutside = (double)this.getConfig().getInt("extraDamageInWaterWhenOutside");
        if(extraDamageInWaterWhenOutside > 20) extraDamageInWaterWhenOutside = 20;
        if(extraDamageInWaterWhenOutside < 0) extraDamageInWaterWhenOutside = 0;
    }

    // Calculates the factor for damage reduction from players worn armor. Max. 0.8 = 80% DamRed
    public double getDamageReduceFactorFromCloth(Player player)
    {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        ItemStack boots = inv.getBoots();
        ItemStack helmet = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack pants = inv.getLeggings();
        double red = 0.0;

        if(null != helmet)
        {
            if(helmet.getType() == Material.LEATHER_HELMET)red = red + 0.04;
            else if(helmet.getType() == Material.GOLD_HELMET)red = red + 0.08;
            else if(helmet.getType() == Material.CHAINMAIL_HELMET)red = red + 0.08;
            else if(helmet.getType() == Material.IRON_HELMET)red = red + 0.08;
            else if(helmet.getType() == Material.DIAMOND_HELMET)red = red + 0.12;
        }       

        if(null != boots)
        {
            if(boots.getType() == Material.LEATHER_BOOTS)red = red + 0.04;
            else if(boots.getType() == Material.GOLD_BOOTS)red = red + 0.04;
            else if(boots.getType() == Material.CHAINMAIL_BOOTS)red = red + 0.04;
            else if(boots.getType() == Material.IRON_BOOTS)red = red + 0.08;
            else if(boots.getType() == Material.DIAMOND_BOOTS)red = red + 0.12;
        }

        if(null != pants)
        {
            if(pants.getType() == Material.LEATHER_LEGGINGS)red = red + 0.08;
            else if(pants.getType() == Material.GOLD_LEGGINGS)red = red + 0.12;
            else if(pants.getType() == Material.CHAINMAIL_LEGGINGS)red = red + 0.16;
            else if(pants.getType() == Material.IRON_LEGGINGS)red = red + 0.20;
            else if(pants.getType() == Material.DIAMOND_LEGGINGS)red = red + 0.24;
        }
        if(null != chest)
        {
            if(chest.getType() == Material.LEATHER_CHESTPLATE)red = red + 0.12;
            else if(chest.getType() == Material.GOLD_CHESTPLATE)red = red + 0.20;
            else if(chest.getType() == Material.CHAINMAIL_CHESTPLATE)red = red + 0.20;
            else if(chest.getType() == Material.IRON_CHESTPLATE)red = red + 0.24;
            else if(chest.getType() == Material.DIAMOND_CHESTPLATE)red = red + 0.32;
        }
        return red;
    }
    
    public boolean playerIsHoldingTorch(String playerName)
    {
        return (eListener.playerIsHoldingTorch(playerName));
    }

    void disablePlugin()
    {
        getServer().getPluginManager().disablePlugin(this);        
    }

    @Override
    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);
        eListener = null;
        cHandler = null;       
        schedHandler = null;
        comHandler = null;
        log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
    }   
}
