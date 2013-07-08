package com.github.CubieX.Arctica;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ColdDamageEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private static LivingEntity victim = null;
    private static int damageToApply = 0;
 
    //Constructor
    public ColdDamageEvent(LivingEntity victim, int damageToApply)
    {
        ColdDamageEvent.victim = victim;
        ColdDamageEvent.damageToApply = damageToApply;
    }
    
    public int getDamageToApply()
    {
       return (damageToApply);
    }
    
    public LivingEntity getAfflictedEntity()
    {
        return (victim);
    }
      
    public HandlerList getHandlers()
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
