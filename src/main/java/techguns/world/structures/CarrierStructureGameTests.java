package techguns.world.structures;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.tileentities.NpcSpawnerEntity;
import techguns.world.*;

@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class CarrierStructureGameTests {
    @GameTest(template="empty",batch="carrier-native",timeoutTicks=1200) public static void carrierNativeStartRequiresOceanWaterFootprintAndKeepsSavedGuardData(GameTestHelper h){
        var level=h.getLevel();var access=level.registryAccess();var structure=access.registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).get(net.minecraft.resources.ResourceLocation.parse("techguns:aircraftcarrier"));
        var biomes=new net.minecraft.world.level.biome.FixedBiomeSource(access.registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(net.minecraft.world.level.biome.Biomes.OCEAN));var settings=access.registryOrThrow(net.minecraft.core.registries.Registries.NOISE_SETTINGS).getHolderOrThrow(net.minecraft.world.level.levelgen.NoiseGeneratorSettings.OVERWORLD);var generator=new net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator(biomes,settings);net.minecraft.world.level.levelgen.structure.StructureStart start=net.minecraft.world.level.levelgen.structure.StructureStart.INVALID_START;long seed=0;
        for(;seed<40&&!start.isValid();seed++){var random=net.minecraft.world.level.levelgen.RandomState.create(settings.value(),access.registryOrThrow(net.minecraft.core.registries.Registries.NOISE).asLookup(),seed);start=structure.generate(access,generator,biomes,random,level.getStructureManager(),seed,new ChunkPos(64,64),0,level,b->true);}
        h.assertTrue(start.isValid()&&start.getPieces().size()>=6,"Loaded native carrier must find a real ocean-water footprint and retain its full multi-chunk geometry");var context=StructurePieceSerializationContext.fromLevel(level);var saved=start.createTag(context,new ChunkPos(64,64));var loaded=net.minecraft.world.level.levelgen.structure.StructureStart.loadStaticStart(context,saved,seed-1);h.assertTrue(loaded!=null&&loaded.isValid()&&loaded.getPieces().size()==start.getPieces().size(),"Carrier native start, palette and source guard data survive save/reload");
        Techguns.LOGGER.info("TECHGUNS_AIRCRAFT_CARRIER_NATIVE_PASSED: actual ocean noise columns, four water-surface corners, original height tolerance, large-grid selection and native saved structure start");h.succeed();
    }
    @GameTest(template="empty",batch="aircraft-carrier",timeoutTicks=1000) public static void originalCarrierScanPlacesAllRotationsWithSourceLootCratesAndHelicopterGuards(GameTestHelper h){
        var level=h.getLevel();var scan=ScannedStructure.load("aircraft_carrier");h.assertTrue(scan.sizeX==54&&scan.sizeY==24&&scan.sizeZ==21,"Original carrier dimensions from the spawn register");int chests=0,crates=0,guards=0,helicopters=0,doubles=0;
        for(int turns=0;turns<4;turns++){
            var origin=new BlockPos(1024+turns*128,96,1024);var plan=new LegacyStructurePlan(RandomSource.create(8128+turns),pos->Blocks.AIR.defaultBlockState());scan.plan(plan,origin,turns,plan.random,RandomSource.create(1729),0);h.assertTrue(scan.blockCount()==3967&&plan.blocks().size()==3929,"Original carrier has 3967 ordered writes to 3929 distinct positions; later source writes must win");var pieces=PlannedStructurePiece.split(plan);var context=StructurePieceSerializationContext.fromLevel(level);int cells=0;
            for(var piece:pieces){var saved=piece.createTag(context);var loaded=TGWorld.PLANNED_PIECE.get().load(context,saved);h.assertTrue(loaded.createTag(context).equals(saved),"Carrier cells, chest loot and guard data survive native piece save/reload");var box=loaded.getBoundingBox();var chunk=new ChunkPos(box.minX()>>4,box.minZ()>>4);h.assertTrue(box.maxX()>>4==chunk.x&&box.maxZ()>>4==chunk.z,"Carrier native pieces cannot write far chunks");var clip=new BoundingBox(chunk.getMinBlockX(),level.getMinBuildHeight(),chunk.getMinBlockZ(),chunk.getMaxBlockX(),level.getMaxBuildHeight()-1,chunk.getMaxBlockZ());loaded.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(1),clip,chunk,BlockPos.ZERO);cells+=piece.cellCount();}
            h.assertTrue(cells==plan.blocks().size(),"Native carrier chunk partition cannot truncate final source geometry");
            for(var entry:plan.blocks().entrySet()){var pos=entry.getKey();var actual=level.getBlockState(pos);h.assertTrue(actual.is(entry.getValue().getBlock()),"Carrier original block at "+pos+" expected="+entry.getValue()+" actual="+actual);
                if(level.getBlockEntity(pos) instanceof ChestBlockEntity chest){h.assertTrue(chest.saveWithoutMetadata().getString("LootTable").equals("techguns:chests/aircraftcarrier"),"Carrier source-specific loot table");if(actual.getValue(ChestBlock.TYPE)!=net.minecraft.world.level.block.state.properties.ChestType.SINGLE)doubles++;chests++;}
                if(actual.getBlock() instanceof techguns.blocks.MilitaryCrateBlock)crates++;
                if(level.getBlockEntity(pos) instanceof NpcSpawnerEntity guard){var data=guard.saveWithoutMetadata();var types=data.getList("mobtypes",10);if(types.getCompound(0).getCompound("Entity").getString("id").equals("techguns:attackhelicopter")){h.assertTrue(guard.remaining()==1&&data.getInt("maxActive")==1&&data.getInt("spawnDelay")==200&&data.getDouble("spawnRange")==0,"Original carrier helicopter spawner");helicopters++;}else{h.assertTrue(guard.remaining()==6&&data.getInt("maxActive")==2&&data.getInt("spawnDelay")==150&&data.getDouble("spawnRange")==2&&types.size()==2,"Original carrier soldier/commando guards");guards++;}}
            }
        }
        h.assertTrue(chests>0&&crates>0&&guards>0&&helicopters==4&&doubles>0,"All rotated carriers need actual cargo, double chests, guards and helicopter spawner");
        Techguns.LOGGER.info("TECHGUNS_AIRCRAFT_CARRIER_PASSED: four complete source rotations, chunk-safe saved pieces, original loot/crates, double inventories and soldier/commando/helicopter spawners");h.succeed();
    }
    private CarrierStructureGameTests(){}
}
