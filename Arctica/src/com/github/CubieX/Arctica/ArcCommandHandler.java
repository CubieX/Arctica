package com.github.CubieX.Arctica;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArcCommandHandler implements CommandExecutor
{
    private final Arctica plugin;
    private final ArcConfigHandler cHandler;
    private final Logger log;

    public ArcCommandHandler(Arctica plugin, Logger log, ArcConfigHandler cHandler)
    {
        this.plugin = plugin;
        this.cHandler = cHandler;
        this.log = log;
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
                        sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to reload " + plugin.getDescription().getName() + "!");
                    }
                }                
                return true;
            }

            if (args.length==2)
            {
                if (args[0].equalsIgnoreCase(".....")) // show the status of the given player
                {            
                    if(sender.hasPermission("........"))
                    {
                        // TODO show status of given player  

                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to see the status of " + plugin.getDescription().getName() + "!");
                    }
                }
            }

            if (args.length==3)
            {
                if (args[0].equalsIgnoreCase("....")) // take away days for the promoted rank for the player
                {                   
                    if(sender.hasPermission(".........."))
                    {
                        // TODO                            
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to manage ranks!");
                    }                   
                }
                return true;
            }            
            else
            {
                sender.sendMessage(ChatColor.YELLOW + plugin.logPrefix + "Falsche Anzahl an Parametern.");
            }  
        }         
        return false; // No valid parameter count. If false is returned, the help for the command stated in the plugin.yml will be displayed to the player
    }
}
