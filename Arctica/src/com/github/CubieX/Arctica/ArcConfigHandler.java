package com.github.CubieX.Arctica;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ArcConfigHandler
{
    private final Arctica plugin;
    private final Logger log;
    
    private FileConfiguration config;

    //Constructor
    public ArcConfigHandler(Arctica plugin, Logger log)
    {        
        this.plugin = plugin;
        this.log = log;

        initConfig();        
    }

    private void initConfig()
    {
        plugin.saveDefaultConfig(); //creates a copy of the provided config.yml in the plugins data folder, if it does not exist
        config = plugin.getConfig(); //re-reads config out of memory. (Reads the config from file only, when invoked the first time!)        
    }

    private void saveConfig() //saves the config to disc (needed when entries have been altered via the plugin in-game)
    {
        // get and set values here!
        plugin.saveConfig();   
    }

    //reloads the config from disc (used if user made manual changes to the config.yml file)
    public void reloadConfig(CommandSender sender)
    {
        plugin.reloadConfig();
        config = plugin.getConfig(); // new assignment neccessary when returned value is assigned to a variable or static field(!)

        sender.sendMessage("[" + ChatColor.AQUA + "Info" + ChatColor.WHITE + "] " + ChatColor.AQUA + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion() + " reloaded!");       
    } 
}
