package com.github.CubieX.Arctica;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class ArcEntityListener implements Listener
{
    private final Arctica plugin;
    private final Logger log;

    //Constructor
    public ArcEntityListener(Arctica plugin, Logger log)
    {
        this.plugin = plugin;
        this.log = log;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    /*
   Event Priorities

There are six priorities in Bukkit

    EventPriority.HIGHEST
    EventPriority.HIGH
    EventPriority.NORMAL
    EventPriority.LOW
    EventPriority.LOWEST
    EventPriority.MONITOR 

They are called in the following order

    EventPriority.LOWEST 
    EventPriority.LOW
    EventPriority.NORMAL
    EventPriority.HIGH
    EventPriority.HIGHEST
    EventPriority.MONITOR 

    All Events can be cancelled. Plugins with a high prio for the event can cancel or uncancel earlier issued lower prio plugin actions.
    MONITOR level should only be used, if the outcome of an event is NOT altered from this plugin and if you want to have the final state of the event.
    If the outcome gets changed (i.e. event gets cancelled, uncancelled or actions taken that can lead to it), a prio from LOWEST to HIGHEST must be used!

    The option "ignoreCancelled" if set to "true" says, that the plugin will not get this event if it has been cancelled beforehand from another plugin.
     */

    //================================================================================================
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true) // event has MONITOR priority and will be skipped if it has been cancelled before
    public void onPlayerJoin(PlayerJoinEvent event)
    {

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) // event has MONITOR priority and will be skipped if it has been cancelled before
    public void onPlayerMove(PlayerMoveEvent event)
    {
        
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) // event has MONITOR priority and will be skipped if it has been cancelled before
    public void onColdDamage(ColdDamageEvent event)
    {
        Player victim = null;
        int damageToApply = event.getDamageToApply();
       
        if(null != event.getAfflictedPlayer())
        {
            if(event.getAfflictedPlayer() instanceof Player)
            {
                victim = event.getAfflictedPlayer();

                if((victim.getHealth() - damageToApply) >= 1)
                {
                    victim.setHealth(victim.getHealth() - damageToApply);
                }
                else if(Arctica.safemode)
                {
                    victim.setHealth(1);
                }
                else
                {            
                    victim.setHealth(0);
                }
            }            
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) // event has MONITOR priority and will be skipped if it has been cancelled before
    public void onPlayerDamage(EntityDamageEvent event)
    {        
        
    }
}
