package techguns.fluids;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

/** Steel-framed glass tank for any fluid; keeps its contents when broken. */
public class FluidTankBlock extends BaseEntityBlock {
    public FluidTankBlock() { super(Properties.of().strength(3F, 8F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops()); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FluidTankEntity(pos, state); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) { return true; }
    @Override public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) { return 1F; }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FluidTankEntity tank)) return InteractionResult.PASS;
        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) return InteractionResult.sidedSuccess(level.isClientSide);
        if (player.getItemInHand(hand).isEmpty()) {
            if (!level.isClientSide) player.displayClientMessage(describe(tank.fluid()), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
    static Component describe(FluidStack fluid) {
        return fluid.isEmpty() ? Component.translatable("techguns.tooltip.fluid_tank.empty", FluidTankEntity.CAPACITY)
                : Component.translatable("techguns.tooltip.fluid_tank.contents", fluid.getDisplayName(), fluid.getAmount(), FluidTankEntity.CAPACITY);
    }
    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) { return level.getBlockEntity(pos) instanceof FluidTankEntity tank ? tank.comparator() : 0; }
    @Override public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        var data = stack.getTagElement("BlockEntityTag");
        FluidStack fluid = data != null ? FluidStack.loadFluidStackFromNBT(data.getCompound("Tank")) : FluidStack.EMPTY;
        tooltip.add(describe(fluid).copy().withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("techguns.tooltip.fluid_tank").withStyle(ChatFormatting.DARK_GRAY));
    }
}
