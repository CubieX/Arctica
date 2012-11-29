package com.github.CubieX.Arctica;

import java.util.ArrayList;

import net.minecraft.server.Entity;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ArcSchedulerHandler
{
    private Arctica plugin = null;

    ArrayList<Integer> craftedBlocksIDlist = new ArrayList<Integer>();

    public ArcSchedulerHandler(Arctica plugin)
    {
        this.plugin = plugin;

        initCraftedBlocksIDlist();
    }

    /* These Blocks will be accepted as suitable for building a safe shelter
     * that grants the "Indoor" bonus against the cold */
    void initCraftedBlocksIDlist()
    {
        craftedBlocksIDlist.add(1);
        craftedBlocksIDlist.add(4);
        craftedBlocksIDlist.add(5);
        craftedBlocksIDlist.add(7);
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

    public void startCleanupScheduler_SyncRep()
    {      
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
        {
            public void run()
            {
                //check for all players if they are in snow biome
                try
                { // TODO WICHTIG: eventuell Teile in async Thread auslagern??
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

                                boolean currPlayerIsOutside = false;

                                currPlayerIsOutside = checkIfOutside(currPlayer);

                                int realDamageToApply = 0;

                                // check if player is currently in water
                                Material mat = currPlayer.getLocation().getBlock().getType();
                                if (mat == Material.STATIONARY_WATER || mat == Material.WATER)
                                {
                                    if(Arctica.debug) currPlayer.sendMessage(ChatColor.AQUA + "Du bist im Wasser.");
                                    if(currPlayerIsOutside)
                                    {
                                        // integer damage is rounded up. So minimum Damage is always 1 if dps is > 0.
                                        realDamageToApply = (int)Math.ceil(((Arctica.baseDamageInWater + Arctica.extraDamageInWaterWhenOutside) * (1.0 - plugin.getDamageReduceFactor(currPlayer))));
                                    }
                                    else                                    
                                    {
                                        // integer damage is rounded up. So minimum Damage is always 1 if dps is > 0.
                                        realDamageToApply = (int)Math.ceil((Arctica.baseDamageInWater * (1.0 - plugin.getDamageReduceFactor(currPlayer))));
                                    }                                    
                                }
                                else
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

                                    if(currPlayerIsOutside)
                                    {
                                        // integer damage is rounded up. So minimum Damage is always 1 if dps is > 0.
                                        realDamageToApply = (int)Math.ceil(((Arctica.baseDamageInAir  + Arctica.extraDamageInAirWhenOutside) * (1.0 - plugin.getDamageReduceFactor(currPlayer))));
                                    }
                                    else
                                    {
                                        // integer damage is rounded up. So minimum Damage is always 1 if dps is > 0.
                                        realDamageToApply = (int)Math.ceil((Arctica.baseDamageInAir * (1.0 - plugin.getDamageReduceFactor(currPlayer))));
                                    }

                                    //currPlayer.damage(realDamageToApply, (org.bukkit.entity.Entity) chick);
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
                    Arctica.log.info(Arctica.logPrefix + ex.getMessage());
                    // player probably no longer online
                }                
            }
        }, (20*5L), 20*Arctica.damageApplyPeriod); // 5 sec delay, configurable period in seconds        
    }

    boolean checkIfOutside(Player player)
    { 
        boolean playerIsOutside = true;
        boolean craftedBlockTOP = false;
        boolean craftedBlockNORTH = false;
        boolean craftedBlockEAST = false;
        boolean craftedBlockSOUTH = false;
        boolean craftedBlockWEST = false;

        Location playerLoc = player.getLocation();

        // Check TOP ========================================================
        // Check if a Block to the TOP of the player is a crafted block
        craftedBlockTOP = TOPhasCraftedBlock(playerLoc);
        // Check NORTH ========================================================
        // Check if a Block to the NORTH of the player is a crafted block
        craftedBlockNORTH = NORTHhasCraftedBlock(playerLoc);
        // Check EAST =========================================================
        // Check if a Block to the EAST of the player is a crafted block
        craftedBlockEAST = EASThasCraftedBlock(playerLoc);
        // Check SOUTH ========================================================
        // Check if a Block to the SOUTH of the player is a crafted block
        craftedBlockSOUTH = SOUTHhasCraftedBlock(playerLoc);
        // Check WEST =========================================================
        // Check if a Block to the WEST of the player is a crafted block
        craftedBlockWEST = WESThasCraftedBlock(playerLoc);

        // Gather all results and combine them
        if(craftedBlockTOP &&
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
            if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "Craftbloecke: T: " + craftedBlockTOP + " |N: " + craftedBlockNORTH + " |E: " + craftedBlockEAST + " |S: " + craftedBlockSOUTH + " |W: " + craftedBlockWEST);
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
        Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
                                
        int checkLimit = (int)checkedLoc.getY() + Arctica.checkRadius;
        checkedLoc.setY(checkedLoc.getY() + 2); // set height to block above players head        
        if(checkLimit > Arctica.maxMapHeight)
        {
            checkLimit = Arctica.maxMapHeight;
        }       

        for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY < checkLimit; checkedLocY++) //safer than "while"
        {   
            if (!checkedLoc.getBlock().isEmpty())
            {
                if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                {
                    //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "TOP Block gefunden: " + checkedLoc.getBlock().getType().toString());
                    res = true;
                    break;
                }
                else
                {
                    checkedLoc.setY(checkedLoc.getY() + 1);                   
                }
            }
            else 
            {
                checkedLoc.setY(checkedLoc.getY() + 1);               
            }
        }

        return (res);
    }

    boolean NORTHhasCraftedBlock(Location startLocation)
    {
        boolean res = false;

        // Check NORTH =========================================================
        // Check if there is a block to the NORTH of the players position which is a valid
        // crafted block for a shelter to gain the "indoor warmth bonus"
        
        Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
        int checkLimit = (int)checkedLoc.getZ() - Arctica.checkRadius;
        checkedLoc.setZ(checkedLoc.getZ() - 1); // set start next to the player                
       
        for(int checkedLocZ = (int)checkedLoc.getZ(); checkedLocZ > checkLimit; checkedLocZ--) //safer than "while"
        {   
            //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "NORTH checkedLocZ: " + checkedLocZ + " <> checkLimit: " + checkLimit);
            if (!checkedLoc.getBlock().isEmpty())
            {                
                if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                {
                    //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "NORTH Block gefunden: " + checkedLoc.getBlock().getType().toString());
                    res = true;                    
                    break;
                }
                else
                {
                    checkedLoc.setZ(checkedLoc.getZ() - 1);                    
                }
            }
            else 
            {
                checkedLoc.setZ(checkedLoc.getZ() - 1);                
            }
        }        

        return (res);
    }

    boolean EASThasCraftedBlock(Location startLocation)
    {
        boolean res = false;

        // Check EAST =========================================================
        // Check if there is a block to the EAST of the players position which is a valid
        // crafted block for a shelter to gain the "indoor warmth bonus"
       
        Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
        int checkLimit = (int)checkedLoc.getX() + Arctica.checkRadius;
        checkedLoc.setX(checkedLoc.getX() + 1); // set start next to the player                
        
        for(int checkedLocX = (int)checkedLoc.getX(); checkedLocX < checkLimit; checkedLocX++) //safer than "while"
        {   
            if (!checkedLoc.getBlock().isEmpty())
            {
                if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                {
                    //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "EAST Block gefunden: " + checkedLoc.getBlock().getType().toString());
                    res = true;
                    break;
                }
                else
                {
                    checkedLoc.setX(checkedLoc.getX() + 1);                   
                }
            }
            else 
            {
                checkedLoc.setX(checkedLoc.getX() + 1);               
            }
        }       

        return (res);
    }

    boolean SOUTHhasCraftedBlock(Location startLocation)
    {
        boolean res = false;

        // Check SOUTH =========================================================
        // Check if there is a block to the SOUTH of the players position which is a valid
        // crafted block for a shelter to gain the "indoor warmth bonus"
        
        Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
        int checkLimit = (int)checkedLoc.getZ() + Arctica.checkRadius;        
        checkedLoc.setZ(checkedLoc.getZ() + 1); // set start next to the player       
       
        for(int checkedLocZ = (int)checkedLoc.getZ(); checkedLocZ < checkLimit; checkedLocZ++) //safer than "while"
        {   
            if (!checkedLoc.getBlock().isEmpty())
            {
                if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                {
                    //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "SOUTH Block gefunden: " + checkedLoc.getBlock().getType().toString());
                    res = true;
                    break;
                }
                else
                {
                    checkedLoc.setZ(checkedLoc.getZ() + 1);                    
                }
            }
            else 
            {
                checkedLoc.setZ(checkedLoc.getZ() + 1);                
            }
        }            

        return (res);
    }

    boolean WESThasCraftedBlock(Location startLocation)
    {
        boolean res = false;

        // Check WEST =========================================================
        // Check if there is a block to the WEST of the players position which is a valid
        // crafted block for a shelter to gain the "indoor warmth bonus"
       
        Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
        int checkLimit = (int)checkedLoc.getX() - Arctica.checkRadius;        
        checkedLoc.setX(checkedLoc.getX() - 1); // set start next to the player        
        
        for(int checkedLocX = (int)checkedLoc.getX(); checkedLocX > checkLimit; checkedLocX--) //safer than "while"
        {   
            if (!checkedLoc.getBlock().isEmpty())
            {
                if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                {
                    //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "WEST Block gefunden: " + checkedLoc.getBlock().getType().toString());
                    res = true;
                    break;
                }
                else
                {
                    checkedLoc.setX(checkedLoc.getX() - 1);                   
                }
            }
            else 
            {
                checkedLoc.setX(checkedLoc.getX() - 1);               
            }
        }       

        return (res);
    }
}
