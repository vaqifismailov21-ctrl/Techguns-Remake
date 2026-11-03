package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Stores energy: takes it on every side but the front and gives it through the front only. */
public final class EnergyCellEntity extends EnergySource {
    public EnergyCellEntity(BlockPos pos,BlockState state){super(PowerBlocks.ENERGY_CELL_ENTITY.get(),pos,state,PowerBlocks.CELL_CAPACITY,PowerBlocks.CELL_TRANSFER);}
    private Direction front(){return getBlockState().getValue(EnergyCellBlock.FACING);}
    @Override protected boolean gives(Direction side){return side==null||side==front();}
    @Override protected boolean takes(Direction side){return side==null||side!=front();}
    @Override protected int inputLimit(){return PowerBlocks.CELL_TRANSFER;}
    public void tick(){distribute();}
}
