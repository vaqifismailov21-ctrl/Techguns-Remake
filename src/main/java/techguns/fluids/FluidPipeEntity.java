package techguns.fluids;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Fluid pipe. A pipe beside a tank takes up to TRANSFER mB a tick out of it and shares it among all other tanks joined
 * to the network. Pipes hold no fluid themselves, so nothing is lost when one is broken. Blocks that push fluid can also
 * fill a pipe directly.
 */
public class FluidPipeEntity extends BlockEntity {
    public static final int TRANSFER = 250;
    @Nullable FluidPipeNetworks.Network network;
    private long tick = -1;
    private int moved;
    private boolean busy;
    private final LazyOptional<IFluidHandler> handler = LazyOptional.of(() -> new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }
        @Override public int getTankCapacity(int tank) { return TRANSFER; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return true; }
        @Override public int fill(FluidStack resource, FluidAction action) {
            if (busy || resource.isEmpty() || level == null) return 0;
            int budget = Math.min(resource.getAmount(), TRANSFER - movedThisTick());
            if (budget <= 0) return 0;
            int sent = send(new FluidStack(resource, budget), null, action.execute());
            if (action.execute()) moved += sent;
            return sent;
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    });

    public FluidPipeEntity(BlockPos pos, BlockState state) {
        super(FluidPipes.ENTITY.get(), pos, state);
    }

    private int movedThisTick() {
        long now = level.getGameTime();
        if (now != tick) { tick = now; moved = 0; }
        return moved;
    }

    private FluidPipeNetworks.Network net() {
        return FluidPipeNetworks.current(level, network) ? network : FluidPipeNetworks.build(level, worldPosition);
    }

    public void tick() {
        if (level == null) return;
        BlockState state = getBlockState();
        for (Direction side : Direction.values()) {
            int budget = TRANSFER - movedThisTick();
            if (budget <= 0) return;
            if (!state.getValue(FluidPipeBlock.SIDES.get(side))) continue;
            BlockEntity other = level.getBlockEntity(worldPosition.relative(side));
            if (other == null || other instanceof FluidPipeEntity) continue;
            IFluidHandler source = other.getCapability(ForgeCapabilities.FLUID_HANDLER, side.getOpposite()).orElse(null);
            if (source == null) continue;
            FluidStack offer = source.drain(budget, IFluidHandler.FluidAction.SIMULATE);
            if (offer.isEmpty()) continue;
            int sent = send(offer, other, true);
            if (sent > 0) {
                source.drain(new FluidStack(offer, sent), IFluidHandler.FluidAction.EXECUTE);
                moved += sent;
            }
        }
    }

    /** Shares the fluid evenly among the network's tanks, never back into the one it came from. */
    private int send(FluidStack offer, @Nullable BlockEntity from, boolean execute) {
        busy = true;
        try {
            List<IFluidHandler> targets = new ArrayList<>();
            for (var target : net().targets) {
                if (!level.isLoaded(target.pos())) continue;
                BlockEntity entity = level.getBlockEntity(target.pos());
                if (entity == null || entity == from || entity instanceof FluidPipeEntity) continue;
                IFluidHandler h = entity.getCapability(ForgeCapabilities.FLUID_HANDLER, target.face()).orElse(null);
                if (h != null && h.fill(new FluidStack(offer, 1), IFluidHandler.FluidAction.SIMULATE) > 0) targets.add(h);
            }
            int remaining = offer.getAmount();
            while (!targets.isEmpty() && remaining > 0) {
                int share = Math.max(1, remaining / targets.size()), before = remaining;
                var it = targets.iterator();
                while (it.hasNext() && remaining > 0) {
                    int accepted = it.next().fill(new FluidStack(offer, Math.min(share, remaining)), execute ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
                    if (accepted <= 0) it.remove(); else remaining -= accepted;
                }
                if (remaining == before) break;
                if (!execute) break;
            }
            return offer.getAmount() - remaining;
        } finally {
            busy = false;
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return handler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }
}
