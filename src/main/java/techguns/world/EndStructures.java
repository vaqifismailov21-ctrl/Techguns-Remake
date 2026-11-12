package techguns.world;

import java.util.List;
import java.util.Set;
import net.minecraft.util.RandomSource;
import techguns.TGConfig;

/** End structures of the community edition: on the big grid, only far from the main island. */
public final class EndStructures {
    private record Spawn(String id, int weight, boolean cluster) {}
    private static final List<Spawn> BIG = List.of(new Spawn("end_floating_ship", 10, false), new Spawn("end_building", 10, false), new Spawn("end_cluster", 10, true));
    public static final Set<String> IDS = Set.of("end_floating_ship", "end_building", "end_cluster");
    /** Blocks from the world centre inside which nothing is placed (the dragon island). */
    public static final int MIN_DISTANCE = 1024;

    public static String choose(int cx, int cz, RandomSource random) {
        if (!TGConfig.doWorldspawn) return null;
        int big = Math.max(1, TGConfig.spawnWeightTGStructureBig);
        if (cx % big != 0 || cz % big != 0) return null;
        long x = cx * 16L, z = cz * 16L;
        if (x * x + z * z < (long) MIN_DISTANCE * MIN_DISTANCE) return null;
        int total = BIG.stream().filter(s -> !s.cluster || TGConfig.spawnOreClusterStructures).mapToInt(Spawn::weight).sum();
        if (total == 0) return null;
        int roll = random.nextInt(total) + 1, sum = 0;
        for (var spawn : BIG) if (!spawn.cluster || TGConfig.spawnOreClusterStructures) { sum += spawn.weight; if (roll <= sum) return spawn.id; }
        return null;
    }

    private EndStructures() {}
}
