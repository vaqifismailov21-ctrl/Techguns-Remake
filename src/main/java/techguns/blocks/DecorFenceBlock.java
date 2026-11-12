package techguns.blocks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Thick panel fence, one block high: a half-block post with arms to its neighbours. Where two arms meet beside another
 * fence the corner is filled, so 2x2 groups of fences become a solid wall.
 */
public class DecorFenceBlock extends Block {
    public static final BooleanProperty NORTH = PipeBlock.NORTH, EAST = PipeBlock.EAST, SOUTH = PipeBlock.SOUTH, WEST = PipeBlock.WEST;
    public static final BooleanProperty CORNER_NE = BooleanProperty.create("corner_ne"), CORNER_ES = BooleanProperty.create("corner_es"),
            CORNER_SW = BooleanProperty.create("corner_sw"), CORNER_WN = BooleanProperty.create("corner_wn");
    private static final VoxelShape POST = box(4, 0, 4, 12, 16, 12);
    private static final VoxelShape[] SIDE = {box(4, 0, 0, 12, 16, 4), box(12, 0, 4, 16, 16, 12), box(4, 0, 12, 12, 16, 16), box(0, 0, 4, 4, 16, 12)};
    private static final VoxelShape[] CORNER = {box(12, 0, 0, 16, 16, 4), box(12, 0, 12, 16, 16, 16), box(0, 0, 12, 4, 16, 16), box(0, 0, 0, 4, 16, 4)};
    private static final Map<Integer, VoxelShape> SHAPES = new ConcurrentHashMap<>();

    public DecorFenceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false)
                .setValue(CORNER_NE, false).setValue(CORNER_ES, false).setValue(CORNER_SW, false).setValue(CORNER_WN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, CORNER_NE, CORNER_ES, CORNER_SW, CORNER_WN);
    }

    /** True if the block at pos joins a fence arm coming from the given side. */
    public static boolean joins(BlockGetter level, BlockPos pos, Direction from) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof DecorFenceBlock || state.getBlock() instanceof FenceGateBlock) return true;
        return !isExceptionForConnection(state) && state.isFaceSturdy(level, pos, from);
    }

    private static boolean arm(BlockGetter level, BlockPos pos, Direction side) {
        return joins(level, pos.relative(side), side.getOpposite());
    }

    private static boolean corner(BlockGetter level, BlockPos pos, Direction a, Direction b) {
        return arm(level, pos, a) && arm(level, pos, b) && (arm(level, pos.relative(a), b) || arm(level, pos.relative(b), a));
    }

    public BlockState connected(BlockState state, BlockGetter level, BlockPos pos) {
        return state.setValue(NORTH, arm(level, pos, Direction.NORTH)).setValue(EAST, arm(level, pos, Direction.EAST))
                .setValue(SOUTH, arm(level, pos, Direction.SOUTH)).setValue(WEST, arm(level, pos, Direction.WEST))
                .setValue(CORNER_NE, corner(level, pos, Direction.NORTH, Direction.EAST))
                .setValue(CORNER_ES, corner(level, pos, Direction.EAST, Direction.SOUTH))
                .setValue(CORNER_SW, corner(level, pos, Direction.SOUTH, Direction.WEST))
                .setValue(CORNER_WN, corner(level, pos, Direction.WEST, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connected(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction side, BlockState neighbour, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return side.getAxis().isHorizontal() ? connected(state, level, pos) : state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int key = (state.getValue(NORTH) ? 1 : 0) | (state.getValue(EAST) ? 2 : 0) | (state.getValue(SOUTH) ? 4 : 0) | (state.getValue(WEST) ? 8 : 0)
                | (state.getValue(CORNER_NE) ? 16 : 0) | (state.getValue(CORNER_ES) ? 32 : 0) | (state.getValue(CORNER_SW) ? 64 : 0) | (state.getValue(CORNER_WN) ? 128 : 0);
        return SHAPES.computeIfAbsent(key, k -> {
            VoxelShape shape = POST;
            for (int i = 0; i < 4; i++) {
                if ((k & (1 << i)) != 0) shape = Shapes.or(shape, SIDE[i]);
                if ((k & (16 << i)) != 0) shape = Shapes.or(shape, CORNER[i]);
            }
            return shape;
        });
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }
}
