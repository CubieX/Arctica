package com.github.CubieX.Arctica;

import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

public class ArcSchedulerHandler
{
    private Arctica plugin = null;

    public ArcSchedulerHandler(Arctica plugin)
    {
        this.plugin = plugin;
    }

    public void startCleanupScheduler_SyncRep()
    {      
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
        {
            public void run()
            {
               //check for all players if they are in snow biome
               try
               {
                   Player[] onlinePlayerList = plugin.getServer().getOnlinePlayers();
                   
                   for(int i = 0; i < onlinePlayerList.length; i++)
                   {
                       Player currPlayer = onlinePlayerList[i];                       
                       String currPlayersBiomeStr = currPlayer.getWorld().getBiome((int)currPlayer.getLocation().getX(), (int)currPlayer.getLocation().getZ()).toString();
                       Biome currPlayersBiome = currPlayer.getWorld().getBiome((int)currPlayer.getLocation().getX(), (int)currPlayer.getLocation().getZ());
                       currPlayer.sendMessage(Arctica.logPrefix + ChatColor.AQUA + "Aktuelles Biom: " + currPlayersBiomeStr);   
                       
                       if((currPlayersBiome == Biome.FROZEN_OCEAN) ||
                               (currPlayersBiome == Biome.FROZEN_RIVER) ||
                               (currPlayersBiome == Biome.ICE_MOUNTAINS) ||
                               (currPlayersBiome == Biome.ICE_PLAINS) ||
                               (currPlayersBiome == Biome.TAIGA) ||
                               (currPlayersBiome == Biome.TAIGA_HILLS))                               
                       {
                           currPlayer.damage(1);
                           currPlayer.sendMessage(ChatColor.AQUA + "Du frierst.");
                       }
                   }
               }
               catch(Exception ex)
               {
                   // player probably no longer online
               }                
            }
        }, (20*20L), 20*10L); // 20 sec delay, 10 sec period        
    }
}
