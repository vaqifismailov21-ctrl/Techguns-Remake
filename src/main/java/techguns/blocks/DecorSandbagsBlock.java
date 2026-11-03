package techguns.blocks;
import net.minecraft.core.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
public final class DecorSandbagsBlock extends Block {
    public static final BooleanProperty NORTH=BlockStateProperties.NORTH,EAST=BlockStateProperties.EAST,SOUTH=BlockStateProperties.SOUTH,WEST=BlockStateProperties.WEST;
    public static final BooleanProperty NE=BooleanProperty.create("corner_ne"),ES=BooleanProperty.create("corner_es"),SW=BooleanProperty.create("corner_sw"),WN=BooleanProperty.create("corner_wn");
    private static final Direction[] DIR={Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
    private static final BooleanProperty[] SIDES={NORTH,EAST,SOUTH,WEST},CORNERS={NE,ES,SW,WN};
    public DecorSandbagsBlock(){super(Properties.ofFullCopy(Blocks.WHITE_WOOL).strength(6,15).noOcclusion());var state=stateDefinition.any();for(var property:SIDES)state=state.setValue(property,false);for(var property:CORNERS)state=state.setValue(property,false);registerDefaultState(state);}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(NORTH,EAST,SOUTH,WEST,NE,ES,SW,WN);}
    private static boolean connects(BlockGetter level,BlockPos pos,Direction side){var p=pos.relative(side);var state=level.getBlockState(p);var block=state.getBlock();return block instanceof DecorSandbagsBlock||block instanceof FenceGateBlock||!state.is(Blocks.BARRIER)&&!state.is(Blocks.MELON)&&!state.is(Blocks.PUMPKIN)&&!state.is(Blocks.CARVED_PUMPKIN)&&!state.is(Blocks.JACK_O_LANTERN)&&state.isFaceSturdy(level,p,side.getOpposite());}
    private BlockState connections(BlockState state,BlockGetter level,BlockPos pos){for(int i=0;i<4;i++){Direction a=DIR[i],b=DIR[(i+1)%4];state=state.setValue(SIDES[i],connects(level,pos,a)).setValue(CORNERS[i],connects(level,pos,a)&&connects(level,pos,b)&&(connects(level,pos.relative(a),b)||connects(level,pos.relative(b),a)));}return state;}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return connections(defaultBlockState(),c.getLevel(),c.getClickedPos());}
    @Override public BlockState updateShape(BlockState state,Direction side,BlockState other,LevelAccessor level,BlockPos pos,BlockPos otherPos){return connections(state,level,pos);}
    @Override public void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,BlockPos from,boolean moving){if(level.isClientSide)return;refresh(level,pos);for(Direction side:DIR)refresh(level,pos.relative(side));}
    private void refresh(Level level,BlockPos pos){var state=level.getBlockState(pos);if(state.is(this)){var next=connections(state,level,pos);if(next!=state)level.setBlock(pos,next,2);}}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext c){var s=connections(state,level,pos);return Block.box(s.getValue(WEST)?0:4,0,s.getValue(NORTH)?0:4,s.getValue(EAST)?16:12,16,s.getValue(SOUTH)?16:12);}
    @Override public VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext c){var s=connections(state,level,pos);var shape=Block.box(4,0,4,12,16,12);if(s.getValue(NORTH))shape=Shapes.or(shape,Block.box(4,0,0,12,16,4));if(s.getValue(EAST))shape=Shapes.or(shape,Block.box(12,0,4,16,16,12));if(s.getValue(SOUTH))shape=Shapes.or(shape,Block.box(4,0,12,12,16,16));if(s.getValue(WEST))shape=Shapes.or(shape,Block.box(0,0,4,4,16,12));return shape;}
}
