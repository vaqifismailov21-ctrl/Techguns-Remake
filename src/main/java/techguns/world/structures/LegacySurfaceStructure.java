package techguns.world.structures;

import java.util.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.*;
import techguns.*;
import techguns.world.*;

/**
 * Every structure of the original TGStructureSpawnRegister as a native structure, one registry entry per original id, so
 * that /locate finds them. All entries share the original decision per grid site (LegacyGridPlacement): a start exists
 * only where the original table chooses this kind. Large structures are planned with their own terrain logic; scanned
 * structures run the original scan placement on the terrain known before generation. Writes are split into chunk pieces,
 * so nothing is written beyond a chunk's generation region. Kind "legacy" is the earlier combined entry (castle, base,
 * carrier and nest), kept only so that worlds saved with it keep generating their started pieces.
 */
public final class LegacySurfaceStructure extends Structure {
    public static final Codec<LegacySurfaceStructure> CODEC=RecordCodecBuilder.create(i->i.group(settingsCodec(i),Codec.STRING.optionalFieldOf("kind","legacy").forGetter(s->s.kind)).apply(i,LegacySurfaceStructure::new));
    public static final Set<String> BIG=Set.of("alien_bug_nest","aircraftcarrier","military_base","castle");
    public final String kind;
    public LegacySurfaceStructure(StructureSettings settings,String kind){
        super(settings);this.kind=kind;
        if(!kind.equals("legacy")&&!SurfaceStructuresFeature.IDS.contains(kind)&&!NetherStructuresFeature.IDS.contains(kind)&&!techguns.world.EndStructures.IDS.contains(kind))throw new IllegalArgumentException("Unknown Techguns structure "+kind);
    }
    @Override public StructureType<?> type(){return TGWorld.SURFACE_STRUCTURE_TYPE.get();}
    private static final Set<String> CLUSTERS=Set.of("orecluster_spike","orecluster_meteorbase","desert_oil_cluster","nether_orecluster_small","nether_orecluster_castle","nether_orecluster_medium","end_cluster");
    /** False when the original table can never choose this kind under the current config (no worldgen, no cluster structures, no oil fluid). */
    public boolean canEverGenerate(){
        if(kind.equals("legacy"))return true;
        if(!TGConfig.doWorldspawn)return false;
        if(CLUSTERS.contains(kind)&&!TGConfig.spawnOreClusterStructures)return false;
        return !kind.equals("desert_oil_cluster")||TGOreClusters.worldOil()!=null;
    }
    /** Lowest rotated corner of a scan, as the original rotated placement computes its origin. */
    /** Community edition BlockUtils.isAreaClear: every 4th block of the volume is air. */
    private static boolean areaClear(NoiseTerrain terrain,int x,int y,int z,int sx,int sy,int sz){
        for(int i=0;i<=sx;i+=4)for(int j=0;j<=sy;j+=4)for(int k=0;k<=sz;k+=4)if(!terrain.block(x+Math.min(i,sx-1),y+Math.min(j,sy-1),z+Math.min(k,sz-1)).isAir())return false;
        return true;
    }
    /** Community edition getValidEndIslandSpawnY: top end stone every step blocks; a third must be island, at most 2*limit apart. */
    private static int endIslandY(NoiseTerrain terrain,int x,int z,int sx,int sz,int limit,int step){
        int count=0,valid=0,min=255,max=0,sum=0;
        for(int ix=0;;ix=Math.min(ix+step,sx)){
            for(int iz=0;;iz=Math.min(iz+step,sz)){count++;int h=-1;
                for(int y=255;y>0;y--){var b=terrain.block(x+ix,y,z+iz);if(b.is(net.minecraft.world.level.block.Blocks.END_STONE)||b.is(net.minecraft.world.level.block.Blocks.END_STONE_BRICKS)){h=y;break;}}
                if(h>0){valid++;min=Math.min(min,h);max=Math.max(max,h);sum+=h;}
                if(iz>=sz)break;}
            if(ix>=sx)break;}
        if(valid<2||valid<count/3||max-min>limit*2)return -1;
        return sum/valid;
    }
    private static int[] minCorner(ScannedStructure scan,int turns){
        int minX=Integer.MAX_VALUE,minZ=Integer.MAX_VALUE;
        for(var corner:List.of(new BlockPos(0,0,0),new BlockPos(scan.sizeX,0,0),new BlockPos(0,0,scan.sizeZ),new BlockPos(scan.sizeX,0,scan.sizeZ))){var point=ScannedStructure.rotate(corner,scan.sizeX/2,scan.sizeZ/2,turns);minX=Math.min(minX,point.getX());minZ=Math.min(minZ,point.getZ());}
        return new int[]{minX,minZ};
    }
    private record PlanKey(RandomState state,String kind,long seed,long chunk){}
    /** /locate first asks whether a start can exist and then generates the chunk, which plans the same start again; a few recent plans are kept. */
    private static final Map<PlanKey,Optional<GenerationStub>> RECENT=Collections.synchronizedMap(new LinkedHashMap<>(16,.75f,true){@Override protected boolean removeEldestEntry(Map.Entry<PlanKey,Optional<GenerationStub>> eldest){return size()>16;}});
    @Override protected Optional<GenerationStub> findGenerationPoint(GenerationContext context){
        var key=new PlanKey(context.randomState(),kind,context.seed(),context.chunkPos().toLong());var cached=RECENT.get(key);if(cached!=null)return cached;
        var result=findGenerationPoint0(context);
        if(result.isPresent())RECENT.put(key,result);return result;
    }
    private Optional<GenerationStub> findGenerationPoint0(GenerationContext context){
        int cx=context.chunkPos().x,cz=context.chunkPos().z,x=cx*16,z=cz*16;
        boolean ownGrid=NetherStructuresFeature.IDS.contains(kind)||techguns.world.EndStructures.IDS.contains(kind);
        // Nether and End use their own grids (community edition); their choose() checks the site.
        if(!TGConfig.doWorldspawn||!ownGrid&&(cx%TGConfig.spawnWeightTGStructureSmall!=0||cz%TGConfig.spawnWeightTGStructureSmall!=0))return Optional.empty();
        var terrain=new NoiseTerrain(context.chunkGenerator(),context.randomState(),context.heightAccessor());
        var random=SurfaceStructuresFeature.selectionRandom(context.seed(),cx,cz);
        if(techguns.world.EndStructures.IDS.contains(kind)){
            var selected=techguns.world.EndStructures.choose(cx,cz,random);if(!kind.equals(selected))return Optional.empty();
            var scan=ScannedStructure.load(selected);int turns=random.nextInt(4);var corner=minCorner(scan,turns);int sx=x+corner[0],sz=z+corner[1];
            int w=turns%2==0?scan.sizeX:scan.sizeZ,d=turns%2==0?scan.sizeZ:scan.sizeX,y;
            // Community edition: the ship at y 150 and the cluster at y 80 in open air; the station on an island (8 high tolerance, every 6 blocks).
            if(selected.equals("end_floating_ship"))y=areaClear(terrain,sx,150,sz,w,scan.sizeY,d)?151:-1;
            else if(selected.equals("end_cluster"))y=areaClear(terrain,sx,80,sz,w,scan.sizeY,d)?81:-1;
            else{int h=endIslandY(terrain,sx,sz,w,d,8,6);y=h<0?-1:h+1;}
            if(y<=0){return Optional.empty();}
            var plan=new LegacyStructurePlan(RandomSource.create(random.nextLong()),terrain::block);
            scan.place(plan,context.heightAccessor(),new BlockPos(sx,y-1,sz),turns,plan.random,RandomSource.create(random.nextLong()),0);
            return stub(plan,new BlockPos(sx,y,sz));
        }
        if(NetherStructuresFeature.IDS.contains(kind)){
            var selected=NetherStructuresFeature.choose(cx,cz,random);if(!kind.equals(selected))return Optional.empty();
            var scan=ScannedStructure.load(selected);int turns=random.nextInt(4);var corner=minCorner(scan,turns);
            int height;
            // Submarine: base 3 below the lava surface (community edition LAVA_SUBMERGE_DEPTH 4).
            if("nether_submarine".equals(selected)){int lava=NetherStructuresFeature.lavaSurface(terrain::block,x+corner[0],z+corner[1],turns%2==0?scan.sizeX:scan.sizeZ,turns%2==0?scan.sizeZ:scan.sizeX);height=lava<0?-1:lava-2;}
            else height=NetherStructuresFeature.caveHeight(terrain::block,cx,cz);if(height<0){return Optional.empty();}
            var plan=new LegacyStructurePlan(RandomSource.create(random.nextLong()),terrain::block);
            var origin=new BlockPos(x+corner[0],height-1,z+corner[1]);scan.place(plan,context.heightAccessor(),origin,turns,plan.random,plan.random,0);
            return stub(plan,origin.above());
        }
        var biome=context.biomeSource().getNoiseBiome(QuartPos.fromBlock(x),QuartPos.fromBlock(64),QuartPos.fromBlock(z),context.randomState().sampler());
        var selected=SurfaceStructuresFeature.choose(cx,cz,biome.is(net.minecraft.tags.BiomeTags.IS_OCEAN),SurfaceStructuresFeature.desert(biome),TGOreClusters.worldOil()!=null,random);
        if(kind.equals("legacy")?!BIG.contains(selected):!kind.equals(selected))return Optional.empty();
        
        if(!BIG.contains(selected)){
            var scan=ScannedStructure.load(selected);int turns=random.nextInt(4);var corner=minCorner(scan,turns);int sx=x+corner[0],sz=z+corner[1];
            int y=SurfaceStructuresFeature.surfaceHeight(new SurfaceStructuresFeature.HeightProbe(){
                @Override public int height(int px,int pz){return terrain.height(px,pz,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);}
                @Override public boolean dry(int px,int py,int pz){return terrain.block(px,py,pz).getFluidState().isEmpty();}
            },sx,sz,turns%2==0?scan.sizeX:scan.sizeZ,turns%2==0?scan.sizeZ:scan.sizeX,SurfaceStructuresFeature.flatness(selected));
            if(y<0){return Optional.empty();}
            var plan=new LegacyStructurePlan(RandomSource.create(random.nextLong()),terrain::block);
            scan.place(plan,context.heightAccessor(),new BlockPos(sx,y-1,sz),turns,plan.random,RandomSource.create(random.nextLong()),SurfaceStructuresFeature.biomeColor(biome));
            return stub(plan,new BlockPos(sx,y,sz));
        }
        if("castle".equals(selected)){
            int sx=32+random.nextInt(16),sz=32+random.nextInt(16),sy=24+random.nextInt(16),turns=random.nextInt(4);int minX=Integer.MAX_VALUE,minZ=Integer.MAX_VALUE;
            for(int px:new int[]{0,sx})for(int pz:new int[]{0,sz}){int dx=px-sx/2,dz=pz-sz/2;for(int i=0;i<turns;i++){int old=dx;dx=dz;dz=-old;}minX=Math.min(minX,dx+sx/2);minZ=Math.min(minZ,dz+sz/2);}
            int anchorX=x+minX,anchorZ=z+minZ;
            var campTerrain=new techguns.world.structures.camp.CampWorld(RandomSource.create(random.nextLong()),terrain::block,(px,pz)->terrain.height(px,pz,Heightmap.Types.WORLD_SURFACE_WG),context.heightAccessor().getMaxBuildHeight(),0);
            int y=techguns.world.structures.camp.CampTerrain.getValidSpawnYArea(campTerrain,anchorX,anchorZ,turns%2==0?sx:sz,turns%2==0?sz:sx,10,4);if(y<0)return Optional.empty();
            var plan=new techguns.world.dungeon.DungeonWorld(campTerrain.random,campTerrain::getBlockState);var preset=techguns.world.dungeon.IDungeonPreset.PRESET_CASTLE;new techguns.world.dungeon.Dungeon(preset).generate(plan,anchorX,y-preset.getSizeY(),anchorZ,sx,sy,sz);var pieces=PlannedStructurePiece.split(plan);
            return Optional.of(new GenerationStub(new BlockPos(anchorX,y,anchorZ),builder->pieces.forEach(builder::addPiece)));
        }
        if("military_base".equals(selected)){
            int sx=32+random.nextInt(48),sz=32+random.nextInt(48);
            var plan=new techguns.world.structures.camp.CampWorld(RandomSource.create(random.nextLong()),terrain::block,(px,pz)->terrain.height(px,pz,Heightmap.Types.WORLD_SURFACE_WG),context.heightAccessor().getMaxBuildHeight(),SurfaceStructuresFeature.biomeColor(biome));
            int y;while((y=techguns.world.structures.camp.CampTerrain.getValidSpawnYArea(plan,x,z,sx,sz,5,8))<=0){if(sx>=32&&sz>=32){sx-=16;sz-=16;}else return Optional.empty();}
            techguns.world.structures.camp.CampTerrain.removeJunkInArea(plan,x-1,z-1,sx+2,sz+2);var camp=new techguns.world.structures.camp.MilitaryCamp(4,random);camp.init(x,y,z,sx,sz);camp.setBlocks(plan,random);var pieces=PlannedStructurePiece.split(plan);int anchorY=y;
            return Optional.of(new GenerationStub(new BlockPos(x,anchorY,z),builder->pieces.forEach(builder::addPiece)));
        }
        if("aircraftcarrier".equals(selected)){
            var scan=ScannedStructure.load("aircraft_carrier");int[] waterHeights=new int[4];int i=0;
            for(int dx:new int[]{0,scan.sizeX})for(int dz:new int[]{0,scan.sizeZ}){int y=terrain.height(x+dx,z+dz,Heightmap.Types.WORLD_SURFACE_WG);if(y<=0||!terrain.block(x+dx,y-1,z+dz).getFluidState().is(net.minecraft.tags.FluidTags.WATER))return Optional.empty();waterHeights[i++]=y;}
            for(int h:waterHeights)if(Math.abs(h-waterHeights[0])>=2)return Optional.empty();Arrays.sort(waterHeights);int y=(waterHeights[1]+waterHeights[2])/2;
            int turns=random.nextInt(4);var plan=new LegacyStructurePlan(RandomSource.create(random.nextLong()),terrain::block);scan.plan(plan,new BlockPos(x,y-3,z),turns,plan.random,random,SurfaceStructuresFeature.biomeColor(biome));var pieces=PlannedStructurePiece.split(plan);
            return Optional.of(new GenerationStub(new BlockPos(x,y,z),builder->pieces.forEach(builder::addPiece)));
        }
        int[] heights=new int[4];int index=0;
        for(int dx:new int[]{0,4})for(int dz:new int[]{0,4}){
            int y=terrain.height(x+dx,z+dz,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);
            if(y<=0||!terrain.block(x+dx,y-1,z+dz).getFluidState().isEmpty())return Optional.empty();heights[index++]=y;
        }
        for(int h:heights)if(Math.abs(h-heights[0])>=256)return Optional.empty();Arrays.sort(heights);int y=(heights[1]+heights[2])/2;
        int sx=16+random.nextInt(16),sz=16+random.nextInt(16);
        var nest=new AlienBugNest(x,y-2,z,sx,0,sz,random);
        var plan=new LegacyStructurePlan(RandomSource.create(random.nextLong()),terrain::block);nest.setBlocks(plan);
        var pieces=PlannedStructurePiece.split(plan);if(pieces.isEmpty())return Optional.empty();
        return Optional.of(new GenerationStub(new BlockPos(x,y,z),builder->pieces.forEach(builder::addPiece)));
    }
    private static Optional<GenerationStub> stub(LegacyStructurePlan plan,BlockPos position){
        var pieces=PlannedStructurePiece.split(plan);if(pieces.isEmpty())return Optional.empty();
        return Optional.of(new GenerationStub(position,builder->pieces.forEach(builder::addPiece)));
    }
}
