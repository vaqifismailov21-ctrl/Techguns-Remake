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
    @GameTest(template="empty",batch="power",timeoutTicks=100) public static void geothermalGeneratorRunsOnLavaWithoutUsingItUp(GameTestHelper h){
        h.setBlock(2,1,2,Blocks.LAVA);h.setBlock(3,2,2,Blocks.LAVA);h.setBlock(2,2,2,PowerBlocks.GEOTHERMAL_GENERATOR.get());
        var generator=(GeothermalGeneratorEntity)h.getBlockEntity(new BlockPos(2,2,2));
        h.runAfterDelay(30,()->{
            h.assertTrue(generator.lava==2&&generator.rate==2*PowerBlocks.GEOTHERMAL_PER_LAVA,"Two lava sources: "+generator.lava+" sources, "+generator.rate+" FE/t");
            h.assertTrue(generator.energyStored()>0,"Energy is made");
            h.assertTrue(h.getLevel().getFluidState(h.absolutePos(new BlockPos(2,1,2))).isSource()&&h.getLevel().getFluidState(h.absolutePos(new BlockPos(3,2,2))).isSource(),"Lava is not used up");
            h.assertTrue(h.getBlockState(new BlockPos(2,2,2)).getValue(FacingMachineBlock.LIT),"Working generator is lit");
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="power",timeoutTicks=200) public static void nuclearGeneratorBurnsMicroReactorsAndNeedsWater(GameTestHelper h){
        h.setBlock(1,1,1,PowerBlocks.NUCLEAR_GENERATOR.get());h.setBlock(3,1,1,PowerBlocks.NUCLEAR_GENERATOR.get());h.setBlock(4,1,1,Blocks.WATER);
        var dry=(NuclearGeneratorEntity)h.getBlockEntity(new BlockPos(1,1,1));var wet=(NuclearGeneratorEntity)h.getBlockEntity(new BlockPos(3,1,1));
        h.assertTrue(dry.slots.insertItem(0,new ItemStack(Items.COAL),true).getCount()==1,"Only micro reactors go in");
        dry.slots.setStackInSlot(0,new ItemStack(techguns.TGItems.NUCLEAR_POWERCELL.get(),2));wet.slots.setStackInSlot(0,new ItemStack(techguns.TGItems.NUCLEAR_POWERCELL.get()));
        h.runAfterDelay(40,()->{
            h.assertTrue(dry.slots.getStackInSlot(0).getCount()==1&&dry.slots.getStackInSlot(1).is(techguns.TGItems.NUCLEAR_POWERCELL_EMPTY.get()),"One reactor burns, the spent one goes to the second slot");
            h.assertTrue(dry.energyStored()>=PowerBlocks.NUCLEAR_RATE*30,"Energy is made: "+dry.energyStored());
            h.assertTrue(dry.heat>20&&wet.heat==0&&wet.cooled&&!dry.cooled,"Without water it heats up, with water it stays cool: "+dry.heat+" / "+wet.heat);
            dry.heat=PowerBlocks.NUCLEAR_HEAT_MAX-1;
            h.runAfterDelay(5,()->{
                h.assertTrue(dry.overheated&&!dry.running(),"At the heat limit it shuts down");
                h.assertTrue(h.getBlockState(new BlockPos(1,1,1)).is(PowerBlocks.NUCLEAR_GENERATOR.get()),"No explosion with the default config");
                h.succeed();
            });
        });
    }
    @GameTest(template="empty",batch="power",timeoutTicks=60) public static void windTurbineColumnBecomesBaseMastsAndHead(GameTestHelper h){
        for(int y=1;y<=4;y++)h.setBlock(2,y,2,PowerBlocks.WIND_TURBINE.get());
        h.runAfterDelay(2,()->{
            var part=(java.util.function.IntFunction<WindTurbineBlock.Part>)y->h.getBlockState(new BlockPos(2,y,2)).getValue(WindTurbineBlock.PART);
            h.assertTrue(part.apply(1)==WindTurbineBlock.Part.BASE&&part.apply(2)==WindTurbineBlock.Part.MAST&&part.apply(3)==WindTurbineBlock.Part.MAST&&part.apply(4)==WindTurbineBlock.Part.HEAD,"Stacked pieces become base, masts and head");
            var base=(WindTurbineEntity)h.getBlockEntity(new BlockPos(2,1,2));base.survey();
            h.assertTrue(base.masts==2&&base.problem!=1,"The base finds two masts and the head");
            if(h.absolutePos(new BlockPos(2,4,2)).getY()<64)h.assertTrue(base.problem==3&&base.rate==0,"No wind below Y 64");
            h.assertTrue(WindTurbineEntity.findBase(h.getLevel(),h.absolutePos(new BlockPos(2,4,2)))==base,"The head finds its base");
            h.setBlock(2,4,2,Blocks.AIR);
            h.runAfterDelay(2,()->{base.survey();h.assertTrue(base.problem==1&&base.rate==0,"Without a head it does not work");h.succeed();});
        });
    }
    @GameTest(template="empty",batch="power",timeoutTicks=60) public static void energyCellAndBankShowTheirCharge(GameTestHelper h){
        h.setBlock(1,1,1,PowerBlocks.ENERGY_CELL.get());h.setBlock(3,1,1,PowerBlocks.ENERGY_BANK.get());
        var cell=(EnergyCellEntity)h.getBlockEntity(new BlockPos(1,1,1));var bank=(EnergyCellEntity)h.getBlockEntity(new BlockPos(3,1,1));
        h.assertTrue(cell.energyCapacity()==PowerBlocks.CELL_CAPACITY&&bank.energyCapacity()==PowerBlocks.BANK_CAPACITY,"Cell and bank capacities");
        var cap=cell.getCapability(ForgeCapabilities.ENERGY,Direction.UP).resolve().orElseThrow();
        for(int i=0;i<500;i++)cap.receiveEnergy(PowerBlocks.CELL_TRANSFER,false);
        h.runAfterDelay(3,()->{
            h.assertTrue(h.getBlockState(new BlockPos(1,1,1)).getValue(EnergyCellBlock.CHARGE)==2,"Half full shows two bars: "+h.getBlockState(new BlockPos(1,1,1)).getValue(EnergyCellBlock.CHARGE));
            h.assertTrue(h.getBlockState(new BlockPos(3,1,1)).getValue(EnergyCellBlock.CHARGE)==0,"An empty bank shows none");
            h.assertTrue(EnergyCellBlock.chargeLevel(1,100)==1&&EnergyCellBlock.chargeLevel(100,100)==4,"Any charge shows a bar, full shows four");
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="power",timeoutTicks=200) public static void energyCableCarriesEnergyFromGeneratorToMachine(GameTestHelper h){
        h.setBlock(0,1,1,PowerBlocks.FUEL_GENERATOR.get());for(int x=1;x<=3;x++)h.setBlock(x,1,1,PowerBlocks.ENERGY_CABLE.get());h.setBlock(4,1,1,TGMachines.BLOCKS.get(MachineKind.AMMO_PRESS).get());
        var generator=(FuelGeneratorEntity)h.getBlockEntity(new BlockPos(0,1,1));var press=(ProcessingMachine)h.getBlockEntity(new BlockPos(4,1,1));
        generator.fuel.setStackInSlot(0,new ItemStack(Items.COAL));
        h.runAfterDelay(100,()->{
            h.assertTrue(press.energy.getEnergyStored()>=PowerBlocks.GENERATOR_RATE*40,"The machine three cables away gets the energy: "+press.energy.getEnergyStored());
            var state=h.getBlockState(new BlockPos(1,1,1));
            h.assertTrue(state.getValue(EnergyCableBlock.SIDES.get(Direction.EAST))&&state.getValue(EnergyCableBlock.SIDES.get(Direction.WEST))&&!state.getValue(EnergyCableBlock.SIDES.get(Direction.UP)),"The cable joins its neighbours only");
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="power",timeoutTicks=120) public static void floodlightLightsItsBeamOnlyWithEnergy(GameTestHelper h){
        h.setBlock(0,1,2,PowerBlocks.FLOODLIGHT.get().defaultBlockState().setValue(FacingMachineBlock.FACING,Direction.EAST));
        var light=(FloodlightEntity)h.getBlockEntity(new BlockPos(0,1,2));
        h.runAfterDelay(25,()->{
            h.assertTrue(!h.getBlockState(new BlockPos(0,1,2)).getValue(FacingMachineBlock.LIT)&&!h.getBlockState(new BlockPos(2,1,2)).is(PowerBlocks.FLOODLIGHT_LIGHT.get()),"No energy, no light");
            light.getCapability(ForgeCapabilities.ENERGY,Direction.WEST).resolve().orElseThrow().receiveEnergy(100,false);
            h.runAfterDelay(25,()->{
                h.assertTrue(h.getBlockState(new BlockPos(0,1,2)).getValue(FacingMachineBlock.LIT),"With energy it is lit");
                h.assertTrue(h.getBlockState(new BlockPos(2,1,2)).is(PowerBlocks.FLOODLIGHT_LIGHT.get())&&h.getLevel().getBrightness(net.minecraft.world.level.LightLayer.BLOCK,h.absolutePos(new BlockPos(2,1,2)))==15,"The beam places light ahead");
                h.setBlock(0,1,2,Blocks.AIR);
                h.runAfterDelay(2,()->{h.assertTrue(!h.getBlockState(new BlockPos(2,1,2)).is(PowerBlocks.FLOODLIGHT_LIGHT.get()),"Removing the floodlight removes its light");h.succeed();});
            });
        });
    }
    @GameTest(template="empty",batch="power",timeoutTicks=20) public static void solarPanelTiersHaveTheirRates(GameTestHelper h){
        h.setBlock(0,1,0,PowerBlocks.SOLAR_PANEL.get());h.setBlock(2,1,0,PowerBlocks.SOLAR_PANEL_ADVANCED.get());h.setBlock(4,1,0,PowerBlocks.SOLAR_PANEL_INDUSTRIAL.get());
        int[] best={20,60,160};int[] cap={20_000,60_000,160_000};
        for(int i=0;i<3;i++){var panel=(SolarPanelEntity)h.getBlockEntity(new BlockPos(i*2,1,0));h.assertTrue(panel.tier==i&&panel.bestRate()==best[i]&&panel.energyCapacity()==cap[i],"Tier "+i);}
        h.succeed();
    }
    private PowerGameTests(){}
}
