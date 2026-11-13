package techguns.fluids;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import techguns.TGItems;
import techguns.TGWeaponBlocks;

public final class FluidPipes {
    public static final RegistryObject<Block> PIPE = TGWeaponBlocks.BLOCKS.register("fluid_pipe", FluidPipeBlock::new);
    public static final RegistryObject<Item> PIPE_ITEM = TGItems.ITEMS.register("fluid_pipe", () -> new BlockItem(PIPE.get(), new Item.Properties()) {
        @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack, @javax.annotation.Nullable net.minecraft.world.level.Level level, java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
            tooltip.add(net.minecraft.network.chat.Component.translatable("techguns.tooltip.fluid_pipe", FluidPipeEntity.TRANSFER).withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    });
    public static final RegistryObject<BlockEntityType<FluidPipeEntity>> ENTITY = TGWeaponBlocks.BLOCK_ENTITIES.register("fluid_pipe",
            () -> BlockEntityType.Builder.of(FluidPipeEntity::new, PIPE.get()).build(null));

    public static final RegistryObject<Block> TANK = TGWeaponBlocks.BLOCKS.register("fluid_tank", FluidTankBlock::new);
    public static final RegistryObject<Item> TANK_ITEM = TGItems.ITEMS.register("fluid_tank", () -> new BlockItem(TANK.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockEntityType<FluidTankEntity>> TANK_ENTITY = TGWeaponBlocks.BLOCK_ENTITIES.register("fluid_tank",
            () -> BlockEntityType.Builder.of(FluidTankEntity::new, TANK.get()).build(null));

    public static void init() {}
    private FluidPipes() {}
}
