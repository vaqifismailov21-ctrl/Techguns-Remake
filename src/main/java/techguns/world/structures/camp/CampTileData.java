package techguns.world.structures.camp;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import techguns.world.structures.LegacyStructurePlan;

/** Small planning proxies serialize source post-placement actions into the native chunk piece. */
public abstract class CampTileData {
    protected final CampWorld world;protected final BlockPos pos;
    CampTileData(CampWorld world,BlockPos pos){this.world=world;this.pos=pos;}
    protected CompoundTag data(){var existing=world.blockData().get(pos);return existing==null?new CompoundTag():existing.copy();}
    protected void save(CompoundTag tag){world.setBlockData(pos,tag);}
}
final class CampChestData extends CampTileData {
    CampChestData(CampWorld world,BlockPos pos){super(world,pos);}
    public void setLootTable(ResourceLocation table,long seed){var tag=data();tag.putString("LootTable",table.toString());tag.putLong("LootTableSeed",seed);save(tag);}
}
final class CampSpawnerData extends CampTileData {
    CampSpawnerData(CampWorld world,BlockPos pos){super(world,pos);if(!world.blockData().containsKey(pos))save(LegacyStructurePlan.guardData(5,3,200,2,new LinkedHashMap<>()));}
    public void addMobType(String entity,int weight){var tag=data();var rows=tag.getList("mobtypes",Tag.TAG_COMPOUND);var type=new CompoundTag();type.putString("id",entity);var row=new CompoundTag();row.put("Entity",type);row.putInt("Weight",weight);rows.add(row);tag.put("mobtypes",rows);save(tag);}
    public void setParams(int remaining,int maxActive,int delay,int range){var tag=data();tag.putInt("mobsLeft",remaining);tag.putInt("maxActive",maxActive);tag.putInt("delay",delay);tag.putInt("spawnDelay",delay);tag.putDouble("spawnRange",range);save(tag);}
    public void setSpawnHeightOffset(int height){var tag=data();tag.putInt("spawnHeightOffset",height);save(tag);}
}
