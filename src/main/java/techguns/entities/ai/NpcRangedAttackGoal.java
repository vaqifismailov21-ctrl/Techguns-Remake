package techguns.entities.ai;
import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import techguns.items.guns.GenericGun;

/** Original EntityAIRangedAttack, including distance-dependent burst timing and twenty visible ticks. Shared by NPCs and turrets, as in the original. */
public final class NpcRangedAttackGoal<T extends PathfinderMob & RangedAttackMob> extends Goal {
    private final T host;
    private LivingEntity target;
    private int rangedAttackTime=-1,ticksTargetSeen,burstCount;
    private final int attackTime,variance,maxBurstCount,shotDelay;
    private final float range,rangeSquared;
    public NpcRangedAttackGoal(T host,GenericGun gun){
        this.host=host;attackTime=gun.aiAttackTime;variance=attackTime/3;range=gun.aiAttackRange;rangeSquared=range*range;maxBurstCount=gun.aiBurstCount;burstCount=maxBurstCount;shotDelay=gun.aiBurstAttackTime;
        setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));
    }
    @Override public boolean canUse(){target=host.getTarget();return target!=null&&target.isAlive();}
    @Override public boolean canContinueToUse(){return canUse()||!host.getNavigation().isDone();}
    @Override public void stop(){target=null;ticksTargetSeen=0;rangedAttackTime=-1;}
    @Override public boolean requiresUpdateEveryTick(){return true;}
    @Override public void tick(){
        if(target==null)return;
        double distance=host.distanceToSqr(target);boolean visible=host.getSensing().hasLineOfSight(target);
        ticksTargetSeen=visible?ticksTargetSeen+1:0;
        if(distance<=rangeSquared&&ticksTargetSeen>=20)host.getNavigation().stop();else host.getNavigation().moveTo(target,1);
        host.getLookControl().setLookAt(target,30,55);
        float factor=Mth.sqrt((float)distance)/range;
        if(--rangedAttackTime==0){
            if(distance>rangeSquared||!visible)return;
            host.performRangedAttack(target,Mth.clamp(factor,.1f,1));
            if(maxBurstCount>0)burstCount--;
            if(burstCount>0)rangedAttackTime=shotDelay;
            else {burstCount=maxBurstCount;rangedAttackTime=Mth.floor(factor*(attackTime-variance)+variance);}
        }else if(rangedAttackTime<0)rangedAttackTime=Mth.floor(factor*(attackTime-variance)+variance);
    }
}
