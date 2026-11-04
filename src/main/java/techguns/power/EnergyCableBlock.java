package techguns.power;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/** Energy cable: joins other cables and every block that takes or gives Forge Energy on the touching side. */
public final class EnergyCableBlock extends BaseEntityBlock {
    public static final Map<Direction,BooleanProperty> SIDES=PipeBlock.PROPERTY_BY_DIRECTION;
    private static final VoxelShape CORE=Block.box(5,5,5,11,11,11);
    private static final Map<Direction,VoxelShape> ARMS=Map.of(
            Direction.NORTH,Block.box(6,6,0,10,10,5),Direction.SOUTH,Block.box(6,6,11,10,10,16),Direction.WEST,Block.box(0,6,6,5,10,10),
            Direction.EAST,Block.box(11,6,6,16,10,10),Direction.DOWN,Block.box(6,0,6,10,5,10),Direction.UP,Block.box(6,11,6,10,16,10));
    private final Map<BlockState,VoxelShape> shapes=new java.util.concurrent.ConcurrentHashMap<>();
    public EnergyCableBlock(){
        super(Properties.of().strength(0.8f,3).sound(SoundType.METAL).noOcclusion());
        var state=stateDefinition.any();for(var p:SIDES.values())state=state.setValue(p,false);registerDefaultState(state);
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){SIDES.values().forEach(builder::add);}
    public boolean connects(BlockGetter level,BlockPos pos,Direction side){
        var other=pos.relative(side);if(level.getBlockState(other).is(this))return true;
        var entity=level.getBlockEntity(other);
        return entity!=null&&entity.getCapability(ForgeCapabilities.ENERGY,side.getOpposite()).map(h->h.canExtract()||h.canReceive()).orElse(false);
    }
    private BlockState connected(BlockState state,BlockGetter level,BlockPos pos){for(var e:SIDES.entrySet())state=state.setValue(e.getValue(),connects(level,pos,e.getKey()));return state;}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return connected(defaultBlockState(),context.getLevel(),context.getClickedPos());}
    @Override public BlockState updateShape(BlockState state,Direction side,BlockState neighbour,LevelAccessor level,BlockPos pos,BlockPos neighbourPos){return state.setValue(SIDES.get(side),connects(level,pos,side));}
    @Override public void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,BlockPos from,boolean moving){
        var next=connected(state,level,pos);if(next!=state)level.setBlock(pos,next,3);
    }
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
        return shapes.computeIfAbsent(state,s->{var shape=CORE;for(var e:SIDES.entrySet())if(s.getValue(e.getValue()))shape=Shapes.or(shape,ARMS.get(e.getKey()));return shape;});
    }
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new EnergyCableEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide?null:PowerBlocks.ticker(level,type,PowerBlocks.ENERGY_CABLE_ENTITY.get(),EnergyCableEntity::tick);}
}
