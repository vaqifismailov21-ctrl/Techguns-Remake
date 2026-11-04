package techguns.gametest;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.tileentities.NpcSpawnerEntity;
import techguns.world.*;

@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class SurfaceStructureGameTests {
    @GameTest(template="weapon_arena",batch="surface-structures",timeoutTicks=500) public static void sourceSurfaceScansRetainGuardsLootAndBlockStatesInFourRotations(GameTestHelper h){
        String[] ids={"factory_building_small","small_trainstation","small_mine","gasstation","policestation","survivor_hideout","orecluster_spike","orecluster_meteorbase"};
        var position=h.absolutePos(new BlockPos(16,0,16));var origin=new BlockPos(position.getX(),96,position.getZ());int fixtures=0,chests=0,guards=0,doors=0;
        for(var id:ids)for(int turn=0;turn<4;turn++){
            for(int x=-5;x<26;x++)for(int z=-5;z<26;z++)for(int y=-7;y<17;y++)h.getLevel().setBlock(origin.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);
            var scan=ScannedStructure.load(id);var placed=scan.place(h.getLevel(),origin,turn,RandomSource.create(1729+turn),RandomSource.create(9712+turn),turn%3);
            h.assertTrue(placed.size()==scan.blockCount(),"Every original surface coordinate must be placed: "+id);
            int realBlocks=0;
            for(var pos:placed){var state=h.getLevel().getBlockState(pos);if(!state.isAir())realBlocks++;
                if(h.getLevel().getBlockEntity(pos) instanceof NpcSpawnerEntity spawner){h.assertTrue(spawner.remaining()>0&&spawner.delay()>0,"Original finite structure guards: "+id);guards++;}
                if(h.getLevel().getBlockEntity(pos) instanceof ChestBlockEntity chest){String expected=switch(id){case "factory_building_small"->"factory_building";case "small_trainstation"->"small_trainstation";default->id;};h.assertTrue(chest.saveWithoutMetadata().getString("LootTable").equals("techguns:chests/"+expected),"Original structure-specific chest loot: "+id);chests++;}
                if(state.is(TGDoors.BUNKER.get())){h.assertTrue(state.getValue(DoorBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER?h.getLevel().getBlockState(pos.above()).is(TGDoors.BUNKER.get()):h.getLevel().getBlockState(pos.below()).is(TGDoors.BUNKER.get()),"Both original bunker door halves must survive structure placement");doors++;}
            }
            h.assertTrue(realBlocks>20,"Surface structure cannot be an empty substitute: "+id);fixtures++;
        }
        h.assertTrue(fixtures==32&&chests>0&&guards>0&&doors>0,"All eight original surface scans need their actual geometry, doors, guards and loot");
        Techguns.LOGGER.info("TECHGUNS_SURFACE_STRUCTURES_PASSED: eight complete original scans, thirty-two rotated fixtures, biome palettes, source-specific loot and finite guards");h.succeed();
    }
    @GameTest(template="weapon_arena",batch="surface-structures",timeoutTicks=400) public static void smallMineChoosesOneSharedClusterKindWithMatchingOrePalette(GameTestHelper h){
        var position=h.absolutePos(new BlockPos(16,0,16));var origin=new BlockPos(position.getX(),96,position.getZ());var seen=new HashSet<ClusterKind>();
        for(int sample=0;sample<12;sample++){
            var placed=ScannedStructure.load("small_mine").place(h.getLevel(),origin,0,RandomSource.create(1729+sample),RandomSource.create(9712+sample*31),sample%3);var kinds=new HashSet<ClusterKind>();
            for(var pos:placed)for(var entry:TGWorld.CLUSTERS.entrySet())if(h.getLevel().getBlockState(pos).is(entry.getValue().get()))kinds.add(entry.getKey());
            h.assertTrue(kinds.size()==1,"One original index roll must select the same cluster family throughout the mine");seen.addAll(kinds);
        }
        h.assertTrue(seen.size()>=3,"Source weighted mine cluster selection must support different resource families");h.succeed();
    }
    @GameTest(template="empty",batch="surface-structures",timeoutTicks=400) public static void sourceSurfaceGridAndRealFeaturePlacementRespectTerrain(GameTestHelper h){
        var random=RandomSource.create(1729);var small=new HashSet<String>();for(int i=0;i<200;i++)small.add(SurfaceStructuresFeature.choose(16,16,false,false,false,random));h.assertTrue(small.size()==4,"All four source small Overworld structures must keep their table weights");
        h.assertTrue(SurfaceStructuresFeature.choose(16,16,true,false,false,random)==null,"Original small structures do not spawn in ocean biomes");h.assertTrue(SurfaceStructuresFeature.choose(64,64,true,false,false,random).equals("aircraftcarrier"),"Original ocean big grid selects the carrier, not land bases");
        // The GameTest server uses a real seed-dependent biome map: (256,256) can be ocean.
        BlockPos site=null;for(int cx=16;cx<256&&site==null;cx+=32)for(int cz=16;cz<256;cz+=32){var point=new BlockPos(cx*16,64,cz*16);if(!h.getLevel().getBiome(point).is(net.minecraft.tags.BiomeTags.IS_OCEAN)){site=point;break;}}
        h.assertTrue(site!=null,"Land biome fixture must be available on an original small grid");int baseX=site.getX(),baseZ=site.getZ();
        // A seed-dependent hill or previously generated building can extend above the fixture's floor.
        for(int x=baseX-10;x<=baseX+30;x++)for(int z=baseZ-10;z<=baseZ+30;z++){int top=h.getLevel().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,x,z);for(int y=top;y>95;y--)h.getLevel().setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),18);h.getLevel().setBlock(new BlockPos(x,95,z),Blocks.STONE.defaultBlockState(),18);}
        h.assertTrue(SurfaceStructuresFeature.surfaceHeight(h.getLevel(),baseX,baseZ,11,12)==96,"Surface height must use the original four-block sampling and mean height");
        boolean generated=TGWorld.SURFACE_STRUCTURES.get().place(new FeaturePlaceContext<>(Optional.empty(),h.getLevel(),h.getLevel().getChunkSource().getGenerator(),RandomSource.create(1729),new BlockPos(baseX,0,baseZ),NoneFeatureConfiguration.INSTANCE));h.assertTrue(generated,"Registered surface feature must place an actual source small structure in a land biome");h.succeed();
    }
    @GameTest(template="empty",batch="surface-structures") public static void originalNestTerrainAndBenchOrientationRemainFunctional(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(1,2,1));h.assertTrue(TGWorld.HARD_SAND.get().defaultBlockState().getDestroySpeed(h.getLevel(),pos)==3&&!(TGWorld.HARD_SAND.get() instanceof FallingBlock),"Original hardened nest sand is a three-hardness solid block without falling physics");
        h.assertTrue(TGWorld.SLIMY_EGGS.get().defaultBlockState().getDestroySpeed(h.getLevel(),pos)==4&&TGWorld.SLIMY_EGGS.get().defaultBlockState().getLightEmission()==0,"Original strange eggs hardness and actual zero light level");
        h.assertTrue(TGWorld.SLIMY_TRAIL.get().defaultBlockState().getDestroySpeed(h.getLevel(),pos)==0&&TGWorld.SLIMY_TRAIL.get().defaultBlockState().is(net.minecraft.tags.BlockTags.CLIMBABLE),"Original slimy trail remains a zero-hardness climbable ladder");
        var trail=TGWorld.SLIMY_TRAIL.get().defaultBlockState().setValue(LadderBlock.FACING,net.minecraft.core.Direction.SOUTH);h.getLevel().setBlock(pos.north(),Blocks.STONE.defaultBlockState(),2);h.assertTrue(trail.canSurvive(h.getLevel(),pos),"Trail must attach to a solid wall");h.getLevel().setBlock(pos.north(),Blocks.REDSTONE_BLOCK.defaultBlockState(),2);h.assertTrue(!trail.canSurvive(h.getLevel(),pos),"Original trail cannot attach to power-providing blocks");
        var bench=TGBenches.BLOCKS.get(techguns.machines.BenchKind.REPAIR).get().defaultBlockState();h.assertTrue(bench.rotate(Rotation.CLOCKWISE_90).getValue(techguns.machines.BenchBlock.FACING)==net.minecraft.core.Direction.EAST,"Original directional workbenches rotate with the structure");h.succeed();
    }
    private SurfaceStructureGameTests(){}
}
