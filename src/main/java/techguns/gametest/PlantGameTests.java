package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.power.plant.*;

/** Multiblock power plants: they form from their parts, make energy, hand it out through every face and fall apart when a part goes. */
@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class PlantGameTests {
    private static void cube(GameTestHelper h,int x0,int y0,int z0,int x1,int y1,int z1,net.minecraft.world.level.block.Block block){for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)h.setBlock(x,y,z,block);}
    @GameTest(template="empty",batch="plants",timeoutTicks=200) public static void fissionReactorFormsBurnsFuelAndFallsApart(GameTestHelper h){
        cube(h,1,1,1,3,3,3,PlantBlocks.CASING.get());h.setBlock(2,2,2,Blocks.AIR);h.setBlock(3,2,2,PlantBlocks.GLASS.get());
        h.setBlock(2,2,1,PlantBlocks.FISSION.get().defaultBlockState().setValue(PlantControllerBlock.FACING,Direction.NORTH));
        var reactor=(FissionReactor)h.getBlockEntity(new BlockPos(2,2,1));
        h.assertTrue(reactor.form(),"The reactor forms: "+reactor.invalid());
        h.assertTrue(h.getBlockState(new BlockPos(1,1,1)).getValue(PlantPartBlock.FORMED),"Parts are marked formed");
        reactor.slots.setStackInSlot(0,new ItemStack(techguns.TGItems.NUCLEAR_POWERCELL.get()));reactor.water.fill(new FluidStack(Fluids.WATER,8000),IFluidHandler.FluidAction.EXECUTE);
        h.runAfterDelay(40,()->{
            h.assertTrue(reactor.rate==FissionReactor.RATE&&reactor.energyStored()>FissionReactor.RATE*20,"Energy is made: "+reactor.rate+" FE/t, "+reactor.energyStored());
            h.assertTrue(reactor.water.getFluidAmount()<8000&&reactor.heat<FissionReactor.HEAT_MAX/2,"Water cools it: "+reactor.water.getFluidAmount()+" mB, heat "+reactor.heat);
            var part=h.getBlockEntity(new BlockPos(1,3,3));h.assertTrue(part.getCapability(ForgeCapabilities.ENERGY,Direction.UP).isPresent(),"Parts hand out the reactor's energy");
            h.setBlock(1,3,3,Blocks.AIR);
            h.assertTrue(!reactor.formed&&!h.getBlockState(new BlockPos(1,1,1)).getValue(PlantPartBlock.FORMED),"Taking a part out unforms it");
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="plants",timeoutTicks=200) public static void fusionReactorIgnitesWithEnergyAndRunsOnWater(GameTestHelper h){
        cube(h,0,1,1,4,1,5,PlantBlocks.CASING.get());cube(h,0,3,1,4,3,5,PlantBlocks.CASING.get());cube(h,0,2,1,4,2,5,PlantBlocks.COIL.get());cube(h,1,2,2,3,2,4,Blocks.AIR);
        h.setBlock(2,2,1,PlantBlocks.FUSION.get().defaultBlockState().setValue(PlantControllerBlock.FACING,Direction.NORTH));
        var reactor=(FusionReactor)h.getBlockEntity(new BlockPos(2,2,1));
        h.assertTrue(reactor.form(),"The reactor forms: "+reactor.invalid());
        reactor.water.fill(new FluidStack(Fluids.WATER,4000),IFluidHandler.FluidAction.EXECUTE);
        var input=reactor.getCapability(ForgeCapabilities.ENERGY,Direction.NORTH).resolve().orElseThrow();
        for(int i=0;i<FusionReactor.IGNITION/FusionReactor.INPUT+1;i++)input.receiveEnergy(FusionReactor.INPUT,false);
        h.runAfterDelay(20,()->{
            h.assertTrue(reactor.ignited&&reactor.rate==FusionReactor.RATE,"Ignited and making energy: "+reactor.ignited+" "+reactor.rate);
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="plants",timeoutTicks=100) public static void geothermalPlantCountsLavaBelow(GameTestHelper h){
        cube(h,1,1,1,3,1,3,Blocks.LAVA);cube(h,1,2,1,3,3,3,PlantBlocks.CASING.get());
        h.setBlock(2,2,1,PlantBlocks.GEOTHERMAL.get().defaultBlockState().setValue(PlantControllerBlock.FACING,Direction.NORTH));
        var plant=(GeothermalPlant)h.getBlockEntity(new BlockPos(2,2,1));
        h.assertTrue(plant.form(),"The plant forms: "+plant.invalid());
        h.runAfterDelay(45,()->{h.assertTrue(plant.lava==9&&plant.rate==9*GeothermalPlant.PER_LAVA,"Nine lava sources: "+plant.lava+" "+plant.rate);h.succeed();});
    }
    @GameTest(template="empty",batch="plants",timeoutTicks=100) public static void windPlantNeedsATowerAndANacelle(GameTestHelper h){
        h.setBlock(2,1,2,PlantBlocks.WIND.get().defaultBlockState().setValue(PlantControllerBlock.FACING,Direction.NORTH));
        var plant=(WindPlant)h.getBlockEntity(new BlockPos(2,1,2));
        for(int y=2;y<2+WindPlant.MIN_TOWER-1;y++)h.setBlock(2,y,2,PlantBlocks.TOWER.get());
        h.assertTrue(!plant.form(),"A tower that is too short does not form");
        h.setBlock(2,1+WindPlant.MIN_TOWER,2,PlantBlocks.TOWER.get());h.setBlock(2,2+WindPlant.MIN_TOWER,2,PlantBlocks.CASING.get());
        var hub=PlantBlocks.HUB.get().defaultBlockState().setValue(RotorHubBlock.FACING,Direction.NORTH);
        h.setBlock(2,2+WindPlant.MIN_TOWER,1,hub.setValue(RotorHubBlock.BLADES,2));
        h.assertTrue(!plant.form(),"A hub with two blades does not form");
        h.setBlock(2,2+WindPlant.MIN_TOWER,1,hub.setValue(RotorHubBlock.BLADES,RotorHubBlock.MAX_BLADES));
        h.assertTrue(plant.form()&&plant.towers==WindPlant.MIN_TOWER,"Eight sections, a nacelle and a full hub form: "+plant.invalid());
        h.runAfterDelay(45,()->{h.assertTrue(plant.rate>0,"It makes energy: "+plant.rate);h.succeed();});
    }
}
