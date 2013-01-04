package com.github.CubieX.Arctica;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class ArcEntityListener implements Listener
{
   private final Arctica plugin;
   ArrayList<String> playersHoldingTorch = new ArrayList<String>();

   //Constructor
   public ArcEntityListener(Arctica plugin)
   {
      this.plugin = plugin;

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
   @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) // event has NORMAL priority and will be skipped if it has been cancelled before
   public void onColdDamage(ColdDamageEvent event)
   {
      Player victim = null;
      int damageToApply = event.getDamageToApply();

      try
      {
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
      catch(Exception ex)
      {
         Arctica.log.info(Arctica.logPrefix + ex.getMessage());
         // player is probably no longer online 
      }
   }

   //================================================================================================
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPlayerItemHeld(PlayerItemHeldEvent event)
   {
      try
      {             
         if((event.getPlayer().hasPermission("arctica.use")) &&
               (!event.getPlayer().hasPermission("arctica.immune")))
         {               
            ItemStack newItem;            
            newItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
            if ((null != newItem) &&
                  (newItem.getTypeId() == 50)) // Check if held item is a torch. Is null if empty slot.
            {
               if(!playersHoldingTorch.contains(event.getPlayer().getName()))
               {
                  //if(Arctica.debug){event.getPlayer().sendMessage(ChatColor.GREEN + "Fackel-Bonus EIN.");}
                  playersHoldingTorch.add(event.getPlayer().getName());
               }
            }
            else // Player with permission has no torch in hand, so delete him from the List if he's on it.
            {                        
               if(playersHoldingTorch.contains(event.getPlayer().getName()))
               {
                  //if(Arctica.debug){event.getPlayer().sendMessage(ChatColor.GREEN + "Fackel-Bonus AUS.");}
                  playersHoldingTorch.remove(event.getPlayer().getName());                    
               }
            }            
         }
      }
      catch(Exception ex)
      {
         // player is probably no longer online
      }
   }

   //================================================================================================
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onBlockIgniteEvent(BlockIgniteEvent event)
   {
      // TODO Almost EVERY block can be ignited! Non-flammable blocks will burn for a short amount of time!
      // So checking those registered fireplaces is mandatory, and/or upon registration the block underneath the ingited block has to be
      // pre-checked for flammable material before actually registering this fire place!

      if(event.getCause() == IgniteCause.FLINT_AND_STEEL)
      {
         Block blockUnderFire = plugin.getServer().getWorld(event.getPlayer().getWorld().getName()).
               getBlockAt((int)event.getBlock().getX(),
                     (int)event.getBlock().getY() - 1,
                     (int)event.getBlock().getZ());

         if(blockUnderFire.getType().isFlammable()) // if it's not flammable, it could be ignitable anyway, but will only burn a couple of seconds.
         {
            int xFire = event.getBlock().getX();
            int yFire = event.getBlock().getY();
            int zFire = event.getBlock().getZ();
            if(Arctica.debug){event.getPlayer().sendMessage(ChatColor.AQUA + "Neue entflammtes Feuer bei " + xFire + ", " + yFire + ", " + zFire + " registriert.");}

            // TODO add fire position to SQLite DB or data file (SQLite recommended) with current time as lastFueled timestamp
            long currTime = ((Calendar)Calendar.getInstance()).getTimeInMillis();  
         }         
      }
   }

   //================================================================================================
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onBlockPlaceEvent(BlockPlaceEvent event)
   {
      Block blockUnderPlacedBlock = plugin.getServer().getWorld(event.getPlayer().getWorld().getName()).
            getBlockAt((int)event.getBlock().getX(),
                  (int)event.getBlock().getY() - 1,
                  (int)event.getBlock().getZ());

      if(null != blockUnderPlacedBlock)
      {
         if(Material.FIRE == blockUnderPlacedBlock.getType())
         {
            // TODO lookup fire block in DB or data file and get its position

            if(plugin.isFlammableBlock(event.getBlock().getTypeId()))
            {             
               // TODO query for the fire (blockUnderPlacedBlock) in DB or data file and update "lastFueled" timestamp to current time
               long currTime = ((Calendar)Calendar.getInstance()).getTimeInMillis();
               if(Arctica.debug){event.getPlayer().sendMessage(ChatColor.AQUA + "Brennbarer Block ueber Feuerstelle erkannt.");}             

            }
         }
      }
   }

   //================================================================================================
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPlayerInteract(PlayerInteractEvent event)
   {
      if(plugin.playerIsAffected(event.getPlayer()))
      {
         if(event.getAction() == Action.LEFT_CLICK_BLOCK)
         {
            // first arg is transparent block, second arg is maxDistance to scan. 5 is default reach for players.
            if (event.getPlayer().getTargetBlock(null, 5).getType() == Material.FIRE)
            {
               // TODO lookup fire block in DB or data file and get its position         
               // IF lookup was successful (= a fire at that position is registered), delete this fireplace from DB or data file

               if(Arctica.debug){event.getPlayer().sendMessage(ChatColor.AQUA + "Feuerstelle aus DB geloescht! (implementation pending!)");} 
            }
         }
      }
   }

   // =========================================================================

   public boolean playerIsHoldingTorch(String playerName)
   {
      boolean res = false;

      if(playersHoldingTorch.contains(playerName))
      {
         res = true;
      }

      return (res);
   }
}
