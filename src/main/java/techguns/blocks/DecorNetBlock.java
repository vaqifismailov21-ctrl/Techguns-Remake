package techguns.blocks;
import net.minecraft.core.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.*;
public final class DecorNetBlock extends Block {
    public static final IntegerProperty CONNECTION=IntegerProperty.create("connection",0,15);
    public final boolean top;
    public DecorNetBlock(boolean top){super(Properties.ofFullCopy(Blocks.WHITE_WOOL).strength(2).noOcclusion());this.top=top;registerDefaultState(stateDefinition.any().setValue(CONNECTION,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(CONNECTION);}
    public int connections(BlockGetter level,BlockPos pos){int bits=0;Direction[] directions={Direction.WEST,Direction.SOUTH,Direction.EAST,Direction.NORTH};for(int i=0;i<4;i++)if(level.getBlockState(pos.relative(directions[i])).getBlock() instanceof DecorNetBlock net&&net.top==top)bits|=1<<i;return bits;}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(CONNECTION,connections(c.getLevel(),c.getClickedPos()));}
    @Override public BlockState updateShape(BlockState state,Direction side,BlockState other,LevelAccessor level,BlockPos pos,BlockPos otherPos){return state.setValue(CONNECTION,connections(level,pos));}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext c){int n=connections(level,pos);if(top){double x0=(n&1)!=0?0:(n&4)!=0?7:4,x1=(n&4)!=0?16:(n&1)!=0?9:12,z0=(n&8)!=0?0:(n&2)!=0?7:4,z1=(n&2)!=0?16:(n&8)!=0?9:12;return Block.box(x0,0,z0,x1,1,z1);}return Block.box((n&1)!=0?0:7,0,(n&8)!=0?0:7,(n&4)!=0?16:9,16,(n&2)!=0?16:9);}
    @Override public VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext c){if(top)return getShape(state,level,pos,c);int n=connections(level,pos);VoxelShape shape=Block.box(7,0,7,9,16,9);if((n&1)!=0)shape=Shapes.or(shape,Block.box(0,0,7,7,16,9));if((n&2)!=0)shape=Shapes.or(shape,Block.box(7,0,9,9,16,16));if((n&4)!=0)shape=Shapes.or(shape,Block.box(9,0,7,16,16,9));if((n&8)!=0)shape=Shapes.or(shape,Block.box(7,0,0,9,16,7));return shape;}
}
