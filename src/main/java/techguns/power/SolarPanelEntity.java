package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

/** Produces energy in daylight under open sky; less in rain or thunder, nothing at night. Rates by tier. */
public final class SolarPanelEntity extends EnergySource {
    public final int tier;
    public SolarPanelEntity(BlockPos pos,BlockState state){
        super(PowerBlocks.SOLAR_PANEL_ENTITY.get(),pos,state,PowerBlocks.SOLAR_CAPACITIES[tierOf(state)],PowerBlocks.SOLAR_OUTPUTS[tierOf(state)]);tier=tierOf(state);
    }
    private static int tierOf(BlockState state){return state.getBlock() instanceof SolarPanelBlock panel?panel.tier:0;}
    public int bestRate(){return PowerBlocks.SOLAR_RATES[tier];}
    public int currentRate(){
        if(level==null||!level.dimensionType().hasSkyLight()||!level.isDay()||!level.canSeeSky(worldPosition.above()))return 0;
        int rate=bestRate();if(level.isThundering())rate=rate/5;else if(level.isRaining())rate=rate/2;
        return level.getBrightness(LightLayer.SKY,worldPosition.above())>=15?rate:rate/2;
    }
    /** Why the output is below the best, for the right click message. */
    public Component reason(){
        if(level==null)return Component.empty();
        if(!level.dimensionType().hasSkyLight())return Component.translatable("techguns.power.reason.no_sky_dimension");
        if(!level.canSeeSky(worldPosition.above()))return Component.translatable("techguns.power.reason.no_sky");
        if(!level.isDay())return Component.translatable("techguns.power.reason.night");
        if(level.isThundering()||level.isRaining())return Component.translatable("techguns.power.reason.rain");
        return Component.empty();
    }
    public void tick(){produce(currentRate());distribute();}
}
