package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Stores energy: takes it on every side but the front and gives it through the front only. */
public final class EnergyCellEntity extends EnergySource {
    private final int transfer;
    public EnergyCellEntity(BlockPos pos,BlockState state){
        super(PowerBlocks.ENERGY_CELL_ENTITY.get(),pos,state,state.getBlock() instanceof EnergyCellBlock cell?cell.capacity():PowerBlocks.CELL_CAPACITY,state.getBlock() instanceof EnergyCellBlock cell?cell.transfer():PowerBlocks.CELL_TRANSFER);
        transfer=state.getBlock() instanceof EnergyCellBlock cell?cell.transfer():PowerBlocks.CELL_TRANSFER;
    }
    private Direction front(){return getBlockState().getValue(EnergyCellBlock.FACING);}
    @Override protected boolean gives(Direction side){return side==null||side==front();}
    @Override protected boolean takes(Direction side){return side==null||side!=front();}
    @Override protected int inputLimit(){return transfer;}
    public void tick(){
        distribute();
        int charge=EnergyCellBlock.chargeLevel(energy.getEnergyStored(),energy.getMaxEnergyStored());
        if(level!=null&&getBlockState().getValue(EnergyCellBlock.CHARGE)!=charge)level.setBlock(worldPosition,getBlockState().setValue(EnergyCellBlock.CHARGE,charge),3);
    }
}
