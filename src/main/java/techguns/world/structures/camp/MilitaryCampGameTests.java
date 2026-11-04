package techguns.world.structures.camp;

import java.util.*;
import com.google.gson.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.tileentities.NpcSpawnerEntity;
import techguns.world.structures.*;

@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class MilitaryCampGameTests {
    private static CampWorld flat(long seed,int color){return new CampWorld(RandomSource.create(seed),p->p.getY()<=95?Blocks.DIRT.defaultBlockState():Blocks.AIR.defaultBlockState(),(x,z)->96,320,color);}
    private static void visit(MilitaryCamp.CampSegment segment,List<MilitaryCamp.CampSegment> list){list.add(segment);if(segment.subSegments!=null)for(var child:segment.subSegments)visit(child,list);}
    @GameTest(template="empty",batch="military-layout",timeoutTicks=400) public static void campSplitLayoutMatchesAllExecutedOriginalOracleSegments(GameTestHelper h){
        JsonArray oracle;try(var input=MilitaryCampGameTests.class.getResourceAsStream("/data/techguns/verification/military-camp-layout.json");var reader=new java.io.InputStreamReader(Objects.requireNonNull(input),java.nio.charset.StandardCharsets.UTF_8)){oracle=JsonParser.parseReader(reader).getAsJsonArray();}catch(Exception error){throw new IllegalStateException(error);}int checked=0;
        for(var raw:oracle){var fixture=raw.getAsJsonObject();var camp=new MilitaryCamp(4,RandomSource.create(fixture.get("seed").getAsLong()));camp.init(256,96,256,fixture.get("sizeX").getAsInt(),fixture.get("sizeZ").getAsInt());var actual=new ArrayList<MilitaryCamp.CampSegment>();visit(camp.rootSegment,actual);var expected=fixture.getAsJsonArray("segments");h.assertTrue(actual.size()==expected.size(),"Original camp tree size, seed "+fixture.get("seed"));
            for(int i=0;i<actual.size();i++){var s=actual.get(i);var row=expected.get(i).getAsJsonArray();int[] ints={s.x,s.y,s.z,s.sizeX,s.sizeZ,s.roadWidth,s.direction,s.splitStep};for(int j=0;j<ints.length;j++)h.assertTrue(ints[j]==row.get(j).getAsInt(),"Original camp segment field "+j+" at segment "+i+" seed "+fixture.get("seed"));h.assertTrue(s.isCenter==row.get(8).getAsBoolean()&&s.subAlignment==row.get(9).getAsBoolean()&&(s.subSegments==null?0:s.subSegments.length)==row.get(10).getAsInt(),"Original center, alignment and children");checked++;}
        }
        h.assertTrue(oracle.size()==9&&checked==700,"All original camp oracle fixtures must execute");Techguns.LOGGER.info("TECHGUNS_CAMP_LAYOUT_PASSED: nine original constructor fixtures, 700 exact source segments, road widths, rotations, center flags and branch topology");h.succeed();
    }
    private record Helper(String id,CampStructure structure,int x,int y,int z){}
    @GameTest(template="empty",batch="military-helpers",timeoutTicks=600) public static void everySourceCampHelperSupportsFourDirectionsAndThreeBiomePalettes(GameTestHelper h){
        var helpers=new ArrayList<Helper>();for(int i=0;i<3;i++)helpers.add(new Helper("tent-"+i,new Tent(1,1,1,-1,-1,-1,i),8,6,8));helpers.add(new Helper("containers",new Containers(2,3,2,7,3,7,2,2,2,new CampBlock(TGDecorations.BLOCKS.get("container_red").get().defaultBlockState())),7,3,7));
        for(int i=0;i<6;i++)helpers.add(new Helper("props-"+i,new CampProps(1,1,1,-1,-1,-1,i),8,7,8));for(int i=0;i<4;i++)helpers.add(new Helper("bunker-"+i,new Bunker(1,1,1,-1,-1,-1,i),10,i==1||i==3?4:3,10));helpers.add(new Helper("barracks",new Barracks(7,7,7,-1,-1,-1),13,7,13));helpers.add(new Helper("helipad",new Helipad(7,7,7,10,-1,10),9,7,9));helpers.add(new Helper("tanks",new Tanks(4,5,4,10,-1,10),10,5,10));helpers.add(new Helper("tower-3",new WatchTowerSmall(3,8,3,4,8,-1,3),4,8,4));helpers.add(new Helper("tower-4",new WatchTowerSmall(4,10,4,-1,12,-1,4),5,12,5));helpers.add(new Helper("fallback",new EmptyPlane(Blocks.COBBLESTONE.defaultBlockState()),4,1,4));int fixtures=0,chests=0,beds=0,doors=0;
        for(var helper:helpers)for(int color=0;color<3;color++)for(int direction=0;direction<4;direction++){var world=flat(8128,color);helper.structure.setBlocks(world,0,95,0,helper.x,helper.y,helper.z,direction,CampStructure.BiomeColorType.values()[color],RandomSource.create(1729));h.assertTrue(world.blocks().values().stream().anyMatch(s->!s.isAir()),"Source helper cannot be empty: "+helper.id);
            for(var entry:world.blocks().entrySet()){var state=entry.getValue();if(state.getBlock() instanceof ChestBlock){var data=world.blockData().get(entry.getKey());h.assertTrue(data!=null&&data.getString("LootTable").equals("techguns:chests/militarybase_"+(helper.id.startsWith("bunker")?"bunker":"barracks")),"Source helper chest loot: "+helper.id);chests++;}if(state.getBlock() instanceof BedBlock)beds++;if(state.getBlock() instanceof DoorBlock){var half=state.getValue(DoorBlock.HALF);h.assertTrue(world.getBlockState(half==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER?entry.getKey().above():entry.getKey().below()).is(state.getBlock()),"Both original helper door halves: "+helper.id);doors++;}}
            fixtures++;
        }
        h.assertTrue(fixtures==240&&chests>0&&beds>0&&doors>0,"All twenty source helper variants, four directions and three biome colors need live source states and furniture");Techguns.LOGGER.info("TECHGUNS_CAMP_HELPERS_PASSED: 240 original helper fixtures, twenty variants, four directions and three biomes; crates, furniture, doors and original chest loot");h.succeed();
    }
    @GameTest(template="empty",batch="military-camp",timeoutTicks=1200) public static void completeCampKeepsRoadsFiniteArmyAndElevatedHelicopterAfterChunkSaveReload(GameTestHelper h){
        var random=RandomSource.create(1729);var plan=flat(8128,2);CampTerrain.removeJunkInArea(plan,2047,2047,50,50);var camp=new MilitaryCamp(4,random);camp.init(2048,96,2048,48,48);camp.setBlocks(plan,random);h.assertTrue(plan.cleanupColumns().size()==2500&&plan.blocks().size()>1500,"Original complete camp footprint and terrain preparation");
        int soldiers=0,helicopters=0,loot=0;for(var entry:plan.blockData().entrySet()){var tag=entry.getValue();if(tag.contains("LootTable"))loot++;else{var types=tag.getList("mobtypes",10);h.assertTrue(types.size()==1,"Source camp uses a single guard type per spawn point");String type=types.getCompound(0).getCompound("Entity").getString("id");if(type.equals("techguns:attackhelicopter")){h.assertTrue(tag.getInt("mobsLeft")==1&&tag.getInt("maxActive")==1&&tag.getInt("spawnHeightOffset")==64,"Original camp helicopter budget and sixty-four-block altitude");helicopters++;}else{h.assertTrue(type.equals("techguns:armysoldier")&&tag.getInt("mobsLeft")==3&&tag.getInt("maxActive")==1&&tag.getInt("spawnDelay")==200&&tag.getDouble("spawnRange")==0,"Original finite road guards");soldiers++;}}}h.assertTrue(soldiers>0&&helicopters==1,"Complete original camp has road guards and exactly one helicopter spawner");
        var level=h.getLevel();for(int x=2047;x<=2096;x++)for(int z=2047;z<=2096;z++){int top=level.getHeight(Heightmap.Types.WORLD_SURFACE,x,z);for(int y=top;y>95;y--)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),18);level.setBlock(new BlockPos(x,95,z),Blocks.DIRT.defaultBlockState(),18);}
        // Native cleanup must also remove a tree that was absent from the initial noise-column plan.
        var tree=new BlockPos(2049,96,2049);for(int i=0;i<8;i++)level.setBlock(tree.above(i),Blocks.OAK_LOG.defaultBlockState(),18);level.setBlock(tree.above(8),Blocks.OAK_LEAVES.defaultBlockState(),18);
        var pieces=PlannedStructurePiece.split(plan);var context=StructurePieceSerializationContext.fromLevel(level);int cells=0;for(var piece:pieces){var saved=piece.createTag(context);var loaded=TGWorld.PLANNED_PIECE.get().load(context,saved);h.assertTrue(loaded.createTag(context).equals(saved),"Camp terrain columns, source cells and guard data survive native piece save/reload");var box=loaded.getBoundingBox();var chunk=new ChunkPos(box.minX()>>4,box.minZ()>>4);h.assertTrue(box.maxX()>>4==chunk.x&&box.maxZ()>>4==chunk.z,"Camp pieces remain confined to one chunk");loaded.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(1),new BoundingBox(chunk.getMinBlockX(),level.getMinBuildHeight(),chunk.getMinBlockZ(),chunk.getMaxBlockX(),level.getMaxBuildHeight()-1,chunk.getMaxBlockZ()),chunk,BlockPos.ZERO);cells+=piece.cellCount();}
        h.assertTrue(cells==plan.blocks().size(),"Native camp pieces retain every final source write");for(var entry:plan.blocks().entrySet())h.assertTrue(level.getBlockState(entry.getKey()).equals(entry.getValue()),"Camp source state survives chunk placement at "+entry.getKey());h.assertTrue(!level.getBlockState(tree.above(8)).is(Blocks.OAK_LEAVES),"Native column cleanup removes post-noise vegetation");
        Techguns.LOGGER.info("TECHGUNS_CAMP_PIECES_PASSED: complete 48x48 source camp, {} chunk pieces, {} source cells, {} finite road guards, one elevated helicopter and {} loot chests; native vegetation cleanup and NBT",pieces.size(),cells,soldiers,loot);h.succeed();
    }
    @GameTest(template="empty",batch="military-native",timeoutTicks=1600) public static void registeredCampGeneratesNativeStartOnPlainsNoiseAndObeysWorldGenerationSwitch(GameTestHelper h){
        var level=h.getLevel();var access=level.registryAccess();var structure=access.registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.parse("techguns:military_base"));var biomes=new FixedBiomeSource(access.registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS));var settings=access.registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);var generator=new NoiseBasedChunkGenerator(biomes,settings);StructureStart start=StructureStart.INVALID_START;long seed=0;
        for(;seed<40&&!start.isValid();seed++){if(!"military_base".equals(techguns.world.SurfaceStructuresFeature.choose(64,64,false,false,false,techguns.world.SurfaceStructuresFeature.selectionRandom(seed,64,64))))continue;var random=RandomState.create(settings.value(),access.registryOrThrow(Registries.NOISE).asLookup(),seed);start=structure.generate(access,generator,biomes,random,level.getStructureManager(),seed,new ChunkPos(64,64),0,level,b->true);}
        h.assertTrue(start.isValid()&&start.getPieces().size()>1,"Registered native camp must create real saved multi-chunk geometry on plains noise");var context=StructurePieceSerializationContext.fromLevel(level);var saved=start.createTag(context,new ChunkPos(64,64));var loaded=StructureStart.loadStaticStart(context,saved,seed-1);h.assertTrue(loaded!=null&&loaded.isValid()&&loaded.getPieces().size()==start.getPieces().size(),"Complete native camp start survives save/reload");
        boolean enabled=TGConfig.doWorldspawn;try{TGConfig.doWorldspawn=false;var random=RandomState.create(settings.value(),access.registryOrThrow(Registries.NOISE).asLookup(),seed-1);h.assertTrue(!structure.generate(access,generator,biomes,random,level.getStructureManager(),seed-1,new ChunkPos(64,64),0,level,b->true).isValid(),"Original world-generation switch disables native camp starts");}finally{TGConfig.doWorldspawn=enabled;}
        Techguns.LOGGER.info("TECHGUNS_CAMP_NATIVE_PASSED: loaded native structure codec, original large-grid table, real plains noise columns, original size/fallback rules, saved StructureStart and global generation switch");h.succeed();
    }
    private MilitaryCampGameTests(){}
}
