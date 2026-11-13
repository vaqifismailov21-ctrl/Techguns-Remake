package techguns.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import techguns.blocks.BunkerDoorBlock;
import techguns.blocks.LargeDoorBlock;

/** Lets NPC paths lead through Techguns bunker and large doors like through wooden doors. */
public class TGWalkNodeEvaluator extends WalkNodeEvaluator {
    @Override public BlockPathTypes getBlockPathType(BlockGetter level,int x,int y,int z){
        var state=level.getBlockState(new BlockPos(x,y,z));
        if(state.getBlock() instanceof BunkerDoorBlock)return state.getValue(DoorBlock.OPEN)?BlockPathTypes.DOOR_OPEN:BlockPathTypes.DOOR_WOOD_CLOSED;
        if(state.getBlock() instanceof LargeDoorBlock)return state.getValue(LargeDoorBlock.PHASE).open()?BlockPathTypes.DOOR_OPEN:BlockPathTypes.DOOR_WOOD_CLOSED;
        return super.getBlockPathType(level,x,y,z);
    }
}
