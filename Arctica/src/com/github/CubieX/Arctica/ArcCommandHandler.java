package com.github.CubieX.Arctica;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArcCommandHandler implements CommandExecutor
{
    private final Arctica plugin;
    private final ArcConfigHandler cHandler;

    public ArcCommandHandler(Arctica plugin, ArcConfigHandler cHandler)
    {
        this.plugin = plugin;
        this.cHandler = cHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = null;
        if (sender instanceof Player) 
        {
            player = (Player) sender;
        }

        if (cmd.getName().equalsIgnoreCase("arc"))
        { // If the player typed /arc then do the following... (can be run from console also)
            if (args.length == 0)
            { //no arguments, so help will be displayed
                return false;
            }
            if (args.length==1)
            {
                if (args[0].equalsIgnoreCase("version")) // show the current version of the plugin
                {            
                    sender.sendMessage(ChatColor.AQUA + "This server is running " + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion());
                }    

                if (args[0].equalsIgnoreCase("reload")) // reload the plugins config and playerfile
                {            
                    if(sender.hasPermission("arctica.admin"))
                    {
                        cHandler.reloadConfig(sender);
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to reload " + plugin.getDescription().getName() + "!");
                    }
                } 

                if (args[0].equalsIgnoreCase("status")) // shows the most important config options
                {            
                    if(sender.hasPermission("arctica.use"))
                    {     
                        
                        if (null != sender)
                        {
                            sender.sendMessage(Arctica.logPrefix + ChatColor.AQUA + "Debug-Info-Mode: " + ChatColor.WHITE + Arctica.debug);
                            sender.sendMessage(Arctica.logPrefix + ChatColor.AQUA + "Safe Mode: " + ChatColor.WHITE + Arctica.safemode);                             
                            
                            if(null != player)
                            {
                                player.sendMessage(Arctica.logPrefix + ChatColor.AQUA + "Biome: " + ChatColor.WHITE + player.getWorld().getBiome((int)player.getLocation().getX(), (int)player.getLocation().getZ()).toString());
                            }
                        }
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to show " + plugin.getDescription().getName() + " status!");
                    }
                } 
                return true;
            }

            if (args.length==2)
            {
                if (args[0].equalsIgnoreCase(".....")) // ..................
                {            
                    if(sender.hasPermission("........"))
                    {
                        

                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to see the status of " + plugin.getDescription().getName() + "!");
                    }
                }
            }

            if (args.length==3)
            {
                if (args[0].equalsIgnoreCase("....")) // ...............
                {                   
                    if(sender.hasPermission(".........."))
                    {
                        // TODO                            
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to manage ranks!");
                    }                   
                }
                return true;
            }            
            else
            {
                sender.sendMessage(ChatColor.YELLOW + Arctica.logPrefix + "Falsche Anzahl an Parametern.");
            }  
        }         
        return false; // No valid parameter count. If false is returned, the help for the command stated in the plugin.yml will be displayed to the player
    }
}
