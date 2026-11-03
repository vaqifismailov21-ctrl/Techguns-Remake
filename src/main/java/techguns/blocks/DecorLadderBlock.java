package techguns.blocks;
import net.minecraft.core.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;
/** Source metal ladders can stand without a backing block and have a two-pixel collision. */
public final class DecorLadderBlock extends HorizontalDirectionalBlock {
    public DecorLadderBlock(){super(Properties.copy(Blocks.IRON_BLOCK).strength(6).noOcclusion());registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){Direction facing=context.getClickedFace();if(facing.getAxis().isHorizontal())return defaultBlockState().setValue(FACING,facing);for(Direction vertical:new Direction[]{Direction.DOWN,Direction.UP}){var other=context.getLevel().getBlockState(context.getClickedPos().relative(vertical));if(other.getBlock() instanceof DecorLadderBlock)return defaultBlockState().setValue(FACING,other.getValue(FACING));}return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override public boolean isLadder(BlockState state,LevelReader level,BlockPos pos,LivingEntity entity){return true;}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return switch(state.getValue(FACING)){case NORTH->Block.box(0,0,14,16,16,16);case SOUTH->Block.box(0,0,0,16,16,2);case WEST->Block.box(14,0,0,16,16,16);default->Block.box(0,0,0,2,16,16);};}
    @Override public BlockState rotate(BlockState state,Rotation rot){return state.setValue(FACING,rot.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror){return rotate(state,mirror.getRotation(state.getValue(FACING)));}
}
