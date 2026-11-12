package techguns.fluids;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * Pipe networks, worked out once and shared by every pipe in them. Any change to a pipe or to a block beside one makes
 * the level's version step on, and each network is worked out again the next time it is used.
 */
public final class FluidPipeNetworks {
    public static final int MAX_SIZE = 2048;
    private static final Map<LevelAccessor, int[]> VERSIONS = new WeakHashMap<>();

    /** A block with a fluid tank, reached by a pipe through the given side of that block. */
    public record Target(BlockPos pos, Direction face) {}

    public static final class Network {
        final int version;
        public final List<Target> targets;
        Network(int version, List<Target> targets) { this.version = version; this.targets = targets; }
    }

    private FluidPipeNetworks() {}

    public static synchronized void changed(LevelAccessor level) {
        VERSIONS.computeIfAbsent(level, l -> new int[1])[0]++;
    }

    static synchronized int version(LevelAccessor level) {
        return VERSIONS.computeIfAbsent(level, l -> new int[1])[0];
    }

    static boolean current(Level level, Network network) {
        return network != null && network.version == version(level);
    }

    /** Finds every pipe joined to start and the tanks around them; all pipes found share the result. */
    static Network build(Level level, BlockPos start) {
        int version = version(level);
        List<Target> targets = new ArrayList<>();
        List<FluidPipeEntity> pipes = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty() && seen.size() < MAX_SIZE) {
            BlockPos pos = queue.poll();
            if (!(level.getBlockEntity(pos) instanceof FluidPipeEntity pipe)) continue;
            pipes.add(pipe);
            var state = pipe.getBlockState();
            for (Direction side : Direction.values()) {
                if (!state.hasProperty(FluidPipeBlock.SIDES.get(side)) || !state.getValue(FluidPipeBlock.SIDES.get(side))) continue;
                BlockPos next = pos.relative(side);
                if (!level.isLoaded(next)) continue;
                if (level.getBlockEntity(next) instanceof FluidPipeEntity) {
                    if (seen.add(next)) queue.add(next);
                } else {
                    targets.add(new Target(next, side.getOpposite()));
                }
            }
        }
        Network network = new Network(version, List.copyOf(targets));
        for (FluidPipeEntity pipe : pipes) pipe.network = network;
        return network;
    }
}
