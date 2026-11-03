package techguns.world.dungeon;
import java.util.*;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import techguns.world.ScannedStructure;
import techguns.world.dungeon.TemplateSegment.SegmentType;

/** Every layer and air cell from the original Java-serialized scan remains ordered. */
public final class DungeonSegment {
    public final DungeonTemplate template;public final SegmentType type;public final int sizeX,sizeY,sizeZ;
    public final List<BlockState> palette=new ArrayList<>();private final List<String> legacyIds=new ArrayList<>();public final List<int[]> cells=new ArrayList<>();
    DungeonSegment(DungeonTemplate template,SegmentType type,JsonObject json){this.template=template;this.type=type;var size=json.getAsJsonArray("size");sizeX=size.get(0).getAsInt();sizeY=size.get(1).getAsInt();sizeZ=size.get(2).getAsInt();for(var raw:json.getAsJsonArray("palette")){var row=raw.getAsJsonObject();palette.add(ScannedStructure.sourceState(row.getAsJsonObject("state")));legacyIds.add(row.get("legacyId").getAsString());}for(var raw:json.getAsJsonArray("blocks")){var row=raw.getAsJsonArray();cells.add(new int[]{row.get(0).getAsInt(),row.get(1).getAsInt(),row.get(2).getAsInt(),row.get(3).getAsInt(),row.get(4).getAsInt()});}}
    public void placeSegment(DungeonWorld world,int x,int y,int z,int rotation){
        for(var cell:cells){if(y+cell[1]<1)continue;double dx=cell[0]+.5-sizeX*.5,dz=cell[2]+.5-sizeZ*.5;for(int i=0;i<rotation;i++){double tmp=dx;dx=dz;dz=-tmp;}var pos=BlockPos.containing(x+sizeX*.5+dx,y+cell[1],z+sizeZ*.5+dz);var state=palette.get(cell[3]);for(int i=0;i<rotation;i++)state=state.rotate(Rotation.COUNTERCLOCKWISE_90);
            // Source skull post-placement sets its rotation to 0..3 directly.
            if(legacyIds.get(cell[3]).equals("minecraft:skull")&&state.hasProperty(SkullBlock.ROTATION))state=state.setValue(SkullBlock.ROTATION,rotation);
            world.setBlockState(pos,state,2);
            if(state.getBlock() instanceof ChestBlock){var data=new CompoundTag();data.putString("LootTable","techguns:chests/castle");data.putLong("LootTableSeed",world.rand.nextLong());world.setBlockData(pos,data);}
        }
    }
}
