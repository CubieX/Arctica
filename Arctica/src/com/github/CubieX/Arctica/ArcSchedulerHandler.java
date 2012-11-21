package com.github.CubieX.Arctica;

import org.bukkit.Material;
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
                        if((currPlayer.hasPermission("arctica.use")) &&
                                (!currPlayer.hasPermission("arctica.immune")))
                        {
                            //String currPlayersBiomeStr = currPlayer.getWorld().getBiome((int)currPlayer.getLocation().getX(), (int)currPlayer.getLocation().getZ()).toString();
                            Biome currPlayersBiome = currPlayer.getWorld().getBiome((int)currPlayer.getLocation().getX(), (int)currPlayer.getLocation().getZ());
                            //currPlayer.sendMessage(Arctica.logPrefix + ChatColor.AQUA + "Aktuelles Biom: " + currPlayersBiomeStr);   

                            if((currPlayersBiome == Biome.FROZEN_OCEAN) ||
                                    (currPlayersBiome == Biome.FROZEN_RIVER) ||
                                    (currPlayersBiome == Biome.ICE_MOUNTAINS) ||
                                    (currPlayersBiome == Biome.ICE_PLAINS) ||
                                    (currPlayersBiome == Biome.TAIGA) ||
                                    (currPlayersBiome == Biome.TAIGA_HILLS))                               
                            {   
                                int realDamageToApply = 0;
                                
                                // check if player is currently in water
                                Material mat = currPlayer.getLocation().getBlock().getType();
                                if (mat == Material.STATIONARY_WATER || mat == Material.WATER)
                                {
                                    // integer damage is rounded up. So minimum Damage is always 1 if dps is > 0.
                                    realDamageToApply = (int)Math.ceil((Arctica.dps_water * (1.0 - plugin.getDamageReduceFactor(currPlayer))));
                                }
                                else
                                {
                                    // integer damage is rounded up. So minimum Damage is always 1 if dps is > 0.
                                    realDamageToApply = (int)Math.ceil((Arctica.dps_air * (1.0 - plugin.getDamageReduceFactor(currPlayer))));
                                }

                                // fire custom damage event ================================================                                
                                ColdDamageEvent cdEvent = new ColdDamageEvent(currPlayer, realDamageToApply); // Create the event                                
                                plugin.getServer().getPluginManager().callEvent(cdEvent); // fire Event         
                                //==========================================================================
                                //currPlayer.sendMessage(ChatColor.AQUA + "Du frierst.");
                            }
                        }
                    }
                }
                catch(Exception ex)
                {
                    // player probably no longer online
                }                
            }
        }, (20*20L), 20*10L); // 20 sec delay, 1 sec period        
    }
}
