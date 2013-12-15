package com.github.CubieX.Arctica;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
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

               return true;
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

               return true;
            } 

            if (args[0].equalsIgnoreCase("status")) // shows the most important config options
            {
               if(sender.hasPermission("arctica.use"))
               {     

                  if (null != sender)
                  {
                     sender.sendMessage(Arctica.logPrefix + ChatColor.AQUA + "Debug-Info-Modus: " + ChatColor.WHITE + Arctica.debug);
                     sender.sendMessage(Arctica.logPrefix + ChatColor.AQUA + "Sicherer Modus: " + ChatColor.WHITE + Arctica.safemode);                             

                     if(null != player)
                     {
                        player.sendMessage(Arctica.logPrefix + ChatColor.AQUA + "Biom: " + ChatColor.WHITE + player.getWorld().getBiome((int)player.getLocation().getX(), (int)player.getLocation().getZ()).toString());                                                                                               
                        player.sendMessage(Arctica.logPrefix + ChatColor.AQUA + "Kaltes Biom: " + ChatColor.WHITE + plugin.posIsWithinColdBiome((int)player.getLocation().getX(), (int)player.getLocation().getZ()));
                        player.sendMessage(Arctica.logPrefix + ChatColor.AQUA + "Bist du betroffen: " + ChatColor.WHITE + plugin.playerIsAffected(player));
                     }
                  }
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to show " + plugin.getDescription().getName() + " status!");
               }

               return true;
            }

            if (args[0].equalsIgnoreCase("tele")) // teleport player on mount to a location
            {
               if(sender.hasPermission("arctica.admin")) //FIXME: ONLY TESTING!!
               {
                  if(null != player) // FIXME: So machen, dass kein Teleport, sondern /warp oder /w von xWarp erwendet wird.
                  {
                     /* Beispiel zum teleporten per /warp: (hier mit Essentials) 
                      * 
                      * Das kommt in die Main class ins onEnable als Methode. Check ob das Plugin da ist. 
                      * Den Namen des Plugins für Teleport am besten in Config einstellbar machen.
                          {
                            Essentials ess = ((Essentials)getServer().getPluginManager().getPlugin("Essentials"));
                            saveDefaultConfig();
                            this.conf = getConfig();

                            if (this.ess == null)
                              this.useEss = false;
                            else {
                              this.useEss = true;
                           }


                           Dann im EventListener das onCommandPreProccess Event einfuegen & anpassen:
                           (Befehl abfangen und eigenes Handling durchfuehren)

                           //================================================================================================
                           @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
                           public void onPreCommand(PlayerCommandPreprocessEvent evt)
                           {
                              HorseTools.this.log("Commanded " + evt.getMessage());
                        
                              String[] pack = evt.getMessage().split(" ");
                              HorseTools.this.log("Packaged: " + Arrays.toString(pack));
                              if ((pack.length != 2) || (!evt.getPlayer().isInsideVehicle())) {
                                 return;
                              }
                        
                              if (((pack[0].equalsIgnoreCase("/warp")) || (pack[0].equalsIgnoreCase("/w"))) && (HorseTools.this.useEss)) {
                                 Location warpLoc = null;
                                 try {
                                    warpLoc = HorseTools.this.ess.getWarps().getWarp(pack[1]);
                                 }
                                 catch (Exception e) {
                                    HorseTools.this.log("Except with getting warp.");
                                    evt.getPlayer().sendMessage("§cError: §4That warp does not exsist.");
                                    return;
                                 }
                        
                                 if (warpLoc == null) {
                                    HorseTools.this.log("NULL location. Abort.");
                                    return;
                                 }
                        
                                 if (((evt.getPlayer().getVehicle() instanceof Horse)) || 
                                       (evt.getPlayer().getVehicle().getType().toString().equals("UNKNOWN"))) {
                                    evt.setCancelled(true);
                                 } else {
                                    HorseTools.this.log("Not on a horse! On a: " + evt.getPlayer().getVehicle().getType().toString());
                                    return;
                                 }
                        
                                 Animals horse = (Animals)evt.getPlayer().getVehicle();
                                 if (horse.eject()) {
                                    HorseTools.this.log("Ejected player, begin TP and mount.");
                                    evt.getPlayer().sendMessage(HorseTools.this.prefix + " §6Warping to §c" + pack[1] + "§6 with horse.");
                                    BukkitTask localBukkitTask = new HorseTools.HorseTele(HorseTools.this, evt.getPlayer(), horse, warpLoc)
                                    .runTaskLater(HorseTools.this.getthis(), 2L);
                                 } else {
                                    HorseTools.this.getthis().getLogger().info("WARNING: ERROR EJECTING " + evt.getPlayer().getName());
                                 }
                              }
                              else;
                           }
                      */

                     if(player.isInsideVehicle() && player.getVehicle().getType() == EntityType.HORSE) // FIXME: TESTING!!
                     {
                        Horse mount = (Horse) player.getVehicle();

                        player.sendMessage("Teleporting you and your horse...");
                        // Prepare teleport location
                        Location desti = new Location(player.getLocation().getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
                        desti.setX(desti.getX() + 10);                             
                        // unmount player
                        boolean resUnmount = player.leaveVehicle();
                        // teleport horse and re-mount player (teleporting him in the proccess)           
                        boolean resTele = mount.teleport(desti);
                        boolean resSetPassenger = mount.setPassenger(player); // will teleport the player to the horses back

                        if(resUnmount && resTele && resSetPassenger)
                        {
                           player.sendMessage(" was successful!");
                        }
                        else
                        {
                           player.sendMessage(" failed!");
                        }
                     }
                  }
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to teleport with a mount!");
               }

               return true;
            } 
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

               return true;
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

               return true;
            }                
         }
         else
         {
            sender.sendMessage(ChatColor.YELLOW + Arctica.logPrefix + "Falsche Anzahl an Parametern.");
         }
      }         
      return false; // No valid parameter count. If false is returned, the help for the command stated in the plugin.yml will be displayed to the player
   }
}
