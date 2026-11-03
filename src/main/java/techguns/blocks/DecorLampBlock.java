package techguns.blocks;
import net.minecraft.core.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
public final class DecorLampBlock extends Block {
    public static final DirectionProperty FACING=BlockStateProperties.FACING;
    public static final BooleanProperty[] CONNECTIONS={BlockStateProperties.DOWN,BlockStateProperties.UP,BlockStateProperties.NORTH,BlockStateProperties.SOUTH,BlockStateProperties.WEST,BlockStateProperties.EAST};
    public final boolean lantern;
    public DecorLampBlock(boolean lantern){super(Properties.copy(Blocks.GLASS).strength(4).lightLevel(s->15).noOcclusion());this.lantern=lantern;var state=stateDefinition.any().setValue(FACING,Direction.DOWN);for(var property:CONNECTIONS)state=state.setValue(property,false);registerDefaultState(state);}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);b.add(CONNECTIONS);}
    @Override public BlockState rotate(BlockState state,Rotation rotation){var result=state.setValue(FACING,rotation.rotate(state.getValue(FACING)));for(Direction side:Direction.values())result=result.setValue(CONNECTIONS[rotation.rotate(side).get3DDataValue()],state.getValue(CONNECTIONS[side.get3DDataValue()]));return result;}
    @Override public BlockState mirror(BlockState state,Mirror mirror){var result=state.setValue(FACING,mirror.mirror(state.getValue(FACING)));for(Direction side:Direction.values())result=result.setValue(CONNECTIONS[mirror.mirror(side).get3DDataValue()],state.getValue(CONNECTIONS[side.get3DDataValue()]));return result;}
    private boolean supports(BlockGetter level,BlockPos pos,Direction face){var p=pos.relative(face);return level.getBlockState(p).isFaceSturdy(level,p,face.getOpposite());}
    private BlockState connections(BlockState state,BlockGetter level,BlockPos pos){for(Direction side:Direction.values())state=state.setValue(CONNECTIONS[side.get3DDataValue()],lantern&&supports(level,pos,side));return state;}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return connections(defaultBlockState().setValue(FACING,c.getClickedFace().getOpposite()),c.getLevel(),c.getClickedPos());}
    @Override public boolean canSurvive(BlockState state,LevelReader level,BlockPos pos){return supports(level,pos,state.getValue(FACING));}
    @Override public BlockState updateShape(BlockState state,Direction side,BlockState other,LevelAccessor level,BlockPos pos,BlockPos otherPos){return canSurvive(state,level,pos)?connections(state,level,pos):Blocks.AIR.defaultBlockState();}
    @Override public VoxelShape getShape(BlockState s,BlockGetter level,BlockPos pos,CollisionContext c){if(lantern)return Block.box(4,2,4,12,14,12);return switch(s.getValue(FACING)){case DOWN->Block.box(4,0,4,12,3,12);case UP->Block.box(4,13,4,12,16,12);case NORTH->Block.box(4,4,0,12,12,3);case SOUTH->Block.box(4,4,13,12,12,16);case WEST->Block.box(0,4,4,3,12,12);case EAST->Block.box(13,4,4,16,12,12);};}
}
