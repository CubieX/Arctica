package com.github.CubieX.Arctica;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ArcSchedulerHandler
{
   private Arctica plugin = null;
   private ArcConfigHandler cHandler = null;
   ArrayList<Integer> craftedBlocksIDlist = new ArrayList<Integer>();
   ArrayList<Integer> warmBlocksIDlist = new ArrayList<Integer>();
   ArrayList<Integer> fuelBlocksGroupIDlist_Wool = new ArrayList<Integer>();
   ArrayList<Integer> fuelBlocksGroupIDlist_CraftedWood = new ArrayList<Integer>();
   ArrayList<Integer> fuelBlocksGroupIDlist_Log = new ArrayList<Integer>();
   ArrayList<Integer> fuelBlocksGroupIDlist_CoalOre = new ArrayList<Integer>();
   ArrayList<String> playersToAffect = new ArrayList<String>();
   static final int playersToHandleEachTick = 5; // set this dependent on the 'checked block count' for each player, to prevent overloading a tick time frame!
   int handledPlayers = 0;
   int cdSchedTaskID = 0; 
   int playersToAffectCount = 0;
   static final int neededCraftBlockRows = 2;  // amount of horizontal rows that must consist of crafted blocks to determine whether the player is inside or outside.
   // 2 means, the scan will look for 2 blocks high walls around the player.

   public ArcSchedulerHandler(Arctica plugin, ArcConfigHandler cHandler)
   {
      this.plugin = plugin;
      this.cHandler = cHandler;

      initCraftedBlocksIDlist();
      initWarmBlocksIDlist();
      initFuelBlocksGroupIDlist_Wool();
      initFuelBlocksGroupIDlist_CraftedWood();
      initFuelBlocksGroupIDlist_Log();
      initFuelBlocksGroupIDlist_CoalOre();
   }

   /* These Blocks will be accepted as suitable for building a safe shelter
    * that grants the "Indoor" bonus against the cold */
   // TODO make configurable via plugin config (sub-tree that is beeing read)
   void initCraftedBlocksIDlist()
   {
      craftedBlocksIDlist.add(1);
      craftedBlocksIDlist.add(4);
      craftedBlocksIDlist.add(5);
      craftedBlocksIDlist.add(7);
      craftedBlocksIDlist.add(17);
      craftedBlocksIDlist.add(20);
      craftedBlocksIDlist.add(22);
      craftedBlocksIDlist.add(24);
      craftedBlocksIDlist.add(35);
      craftedBlocksIDlist.add(41);
      craftedBlocksIDlist.add(42);
      craftedBlocksIDlist.add(43);
      craftedBlocksIDlist.add(45);
      craftedBlocksIDlist.add(48);
      craftedBlocksIDlist.add(49);
      craftedBlocksIDlist.add(57);
      craftedBlocksIDlist.add(64);
      craftedBlocksIDlist.add(71);
      craftedBlocksIDlist.add(82);
      craftedBlocksIDlist.add(87);
      craftedBlocksIDlist.add(88);
      craftedBlocksIDlist.add(89);
      craftedBlocksIDlist.add(97);
      craftedBlocksIDlist.add(98);
      craftedBlocksIDlist.add(102);
      craftedBlocksIDlist.add(110);
      craftedBlocksIDlist.add(112);
      craftedBlocksIDlist.add(123);
      craftedBlocksIDlist.add(124);
      craftedBlocksIDlist.add(125);
      craftedBlocksIDlist.add(133);
   }

   /* These Blocks will be accepted as warm blocks that grant a warmth bonus
    * if player is near one of them */    
   void initWarmBlocksIDlist()
   {
      warmBlocksIDlist.add(10);
      warmBlocksIDlist.add(11);
      warmBlocksIDlist.add(51);
   }

   /* These Blocks will be accepted as fuel blocks to feed the fire */
   void initFuelBlocksGroupIDlist_Wool()
   {      
      fuelBlocksGroupIDlist_Wool.add(35);      
   }

   /* These Blocks will be accepted as fuel blocks to feed the fire */
   void initFuelBlocksGroupIDlist_CraftedWood()
   {
      fuelBlocksGroupIDlist_CraftedWood.add(5);    
      fuelBlocksGroupIDlist_CraftedWood.add(53);
      fuelBlocksGroupIDlist_CraftedWood.add(85);
      fuelBlocksGroupIDlist_CraftedWood.add(126);
      fuelBlocksGroupIDlist_CraftedWood.add(134);
      fuelBlocksGroupIDlist_CraftedWood.add(136);
   }

   /* These Blocks will be accepted as fuel blocks to feed the fire */
   void initFuelBlocksGroupIDlist_Log()
   {      
      fuelBlocksGroupIDlist_Log.add(17);     
   }

   /* These Blocks will be accepted as fuel blocks to feed the fire */
   void initFuelBlocksGroupIDlist_CoalOre()
   {
      fuelBlocksGroupIDlist_CoalOre.add(16);      
   }

   public void startColdDamageScheduler_SyncRep()
   {
      // this is a synchronous repeating task
      plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable()
      {
         @Override
         public void run()
         {
            Player[] onlinePlayerList = plugin.getServer().getOnlinePlayers();
            playersToAffect.clear();
            playersToAffectCount = 0;

            //check for all players if they are in cold biome
            for(int i = 0; i < onlinePlayerList.length; i++)
            {
               Player currPlayer = onlinePlayerList[i];

               if((currPlayer.hasPermission("arctica.use")) &&
                     (!currPlayer.hasPermission("arctica.immune")))
               {
                  if(plugin.posIsWithinColdBiome((int)currPlayer.getLocation().getX(), (int)currPlayer.getLocation().getZ()))                              
                  {    
                     playersToAffect.add(currPlayer.getName());
                     playersToAffectCount++;
                  }
               }
            }

            if(!playersToAffect.isEmpty())
            {
               // calculate how many ticks will be needed to handle all players that should be affected
               int amountOfTicksNeededToHandleAllAffectedPlayers = (int)(Math.ceil((double)playersToAffectCount / (double)playersToHandleEachTick));
               handledPlayers = 0;

               for(int i = 0; i < amountOfTicksNeededToHandleAllAffectedPlayers; i++)
               {
                  task_applyColdDamage(); // handles the given amount of players each tick. Must be called multiple times until all
                  // players have been handled
               }
            }
         }
      }, (20*5L), 20*Arctica.damageApplyPeriod); // 5 sec delay, configurable period in seconds
   }

   public void startFireCheckerScheduler_SyncRep()
   {
      // this is a synchronous repeating task
      plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable()
      {
         @Override
         public void run()
         {
            // TODO When checking all registered fire places, also check if there is still fire present. If not, remove the fire place from data file.
            // if check is only done for fire in range of a player, then use a seperate scheduler to check and cleanup the file cyclic
            // while checking for fire blocks at registered positions.
            // TODO performance test with big fire list!
            // TODO cycle through all fires in fireList.yml and check dieTime. If a fires dieTime is expired, delete the fire block and delete the fire from file
            // TODO use this Scheduler also, to check if the configured percentage of "burnDuration" of the fuel Block has expired, id if so, remove the block
            // to show the player, that this fire is about to die

            Set<String> firesInConfig = cHandler.getFireListFile().getKeys(false);
            String[] coords;
            long currTime = plugin.getCurrTimeInMillis();
            double burnDurationInMillis = 0;
            double burnDurationLeftInMillis = 0;
            Block checkedBlock = null;
            Location loc = null;
            

            for(String fire : firesInConfig)
            {
               coords = fire.split("_");

               // Check if a fuel block above a fire is due to be consumed ***********************************

               if(coords.length == 3) // the "config_version" key gets picked up in the first run. This way, it's ignored.
               {
                  // fetch (possible) fire block
                  checkedBlock = plugin.getServer().getWorld(Arctica.affectedWorld).getBlockAt(
                        Integer.parseInt(coords[0]),
                        Integer.parseInt(coords[1]),
                        Integer.parseInt(coords[2]));

                  loc = checkedBlock.getLocation();

                  if(checkedBlock.getType() == Material.FIRE)
                  {
                     loc.setY(loc.getY() + 1); // shift checked position to (possible) fuel block
                     checkedBlock = loc.getBlock();

                     if(isFuelBlock(checkedBlock.getTypeId())) // neccessary to avoid DIV/0 in next statement!
                     {
                        if(cHandler.getFireListFile().getLong(fire + ".dieTime") > currTime) // avoid negative value in next step
                        {             
                           burnDurationInMillis = (double)getBurnDurationOfFuelBlock(loc.getBlock().getTypeId()) * 60 * 1000;
                           burnDurationLeftInMillis = (double)cHandler.getFireListFile().getLong(fire + ".dieTime") - currTime;                                                     
                           double percentageOfBurnDuration = ((burnDurationInMillis - burnDurationLeftInMillis) / burnDurationInMillis) * 100;
                           
                           if(percentageOfBurnDuration >= Arctica.fuelBlockConsumeThreshold)
                           {                                                    
                              checkedBlock.setType(Material.AIR);

                              if(Arctica.debug) Arctica.log.info("Fire at " + checkedBlock.getX() + ", " + (checkedBlock.getY() - 1) + ", " + checkedBlock.getZ() + " will die soon.");                           
                           }
                        }
                        else
                        {
                           // this case will happen, if the server is not running for a while
                           checkedBlock.setType(Material.AIR);
                        }
                     }
                  }
                  else
                  {
                     loc.setY(loc.getY() + 1); // shift checked position to (possible) fuel block to maintain logic for next steps
                  }

                  // Check if a fire is due to die **************************************************************
                  if(currTime > cHandler.getFireListFile().getLong(fire + ".dieTime"))
                  {
                     try
                     {
                        loc.setY(loc.getY() - 1); // shift checked position back to (possible) fire block
                        checkedBlock = loc.getBlock();                      
                        if(checkedBlock.getType() == Material.FIRE)
                        {
                           checkedBlock.setType(Material.AIR); // remove fire
                           plugin.getServer().getWorld(Arctica.affectedWorld).playSound(checkedBlock.getLocation(), Sound.FIZZ, 10, 1); // volume = 10 good?
                        }
                        plugin.removeDiedFireFromFireList(checkedBlock.getX(), checkedBlock.getY(), checkedBlock.getZ());

                        if(Arctica.debug) Arctica.log.info("Fire at " + checkedBlock.getX() + ", " + checkedBlock.getY() + ", " + checkedBlock.getZ() + " removed.");
                     }
                     catch(Exception ex)
                     {
                        Arctica.log.severe(Arctica.logPrefix + "An error occurred in fireCheckerScheduler.");
                        Arctica.log.severe(ex.getMessage());
                     }                  
                  }
                  // *****************************************************************************************               
               }
            }
         }
      }, (20*5L), 20*11); // 5 sec delay, 11 sec period (should not occurr at the same time as the coldDamageScheduler)
   }

   void task_applyColdDamage() // this will be run as many ticks as needed to handle all players.
   {   
      // this is a synchronous single task. It will be run once on the next tick.
      plugin.getServer().getScheduler().runTask(plugin, new Runnable()
      {
         @Override
         public void run() //current scan duration: approx. 2 ms/Player (limited to 5x this time per tick!)
         {
            Player currPlayer = null;
            boolean currPlayerIsOutside = false;
            boolean currPlayerIsNearFire = false;
            boolean currPlayerIsInWater = false;
            boolean currPlayerIsHoldingTorch = false;
            int realDamageToApply = 0;
            int handledPlayersThisTick = 0;

            double baseDamageInWaterToApply = 0.0;
            double extraDamageInWaterWhenOutsideToApply = 0.0;
            double warmthBonusFactorToApply = 0.0;
            double torchBonusFactorToApply = 0.0;
            double baseDamageInAirToApply = 0.0;
            double extraDamageInAirWhenOutsideToApply = 0.0;

            try
            {
               if(handledPlayers < playersToAffectCount)
               {
                  for (int currPlayerIndex = handledPlayers; currPlayerIndex < playersToAffect.size(); currPlayerIndex++) // go through Hashmap
                  {
                     if(null != playersToAffect.get(currPlayerIndex)) // Hashmap may contain NULL values! So assure it's a real player.
                     {
                        if((handledPlayersThisTick < playersToAffectCount) &&
                              (handledPlayersThisTick < playersToHandleEachTick))
                        {
                           currPlayer = plugin.getServer().getPlayerExact(playersToAffect.get(currPlayerIndex));

                           currPlayerIsOutside = checkIfOutside(currPlayer);
                           currPlayerIsNearFire = checkIfNearFire(currPlayer);
                           currPlayerIsInWater = checkIfInWater(currPlayer);
                           currPlayerIsHoldingTorch = plugin.playerIsHoldingTorch(currPlayer.getName());

                           if (currPlayerIsInWater)
                           {
                              if(currPlayerIsOutside)
                              { // player is outside and in water
                                 if(currPlayerIsNearFire)
                                 {
                                    if(Arctica.debug) currPlayer.sendMessage(ChatColor.AQUA + "Du bist in kaltem Wasser.");
                                    baseDamageInWaterToApply = Arctica.baseDamageInWater;
                                    extraDamageInWaterWhenOutsideToApply = Arctica.extraDamageInWaterWhenOutside;
                                    warmthBonusFactorToApply = Arctica.warmthBonusFactor;
                                 }
                                 else
                                 { // player is outside and in ice water
                                    if(Arctica.debug) currPlayer.sendMessage(ChatColor.AQUA + "Du bist in Eiswasser!");
                                    baseDamageInWaterToApply = Arctica.baseDamageInWater;
                                    extraDamageInWaterWhenOutsideToApply = Arctica.extraDamageInWaterWhenOutside;
                                 }                                            
                              }
                              else                                    
                              { // player is inside and in water.
                                 if(currPlayerIsNearFire)
                                 {
                                    baseDamageInWaterToApply = Arctica.baseDamageInWater;
                                    warmthBonusFactorToApply = Arctica.warmthBonusFactor;
                                 }
                                 else
                                 {
                                    baseDamageInWaterToApply = Arctica.baseDamageInWater;                                                   
                                 }
                              }
                           }
                           else // player is in air
                           {
                              /*
                                                     [syntax = java]
                                                        EntityPig piggy= new EntityPig (mcWorld);

                                                    mcWorld.addEntity(piggy, SpawnReason.CUSTOM);
                                                    [/syntax]

                                                    Where mcWorld is the craftbukkit world.getHandle().
                               * */

                              /*Entity chick = null;
                                                    CraftPlayer craftPlayer = (CraftPlayer)currPlayer;
                                                    CraftWorld craftWorld = (CraftWorld)craftPlayer.getWorld();
                                                    net.minecraft.server.World mworld = (net.minecraft.server.World)(craftWorld.getHandle());
                                                    chick = new Entity(mworld);*/

                              if(currPlayerIsHoldingTorch)
                              {
                                 torchBonusFactorToApply = Arctica.torchBonusFactor;
                              }

                              if(currPlayerIsNearFire)
                              {
                                 warmthBonusFactorToApply = Arctica.warmthBonusFactor;

                                 if(currPlayerIsOutside)
                                 {
                                    baseDamageInAirToApply = Arctica.baseDamageInAir;
                                    extraDamageInAirWhenOutsideToApply = Arctica.extraDamageInAirWhenOutside;                                                
                                 }
                                 else
                                 { // player is inside near a fire. No Damage.                                                
                                    // No Damage.
                                 }
                              }
                              else
                              { // player is in air, but not near fire
                                 baseDamageInAirToApply = Arctica.baseDamageInAir;

                                 if(currPlayerIsOutside)
                                 { // player is outside in air, not near fire                                                
                                    extraDamageInAirWhenOutsideToApply = Arctica.extraDamageInAirWhenOutside;                                                
                                 }
                                 else
                                 { // player is inside, not near fire.                                                
                                    // No extra damage. Only base Damage.
                                 }
                              }

                              //currPlayer.damage(realDamageToApply, (org.bukkit.entity.Entity) chick);
                           }

                           // Combined calculation. Some values will be 0, depending on above evaluation
                           realDamageToApply = (int)Math.ceil(((
                                 baseDamageInAirToApply +
                                 extraDamageInAirWhenOutsideToApply +
                                 baseDamageInWaterToApply +
                                 extraDamageInWaterWhenOutsideToApply) *
                                 (1.0 - warmthBonusFactorToApply) *
                                 (1.0 - torchBonusFactorToApply) *                                            
                                 (1.0 - plugin.getDamageReduceFactorFromCloth(currPlayer))));

                           // TODO Mehr Schaden in der Nacht als am Tag drinnen/draussen! Wert per Config einstellbar (ExtraDamageAtNight)
                           // fire custom damage event ================================================                                
                           ColdDamageEvent cdEvent = new ColdDamageEvent(currPlayer, realDamageToApply); // Create the event
                           plugin.getServer().getPluginManager().callEvent(cdEvent); // fire Event         
                           //==========================================================================                                                           
                           handledPlayersThisTick++; // a player was handled. Increase counter for current tick.
                           handledPlayers++; // a player was handled. Increase counter for playersToAffect list.   
                        }
                        else
                        {
                           // limit of players for this tick is reached. Leave loop.
                           break;
                        } // end if/else Handled players count check
                     } // end if NULL check
                  } // end if FOR loop   
               }// end if handledPlayers < playersToAffectCount
            }
            catch(Exception ex)
            {
               Arctica.log.info(Arctica.logPrefix + ex.getMessage());
               // player probably no longer online or no longer meeting the necessary requirements for beeing affected
            } // end TRY/CATCH

         } // end RUN()
      }); // end scheduler call
   }

   public void setTaskID(int id)
   {
      this.cdSchedTaskID = id;
   }

   boolean checkIfOutside(Player player)
   { 
      boolean playerIsOutside = true;
      boolean craftedBlockTOP = false;
      boolean craftedBlockNORTHdiagonal = false;
      boolean craftedBlockEASTdiagonal = false;
      boolean craftedBlockSOUTHdiagonal = false;
      boolean craftedBlockWESTdiagonal = false;
      boolean craftedBlockNORTH = false;
      boolean craftedBlockEAST = false;
      boolean craftedBlockSOUTH = false;
      boolean craftedBlockWEST = false;

      Location playerLoc = player.getLocation();

      // Check TOP ========================================================
      // Check if a Block straight to the TOP of the player is a crafted block
      craftedBlockTOP = TOPhasCraftedBlock(playerLoc);

      // Check NORTH 45 degrees ================================================================
      // Check if a Block to the NORTH in 45 degrees to the top of the player is a crafted block
      craftedBlockNORTHdiagonal = NORTHdiagnoalHasCraftedBlock(playerLoc);
      // Check EAST 45 degrees ================================================================
      // Check if a Block to the EAST in 45 degrees to the top of the player is a crafted block
      craftedBlockEASTdiagonal = EASTdiagnoalHasCraftedBlock(playerLoc);
      // Check SOUTH 45 degrees ================================================================
      // Check if a Block to the SOUTH in 45 degrees to the top of the player is a crafted block
      craftedBlockSOUTHdiagonal = SOUTHdiagnoalHasCraftedBlock(playerLoc);
      // Check WEST 45 degrees ================================================================
      // Check if a Block to the WEST in 45 degrees to the top of the player is a crafted block
      craftedBlockWESTdiagonal = WESTdiagnoalHasCraftedBlock(playerLoc);

      // Check NORTH ========================================================================================
      // Check if a Block to the NORTH of the player is a crafted block (block on foot and head level needed)
      craftedBlockNORTH = NORTHhasCraftedBlock(playerLoc);
      // Check EAST =========================================================================================
      // Check if a Block to the EAST of the player is a crafted block (block on foot and head level needed)
      craftedBlockEAST = EASThasCraftedBlock(playerLoc);
      // Check SOUTH ========================================================================================
      // Check if a Block to the SOUTH of the player is a crafted block (block on foot and head level needed)
      craftedBlockSOUTH = SOUTHhasCraftedBlock(playerLoc);
      // Check WEST =========================================================================================
      // Check if a Block to the WEST of the player is a crafted block (block on foot and head level needed)
      craftedBlockWEST = WESThasCraftedBlock(playerLoc);

      // Gather all results and combine them
      if(craftedBlockTOP &&
            craftedBlockNORTHdiagonal &&
            craftedBlockEASTdiagonal &&
            craftedBlockSOUTHdiagonal &&
            craftedBlockWESTdiagonal &&
            craftedBlockNORTH &&
            craftedBlockEAST &&
            craftedBlockSOUTH &&
            craftedBlockWEST) // player is surrounded (as far as evaluated) with valid crafted blocks
      {
         playerIsOutside = false;
         if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "Du bist im Innenbereich.");
      }
      else
      {
         playerIsOutside = true;
         if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "Craftbloecke: T: " + craftedBlockTOP + " |N: " + craftedBlockNORTH + " |N45: " + craftedBlockNORTHdiagonal + " |E: " + craftedBlockEAST + " |E45: " + craftedBlockEASTdiagonal + " |S: " + craftedBlockSOUTH + " |S45: " + craftedBlockSOUTHdiagonal + " |W: " + craftedBlockWEST + " |W45: " + craftedBlockWESTdiagonal);
         if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "Du bist im Freien.");
      }

      return(playerIsOutside);
   }

   boolean TOPhasCraftedBlock(Location startLocation)
   {
      boolean res = false;

      // Check TOP =========================================================
      // Check if there is a block above the players position which is a valid
      // crafted block for a shelter to gain the "indoor warmth bonus"

      // Important for C-Programmers: Objects in JAVA are NOT copied by using '='. It just sets another object reference!
      // Objects can only be copied by instantiating a new object and then copying ALL attributes from the other object
      Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()); // loc of player (foot-level)

      int checkLimit = (int)checkedLoc.getY() + Arctica.checkRadius;
      checkedLoc.setY(checkedLoc.getY() + 2); // set height to block above players head        
      if(checkLimit > Arctica.maxMapHeight)
      {
         checkLimit = Arctica.maxMapHeight;
      }

      for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY < checkLimit; checkedLocY++, checkedLoc.setY(checkedLoc.getY() + 1)) //safer than "while"
      {   
         if (!checkedLoc.getBlock().isEmpty())
         {
            if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
            {
               //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "TOP Block gefunden: " + checkedLoc.getBlock().getType().toString());
               res = true;
               break;
            }
         }
      }

      return (res);
   }

   boolean NORTHhasCraftedBlock(Location startLocation)
   {
      boolean res = false;
      int validCraftBlockRows = 0; // amount of horizontal rows that have been successfully checked for crafted blocks.

      // Check NORTH =========================================================
      // Check if there is a block to the NORTH of the players position which is a valid
      // crafted block for a shelter to gain the "indoor warmth bonus"

      Location fixedStartLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()); // loc of player (foot-level)
      Location checkedLoc = new Location(fixedStartLoc.getWorld(), fixedStartLoc.getX(), fixedStartLoc.getY(), fixedStartLoc.getZ()); // loc of player (foot-level)
      
      int checkLimitY = (int)checkedLoc.getY() + 1; // check should be done for foot and head level of player
      int checkLimit = (int)checkedLoc.getZ() - Arctica.checkRadius;
      checkedLoc.setZ(checkedLoc.getZ() - 1); // set start next to the player                

      for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY()+1))
      {
         for(int checkedLocZ = (int)checkedLoc.getZ(); checkedLocZ > checkLimit; checkedLocZ--, checkedLoc.setZ(checkedLoc.getZ() - 1)) //safer than "while"
         {   
            //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "NORTH checkedLocZ: " + checkedLocZ + " <> checkLimit: " + checkLimit);
            if (!checkedLoc.getBlock().isEmpty())
            {
               if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "Gecheckt: " + checkedLoc.getBlock().getX() +  " " + checkedLoc.getBlock().getY() + " " + checkedLoc.getBlock().getZ());
               if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
               {
                  if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "NORTH Block gefunden: " + checkedLoc.getBlock().getType().toString());                        
                  validCraftBlockRows++;
                  break;
               }
            }
         }
         checkedLoc.setZ(fixedStartLoc.getZ() - 1); // reset horizontal location to initial location (= next to players feet) after checking foot level to prepare for head level check
      }

      if(validCraftBlockRows >= neededCraftBlockRows) // all height levels have valid craft blocks
      {
         res = true;
      }

      return (res);
   }

   boolean EASThasCraftedBlock(Location startLocation)
   {
      boolean res = false;
      int validCraftBlockRows = 0; // amount of horizontal rows that have been successfully checked for crafted blocks.

      // Check EAST =========================================================
      // Check if there is a block to the EAST of the players position which is a valid
      // crafted block for a shelter to gain the "indoor warmth bonus"

      Location fixedStartLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()); // loc of player (foot-level)
      Location checkedLoc = new Location(fixedStartLoc.getWorld(), fixedStartLoc.getX(), fixedStartLoc.getY(), fixedStartLoc.getZ()); // loc of player (foot-level)
      
      int checkLimitY = (int)checkedLoc.getY() + 1; // check should be done for foot and head level of player
      int checkLimit = (int)checkedLoc.getX() + Arctica.checkRadius;
      checkedLoc.setX(checkedLoc.getX() + 1); // set start next to the player                

      for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY()+1))
      {
         for(int checkedLocX = (int)checkedLoc.getX(); checkedLocX < checkLimit; checkedLocX++, checkedLoc.setX(checkedLoc.getX() + 1)) //safer than "while"
         {   
            if (!checkedLoc.getBlock().isEmpty())
            {
               if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "Gecheckt: " + checkedLoc.getBlock().getX() +  " " + checkedLoc.getBlock().getY() + " " + checkedLoc.getBlock().getZ());
               if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
               {
                  if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "EAST Block gefunden: " + checkedLoc.getBlock().getType().toString());
                  validCraftBlockRows++;
                  break;
               }
            }
         }
         checkedLoc.setX(fixedStartLoc.getX() + 1); // reset horizontal location to initial location (= next to players feet) after checking foot level to prepare for head level check
      }

      if(validCraftBlockRows >= neededCraftBlockRows) // all height levels have valid craft blocks
      {
         res = true;
      }

      return (res);
   }

   boolean SOUTHhasCraftedBlock(Location startLocation)
   {
      boolean res = false;
      int validCraftBlockRows = 0; // amount of horizontal rows that have been successfully checked for crafted blocks.

      // Check SOUTH =========================================================
      // Check if there is a block to the SOUTH of the players position which is a valid
      // crafted block for a shelter to gain the "indoor warmth bonus"

      Location fixedStartLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()); // loc of player (foot-level)
      Location checkedLoc = new Location(fixedStartLoc.getWorld(), fixedStartLoc.getX(), fixedStartLoc.getY(), fixedStartLoc.getZ()); // loc of player (foot-level)
      
      int checkLimitY = (int)checkedLoc.getY() + 1; // check should be done for foot and head level of player
      int checkLimit = (int)checkedLoc.getZ() + Arctica.checkRadius;        
      checkedLoc.setZ(checkedLoc.getZ() + 1); // set start next to the player       

      for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY()+1))
      {
         for(int checkedLocZ = (int)checkedLoc.getZ(); checkedLocZ < checkLimit; checkedLocZ++, checkedLoc.setZ(checkedLoc.getZ() + 1)) //safer than "while"
         {   
            if (!checkedLoc.getBlock().isEmpty())
            {
               if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "Gecheckt: " + checkedLoc.getBlock().getX() +  " " + checkedLoc.getBlock().getY() + " " + checkedLoc.getBlock().getZ());
               if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
               {
                  if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "SOUTH Block gefunden: " + checkedLoc.getBlock().getType().toString());
                  validCraftBlockRows++;
                  break;
               }
            }
         }
         checkedLoc.setZ(fixedStartLoc.getZ() + 1); // reset horizontal location to initial location (= next to players feet) after checking foot level to prepare for head level check
      }

      if(validCraftBlockRows >= neededCraftBlockRows) // all height levels have valid craft blocks
      {
         res = true;
      }

      return (res);
   }

   boolean WESThasCraftedBlock(Location startLocation)
   {
      boolean res = false;
      int validCraftBlockRows = 0; // amount of horizontal rows that have been successfully checked for crafted blocks.

      // Check WEST =========================================================
      // Check if there is a block to the WEST of the players position which is a valid
      // crafted block for a shelter to gain the "indoor warmth bonus"

      Location fixedStartLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()); // loc of player (foot-level)
      Location checkedLoc = new Location(fixedStartLoc.getWorld(), fixedStartLoc.getX(), fixedStartLoc.getY(), fixedStartLoc.getZ()); // loc of player (foot-level)
      int checkLimitY = (int)checkedLoc.getY() + 1; // check should be done for foot and head level of player
      int checkLimit = (int)checkedLoc.getX() - Arctica.checkRadius;
      checkedLoc.setX(checkedLoc.getX() - 1); // set start next to the player        

      for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY()+1))
      {
         for(int checkedLocX = (int)checkedLoc.getX(); checkedLocX > checkLimit; checkedLocX--, checkedLoc.setX(checkedLoc.getX() - 1)) //safer than "while"
         {
            if (!checkedLoc.getBlock().isEmpty())
            {
               if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "Gecheckt: " + checkedLoc.getBlock().getX() +  " " + checkedLoc.getBlock().getY() + " " + checkedLoc.getBlock().getZ());
               if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
               {
                  if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "WEST Block gefunden: " + checkedLoc.getBlock().getType().toString());
                  validCraftBlockRows++;
                  break;
               }
            }                        
         }
         checkedLoc.setX(fixedStartLoc.getX() - 1); // reset horizontal location to initial location (= next to players feet) after checking foot level to prepare for head level check
      }

      if(validCraftBlockRows >= neededCraftBlockRows) // all height levels have valid craft blocks
      {
         res = true;
      }

      return (res);
   }

   boolean NORTHdiagnoalHasCraftedBlock(Location startLocation)
   {
      boolean res = false;

      // Check NORTH 45 degrees =========================================================
      // Check if there is a valid crafted ceiling block in 45 degrees upwards to the player within given distance

      Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
      
      // set start of check to the block 45 degrees above players head in checked direction
      checkedLoc.setY(checkedLoc.getY() + 2);
      checkedLoc.setZ(checkedLoc.getZ() - 1);
      
      int checkLimitY = (int)checkedLoc.getY() + Arctica.checkRadius;

      if(checkLimitY > Arctica.maxMapHeight)
      {
         checkLimitY = Arctica.maxMapHeight;
      }

      for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY() + 1), checkedLoc.setZ(checkedLoc.getZ() - 1)) // go one block up and to the north
      {          
         if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "Gecheckt: " + checkedLoc.getBlock().getX() +  " " + checkedLoc.getBlock().getY() + " " + checkedLoc.getBlock().getZ());
         if (!checkedLoc.getBlock().isEmpty())
         {
            if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
            {
               if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "NORTH_TOP45 Block gefunden: " + checkedLoc.getBlock().getType().toString());
               res = true;
               break;
            }
         }
      }

      return (res);
   }

   boolean EASTdiagnoalHasCraftedBlock(Location startLocation)
   {
      boolean res = false;

      // Check EAST 45 degrees =========================================================
      // Check if there is a valid crafted ceiling block in 45 degrees upwards to the player within given distance

      Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
      
      // set start of check to the block 45 degrees above players head in checked direction
      checkedLoc.setY(checkedLoc.getY() + 2);
      checkedLoc.setX(checkedLoc.getX() + 1);
      
      int checkLimitY = (int)checkedLoc.getY() + Arctica.checkRadius;

      for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY() + 1), checkedLoc.setX(checkedLoc.getX() + 1)) // go one block up and to the east
      {   
         if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "Gecheckt: " + checkedLoc.getBlock().getX() +  " " + checkedLoc.getBlock().getY() + " " + checkedLoc.getBlock().getZ());
         if (!checkedLoc.getBlock().isEmpty())
         {
            if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
            {
               if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "EAST_TOP45 Block gefunden: " + checkedLoc.getBlock().getType().toString());
               res = true;
               break;
            }
         }
      }
      return (res);
   }

   boolean SOUTHdiagnoalHasCraftedBlock(Location startLocation)
   {
      boolean res = false;

      // Check SOUTH 45 degrees =========================================================
      // Check if there is a valid crafted ceiling block in 45 degrees upwards to the player within given distance

      Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
      
      // set start of check to the block 45 degrees above players head in checked direction
      checkedLoc.setY(checkedLoc.getY() + 2);
      checkedLoc.setZ(checkedLoc.getZ() + 1);
      
      int checkLimitY = (int)checkedLoc.getY() + Arctica.checkRadius;

      for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY() + 1), checkedLoc.setZ(checkedLoc.getZ() + 1)) // go one block up and SOUTH
      {   
         if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "Gecheckt: " + checkedLoc.getBlock().getX() +  " " + checkedLoc.getBlock().getY() + " " + checkedLoc.getBlock().getZ());
         if (!checkedLoc.getBlock().isEmpty())
         {
            if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
            {
               if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "SOUTH_TOP45 Block gefunden: " + checkedLoc.getBlock().getType().toString());
               res = true;
               break;
            }
         }
      }
      return (res);
   }

   boolean WESTdiagnoalHasCraftedBlock(Location startLocation)
   {
      boolean res = false;

      // Check WEST 45 degrees =========================================================
      // Check if there is a valid crafted ceiling block in 45 degrees upwards to the player within given distance

      Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
      
      // set start of check to the block 45 degrees above players head in checked direction
      checkedLoc.setY(checkedLoc.getY() + 2);
      checkedLoc.setX(checkedLoc.getX() - 1);
      
      int checkLimitY = (int)checkedLoc.getY() + Arctica.checkRadius;

      for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY() + 1), checkedLoc.setX(checkedLoc.getX() - 1)) // go one block up and WEST
      {   
         if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "Gecheckt: " + checkedLoc.getBlock().getX() +  " " + checkedLoc.getBlock().getY() + " " + checkedLoc.getBlock().getZ());
         if (!checkedLoc.getBlock().isEmpty())
         {
            if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
            {
               if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "WEST_TOP45 Block gefunden: " + checkedLoc.getBlock().getType().toString());
               res = true;
               break;
            }
         }
      }
      return (res);
   }

   boolean checkIfNearFire(Player player)
   {
      boolean playerIsNearFire = false;

      Location playerLoc = player.getLocation();
      //Location checkedWarmBlock = player.getLocation();

      //int distanceToNearestWarmBlock = Arctica.warmBlockSearchRadius + 1;

      int x1 = playerLoc.getBlockX() - Arctica.horizontalWarmBlockSearchRadius; // first corner of cube to check
      int y1 = playerLoc.getBlockY() - Arctica.verticalWarmBlockSearchRadius;
      int z1 = playerLoc.getBlockZ() - Arctica.horizontalWarmBlockSearchRadius;
      World world = player.getWorld();

      int x2 = x1 + (2 * Arctica.horizontalWarmBlockSearchRadius); //second corner of cube to check
      int y2 = y1 + (2 * Arctica.verticalWarmBlockSearchRadius);
      int z2 = z1 + (2 * Arctica.horizontalWarmBlockSearchRadius);

      //if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "SpielerPos: " + playerLoc.getBlockX() + ", " + playerLoc.getBlockY() + ", " + playerLoc.getBlockZ());
      //if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "Ecke 1: " + x1 + ", " + y1 + ", " + z1);
      //if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "Ecke 2: " + x2 + ", " + y2 + ", " + z2);

      //int checkCount = 0;
      //int foundCount = 0;

      for (int checkedX = x1; checkedX < x2; checkedX++)
      {
         for (int checkedY = y1; checkedY < y2; checkedY++)
         {
            for (int checkedZ = z1; checkedZ < z2; checkedZ++)
            {
               //checkCount++;
               if(warmBlocksIDlist.contains(world.getBlockTypeIdAt(checkedX, checkedY, checkedZ)))
               {       
                  //foundCount++;
                  /*checkedWarmBlock.setWorld(world); // Exact Distance gets not evaluated until now.
                        checkedWarmBlock.setX(checkedX);
                        checkedWarmBlock.setY(checkedY);
                        checkedWarmBlock.setZ(checkedZ);

                        int distanceToCheckedWarmBlock = (int)checkedWarmBlock.distance(playerLoc);
                        if(distanceToCheckedWarmBlock < distanceToNearestWarmBlock)
                        {
                            distanceToNearestWarmBlock = distanceToCheckedWarmBlock;
                        }*/                        
                  playerIsNearFire = true;
               }
            }
         }
      }
      if(Arctica.debug && playerIsNearFire) player.sendMessage(ChatColor.AQUA + "Waermequelle gefunden.");
      //if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "Blocks gecheckt: " + checkCount + " | Waermeqellen gefunden: " + foundCount);
      return(playerIsNearFire);
   }

   boolean checkIfInWater(Player player)
   {
      boolean playerIsInWater = false;

      Material mat = player.getLocation().getBlock().getType();
      if (mat == Material.STATIONARY_WATER || mat == Material.WATER)
      {
         playerIsInWater = true;
      }

      return (playerIsInWater);
   }

   // this does not use Minecrafts "flammable" marker! It checks if the block is a valid fueling material for fire.
   public boolean isFuelBlock(int itemID)
   {
      boolean res = false;

      if((fuelBlocksGroupIDlist_Wool.contains(itemID)) ||
            (fuelBlocksGroupIDlist_CraftedWood.contains(itemID)) ||
            (fuelBlocksGroupIDlist_Log.contains(itemID)) ||
            (fuelBlocksGroupIDlist_CoalOre.contains(itemID)))
      {
         res = true;
      }

      return (res);
   }

   public Arctica.fuelGroups getFuelGroup(int blockID)
   {
      Arctica.fuelGroups group = Arctica.fuelGroups.NONE;

      if(fuelBlocksGroupIDlist_Wool.contains(blockID))
      {
         group = Arctica.fuelGroups.WOOL;
      }
      else if(fuelBlocksGroupIDlist_CraftedWood.contains(blockID))
      {
         group = Arctica.fuelGroups.CRAFTED_WOOD;
      }
      else if(fuelBlocksGroupIDlist_Log.contains(blockID))
      {
         group = Arctica.fuelGroups.LOG;
      }
      else if(fuelBlocksGroupIDlist_CoalOre.contains(blockID))
      {
         group = Arctica.fuelGroups.COAL_ORE;
      }
      else
      {
         // block with this ID is not a valid flammable block, so it has no Group
      }      

      return (group);
   }

   public int getBurnDurationOfFuelBlock(int blockID)
   {
      int time = 0;

      if(fuelBlocksGroupIDlist_Wool.contains(blockID))
      {
         time = Arctica.burnDuration_Wool;
      }
      else if(fuelBlocksGroupIDlist_CraftedWood.contains(blockID))
      {
         time = Arctica.burnDuration_CraftedWood;
      }
      else if(fuelBlocksGroupIDlist_Log.contains(blockID))
      {
         time = Arctica.burnDuration_Log;
      }
      else if(fuelBlocksGroupIDlist_CoalOre.contains(blockID))
      {
         time = Arctica.burnDuration_CoalOre;
      }
      else
      {
         // block with this ID is not a valid flammable block, so it has no Group and no burn duration
      } 

      return (time);
   }

   public boolean playerIsAffected(Player player) // returns whether or not a player is currently affected by Arctica
   {
      boolean affected = false;

      if((null != player ) &&
            (playersToAffect.contains(player.getName())))
      {
         affected = true;   
      }
      return (affected);
   }
}
