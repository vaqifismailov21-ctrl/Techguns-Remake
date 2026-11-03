package techguns.world.structures.camp;

import java.util.*;
import java.util.function.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import techguns.blocks.NpcSpawnerBlock;
import techguns.world.structures.LegacyStructurePlan;

/** Source camp world reads see previous planned terrain edits without loading or writing distant chunks. */
public final class CampWorld extends LegacyStructurePlan {
    private final ToIntBiFunction<Integer,Integer> initialHeight;
    private final Map<Long,Integer> rawHeights=new HashMap<>(),lightHeights=new HashMap<>();
    private final Set<Long> cleanup=new LinkedHashSet<>();
    public final CampStructure.BiomeColorType color;private final int maxHeight;
    public CampWorld(RandomSource random,Function<BlockPos,BlockState> terrain,ToIntBiFunction<Integer,Integer> initialHeight,int maxHeight,int color){super(random,terrain);this.initialHeight=initialHeight;this.maxHeight=maxHeight;this.color=CampStructure.BiomeColorType.values()[color];}
    public int getActualHeight(){return maxHeight;}
    public void markCleanup(int x,int z){cleanup.add(net.minecraft.world.level.ChunkPos.asLong(x,z));}
    @Override public Set<Long> cleanupColumns(){return Collections.unmodifiableSet(cleanup);}
    public int rawHeight(int x,int z){return rawHeights.computeIfAbsent(net.minecraft.world.level.ChunkPos.asLong(x,z),key->initialHeight.applyAsInt(x,z));}
    public int getHeight(int x,int z){return lightHeights.computeIfAbsent(net.minecraft.world.level.ChunkPos.asLong(x,z),key->{int y=rawHeight(x,z);while(y>0){var pos=new BlockPos(x,y-1,z);if(getBlockState(pos).getLightBlock(EmptyBlockGetter.INSTANCE,pos)>0)return y;y--;}return 0;});}
    @Override public void setBlockState(BlockPos pos,BlockState state,int flags){if(pos.getY()<1||pos.getY()>=maxHeight)return;int x=pos.getX(),z=pos.getZ();long key=net.minecraft.world.level.ChunkPos.asLong(x,z);int h=rawHeight(x,z);super.setBlockState(pos,state,flags);lightHeights.remove(key);if(!state.isAir()&&pos.getY()>=h)rawHeights.put(key,pos.getY()+1);else if(state.isAir()&&pos.getY()==h-1){while(h>0&&getBlockState(new BlockPos(x,h-1,z)).isAir())h--;rawHeights.put(key,h);}}
    public void setBlockState(BlockPos pos,BlockState state){setBlockState(pos,state,3);}
    public void setBlockToAir(BlockPos pos){setBlockState(pos,Blocks.AIR.defaultBlockState(),3);}
    public CampTileData getTileEntity(BlockPos pos){var state=getBlockState(pos);if(state.getBlock() instanceof ChestBlock)return new CampChestData(this,pos.immutable());if(state.getBlock() instanceof NpcSpawnerBlock)return new CampSpawnerData(this,pos.immutable());return null;}
}
