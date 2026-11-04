package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import techguns.TGFluids;
import techguns.TGMachines;
import techguns.TGOreClusters;
import techguns.Techguns;
import techguns.machines.MachineKind;
import techguns.machines.ProcessingMachine;
import techguns.power.*;

/** Added power blocks and oil: generators charge adjacent Techguns machines, the cell stores and forwards, oil is the world oil. */
@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class PowerGameTests {
    @GameTest(template="empty",batch="power",timeoutTicks=200) public static void fuelGeneratorBurnsCoalAndPowersAdjacentMachine(GameTestHelper h){
        h.setBlock(1,1,1,PowerBlocks.FUEL_GENERATOR.get());h.setBlock(2,1,1,TGMachines.BLOCKS.get(MachineKind.AMMO_PRESS).get());
        var generator=(FuelGeneratorEntity)h.getBlockEntity(new BlockPos(1,1,1));var press=(ProcessingMachine)h.getBlockEntity(new BlockPos(2,1,1));
        h.assertTrue(generator.fuel.insertItem(0,new ItemStack(Items.DIRT),true).getCount()==1,"Non-fuel items are refused");
        generator.fuel.setStackInSlot(0,new ItemStack(Items.COAL,2));
        h.runAfterDelay(60,()->{
            h.assertTrue(generator.fuel.getStackInSlot(0).getCount()==1,"One coal burns at a time");
            h.assertTrue(press.energy.getEnergyStored()>=PowerBlocks.GENERATOR_RATE*50,"Adjacent machine receives the generated energy: "+press.energy.getEnergyStored());
            h.assertTrue(h.getBlockState(new BlockPos(1,1,1)).getValue(GeneratorBlock.LIT),"Burning generator is lit");
            var cap=generator.getCapability(ForgeCapabilities.ENERGY,Direction.UP).resolve().orElseThrow();h.assertTrue(cap.canExtract()&&!cap.canReceive()&&cap.receiveEnergy(100,false)==0,"Generators only give energy");
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="power",timeoutTicks=200) public static void liquidGeneratorBurnsOilFromABucketAndChargesACell(GameTestHelper h){
        h.assertTrue(TGOreClusters.worldOil()==TGFluids.OIL.get(),"Techguns oil is the world oil with the default config");
        h.setBlock(1,1,1,PowerBlocks.LIQUID_GENERATOR.get());h.setBlock(2,1,1,PowerBlocks.ENERGY_CELL.get().defaultBlockState().setValue(EnergyCellBlock.FACING,Direction.EAST));h.setBlock(3,1,1,TGMachines.BLOCKS.get(MachineKind.METAL_PRESS).get());
        var generator=(LiquidGeneratorEntity)h.getBlockEntity(new BlockPos(1,1,1));var cell=(EnergyCellEntity)h.getBlockEntity(new BlockPos(2,1,1));var press=(ProcessingMachine)h.getBlockEntity(new BlockPos(3,1,1));
        generator.container.setStackInSlot(0,new ItemStack(TGFluids.OIL_BUCKET.get()));
        h.runAfterDelay(60,()->{
            h.assertTrue(generator.container.getStackInSlot(0).is(Items.BUCKET),"The bucket is emptied into the tank and stays in the slot");
            h.assertTrue(generator.tank.getFluidAmount()>0&&generator.tank.getFluidAmount()<1000,"Oil is in the tank and being burnt: "+generator.tank.getFluidAmount());
            h.assertTrue(press.energy.getEnergyStored()>0,"The cell forwards energy through its front to the machine");
            h.assertTrue(cell.getCapability(ForgeCapabilities.ENERGY,Direction.WEST).resolve().orElseThrow().canReceive()&&!cell.getCapability(ForgeCapabilities.ENERGY,Direction.EAST).resolve().orElseThrow().canReceive(),"Cell takes on its back and sides, gives through the front");
            h.assertTrue(LiquidGeneratorEntity.energyPerMb(net.minecraft.world.level.material.Fluids.LAVA)>0&&LiquidGeneratorEntity.energyPerMb(net.minecraft.world.level.material.Fluids.WATER)==0,"Lava burns, water does not");
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="power",timeoutTicks=100) public static void solarPanelWorksOnlyInDaylightUnderOpenSky(GameTestHelper h){
        h.setBlock(1,1,1,PowerBlocks.SOLAR_PANEL.get());var panel=(SolarPanelEntity)h.getBlockEntity(new BlockPos(1,1,1));
        var level=h.getLevel();long time=level.getDayTime();
        try{
            level.setDayTime(6000);int day=panel.currentRate();
            level.setDayTime(18000);int night=panel.currentRate();
            level.setDayTime(6000);h.setBlock(1,3,1,Blocks.STONE);int covered=panel.currentRate();
            h.assertTrue(night==0&&covered==0,"No power at night or without sky");
            h.assertTrue(day>0||!level.canSeeSky(h.absolutePos(new BlockPos(1,2,1))),"Daylight power under open sky: "+day);
        }finally{level.setDayTime(time);}
        h.succeed();
    }
    private PowerGameTests(){}
}
