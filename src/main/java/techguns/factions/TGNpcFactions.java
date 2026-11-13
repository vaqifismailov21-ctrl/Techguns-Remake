package techguns.factions;
import net.minecraft.world.entity.LivingEntity;
import techguns.api.npc.factions.*;
/** Original symmetric NPC hostility table. Player machine access remains in MachineSecurity. */
public final class TGNpcFactions {
    public static boolean isHostile(TGNpcFaction a,TGNpcFaction b){return a!=b&&(a==TGNpcFaction.HOSTILE||b==TGNpcFaction.HOSTILE);}
    public static boolean canRetaliate(LivingEntity owner,LivingEntity attacker){return NpcWarGroup.enemies(owner,attacker)||!(owner instanceof ITGNpcTeam a&&attacker instanceof ITGNpcTeam b)||isHostile(a.getTGFaction(),b.getTGFaction());}
    /**
     * Original turret PvP rule. Without a team integration (the original FTBLib plugin) allies and team members
     * are everyone, enemies are no one: settings 1-3 attack nobody and 4 attacks everyone but the owner.
     */
    public static boolean shouldAttack(java.util.UUID owner,java.util.UUID target,int pvpsetting){
        if(owner.equals(target))return false;
        return switch(pvpsetting){case 4->true;default->false;};
    }
    private TGNpcFactions(){}
}
