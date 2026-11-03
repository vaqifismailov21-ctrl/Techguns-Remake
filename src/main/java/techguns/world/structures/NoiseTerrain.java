package techguns.world.structures;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

/**
 * Terrain of the noise stage, before any chunk exists, for planning structures. Vanilla getBaseColumn/getBaseHeight
 * build a one-cell noise chunk for every single column, which made planning a military base take seconds (several
 * thousand columns) and froze /locate. Here whole chunks are filled at once with the same loop as the chunk generator
 * (without structure beards or blending, like getBaseColumn), so neighbouring columns share their noise cells.
 */
public final class NoiseTerrain {
    private final ChunkGenerator generator;
    private final RandomState randomState;
    private final LevelHeightAccessor heightAccessor;
    private final NoiseGeneratorSettings settings;
    private final int minY,height;
    private final Map<Long,BlockState[]> chunks=new HashMap<>();
    private final Map<Long,net.minecraft.world.level.NoiseColumn> columns=new HashMap<>();

    public NoiseTerrain(ChunkGenerator generator,RandomState randomState,LevelHeightAccessor heightAccessor){
        this.generator=generator;this.randomState=randomState;this.heightAccessor=heightAccessor;
        if(generator instanceof NoiseBasedChunkGenerator noise){settings=noise.generatorSettings().value();var clamped=settings.noiseSettings().clampToHeightAccessor(heightAccessor);minY=clamped.minY();height=clamped.height();}
        else{settings=null;minY=heightAccessor.getMinBuildHeight();height=heightAccessor.getHeight();}
    }

    public BlockState block(int x,int y,int z){
        if(settings==null)return columns.computeIfAbsent(ChunkPos.asLong(x,z),key->generator.getBaseColumn(x,z,heightAccessor,randomState)).getBlock(y);
        if(y<minY||y>=minY+height)return Blocks.AIR.defaultBlockState();
        long key=ChunkPos.asLong(x>>4,z>>4);var states=chunks.computeIfAbsent(key,k->fill(x>>4,z>>4,false));
        var state=states[index(x&15,y-minY,z&15)];
        // Below the surface margin of a quick fill: fill the whole chunk (same values, the noise is positional).
        if(state==null){states=fill(x>>4,z>>4,true);chunks.put(key,states);state=states[index(x&15,y-minY,z&15)];}
        return state;
    }
    public BlockState block(BlockPos pos){return block(pos.getX(),pos.getY(),pos.getZ());}

    /** Same result as ChunkGenerator.getBaseHeight: one above the highest block matching the heightmap. */
    public int height(int x,int z,Heightmap.Types type){
        if(settings==null)return generator.getBaseHeight(x,z,type,heightAccessor,randomState);
        Predicate<BlockState> opaque=type.isOpaque();
        for(int y=minY+height-1;y>=minY;y--)if(opaque.test(block(x,y,z)))return y+1;
        return heightAccessor.getMinBuildHeight();
    }

    private int index(int x,int y,int z){return (y*16+z)*16+x;}

    /** No structure terrain adaptation, like the marker getBaseColumn uses (the marker itself is not public). */
    private static final DensityFunctions.BeardifierOrMarker NO_BEARD=new DensityFunctions.BeardifierOrMarker(){
        @Override public double compute(net.minecraft.world.level.levelgen.DensityFunction.FunctionContext context){return 0;}
        @Override public void fillArray(double[] values,net.minecraft.world.level.levelgen.DensityFunction.ContextProvider provider){java.util.Arrays.fill(values,0);}
        @Override public double minValue(){return 0;}
        @Override public double maxValue(){return 0;}
    };
    /** Exposes the interpolated block state that NoiseChunk only offers to its own package. */
    private static final class Chunk extends NoiseChunk {
        Chunk(int cells,RandomState random,int x,int z,net.minecraft.world.level.levelgen.NoiseSettings noise,DensityFunctions.BeardifierOrMarker beardifier,NoiseGeneratorSettings settings,Aquifer.FluidPicker fluids,Blender blender){super(cells,random,x,z,noise,beardifier,settings,fluids,blender);}
        BlockState state(){return getInterpolatedState();}
    }
    /** NoiseBasedChunkGenerator.createFluidPicker. */
    private static Aquifer.FluidPicker fluidPicker(NoiseGeneratorSettings settings){
        var lava=new Aquifer.FluidStatus(-54,Blocks.LAVA.defaultBlockState());int sea=settings.seaLevel();var water=new Aquifer.FluidStatus(sea,settings.defaultFluid());
        return (x,y,z)->y<Math.min(-54,sea)?lava:water;
    }

