package techguns.world.structures;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import techguns.*;
import techguns.tileentities.NpcSpawnerEntity;

/** A source plan is split at chunk borders; no piece writes into a neighbor's generation region. */
public final class PlannedStructurePiece extends StructurePiece {
    private final List<BlockState> palette=new ArrayList<>();
    private final int[] cells;
    private final Map<Integer,CompoundTag> blockData=new HashMap<>();
    private final long[] cleanupColumns;
    /** Every non-air block is updated from its neighbours when the chunk is finished (scans placed like the live feature). */
    private final boolean resolveAllShapes;
    public PlannedStructurePiece(Map<BlockPos,BlockState> blocks,Map<BlockPos,CompoundTag> data,Set<Long> cleanup){this(blocks,data,cleanup,false);}
    public PlannedStructurePiece(Map<BlockPos,BlockState> blocks,Map<BlockPos,CompoundTag> data,Set<Long> cleanup,boolean resolveAllShapes){
        super(TGWorld.PLANNED_PIECE.get(),0,BoundingBox.encapsulatingPositions(blocks.keySet()).orElseThrow());
        this.resolveAllShapes=resolveAllShapes;var ids=new LinkedHashMap<BlockState,Integer>();cells=new int[blocks.size()*4];int i=0;
        var sample=blocks.keySet().iterator().next();cleanupColumns=cleanup.stream().filter(key->ChunkPos.getX(key)>>4==sample.getX()>>4&&ChunkPos.getZ(key)>>4==sample.getZ()>>4).mapToLong(Long::longValue).toArray();
        for(var entry:blocks.entrySet()){var pos=entry.getKey();if(data.containsKey(pos))blockData.put(i/4,data.get(pos).copy());cells[i++]=pos.getX();cells[i++]=pos.getY();cells[i++]=pos.getZ();cells[i++]=ids.computeIfAbsent(entry.getValue(),key->{palette.add(key);return palette.size()-1;});}
    }
    public PlannedStructurePiece(StructurePieceSerializationContext context,CompoundTag tag){
        super(TGWorld.PLANNED_PIECE.get(),tag);cells=tag.getIntArray("SourceCells");
        cleanupColumns=tag.getLongArray("SourceCleanupColumns");resolveAllShapes=tag.getBoolean("SourceResolveShapes");
        for(var row:tag.getList("SourcePalette",Tag.TAG_COMPOUND))palette.add(NbtUtils.readBlockState(context.registryAccess().registryOrThrow(Registries.BLOCK).asLookup(),(CompoundTag)row));
        for(var row:tag.getList("SourceBlockData",Tag.TAG_COMPOUND)){var entry=(CompoundTag)row;int index=entry.getInt("Cell");if(index<0||index>=cells.length/4)throw new IllegalStateException("Invalid source block data index");blockData.put(index,entry.getCompound("Data").copy());}
        if(cells.length%4!=0)throw new IllegalStateException("Invalid source plan cell array");
        for(int i=3;i<cells.length;i+=4)if(cells[i]<0||cells[i]>=palette.size())throw new IllegalStateException("Invalid source plan palette index");
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag tag){
        var states=new ListTag();for(var state:palette)states.add(NbtUtils.writeBlockState(state));tag.put("SourcePalette",states);tag.putIntArray("SourceCells",cells);tag.putLongArray("SourceCleanupColumns",cleanupColumns);if(resolveAllShapes)tag.putBoolean("SourceResolveShapes",true);var data=new ListTag();for(var entry:new TreeMap<>(blockData).entrySet()){var row=new CompoundTag();row.putInt("Cell",entry.getKey());row.put("Data",entry.getValue().copy());data.add(row);}tag.put("SourceBlockData",data);
    }
    public static List<PlannedStructurePiece> split(LegacyStructurePlan plan){
        var chunks=new TreeMap<Long,Map<BlockPos,BlockState>>();
        for(var entry:plan.blocks().entrySet())chunks.computeIfAbsent(ChunkPos.asLong(entry.getKey().getX()>>4,entry.getKey().getZ()>>4),key->new LinkedHashMap<>()).put(entry.getKey(),entry.getValue());
        return chunks.values().stream().map(blocks->new PlannedStructurePiece(blocks,plan.blockData(),plan.cleanupColumns(),plan.resolvesAllShapes())).toList();
    }
    public int cellCount(){return cells.length/4;}
    /** Planned block of every cell, in write order. */
    public void forEachCell(java.util.function.BiConsumer<BlockPos,BlockState> consumer){for(int i=0;i<cells.length;i+=4)consumer.accept(new BlockPos(cells[i],cells[i+1],cells[i+2]),palette.get(cells[i+3]));}
    @Override public void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
        for(long column:cleanupColumns){int x=ChunkPos.getX(column),z=ChunkPos.getZ(column);if(x>=clip.minX()&&x<=clip.maxX()&&z>=clip.minZ()&&z<=clip.maxZ())techguns.world.structures.camp.CampTerrain.cleanupNativeColumn(world,x,z);}
        for(int pass=0;pass<2;pass++)for(int i=0;i<cells.length;i+=4){
            var state=palette.get(cells[i+3]);boolean trail=state.getBlock() instanceof LadderBlock;if(trail!=(pass==1))continue;
            var pos=new BlockPos(cells[i],cells[i+1],cells[i+2]);if(!clip.isInside(pos)||world.isOutsideBuildHeight(pos))continue;
            // 1.12 source flag 2 did not perform modern neighbor-shape cascades during placement.
            // Keep the complete source plan while writing; native postprocessing resolves ordinary attachments.
            world.setBlock(pos,state,18);
            var data=blockData.get(i/4);if(data!=null&&world.getBlockEntity(pos)!=null){var entity=world.getBlockEntity(pos);entity.load(data.copy());entity.setChanged();}
            boolean postprocess=resolveAllShapes&&!state.isAir()&&state.getFluidState().isEmpty()||trail&&!state.is(TGWorld.SLIMY_TRAIL.get())||state.is(net.minecraft.tags.BlockTags.FENCES)||state.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock||state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock||state.getBlock() instanceof net.minecraft.world.level.block.StairBlock||state.getBlock() instanceof techguns.blocks.DecorSandbagsBlock||state.getBlock() instanceof techguns.blocks.DecorNetBlock;
            var chunkAccess=world.getChunk(pos);if(postprocess&&chunkAccess.getStatus()!=net.minecraft.world.level.chunk.ChunkStatus.FULL)chunkAccess.markPosForPostprocessing(pos);
        }
    }
}
