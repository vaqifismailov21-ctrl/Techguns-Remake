package techguns.power.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.energy.IEnergyStorage;

/** A block wires hang on: connectors, transformers, pylons. Implemented by the main block of the host. */
public interface WireHost {
    /** Number of points wires can hang on. */
    int points(BlockState state);
    /** World position of a point. */
    Vec3 point(BlockState state,BlockPos pos,int index);
    /** Whether a point takes a wire of this kind. */
    boolean accepts(BlockState state,int index,WireTier tier);
    /** Where energy arriving over the wires at this point goes, or null (relays). from: the point it comes from. */
    default IEnergyStorage sink(Level level,BlockPos pos,BlockState state,int index){return null;}
    /** Fill (0..1) of a storing sink; energy only flows to a storing sink that is emptier than the one it comes from. */
    default float fill(Level level,BlockPos pos){return -1;}
}
