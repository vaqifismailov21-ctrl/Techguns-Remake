package techguns.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import techguns.blocks.BunkerDoorBlock;
import techguns.blocks.LargeDoorBlock;
import techguns.tileentities.LargeDoorEntity;

/**
 * Techguns NPC door handling: bunker doors, large doors and wooden doors on the path are opened and left open, so the NPC
 * walks through instead of shutting the door in front of itself.
 */
public class OpenTGDoorGoal extends Goal {
    private static final int COOLDOWN=20;
    private final Mob mob;
    private BlockPos door;
    private boolean done;
    private float startX,startZ;
    private int cooldown;
    public OpenTGDoorGoal(Mob mob){this.mob=mob;}

    @Override public boolean canUse(){
        if(!mob.horizontalCollision||!(mob.getNavigation() instanceof GroundPathNavigation nav)||!nav.canOpenDoors())return false;
        var path=nav.getPath();
        if(path==null||path.isDone())return false;
        for(int i=0;i<Math.min(path.getNextNodeIndex()+2,path.getNodeCount());i++){
            var node=path.getNode(i);var pos=new BlockPos(node.x,node.y+1,node.z);
            if(mob.distanceToSqr(pos.getX(),mob.getY(),pos.getZ())<=2.25){door=doorAt(pos);if(door==null)door=doorAt(pos.below());if(door!=null)return true;}
        }
        door=doorAt(mob.blockPosition().above());
        if(door==null)door=doorAt(mob.blockPosition());
        return door!=null;
    }
    @Override public boolean canContinueToUse(){return !done;}
    @Override public void start(){done=false;cooldown=0;startX=(float)(door.getX()+0.5-mob.getX());startZ=(float)(door.getZ()+0.5-mob.getZ());}
    @Override public boolean requiresUpdateEveryTick(){return true;}
    @Override public void tick(){
        if(door==null){done=true;return;}
        if(cooldown>0)cooldown--;
        boolean near=mob.distanceToSqr(door.getX()+0.5,door.getY()+0.5,door.getZ()+0.5)<2.25;
        if(near&&cooldown==0&&open(door)){cooldown=COOLDOWN;done=true;}
        float dx=(float)(door.getX()+0.5-mob.getX()),dz=(float)(door.getZ()+0.5-mob.getZ());
        if(startX*dx+startZ*dz<0||!near)done=true;
    }
    private BlockPos doorAt(BlockPos pos){
        BlockState state=mob.level().getBlockState(pos);
        if(state.getBlock() instanceof LargeDoorBlock)return pos;
        if(state.getBlock() instanceof BunkerDoorBlock)return pos;
        if(state.getBlock() instanceof DoorBlock door&&door.type().canOpenByHand())return pos;
        return null;
    }
    private boolean open(BlockPos pos){
        var level=mob.level();BlockState state=level.getBlockState(pos);
        if(state.getBlock() instanceof LargeDoorBlock){
            if(level.getBlockEntity(LargeDoorBlock.masterPos(pos,state)) instanceof LargeDoorEntity entity&&entity.phase()==LargeDoorBlock.Phase.CLOSED)return entity.toggle(true);
            return false;
        }
        if(state.getBlock() instanceof BunkerDoorBlock bunker)return bunker.openFor(level,pos,mob);
        if(state.getBlock() instanceof DoorBlock door&&!state.getValue(DoorBlock.OPEN)){door.setOpen(mob,level,state,pos,true);return true;}
        return false;
    }
}
