package com.github.CubieX.Arctica;

import java.util.ArrayList;
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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
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
            if((event.getAfflictedPlayer() instanceof Player) &&
                  (!event.getAfflictedPlayer().isDead()))
            {
               victim = event.getAfflictedPlayer();

               if(!victim.hasPermission("arctica.debug"))
               {
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
                     // player will die, so if configured, delete his inventory before his items get dropped when he dies
                     if(plugin.getConfig().getBoolean("clearInventoryOnDeath"))
                     {
                        // TODO keep some specified items??
                        victim.getInventory().clear();
                     }
                     victim.setHealth(0);
                  }   
               } // if player has debug permission, don't apply the calculated damage, but send debug message

               if(0 < damageToApply)
               {
                  if(Arctica.debug || victim.hasPermission("arctica.debug"))
                  {
                     victim.sendMessage(ChatColor.AQUA + "" + damageToApply + " Kaelteschaden erhalten.");
                  }
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
               if((plugin.playerIsAffected(event.getPlayer())) &&
                     (!playersHoldingTorch.contains(event.getPlayer().getName())))
               {
                  //if(Arctica.debug){event.getPlayer().sendMessage(ChatColor.GREEN + "Fackel-Bonus EIN.");}
                  playersHoldingTorch.add(event.getPlayer().getName());
               }
            }
            else  // Player with permission has no torch in hand, so delete him from the List if he's on it.
            {     // This should always be done, even if the player is currently not affected, to prevent exploiting the mechanism
               // Through leaving the cold biome
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
   public void onBlockPlaceEvent(BlockPlaceEvent event)
   {
      if(plugin.posIsWithinColdBiome(event.getBlock().getX(), event.getBlock().getZ()))
      {
         // Check if placed block is a block which may be used as fuel for a fire ************************
         if(plugin.isFuelBlock(event.getBlock().getTypeId()))
         {
            Block blockUnderPlacedBlock = plugin.getServer().getWorld(event.getPlayer().getWorld().getName()).
                  getBlockAt((int)event.getBlock().getX(),
                        (int)event.getBlock().getY() - 1,
                        (int)event.getBlock().getZ());

            if(null != blockUnderPlacedBlock)
            {
               if(Material.FIRE == blockUnderPlacedBlock.getType())
               {
                  // query for the fire (blockUnderPlacedBlock) in data file and update "dieTime" timestamp to new current time + burnDuration
                  // if it is a registered fire.
                  if(plugin.blockIsRegisteredFire(blockUnderPlacedBlock.getX(), blockUnderPlacedBlock.getY(), blockUnderPlacedBlock.getZ()))
                  {
                     // Caution: this method needs the coordinates of the fire, but the group of the placed block!
                     plugin.updateFueledFireOnFireList(blockUnderPlacedBlock.getX(), blockUnderPlacedBlock.getY(), blockUnderPlacedBlock.getZ(), plugin.getFuelGroup(event.getBlock().getTypeId()));
                     event.getPlayer().sendMessage(ChatColor.AQUA + "Dieses Feuer brennt nun weitere " + ChatColor.GREEN + plugin.getBurnDurationOfFuelBlock(event.getBlock().getTypeId()) + ChatColor.AQUA + " Minuten.");
                     return; // only one of these checks can be successful. So skip the others to keep the time in here short.
                  }
               }
            }
         }

         // ************ Check if placed block is FIRE and if so, register it, if it is placed on a flammable block **********************

         if(Material.FIRE == event.getBlock().getType())
         {
            Location loc = event.getBlock().getLocation();
            loc.setY(loc.getY() + 1); // shift selection to (possible) fuel block

            Block blockUnderFire = plugin.getServer().getWorld(event.getPlayer().getWorld().getName()).
                  getBlockAt((int)event.getBlock().getX(),
                        (int)event.getBlock().getY() - 1,
                        (int)event.getBlock().getZ());

            if(blockUnderFire.getType().isFlammable()) // if it's not flammable, it could be ignitable anyway, but will only burn a couple of seconds.
            {
               plugin.addNewFireToFireList(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
               if(Arctica.debug){event.getPlayer().sendMessage(ChatColor.AQUA + "Neu entflammtes Feuer bei " + event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ() + " registriert.");}

               if(plugin.isFuelBlock(loc.getBlock().getTypeId())) // there is a fuel block already present, so update the fire with new dieTime
               {
                  plugin.updateFueledFireOnFireList(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), plugin.getFuelGroup(loc.getBlock().getTypeId()));
                  event.getPlayer().sendMessage(ChatColor.AQUA + "Dieses Feuer brennt nun " + ChatColor.GREEN + plugin.getBurnDurationOfFuelBlock(loc.getBlock().getTypeId()) + ChatColor.AQUA + " Minuten.");
               }

               return; // only one of these checks can be successful. So skip the others to keep the time in here short.
            }
         }

         // ************* check if player has placed a block at the spot of a registered fire (thus extinguished it) and if so, delete the fire from file **********
         if(plugin.blockIsRegisteredFire(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ()))
         {
            plugin.removeDiedFireFromFireList(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());                  
            if(Arctica.debug){event.getPlayer().sendMessage(ChatColor.AQUA + "Feuerstelle aus Liste geloescht!");}
         }
      }

      return; // only one of these checks can be successful. So skip the others to keep the time in here short.
   }

   //================================================================================================
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPlayerInteract(PlayerInteractEvent event)
   {
      if(plugin.posIsWithinColdBiome(event.getPlayer().getTargetBlock(null, 5).getX(), event.getPlayer().getTargetBlock(null, 5).getZ()))
      {         
         if(event.getAction() == Action.LEFT_CLICK_BLOCK)
         {
            Block targetedBlock = event.getPlayer().getTargetBlock(null, 5); // first arg is transparent block, second arg is maxDistance to scan. 5 is default reach for players.

            if ((null != targetedBlock) && (targetedBlock.getType() == Material.FIRE))
            {
               // check if player is about to extinguish a registered fire with his hands or a tool (and not a Block)
               if(plugin.blockIsRegisteredFire(targetedBlock.getX(), targetedBlock.getY(), targetedBlock.getZ()))
               {
                  plugin.removeDiedFireFromFireList(targetedBlock.getX(), targetedBlock.getY(), targetedBlock.getZ());                  
                  if(Arctica.debug){event.getPlayer().sendMessage(ChatColor.AQUA + "Feuerstelle aus Liste geloescht!");}
               }
            }
         }
      }
   }

   //================================================================================================
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onBlockBreakEvent(BlockBreakEvent event)
   {
      if(plugin.posIsWithinColdBiome(event.getBlock().getX(), event.getBlock().getZ()))
      {
         // Check if broken block is a block which may be used as fuel for a fire ************************
         if(plugin.isFuelBlock(event.getBlock().getTypeId()))
         {
            Block blockUnderPlacedBlock = plugin.getServer().getWorld(event.getPlayer().getWorld().getName()).
                  getBlockAt((int)event.getBlock().getX(),
                        (int)event.getBlock().getY() - 1,
                        (int)event.getBlock().getZ());

            if(null != blockUnderPlacedBlock)
            {
               if(Material.FIRE == blockUnderPlacedBlock.getType())
               {               
                  // if it is a registered fire.
                  if(plugin.blockIsRegisteredFire(blockUnderPlacedBlock.getX(), blockUnderPlacedBlock.getY(), blockUnderPlacedBlock.getZ()))
                  {
                     // Caution: this method needs the coordinates of the fire, but the group of the placed block!
                     plugin.updateFueledFireOnFireList(blockUnderPlacedBlock.getX(), blockUnderPlacedBlock.getY(), blockUnderPlacedBlock.getZ(), Arctica.fuelGroups.NONE);
                     event.getPlayer().sendMessage(ChatColor.AQUA + "Diesem Feuer wurde das Brennmaterial entzogen. Es wird in kuerze erloeschen!");
                  }
               }
            }
         }
      }
   }

   //================================================================================================
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPlayerDeath(PlayerDeathEvent event)
   {
      if(plugin.playerIsAffected(event.getEntity().getPlayer())) // TODO this will trigger, regardless of the death cause. Should be checking if player died from coldDamage!
      {
         // TODO make maxJailDuration function. Finally there will be more than one jail!
         // duration will become higher when a player dies more often.
         if(Arctica.minJailDuration > 0)
         {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "tjail " + event.getEntity().getPlayer().getName() + " " + Arctica.jailName + " " + Arctica.minJailDuration + "m");
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
