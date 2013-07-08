package com.github.CubieX.Arctica;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ArcConfigHandler
{
   private final Arctica plugin;    
   private FileConfiguration fireListCfg = null;
   private File fireListFile = null;
   private final String fireListFileName = "fireList.yml";

   //Constructor
   public ArcConfigHandler(Arctica plugin)
   {        
      this.plugin = plugin;

      initConfig();        
   }

   private void initConfig()
   {
      plugin.saveDefaultConfig(); //creates a copy of the provided config.yml in the plugins data folder, if it does not exist
      plugin.getConfig(); //re-reads config out of memory. (Reads the config from file only, when invoked the first time!)

      // fire list
      reloadFireListFile(); // load file from disk and create objects
      saveFireListDefaultFile(); // creates a copy of the provided fireList.yml
      reloadFireListFile(); // load file again after it is physically present now
   }

   /*private void saveConfig() //saves the config to disc (needed when entries have been altered via the plugin in-game)
   {
      // get and set values here!
      plugin.saveConfig();
   }*/

   //reloads the config from disc (used if user made manual changes to the config.yml file)
   public void reloadConfig(CommandSender sender)
   {
      plugin.reloadConfig();
      plugin.getConfig(); // new assignment necessary when returned value is assigned to a variable or static field(!)

      reloadFireListFile();

      plugin.readConfigValues();

      sender.sendMessage("[" + ChatColor.GREEN + "Info" + ChatColor.WHITE + "] " + ChatColor.GREEN + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion() + " reloaded!");       
   }

   // =========================
   // fireList file handling
   // =========================

   // reload from disk
   public void reloadFireListFile()
   {
      if (fireListFile == null)
      {
         fireListFile = new File(plugin.getDataFolder(), fireListFileName);
      }
      fireListCfg = YamlConfiguration.loadConfiguration(fireListFile);

      // Look for defaults in the jar
      InputStream defConfigStream = plugin.getResource(fireListFileName);
      if (defConfigStream != null)
      {
         YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
         fireListCfg.setDefaults(defConfig);
      }
   }

   // reload config and return it
   public FileConfiguration getFireListFile()
   {
      if (fireListCfg == null)
      {
         this.reloadFireListFile();
      }
      return fireListCfg;
   }

   //save config
   public void saveFireListFile()
   {
      if (fireListCfg == null || fireListFile == null)
      {
         return;
      }
      try
      {
         getFireListFile().save(fireListFile);
      }
      catch (IOException ex)
      {
         Arctica.log.severe("Could not save data to " + fireListFile.getName());
         Arctica.log.severe(ex.getMessage());
      }
   }

   // safe a default config if there is no file present
   public void saveFireListDefaultFile()
   {
      if (!fireListFile.exists())
      {            
         plugin.saveResource(fireListFileName, false);
      }
   }
}
