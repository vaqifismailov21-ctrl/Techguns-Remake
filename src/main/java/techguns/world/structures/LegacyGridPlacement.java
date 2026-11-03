package techguns.world.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import techguns.TGConfig;
import techguns.TGWorld;

/**
 * The original fixed chunk grid of TGStructureSpawnRegister (every spawnWeightTGStructureSmall/Medium/Big chunks, no
 * random offset). It extends random spread only so that vanilla /locate searches it: the search steps by spacing() and
 * asks getPotentialStructureChunk() for the site of each cell, so only real grid sites are checked.
 */
public final class LegacyGridPlacement extends RandomSpreadStructurePlacement {
    public static final com.mojang.serialization.MapCodec<LegacyGridPlacement> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
        Codec.STRING.fieldOf("grid").forGetter(p->p.grid),
        Codec.INT.fieldOf("salt").forGetter(LegacyGridPlacement::saltValue)
    ).apply(i,LegacyGridPlacement::new));
    private final String grid;
    public LegacyGridPlacement(String grid,int salt){
        super(Vec3i.ZERO,StructurePlacement.FrequencyReductionMethod.DEFAULT,1f,salt,Optional.empty(),2,1,RandomSpreadType.LINEAR);
        if(!grid.equals("small")&&!grid.equals("medium")&&!grid.equals("big"))throw new IllegalArgumentException("Unknown Techguns structure grid "+grid);
        this.grid=grid;
    }
    private int saltValue(){return salt();}
    /** Current config value, read on every call like the original generator. */
    @Override public int spacing(){return Math.max(1,switch(grid){case "small"->TGConfig.spawnWeightTGStructureSmall;case "medium"->TGConfig.spawnWeightTGStructureMedium;default->TGConfig.spawnWeightTGStructureBig;});}
    @Override public int separation(){return 0;}
    @Override public ChunkPos getPotentialStructureChunk(long seed,int x,int z){int size=spacing();return new ChunkPos(Math.floorDiv(x,size)*size,Math.floorDiv(z,size)*size);}
    @Override protected boolean isPlacementChunk(ChunkGeneratorStructureState state,int x,int z){return TGConfig.doWorldspawn&&Math.floorMod(x,spacing())==0&&Math.floorMod(z,spacing())==0;}
    @Override public StructurePlacementType<?> type(){return TGWorld.GRID_PLACEMENT.get();}
}
