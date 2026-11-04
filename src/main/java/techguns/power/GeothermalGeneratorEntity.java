package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/** GEOTHERMAL_PER_LAVA FE per tick for each lava source below or on the four sides, half again more in the Nether. */
public final class GeothermalGeneratorEntity extends EnergySource {
    private static final Direction[] SIDES={Direction.DOWN,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST};
    public int rate,lava;
    public GeothermalGeneratorEntity(BlockPos pos,BlockState state){super(PowerBlocks.GEOTHERMAL_GENERATOR_ENTITY.get(),pos,state,PowerBlocks.GEOTHERMAL_CAPACITY,PowerBlocks.GEOTHERMAL_OUTPUT);}
    public static int lavaAround(Level level,BlockPos pos){
        int count=0;for(var side:SIDES){var fluid=level.getFluidState(pos.relative(side));if(fluid.is(Fluids.LAVA)&&fluid.isSource())count++;}
        return count;
    }
    public static int rateFor(Level level,int lava){int rate=lava*PowerBlocks.GEOTHERMAL_PER_LAVA;return level.dimension()==Level.NETHER?rate*3/2:rate;}
    public void tick(){
        if(level==null)return;
        if(level.getGameTime()%20==Math.floorMod(worldPosition.asLong(),20L)||rate==0&&lava==0){lava=lavaAround(level,worldPosition);rate=rateFor(level,lava);}
        if(energy.getEnergyStored()<energy.getMaxEnergyStored())produce(rate);
        distribute();
        FacingMachineBlock.setLit(level,worldPosition,getBlockState(),rate>0);
    }
}
