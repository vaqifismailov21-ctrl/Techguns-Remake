package techguns.world.dungeon;
import java.util.LinkedHashMap;
import net.minecraft.core.BlockPos;
import techguns.world.structures.LegacyStructurePlan;
public final class DungeonSpawner {
    private final DungeonWorld world;private final BlockPos pos;
    public DungeonSpawner(DungeonWorld world,BlockPos pos){this.world=world;this.pos=pos.immutable();}
    public void castleGuard(){var mobs=new LinkedHashMap<String,Integer>();mobs.put("techguns:zombiesoldier",1);mobs.put("techguns:skeletonsoldier",1);world.setBlockData(pos,LegacyStructurePlan.guardData(2,2,200,2,mobs));}
}
