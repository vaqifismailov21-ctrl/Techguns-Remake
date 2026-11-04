package techguns.gametest;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.tileentities.NpcSpawnerEntity;
import techguns.world.*;

@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class OilStructureGameTests {
    @GameTest(template="empty",batch="desert-oil",timeoutTicks=600) public static void originalOilScanUsesConfiguredWorldFluidAndSourceFallbackWithoutInventingOil(GameTestHelper h){
        var previous=TGConfig.fluidListOilWorldspawn;int guards=0,clusters=0,fixtures=0;
        try{for(boolean configured:new boolean[]{false,true}){
            // Vanilla water stands in for an explicitly configured external source fluid; no oil mod is required.
            TGConfig.fluidListOilWorldspawn=configured?new String[]{"water"}:new String[]{"techguns_test_missing_oil"};h.assertTrue((TGOreClusters.worldOil()!=null)==configured,"Only configured fluids with real world blocks are eligible");var seen=new HashSet<String>();var selection=RandomSource.create(1729);for(int i=0;i<500;i++)seen.add(SurfaceStructuresFeature.choose(32,32,false,true,TGOreClusters.worldOil()!=null,selection));h.assertTrue(seen.contains("desert_oil_cluster")==configured,"Natural source oil structures require the original oil integration");
            for(int turns=0;turns<4;turns++){var origin=new BlockPos(1600+(configured?128:0)+turns*24,96,1600);var scan=ScannedStructure.load("desert_oil_cluster");var placed=scan.place(h.getLevel(),origin,turns,RandomSource.create(1729),RandomSource.create(8128),2);h.assertTrue(placed.size()==scan.blockCount()&&scan.heightOffset==-4&&scan.cleanup==6,"Complete original oil geometry, height offset and upward cleanup");int localFluid=0,localGuards=0,localClusters=0;
                for(var pos:placed){var state=h.getLevel().getBlockState(pos);if(state.is(configured?Blocks.WATER:Blocks.LAVA))localFluid++;if(state.is(TGWorld.CLUSTERS.get(ClusterKind.OIL).get()))localClusters++;
                    if(h.getLevel().getBlockEntity(pos) instanceof NpcSpawnerEntity guard){var data=guard.saveWithoutMetadata();var types=data.getList("mobtypes",10);h.assertTrue(guard.remaining()==3&&data.getInt("maxActive")==1&&data.getInt("spawnDelay")==200&&data.getDouble("spawnRange")==1&&types.size()==2&&types.getCompound(0).getInt("Weight")==4&&types.getCompound(1).getInt("Weight")==1,"Original army/commando guard settings");localGuards++;}
                }h.assertTrue(localFluid>0&&localClusters>0&&localGuards>0,"Original oil/fluid pool, cluster and guard must all survive each rotation");guards+=localGuards;clusters+=localClusters;fixtures++;
            }
        }}finally{TGConfig.fluidListOilWorldspawn=previous;}
        h.assertTrue(fixtures==8&&guards>0&&clusters>0,"Both original palette branches and every rotation");Techguns.LOGGER.info("TECHGUNS_DESERT_OIL_PASSED: eight rotated fixtures, configured placeable-fluid integration, source lava/magma/sandstone fallback, original clusters/guards and natural-spawn gate");h.succeed();
    }
    private OilStructureGameTests(){}
}
