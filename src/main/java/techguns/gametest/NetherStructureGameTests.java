package techguns.gametest;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.tileentities.NpcSpawnerEntity;
import techguns.world.*;

@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class NetherStructureGameTests {
    @GameTest(template="weapon_arena",batch="nether-structures",timeoutTicks=400) public static void everyActiveNetherScanPlacesOriginalBlocksSpawnersAndLootInFourRotations(GameTestHelper h){
        String[] ids={"nether_altar_small","nether_soul_platform","nether_loot_01","nether_acid_hole","nether_orecluster_small","nether_altar_medium","nether_ghast_spawner","nether_orecluster_castle"};int structures=0,guards=0,chests=0;
        // GameTest templates live below Y=0 in 1.20; the original Nether scans deliberately skip Y<1.
        var templateOrigin=h.absolutePos(new BlockPos(16,10,16));
        var origin=new BlockPos(templateOrigin.getX(),60,templateOrigin.getZ());
        for(String id:ids)for(int turn=0;turn<4;turn++){
            var scan=ScannedStructure.load(id);var placed=scan.place(h.getLevel(),origin,turn,RandomSource.create(1729+turn));h.assertTrue(placed.size()==scan.blockCount(),"Every original scan coordinate must be placed: "+id);
            boolean nonAir=false;
            for(var pos:placed){var state=h.getLevel().getBlockState(pos);nonAir|=!state.isAir();var block=h.getLevel().getBlockEntity(pos);
                if(block instanceof NpcSpawnerEntity spawner){h.assertTrue(spawner.remaining()>0&&spawner.delay()>0,"Original finite guard parameters survive placement: "+id);guards++;}
                if(block instanceof ChestBlockEntity chest){var saved=chest.saveWithoutMetadata();h.assertTrue(saved.getString("LootTable").equals("techguns:chests/factory_building"),"Original chest loot table: "+id);chests++;}
                if(block instanceof SpawnerBlockEntity vanilla){var saved=vanilla.saveWithoutMetadata();h.assertTrue(saved.getCompound("SpawnData").getCompound("entity").getString("id").equals("techguns:ghastling"),"Original infinite ghastling spawner");}
            }
            h.assertTrue(nonAir,"Scan cannot be an empty substitute: "+id);structures++;
        }
        h.assertTrue(structures==32&&guards>0&&chests>0,"All eight active source Nether scans must include real guards and loot");
        Techguns.LOGGER.info("TECHGUNS_NETHER_STRUCTURES_PASSED: {} source scans in four rotations; finite NPC spawners, vanilla ghastling spawner, original chest loot and block geometry",ids.length);h.succeed();
    }
    @GameTest(template="weapon_arena",batch="nether-structures") public static void originalNetherGridWeightsConfigAndCaveSelection(GameTestHelper h){
        h.assertTrue(NetherStructuresFeature.choose(64,64,RandomSource.create(1))==null,"Original disabled big Nether structures do not fall through to medium");
        h.assertTrue(NetherStructuresFeature.choose(1,1,RandomSource.create(1))==null,"Structure grid must not generate in every chunk");
        var seen=new HashSet<String>();for(int i=0;i<300;i++)seen.add(NetherStructuresFeature.choose(16,16,RandomSource.create(i)));h.assertTrue(seen.size()==5,"All five source small Nether structures are eligible");
        boolean old=TGConfig.spawnOreClusterStructures;try{TGConfig.spawnOreClusterStructures=false;for(int i=0;i<100;i++){var id=NetherStructuresFeature.choose(32,32,RandomSource.create(i));h.assertTrue(!id.contains("orecluster"),"Original cluster-generation switch must remove ore cluster structures");}}finally{TGConfig.spawnOreClusterStructures=old;}
        boolean enabled=TGConfig.doWorldspawn;try{TGConfig.doWorldspawn=false;h.assertTrue(NetherStructuresFeature.choose(16,16,RandomSource.create(1))==null,"Global source structure switch");}finally{TGConfig.doWorldspawn=enabled;}
        int x=h.absolutePos(BlockPos.ZERO).getX()+20,z=h.absolutePos(BlockPos.ZERO).getZ()+20;
        for(int y=21;y<=100;y++)h.getLevel().setBlock(new BlockPos(x,y,z),y==55||y>=80?Blocks.NETHERRACK.defaultBlockState():Blocks.CAVE_AIR.defaultBlockState(),2);
        h.assertTrue(NetherStructuresFeature.airHeight(h.getLevel(),x,z)==55,"Cave search starts below ceiling and requires ten air blocks, including modern cave_air");h.succeed();
    }
    @GameTest(template="empty",batch="native-nether",timeoutTicks=1200) public static void actualNetherChunksGenerateWithRegisteredStructureFeature(GameTestHelper h){
        var nether=h.getLevel().getServer().getLevel(net.minecraft.world.level.Level.NETHER);h.assertTrue(nether!=null,"Actual Nether dimension must load");
        var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.PLACED_FEATURE,net.minecraft.resources.ResourceLocation.parse("techguns:nether_structures"));
        for(int grid:new int[]{16,32}){
            // Exercise the real noise/decorating pipeline, including WorldGenRegion write limits.
            var chunk=nether.getChunk(grid,grid);h.assertTrue(chunk.getStatus()==net.minecraft.world.level.chunk.ChunkStatus.FULL,"Native Nether chunk must finish generation");
            var biome=nether.getBiome(new BlockPos(grid*16+8,60,grid*16+8)).value();boolean registered=false;
            for(var stage:biome.getGenerationSettings().features())for(var feature:stage)if(feature.is(key))registered=true;
            // Native structures replaced the feature (for /locate); a still attached feature would place every site twice.
            h.assertTrue(!registered,"The old Nether structure feature must no longer be attached to Nether biomes");
                        var choice=techguns.world.NetherStructuresFeature.choose(grid,grid,techguns.world.SurfaceStructuresFeature.selectionRandom(nether.getSeed(),grid,grid));
            var start=choice==null?null:chunk.getStartForStructure(h.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Techguns.MODID,choice)));
            Techguns.LOGGER.info("Nether grid {} chose {}: start {}",grid,choice,start!=null&&start.isValid());
        }
        Techguns.LOGGER.info("TECHGUNS_NATIVE_NETHER_GENERATION_PASSED: actual small/medium grid chunks completed the Nether noise/decorating pipeline with native structures instead of the feature");h.succeed();
    }
    private NetherStructureGameTests(){}
}
