package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

/** Produces energy in daylight under open sky; less in rain or thunder, nothing at night. */
public final class SolarPanelEntity extends EnergySource {
    public SolarPanelEntity(BlockPos pos,BlockState state){super(PowerBlocks.SOLAR_PANEL_ENTITY.get(),pos,state,PowerBlocks.SOLAR_CAPACITY,PowerBlocks.SOLAR_OUTPUT);}
    public int currentRate(){
        if(level==null||!level.dimensionType().hasSkyLight()||!level.isDay()||!level.canSeeSky(worldPosition.above()))return 0;
        int rate=PowerBlocks.SOLAR_RATE;if(level.isThundering())rate=rate/5;else if(level.isRaining())rate=rate*2/5;
        return level.getBrightness(LightLayer.SKY,worldPosition.above())>=15?rate:rate/2;
    }
    public void tick(){produce(currentRate());distribute();}
}
