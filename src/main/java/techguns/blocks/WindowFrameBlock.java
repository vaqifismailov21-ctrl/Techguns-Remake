package techguns.blocks;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

/** Metal window frame. Right-click with a glass pane to glaze it, with an empty hand to take the pane out again. */
public class WindowFrameBlock extends HorizontalDirectionalBlock {
    public enum Pane implements StringRepresentable {
        NONE, GLASS, WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY, LIGHT_GRAY, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK;
        @Override public String getSerializedName() { return name().toLowerCase(Locale.ROOT); }
        public Item item() {
            if (this == NONE) return Items.AIR;
            if (this == GLASS) return Items.GLASS_PANE;
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", getSerializedName() + "_stained_glass_pane"));
        }
        public static Pane of(ItemStack stack) {
            if (stack.isEmpty()) return NONE;
            for (Pane pane : values()) if (pane != NONE && stack.is(pane.item())) return pane;
            return NONE;
        }
        /** Texture of the glass for the block models. */
        public String texture() { return this == GLASS ? "minecraft:block/glass" : "minecraft:block/" + DyeColor.valueOf(name()).getName() + "_stained_glass"; }
    }
    public static final EnumProperty<Pane> PANE = EnumProperty.create("pane", Pane.class);
    private static final VoxelShape FRAME_X = Shapes.or(box(0, 0, 6, 2, 16, 10), box(14, 0, 6, 16, 16, 10), box(2, 0, 6, 14, 2, 10),
            box(2, 14, 6, 14, 16, 10), box(2, 5, 6, 14, 7, 10), box(2, 9, 6, 14, 11, 10));
    private static final VoxelShape FRAME_Z = Shapes.or(box(6, 0, 0, 10, 16, 2), box(6, 0, 14, 10, 16, 16), box(6, 0, 2, 10, 2, 14),
            box(6, 14, 2, 10, 16, 14), box(6, 5, 2, 10, 7, 14), box(6, 9, 2, 10, 11, 14));
    private static final VoxelShape OUTLINE_X = box(0, 0, 6, 16, 16, 10), OUTLINE_Z = box(6, 0, 0, 10, 16, 16);
    private static final VoxelShape GLAZED_X = Shapes.or(FRAME_X, box(0, 0, 7, 16, 16, 9)), GLAZED_Z = Shapes.or(FRAME_Z, box(7, 0, 0, 9, 16, 16));

    public WindowFrameBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PANE, Pane.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PANE);
    }

    private static boolean xPlane(BlockState state) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        return defaultBlockState().setValue(FACING, face.getAxis().isHorizontal() ? face : context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return xPlane(state) ? OUTLINE_X : OUTLINE_Z;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean glazed = state.getValue(PANE) != Pane.NONE;
        return xPlane(state) ? (glazed ? GLAZED_X : FRAME_X) : (glazed ? GLAZED_Z : FRAME_Z);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        Pane current = state.getValue(PANE);
        if (current == Pane.NONE) {
            Pane pane = Pane.of(held);
            if (pane == Pane.NONE) return InteractionResult.PASS;
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(PANE, pane), 3);
                if (!player.getAbilities().instabuild) held.shrink(1);
                level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!held.isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(PANE, Pane.NONE), 3);
            ItemStack pane = new ItemStack(current.item());
            if (!player.getInventory().add(pane)) player.drop(pane, false);
            level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.8F, 1.2F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && state.getValue(PANE) != Pane.NONE && !level.isClientSide)
            popResource(level, pos, new ItemStack(state.getValue(PANE).item()));
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
