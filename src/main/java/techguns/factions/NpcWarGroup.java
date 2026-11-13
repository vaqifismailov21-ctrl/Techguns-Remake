package techguns.factions;

import net.minecraft.world.entity.Entity;
import techguns.entities.npcs.*;

/** Sides of the NPC wars: soldiers against bandits and the undead, each side against the other two. */
public enum NpcWarGroup {
    NONE,ARMY,BANDITS,UNDEAD;
    public static NpcWarGroup of(Entity entity){
        if(entity instanceof ArmySoldier||entity instanceof Commando)return ARMY;
        if(entity instanceof Bandit||entity instanceof DictatorDave)return BANDITS;
        if(entity instanceof GenericNPCUndead)return UNDEAD;
        return NONE;
    }
    /** True when the two are on opposing sides and the wars are switched on in the config. */
    public static boolean enemies(Entity a,Entity b){
        if(!techguns.TGConfig.npcFactionWars||a==null||b==null)return false;
        var x=of(a);var y=of(b);
        return x!=NONE&&y!=NONE&&x!=y;
    }
}
