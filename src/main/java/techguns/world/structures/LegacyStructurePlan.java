package techguns.world.structures;

import java.util.*;
import java.util.function.Function;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import techguns.TGWorld;
import net.minecraft.nbt.*;

/** Final source writes, evaluated once before being split into independently saved chunk pieces. */
public class LegacyStructurePlan {
    public final RandomSource random;
    private final Function<BlockPos,BlockState> terrain;
    private final Map<BlockPos,BlockState> blocks=new LinkedHashMap<>();
    private final Map<BlockPos,CompoundTag> blockData=new LinkedHashMap<>();
    private boolean resolveAllShapes;
    public LegacyStructurePlan(RandomSource random,Function<BlockPos,BlockState> terrain){this.random=random;this.terrain=terrain;}
    /** Scans placed through ScannedStructure update every written block from its neighbours, as the live placement does. */
    public void resolveAllShapes(){resolveAllShapes=true;}
    public boolean resolvesAllShapes(){return resolveAllShapes;}
    public Map<BlockPos,BlockState> blocks(){return Collections.unmodifiableMap(blocks);}
    public Map<BlockPos,CompoundTag> blockData(){return Collections.unmodifiableMap(blockData);}
    public Set<Long> cleanupColumns(){return Set.of();}
    public BlockState getBlockState(BlockPos pos){var state=blocks.get(pos);return state!=null?state:terrain.apply(pos);}
    public boolean isAirBlock(BlockPos pos){return getBlockState(pos).isAir();}
    public void setBlockState(BlockPos pos,BlockState state,int flags){if(pos.getY()>=1){blocks.put(pos.immutable(),state);blockData.remove(pos);}}
    public void setBlockData(BlockPos pos,CompoundTag data){if(blocks.containsKey(pos))blockData.put(pos.immutable(),data.copy());}
    public static CompoundTag guardData(int remaining,int maxActive,int delay,int range,Map<String,Integer> types){
        var tag=new CompoundTag();tag.putInt("mobsLeft",remaining);tag.putInt("maxActive",maxActive);tag.putInt("delay",delay);tag.putInt("spawnDelay",delay);tag.putDouble("spawnRange",range);var mobs=new ListTag();
        for(var entry:types.entrySet()){var entity=new CompoundTag();entity.putString("id",entry.getKey());var row=new CompoundTag();row.put("Entity",entity);row.putInt("Weight",entry.getValue());mobs.add(row);}tag.put("mobtypes",mobs);return tag;
    }
    public void placeTrail(BlockPos pos,boolean onlyAir){
        if(onlyAir&&!isAirBlock(pos))return;
        // Source getStateForPlacement tries the requested NORTH face, then the horizontal plane.
        for(var facing:new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST}){
            var supportPos=pos.relative(facing.getOpposite());var support=getBlockState(supportPos);
            if(!support.isSignalSource()&&support.isFaceSturdy(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,supportPos,facing)){
                setBlockState(pos,TGWorld.SLIMY_TRAIL.get().defaultBlockState().setValue(LadderBlock.FACING,facing),2);return;
            }
        }
    }
}
