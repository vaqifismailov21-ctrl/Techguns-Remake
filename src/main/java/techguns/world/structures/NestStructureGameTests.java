package techguns.world.structures;

import java.util.*;
import com.google.gson.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.tileentities.NpcSpawnerEntity;

@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class NestStructureGameTests {
    private static void visit(AlienBugNest.BugNestSegment room,List<AlienBugNest.BugNestSegment> all){if(all.contains(room))return;all.add(room);for(var next:room.neighbors)visit(next,all);}
    @GameTest(template="empty",batch="alien-nest",timeoutTicks=400) public static void originalConstructorRoomsAndConnectionsMatchExecutedLegacyOracle(GameTestHelper h){
        JsonArray oracle;try(var input=NestStructureGameTests.class.getResourceAsStream("/data/techguns/verification/alien-nest-graph.json");var reader=new java.io.InputStreamReader(Objects.requireNonNull(input),java.nio.charset.StandardCharsets.UTF_8)){oracle=JsonParser.parseReader(reader).getAsJsonArray();}catch(Exception error){throw new IllegalStateException(error);}
        for(var raw:oracle){var fixture=raw.getAsJsonObject();var random=RandomSource.create(fixture.get("seed").getAsLong());int sx=16+random.nextInt(16),sz=16+random.nextInt(16);var nest=new AlienBugNest(256,94,256,sx,0,sz,random);var rooms=new ArrayList<AlienBugNest.BugNestSegment>();visit(nest.mainSegment,rooms);var expected=fixture.getAsJsonArray("rooms");h.assertTrue(rooms.size()==expected.size(),"Original graph room count");
            for(int i=0;i<rooms.size();i++){var room=rooms.get(i);var data=expected.get(i).getAsJsonObject();var pos=data.getAsJsonArray("position");h.assertTrue(room.posX==pos.get(0).getAsInt()&&room.posY==pos.get(1).getAsInt()&&room.posZ==pos.get(2).getAsInt()&&room.radius==data.get("radius").getAsInt()&&room.type==data.get("type").getAsInt(),"Original room coordinates, radius and type, seed "+fixture.get("seed")+" room "+i);var links=data.getAsJsonArray("neighbors");h.assertTrue(room.neighbors.size()==links.size(),"Original branch count");for(int j=0;j<links.size();j++)h.assertTrue(rooms.indexOf(room.neighbors.get(j))==links.get(j).getAsInt(),"Original directed branch topology");}
            var plan=new LegacyStructurePlan(RandomSource.create(8128),pos->Blocks.STONE.defaultBlockState());nest.setBlocks(plan);
            long air=plan.blocks().values().stream().filter(state->state.isAir()).count(),eggs=plan.blocks().values().stream().filter(state->state.is(TGWorld.SLIMY_EGGS.get())).count(),guards=plan.blocks().values().stream().filter(state->state.is(TGWorld.SPAWNER_HOLE.get())).count(),trail=plan.blocks().values().stream().filter(state->state.is(TGWorld.SLIMY_TRAIL.get())).count();
            h.assertTrue(air>500&&eggs>10&&guards>=3&&guards<=10&&trail>0,"Original nest must retain hollow rooms, egg piles, finite guards and climbable entrance");
        }
        Techguns.LOGGER.info("TECHGUNS_ALIEN_NEST_SOURCE_PASSED: three graph fixtures executed from the original constructor; room coordinates, branch topology, strict source geometry, egg piles and climbable trails");h.succeed();
    }
    @GameTest(template="empty",batch="alien-nest",timeoutTicks=600) public static void nativeChunkPiecesSaveReloadAndPlaceEverySourceCellWithFiniteGuards(GameTestHelper h){
        var level=h.getLevel();var plan=new LegacyStructurePlan(RandomSource.create(8128),pos->Blocks.STONE.defaultBlockState());new AlienBugNest(768,94,768,24,0,24,RandomSource.create(1729)).setBlocks(plan);var pieces=PlannedStructurePiece.split(plan);h.assertTrue(pieces.size()>4,"Original nest spans multiple chunks");var context=StructurePieceSerializationContext.fromLevel(level);int total=0,guards=0;
        var terrain=BoundingBox.encapsulatingPositions(plan.blocks().keySet()).orElseThrow().inflatedBy(1);for(int x=terrain.minX();x<=terrain.maxX();x++)for(int y=terrain.minY();y<=terrain.maxY();y++)for(int z=terrain.minZ();z<=terrain.maxZ();z++)level.setBlock(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState(),18);
        for(var piece:pieces){var saved=piece.createTag(context);var loaded=TGWorld.PLANNED_PIECE.get().load(context,saved);h.assertTrue(loaded.createTag(context).equals(saved),"Saved source cells and palette survive native piece codec round-trip");var box=loaded.getBoundingBox();h.assertTrue(box.getXSpan()<=16&&box.getZSpan()<=16&&box.minX()>>4==box.maxX()>>4&&box.minZ()>>4==box.maxZ()>>4,"One native piece must be confined to one chunk");var chunk=new ChunkPos(box.minX()>>4,box.minZ()>>4);var clip=new BoundingBox(chunk.getMinBlockX(),level.getMinBuildHeight(),chunk.getMinBlockZ(),chunk.getMaxBlockX(),level.getMaxBuildHeight()-1,chunk.getMaxBlockZ());loaded.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(1),clip,chunk,BlockPos.ZERO);total+=piece.cellCount();}
        h.assertTrue(total==plan.blocks().size(),"Chunk partition must retain every final original write exactly once");
        for(var entry:plan.blocks().entrySet()){h.assertTrue(level.getBlockState(entry.getKey()).equals(entry.getValue()),"Actual saved piece placement must match source plan at "+entry.getKey()+"; expected="+entry.getValue()+" actual="+level.getBlockState(entry.getKey()));if(level.getBlockEntity(entry.getKey()) instanceof NpcSpawnerEntity guard){var data=guard.saveWithoutMetadata();h.assertTrue(guard.remaining()==7&&data.getInt("maxActive")==3&&data.getInt("spawnDelay")==150&&data.getDouble("spawnRange")==1&&data.getList("mobtypes",10).getCompound(0).getCompound("Entity").getString("id").equals("techguns:alienbug"),"Original nest guard budget, interval, range and entity");guards++;}}
        h.assertTrue(guards>=3,"Original nest needs active finite bug spawners");
        // Calling a piece for another chunk must not leak any writes outside the supplied clip.
        var sample=plan.blocks().entrySet().stream().filter(e->!e.getValue().is(Blocks.STONE)).findFirst().orElseThrow();level.setBlock(sample.getKey(),Blocks.STONE.defaultBlockState(),2);var wrongChunk=new ChunkPos(0,0);var wrongClip=new BoundingBox(0,level.getMinBuildHeight(),0,15,level.getMaxBuildHeight()-1,15);for(var piece:pieces)piece.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(1),wrongClip,wrongChunk,BlockPos.ZERO);h.assertTrue(level.getBlockState(sample.getKey()).is(Blocks.STONE),"Native chunk clip prevents far-chunk writes");
        Techguns.LOGGER.info("TECHGUNS_ALIEN_NEST_PIECES_PASSED: {} saved chunk pieces, {} exact source cells and {} finite guards; reload and chunk write bounds",pieces.size(),total,guards);h.succeed();
    }
    @GameTest(template="empty",batch="alien-nest-native",timeoutTicks=1200) public static void loadedStructureCodecGeneratesNativeStartsFromDesertNoiseTerrain(GameTestHelper h){
        var level=h.getLevel();var access=level.registryAccess();var structure=access.registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.parse("techguns:alien_bug_nest"));h.assertTrue(structure instanceof LegacySurfaceStructure,"Native structure must load from the registered codec and datapack");
        var biome=new FixedBiomeSource(access.registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.DESERT));var settings=access.registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);var generator=new NoiseBasedChunkGenerator(biome,settings);StructureStart start=StructureStart.INVALID_START;long seed=0;
        for(;seed<40&&!start.isValid();seed++){var random=RandomState.create(settings.value(),access.registryOrThrow(Registries.NOISE).asLookup(),seed);start=structure.generate(access,generator,biome,random,level.getStructureManager(),seed,new ChunkPos(32,32),0,level,b->true);}
        h.assertTrue(start.isValid()&&start.getPieces().size()>4,"Registered native structure must create a real multi-chunk nest on desert noise terrain");var context=StructurePieceSerializationContext.fromLevel(level);var saved=start.createTag(context,new ChunkPos(32,32));var loaded=StructureStart.loadStaticStart(context,saved,seed-1);h.assertTrue(loaded!=null&&loaded.isValid()&&loaded.getPieces().size()==start.getPieces().size(),"Whole native structure start survives save/reload");
        boolean enabled=TGConfig.doWorldspawn;try{TGConfig.doWorldspawn=false;var random=RandomState.create(settings.value(),access.registryOrThrow(Registries.NOISE).asLookup(),seed-1);h.assertTrue(!structure.generate(access,generator,biome,random,level.getStructureManager(),seed-1,new ChunkPos(32,32),0,level,b->true).isValid(),"Original world-generation switch disables native nest starts");}finally{TGConfig.doWorldspawn=enabled;}
        Techguns.LOGGER.info("TECHGUNS_ALIEN_NEST_NATIVE_PASSED: loaded structure codec, actual desert noise columns, original grid/table, native structure start and piece serialization; global generation switch");h.succeed();
    }
    private NestStructureGameTests(){}
}
