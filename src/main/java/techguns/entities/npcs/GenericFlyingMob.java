package techguns.entities.npcs;
import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import techguns.api.npc.*;

/** Original wandering flight and collision probe over the current FlyingMob travel implementation. */
public abstract class GenericFlyingMob extends FlyingMob implements Enemy,INpcTGDamageSystem,INPCTechgunsShooter {
    protected GenericFlyingMob(EntityType<? extends GenericFlyingMob> type,Level level){super(type,level);moveControl=new FlightControl(this);}
    protected abstract int getTargetFlyHeight();
    @Override public void tick(){super.tick();if(!level.isClientSide&&level.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL)discard();}
    protected static final class RandomFly extends Goal {
        private final GenericFlyingMob host;
        RandomFly(GenericFlyingMob host){this.host=host;setFlags(EnumSet.of(Flag.MOVE));}
        @Override public boolean canUse(){var move=host.getMoveControl();if(!move.hasWanted())return true;double d=host.distanceToSqr(move.getWantedX(),move.getWantedY(),move.getWantedZ());return d<1||d>3600;}
        @Override public boolean canContinueToUse(){return false;}
        @Override public void start(){var r=host.random;double x=host.getX()+(r.nextFloat()*2-1)*16,z=host.getZ()+(r.nextFloat()*2-1)*16;double y=host.level.getHeight(Heightmap.Types.WORLD_SURFACE,(int)host.getX(),(int)host.getZ())+host.getTargetFlyHeight();host.getMoveControl().setWantedPosition(x,y,z,1);}
    }
    protected static final class LookAround extends Goal {
        private final GenericFlyingMob host;
        LookAround(GenericFlyingMob host){this.host=host;setFlags(EnumSet.of(Flag.LOOK));}
        @Override public boolean canUse(){return true;}@Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){
            var target=host.getTarget();var motion=host.getDeltaMovement();
            if(target==null){host.setYRot(-(float)Mth.atan2(motion.x,motion.z)*180/(float)Math.PI);host.yBodyRot=host.getYRot();host.setXRot(0);}
            else if(host.distanceToSqr(target)<96*96){
                host.setYRot(-(float)Mth.atan2(target.getX()-host.getX(),target.getZ()-host.getZ())*180/(float)Math.PI);host.yBodyRot=host.getYRot();host.setYHeadRot(host.getYRot());
                if(host.getY()>target.getY()){var delta=target.position().subtract(host.position()).normalize();float angle=-(float)Math.max(0,Math.min(90,90-Math.toDegrees(Math.acos(delta.dot(new Vec3(0,-1,0))))));host.getLookControl().setLookAt(target,45,angle);}
            }
        }
    }
    private static final class FlightControl extends MoveControl {
        private final GenericFlyingMob host;private int cooldown;
        FlightControl(GenericFlyingMob host){super(host);this.host=host;}
        @Override public void tick(){
            if(operation!=Operation.MOVE_TO)return;
            var delta=new Vec3(wantedX-host.getX(),wantedY-host.getY(),wantedZ-host.getZ());double distance=delta.length();
            if(cooldown--<=0){cooldown+=host.random.nextInt(5)+2;if(distance<1e-8){operation=Operation.WAIT;return;}
                var step=delta.scale(1/distance);var box=host.getBoundingBox();boolean clear=true;
                for(int i=1;i<distance;i++){box=box.move(step);if(!host.level.noCollision(host,box)){clear=false;break;}}
                if(clear)host.setDeltaMovement(host.getDeltaMovement().add(step.scale(.1)));else operation=Operation.WAIT;
            }
        }
    }
}
