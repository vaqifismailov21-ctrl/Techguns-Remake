package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Takes energy on every side. While it has energy it is lit and keeps invisible light blocks every third block along its beam. */
public final class FloodlightEntity extends EnergySource {
    public FloodlightEntity(BlockPos pos,BlockState state){super(PowerBlocks.FLOODLIGHT_ENTITY.get(),pos,state,PowerBlocks.FLOODLIGHT_CAPACITY,0);}
    @Override protected boolean gives(Direction side){return false;}
    @Override protected boolean takes(Direction side){return true;}
    @Override protected int inputLimit(){return 100;}
    /** Free cells of the beam: every third block in front, up to the first solid block. */
    public static void forBeam(Level level,BlockPos pos,Direction facing,java.util.function.Consumer<BlockPos> cell){
        for(int i=1;i<=PowerBlocks.FLOODLIGHT_RANGE;i++){
            var p=pos.relative(facing,i);var state=level.getBlockState(p);
            if(!state.isAir()&&!state.is(PowerBlocks.FLOODLIGHT_LIGHT.get()))break;
            if(i%3==2)cell.accept(p);
        }
    }
    public static void clearBeam(Level level,BlockPos pos,Direction facing){forBeam(level,pos,facing,p->{if(level.getBlockState(p).is(PowerBlocks.FLOODLIGHT_LIGHT.get()))level.removeBlock(p,false);});}
    public void tick(){
        if(level==null)return;
        boolean lit=energy.getEnergyStored()>=PowerBlocks.FLOODLIGHT_USE;
        if(lit){energy.extractEnergy(PowerBlocks.FLOODLIGHT_USE,false);setChanged();}
        var state=getBlockState();var facing=state.getValue(FacingMachineBlock.FACING);
        if(lit!=state.getValue(FacingMachineBlock.LIT)){FacingMachineBlock.setLit(level,worldPosition,state,lit);if(!lit)clearBeam(level,worldPosition,facing);}
        if(lit&&level.getGameTime()%20==Math.floorMod(worldPosition.asLong(),20L))
            forBeam(level,worldPosition,facing,p->{if(level.getBlockState(p).isAir()&&!level.getBlockState(p).is(PowerBlocks.FLOODLIGHT_LIGHT.get()))level.setBlock(p,PowerBlocks.FLOODLIGHT_LIGHT.get().defaultBlockState(),3);});
    }
}