    /** Blocks kept below the highest block of every column in a quick fill; deeper queries fill the whole chunk. */
    private static final int SURFACE_MARGIN=24;
    /**
     * The fill loop of NoiseBasedChunkGenerator.doFill, writing into an array instead of chunk sections. A quick fill
     * stops each noise cell column once all its block columns reached their highest block and SURFACE_MARGIN more;
     * the skipped blocks stay null. Above the noise range everything is air.
     */
    private BlockState[] fill(int chunkX,int chunkZ,boolean full){
        var states=new BlockState[16*16*height];
        var noiseSettings=settings.noiseSettings().clampToHeightAccessor(heightAccessor);
        int cellWidth=noiseSettings.getCellWidth(),cellHeight=noiseSettings.getCellHeight(),cellsXZ=16/cellWidth;
        int minCellY=Mth.floorDiv(noiseSettings.minY(),cellHeight),cellCountY=Mth.floorDiv(noiseSettings.height(),cellHeight);
        int minX=chunkX*16,minZ=chunkZ*16;
        var noise=new Chunk(cellsXZ,randomState,minX,minZ,noiseSettings,NO_BEARD,settings,fluidPicker(settings),Blender.empty());
        noise.initializeForFirstCellX();
        for(int cellX=0;cellX<cellsXZ;cellX++){
            noise.advanceCellX(cellX);
            for(int cellZ=0;cellZ<cellsXZ;cellZ++){int[] top=new int[cellWidth*cellWidth];java.util.Arrays.fill(top,Integer.MIN_VALUE);
              for(int cellY=cellCountY-1;cellY>=0;cellY--){
                if(!full){int lowest=Integer.MAX_VALUE;for(int t:top)lowest=Math.min(lowest,t);if(lowest!=Integer.MIN_VALUE&&lowest-((minCellY+cellY+1)*cellHeight)>=SURFACE_MARGIN)break;}
                noise.selectCellYZ(cellY,cellZ);
                for(int inY=cellHeight-1;inY>=0;inY--){
                    int y=(minCellY+cellY)*cellHeight+inY;noise.updateForY(y,(double)inY/cellHeight);
                    for(int inX=0;inX<cellWidth;inX++){
                        int x=minX+cellX*cellWidth+inX;noise.updateForX(x,(double)inX/cellWidth);
                        for(int inZ=0;inZ<cellWidth;inZ++){
                            int z=minZ+cellZ*cellWidth+inZ;noise.updateForZ(z,(double)inZ/cellWidth);
                            var state=noise.state();if(state==null)state=settings.defaultBlock();
                            if(y>=minY&&y<minY+height)states[index(x&15,y-minY,z&15)]=state;
                            if(!state.isAir()&&top[inX*cellWidth+inZ]==Integer.MIN_VALUE)top[inX*cellWidth+inZ]=y;
                        }
                    }
                }
              }
            }
            noise.swapSlices();
        }
        noise.stopInterpolation();
        // Above the noise range (clamped settings) nothing generates.
        for(int i=0;i<states.length;i++)if(states[i]==null&&full)states[i]=Blocks.AIR.defaultBlockState();
        if(!full){int noiseTop=noiseSettings.minY()+noiseSettings.height();for(int y=Math.max(noiseTop,minY);y<minY+height;y++)for(int x=0;x<16;x++)for(int z=0;z<16;z++)states[index(x,y-minY,z)]=Blocks.AIR.defaultBlockState();}
        return states;
    }
}
