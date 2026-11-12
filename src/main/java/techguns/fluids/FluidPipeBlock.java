package techguns.fluids;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
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

/** Fluid pipe: joins other pipes and every block with a fluid tank on the touching side. */
public class FluidPipeBlock extends BaseEntityBlock {
    public static final Map<Direction, BooleanProperty> SIDES = PipeBlock.PROPERTY_BY_DIRECTION;
    private static final VoxelShape CORE = box(5, 5, 5, 11, 11, 11);
    private static final Map<Direction, VoxelShape> ARMS = Map.of(Direction.NORTH, box(5, 5, 0, 11, 11, 5), Direction.SOUTH, box(5, 5, 11, 11, 11, 16),
            Direction.WEST, box(0, 5, 5, 5, 11, 11), Direction.EAST, box(11, 5, 5, 16, 11, 11), Direction.DOWN, box(5, 0, 5, 11, 5, 11), Direction.UP, box(5, 11, 5, 11, 16, 11));
    private final Map<BlockState, VoxelShape> shapes = new ConcurrentHashMap<>();

    public FluidPipeBlock() {
        super(Properties.of().strength(1.5F, 6F).sound(SoundType.METAL).noOcclusion());
        BlockState state = stateDefinition.any();
        for (BooleanProperty p : SIDES.values()) state = state.setValue(p, false);
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        SIDES.values().forEach(builder::add);
    }

    public static boolean connects(BlockGetter level, BlockPos pos, Direction side) {
        BlockPos other = pos.relative(side);
        if (level.getBlockState(other).getBlock() instanceof FluidPipeBlock) return true;
        BlockEntity entity = level.getBlockEntity(other);
        return entity != null && !(entity instanceof FluidPipeEntity) && entity.getCapability(ForgeCapabilities.FLUID_HANDLER, side.getOpposite()).isPresent();
    }

    private BlockState connected(BlockState state, BlockGetter level, BlockPos pos) {
        for (var e : SIDES.entrySet()) state = state.setValue(e.getValue(), connects(level, pos, e.getKey()));
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connected(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction side, BlockState neighbour, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        FluidPipeNetworks.changed(level);
        return state.setValue(SIDES.get(side), connects(level, pos, side));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        BlockState next = connected(state, level, pos);
        if (next != state) level.setBlock(pos, next, 3);
        FluidPipeNetworks.changed(level);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        FluidPipeNetworks.changed(level);
        super.onPlace(state, level, pos, old, moving);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        FluidPipeNetworks.changed(level);
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.computeIfAbsent(state, s -> {
            VoxelShape shape = CORE;
            for (var e : SIDES.entrySet()) if (s.getValue(e.getValue())) shape = Shapes.or(shape, ARMS.get(e.getKey()));
            return shape;
        });
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidPipeEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, FluidPipes.ENTITY.get(), (l, p, s, e) -> e.tick());
    }
}
