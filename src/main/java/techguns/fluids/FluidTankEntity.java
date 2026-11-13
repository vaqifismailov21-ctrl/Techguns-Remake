package techguns.fluids;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/** Fluid tank: holds one fluid of any kind; buckets, machines and fluid pipes fill and drain it from every side. */
public class FluidTankEntity extends BlockEntity {
    public static final int CAPACITY = 32000;
    public final FluidTank tank = new FluidTank(CAPACITY) {
        @Override protected void onContentsChanged() { changed(); }
    };
    private final LazyOptional<FluidTank> handler = LazyOptional.of(() -> tank);
    private int lastSent = -1;
    public FluidTankEntity(BlockPos pos, BlockState state) { super(FluidPipes.TANK_ENTITY.get(), pos, state); }

    private void changed() {
        setChanged();
        if (level == null || level.isClientSide) return;
        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        // the renderer only needs a new level when it moved by a visible step, so pipes trickling in do not flood packets
        int step = tank.getFluidAmount() * 64 / CAPACITY + (tank.isEmpty() ? 0 : 1);
        if (step != lastSent || tank.isEmpty()) { lastSent = step; level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2); }
    }
    public int comparator() { return tank.isEmpty() ? 0 : 1 + tank.getFluidAmount() * 14 / CAPACITY; }

    @Override public void load(CompoundTag tag) { super.load(tag); tank.readFromNBT(tag.getCompound("Tank")); }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.put("Tank", tank.writeToNBT(new CompoundTag())); }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return handler.cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); handler.invalidate(); }
    public FluidStack fluid() { return tank.getFluid(); }
}
