package techguns.capabilities.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;

/** Forge-named capability tokens plus the bridge to NeoForge 1.21 block/item capabilities. */
public final class Capabilities {
    public static final Capability<IItemHandler> ITEM_HANDLER = new Capability<>("item_handler");
    public static final Capability<IEnergyStorage> ENERGY = new Capability<>("energy");
    public static final Capability<IFluidHandler> FLUID_HANDLER = new Capability<>("fluid_handler");
    public static final Capability<IFluidHandlerItem> FLUID_HANDLER_ITEM = new Capability<>("fluid_handler_item");

    /** Expose item/energy/fluid handlers of a {@link ICapabilityHost} block entity type to other mods and hoppers. */
    public static <B extends BlockEntity & ICapabilityHost> void register(RegisterCapabilitiesEvent event, BlockEntityType<? extends B> type) {
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, type, (be, side) -> be.getCapability(ITEM_HANDLER, side).orElse(null));
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, type, (be, side) -> be.getCapability(ENERGY, side).orElse(null));
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, type, (be, side) -> be.getCapability(FLUID_HANDLER, side).orElse(null));
    }

    /** Capability of whatever block sits at a position (any mod), like {@code neighbour.getCapability(cap, side)} in 1.20.1. */
    @SuppressWarnings("unchecked")
    public static <T> LazyOptional<T> block(Level level, BlockPos pos, Capability<T> capability, Direction side) {
        Object found = null;
        if (capability == ITEM_HANDLER) found = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, pos, side);
        else if (capability == ENERGY) found = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, pos, side);
        else if (capability == FLUID_HANDLER) found = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, pos, side);
        return LazyOptional.ofNullable((T) found);
    }
    public static <T> LazyOptional<T> block(BlockEntity be, Capability<T> capability, Direction side) {
        if (be == null || be.getLevel() == null) return LazyOptional.empty();
        return block(be.getLevel(), be.getBlockPos(), capability, side);
    }

    /** Item capabilities (energy-holding items, fluid containers). */
    @SuppressWarnings("unchecked")
    public static <T> LazyOptional<T> item(ItemStack stack, Capability<T> capability) {
        Object found = null;
        if (capability == ENERGY) found = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.ITEM);
        else if (capability == FLUID_HANDLER_ITEM || capability == FLUID_HANDLER) found = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        else if (capability == ITEM_HANDLER) found = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.ITEM);
        return LazyOptional.ofNullable((T) found);
    }
    private Capabilities() {}
}
