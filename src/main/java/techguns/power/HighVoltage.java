package techguns.power;

import net.minecraft.world.level.block.entity.BlockEntity;

/** What high-voltage lines (bus ducts, high-voltage insulators) may join: generators, power plants and transformers, never machines. */
public final class HighVoltage {
    private HighVoltage(){}
    /** A source of high-voltage power: generators and power plants. */
    public static boolean generator(BlockEntity be){
        return be instanceof FuelGeneratorEntity||be instanceof LiquidGeneratorEntity||be instanceof GeothermalGeneratorEntity||be instanceof NuclearGeneratorEntity
            ||be instanceof WindTurbineEntity||be instanceof SolarPanelEntity||be instanceof techguns.power.plant.PlantController||be instanceof techguns.power.plant.PlantPartEntity;
    }
    /** Something a high-voltage line may be joined to: generators, transformers, bus ducts and high-voltage insulators. */
    public static boolean equipment(BlockEntity be){
        if(be==null)return false;
        if(generator(be)||be instanceof techguns.power.wire.TransformerEntity)return true;
        var state=be.getBlockState();
        if(state.getBlock() instanceof EnergyCableBlock cable)return cable.highVoltage;
        return be instanceof techguns.power.wire.ConnectorEntity&&state.is(techguns.power.wire.WireBlocks.CONNECTOR_HV.get());
    }
}
