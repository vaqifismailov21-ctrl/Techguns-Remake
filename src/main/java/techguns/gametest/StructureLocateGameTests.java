package techguns.gametest;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.world.*;
import techguns.world.structures.*;

/**
 * Every original structure is its own native structure (/locate structure techguns:&lt;id&gt;): registered once, placed on
 * the original grid, generated only where the original table chooses it, saved as a whole start, and found by the
 * vanilla locate search in both dimensions.
 */
@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class StructureLocateGameTests {
    private static Structure structure(GameTestHelper h,String id){
        var value=h.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath(Techguns.MODID,id));
        h.assertTrue(value instanceof LegacySurfaceStructure s&&s.kind.equals(id),"Missing native structure techguns:"+id);return value;
    }
    private static int gridOf(String id){
        if(Set.of("military_base","castle","aircraftcarrier").contains(id))return TGConfig.spawnWeightTGStructureBig;
        if(Set.of("alien_bug_nest","policestation","survivor_hideout","orecluster_spike","orecluster_meteorbase","desert_oil_cluster","nether_altar_medium","nether_ghast_spawner","nether_orecluster_castle").contains(id))return TGConfig.spawnWeightTGStructureMedium;
        return TGConfig.spawnWeightTGStructureSmall;
    }

    @GameTest(template="empty",batch="structure-locate",timeoutTicks=200) public static void everyOriginalStructureIsANativeStructureOnItsOriginalGrid(GameTestHelper h){
        var server=h.getLevel().getServer();var registry=h.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);var all=new TreeSet<String>();all.addAll(SurfaceStructuresFeature.IDS);all.addAll(NetherStructuresFeature.IDS);
        h.assertTrue(all.size()==21,"Original TGStructureSpawnRegister has 13 Overworld and 8 Nether entries");
        for(var id:all){
            var holder=registry.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE,ResourceLocation.fromNamespaceAndPath(Techguns.MODID,id)));structure(h,id);
            var level=server.getLevel(NetherStructuresFeature.IDS.contains(id)?Level.NETHER:Level.OVERWORLD);var placements=level.getChunkSource().getGeneratorState().getPlacementsForStructure(holder);
            if(!((LegacySurfaceStructure)holder.value()).canEverGenerate()){h.assertTrue(placements.isEmpty(),"techguns:"+id+" cannot generate under this config, so it is never planned and /locate answers at once");continue;}
            h.assertTrue(placements.size()==1&&placements.get(0) instanceof LegacyGridPlacement grid&&grid.spacing()==gridOf(id),"techguns:"+id+" must be placed by exactly one original grid ("+gridOf(id)+" chunks): "+placements);
            var other=server.getLevel(NetherStructuresFeature.IDS.contains(id)?Level.OVERWORLD:Level.NETHER);h.assertTrue(other.getChunkSource().getGeneratorState().getPlacementsForStructure(holder).isEmpty(),"techguns:"+id+" must not be planned in the other dimension");
        }
        var tag=registry.getTag(net.minecraft.tags.TagKey.create(Registries.STRUCTURE,ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"all")));h.assertTrue(tag.isPresent()&&tag.get().size()==21,"Tag #techguns:all lists every original structure for /locate");
        var legacy=registry.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE,ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"legacy_surface")));
        h.assertTrue(server.overworld().getChunkSource().getGeneratorState().getPlacementsForStructure(legacy).isEmpty(),"The earlier combined entry only loads saved starts and never generates");
        Techguns.LOGGER.info("TECHGUNS_STRUCTURE_REGISTRY_PASSED: 21 original structures registered once each, on their original grids and dimensions, tag #techguns:all");h.succeed();
    }

    @GameTest(template="empty",batch="structure-locate",timeoutTicks=4000) public static void scannedStructuresGenerateAsWholeSavedStartsOnNoiseTerrain(GameTestHelper h){
        var level=h.getLevel();var access=level.registryAccess();var previous=TGConfig.fluidListOilWorldspawn;var report=new TreeMap<String,Integer>();
        try{
            // Vanilla water stands in for an explicitly configured oil fluid, so the oil table entry is eligible.
            TGConfig.fluidListOilWorldspawn=new String[]{"water"};
            var ids=new ArrayList<String>();for(var id:SurfaceStructuresFeature.IDS)if(!LegacySurfaceStructure.BIG.contains(id))ids.add(id);ids.addAll(NetherStructuresFeature.IDS);Collections.sort(ids);
            for(var id:ids){
                boolean nether=NetherStructuresFeature.IDS.contains(id),desert=id.equals("desert_oil_cluster");
                var biome=access.registryOrThrow(Registries.BIOME).getHolderOrThrow(nether?Biomes.NETHER_WASTES:desert?Biomes.DESERT:Biomes.PLAINS);var source=new FixedBiomeSource(biome);
                var settings=access.registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(nether?NoiseGeneratorSettings.NETHER:NoiseGeneratorSettings.OVERWORLD);var generator=new NoiseBasedChunkGenerator(source,settings);
                var structure=structure(h,id);int grid=gridOf(id);StructureStart start=StructureStart.INVALID_START;ChunkPos chunk=null;long seed=0;
                // Rare entries (Nether altar: 10 of 1020 weight) need many sites; only chosen sites are generated.
                for(;seed<20000&&!start.isValid();seed++){
                    int cx=grid*(1+(int)(seed%5)),cz=grid*(1+(int)(seed/5%5));for(int coarser:new int[]{TGConfig.spawnWeightTGStructureMedium,TGConfig.spawnWeightTGStructureBig})if(grid<coarser&&cx%coarser==0&&cz%coarser==0)cx+=grid;
                    var random=SurfaceStructuresFeature.selectionRandom(seed,cx,cz);
                    String chosen=nether?NetherStructuresFeature.choose(cx,cz,random):SurfaceStructuresFeature.choose(cx,cz,false,desert,true,random);if(!id.equals(chosen))continue;
                    chunk=new ChunkPos(cx,cz);start=structure.generate(access,generator,source,RandomState.create(settings.value(),access.registryOrThrow(Registries.NOISE).asLookup(),seed),level.getStructureManager(),seed,chunk,0,level,b->true);
                }
                h.assertTrue(start.isValid(),"techguns:"+id+" must generate on real "+(nether?"Nether":desert?"desert":"plains")+" noise at a site where the original table chooses it");
                // Another kind at the same site never starts: one original decision per grid site.
                for(var otherId:ids)if(!otherId.equals(id)&&NetherStructuresFeature.IDS.contains(otherId)==nether){var random=RandomState.create(settings.value(),access.registryOrThrow(Registries.NOISE).asLookup(),seed-1);
                    h.assertTrue(!structure(h,otherId).generate(access,generator,source,random,level.getStructureManager(),seed-1,chunk,0,level,b->true).isValid(),"Only the chosen structure may start at "+chunk+": "+otherId);}
                var context=StructurePieceSerializationContext.fromLevel(level);
                if(nether||SurfaceStructuresFeature.IMPLEMENTED.contains(id)){
                    // Built later by the Techguns generator on the real terrain; the start only marks the site for /locate.
                    h.assertTrue(start.getPieces().size()==1&&start.getPieces().get(0) instanceof techguns.world.structures.SiteMarkerPiece,"techguns:"+id+" start is a single site marker");
                    var box=start.getPieces().get(0).getBoundingBox();var scan=ScannedStructure.load(id);
                    h.assertTrue(box.getXSpan()>=Math.min(scan.sizeX,scan.sizeZ)&&box.getZSpan()>=Math.min(scan.sizeX,scan.sizeZ),"techguns:"+id+" marker covers the building footprint");
                    var loaded=StructureStart.loadStaticStart(context,start.createTag(context,chunk),seed-1);
                    h.assertTrue(loaded!=null&&loaded.isValid(),"techguns:"+id+" marker survives save/reload");
                    report.put(id,box.getXSpan()*box.getZSpan());continue;
                }
                var scan=ScannedStructure.load(id);var cells=new LinkedHashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
                for(var piece:start.getPieces()){h.assertTrue(piece instanceof PlannedStructurePiece,"Scanned structures are saved as source plan pieces");((PlannedStructurePiece)piece).forEachCell(cells::put);
                    var box=piece.getBoundingBox();h.assertTrue(box.minX()>>4==box.maxX()>>4&&box.minZ()>>4==box.maxZ()>>4,"Every piece stays inside one chunk");}
                long solid=cells.values().stream().filter(state->!state.isAir()).count();
                h.assertTrue(solid>=Math.min(15,scan.blockCount()/2)&&cells.size()>=scan.blockCount(),"techguns:"+id+" must plan the whole original scan: "+cells.size()+" cells, "+solid+" solid, scan "+scan.blockCount());
                var loaded=StructureStart.loadStaticStart(context,start.createTag(context,chunk),seed-1);
                h.assertTrue(loaded!=null&&loaded.isValid()&&loaded.getPieces().size()==start.getPieces().size(),"techguns:"+id+" start survives save/reload");
                report.put(id,cells.size());
            }
        }finally{TGConfig.fluidListOilWorldspawn=previous;}
        Techguns.LOGGER.info("TECHGUNS_STRUCTURE_STARTS_PASSED: {} scanned structures planned on noise terrain as saved chunk pieces: {}",report.size(),report);h.succeed();
    }

    @GameTest(template="empty",batch="structure-locate",timeoutTicks=6000) public static void vanillaLocateFindsTechgunsStructuresInBothDimensions(GameTestHelper h){
        var server=h.getLevel().getServer();var registry=h.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);var found=new TreeMap<String,String>();
        for(var dimension:List.of(Level.OVERWORLD,Level.NETHER)){
            var level=server.getLevel(dimension);var set=dimension==Level.OVERWORLD?SurfaceStructuresFeature.IDS:NetherStructuresFeature.IDS;
            var holders=set.stream().map(id->(net.minecraft.core.Holder<Structure>)registry.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE,ResourceLocation.fromNamespaceAndPath(Techguns.MODID,id)))).toList();
            // The same call as /locate structure #techguns:all (radius 100 grid cells).
            var result=level.getChunkSource().getGenerator().findNearestMapStructure(level,HolderSet.direct(holders),new BlockPos(0,64,0),100,false);
            h.assertTrue(result!=null,"/locate must find a Techguns structure in "+dimension.location());
            var pos=result.getFirst();var id=result.getSecond().unwrapKey().orElseThrow().location().getPath();int cx=pos.getX()>>4,cz=pos.getZ()>>4;
            h.assertTrue(cx%TGConfig.spawnWeightTGStructureSmall==0&&cz%TGConfig.spawnWeightTGStructureSmall==0,"Located "+id+" must be at an original grid site, got chunk "+cx+","+cz);
            var start=level.getChunk(cx,cz).getStartForStructure(result.getSecond().value());h.assertTrue(start!=null&&start.isValid(),"The located chunk holds the real start of "+id);
            found.put(dimension.location().getPath(),id+" @ "+pos.toShortString());
        }
        Techguns.LOGGER.info("TECHGUNS_STRUCTURE_LOCATE_PASSED: vanilla locate search finds real starts on the original grid: {}",found);h.succeed();
    }
    /** Time of the same search as /locate structure techguns:<id> for every structure; a long search freezes the server thread. */
    @GameTest(template="empty",batch="structure-locate-timing",timeoutTicks=100) public static void locateEveryStructureTiming(GameTestHelper h){
        var server=h.getLevel().getServer();var registry=h.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);var times=new LinkedHashMap<String,String>();
        var ids=new ArrayList<String>(new TreeSet<>(SurfaceStructuresFeature.IDS));ids.addAll(new TreeSet<>(NetherStructuresFeature.IDS));
        for(var id:ids){
            var level=server.getLevel(NetherStructuresFeature.IDS.contains(id)?Level.NETHER:Level.OVERWORLD);
            var holder=registry.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE,ResourceLocation.fromNamespaceAndPath(Techguns.MODID,id)));
            long start=System.nanoTime();var result=level.getChunkSource().getGenerator().findNearestMapStructure(level,HolderSet.direct(holder),new BlockPos(10000,64,10000),100,false);long ms=(System.nanoTime()-start)/1_000_000;
            times.put(id,ms+" ms "+(result==null?"not found":result.getFirst().toShortString()));
            Techguns.LOGGER.info("TECHGUNS_LOCATE_TIME {} {}",id,times.get(id));
            // A long search blocks the server thread (the world stops); every search must stay short.
            h.assertTrue(ms<5000,"/locate structure techguns:"+id+" took "+ms+" ms");
        }
        Techguns.LOGGER.info("TECHGUNS_LOCATE_TIMING_PASSED: {}",times);h.succeed();
    }
    private StructureLocateGameTests(){}
}
