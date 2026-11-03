package techguns.world.structures.camp;
import net.minecraft.core.*;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

public final class CampRotator {
    public static BlockState getWithFacing(BlockState state,Direction target){for(var property:state.getProperties())if(property.getName().equals("facing")&&property instanceof DirectionProperty direction){if(!direction.getPossibleValues().contains(target))throw new IllegalArgumentException("Unsupported source camp facing "+state+" "+target);return state.setValue(direction,target);}return state;}
    public static BlockState getRotatedHorizontal(BlockState state,int times){for(int i=0;i<Math.floorMod(times,4);i++)state=state.rotate(Rotation.COUNTERCLOCKWISE_90);return state;}
    private static boolean cube(CampWorld world,BlockPos pos){return world.getBlockState(pos).isSolidRender(EmptyBlockGetter.INSTANCE,pos);}
    public static void placeDoor(CampWorld world,BlockPos pos,Direction facing,Block door,boolean right){
        var clockwise=pos.relative(facing.getClockWise());var counter=pos.relative(facing.getCounterClockWise());int i=(cube(world,counter)?1:0)+(cube(world,counter.above())?1:0),j=(cube(world,clockwise)?1:0)+(cube(world,clockwise.above())?1:0);boolean flag=world.getBlockState(counter).is(door)||world.getBlockState(counter.above()).is(door),flag1=world.getBlockState(clockwise).is(door)||world.getBlockState(clockwise.above()).is(door);
        if((!flag||flag1)&&j<=i){if(flag1&&!flag||j<i)right=false;}else right=true;
        var state=door.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.HINGE,right?DoorHingeSide.RIGHT:DoorHingeSide.LEFT).setValue(DoorBlock.POWERED,false).setValue(DoorBlock.OPEN,false);world.setBlockState(pos,state.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),2);world.setBlockState(pos.above(),state.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),2);
    }
    private CampRotator(){}
}
