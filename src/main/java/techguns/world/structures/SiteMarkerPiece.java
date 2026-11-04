package techguns.world.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import techguns.TGWorld;

/**
 * Marks where a Techguns building was tried so /locate finds it. It places nothing: the building itself is put into
 * the finished chunk by the Techguns generator (SurfaceStructuresFeature / NetherStructuresFeature), on the real
 * terrain after decoration, like the 1.12 world generator did.
 */
public final class SiteMarkerPiece extends StructurePiece {
    public SiteMarkerPiece(BoundingBox box) { super(TGWorld.SITE_MARKER.get(), 0, box); }
    public SiteMarkerPiece(StructurePieceSerializationContext context, CompoundTag tag) { super(TGWorld.SITE_MARKER.get(), tag); }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {}
    @Override public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource random,
            BoundingBox clip, ChunkPos chunk, BlockPos pivot) {}
}
