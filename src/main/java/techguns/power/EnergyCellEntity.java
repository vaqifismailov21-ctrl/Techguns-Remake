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
    @Override protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder components){super.collectImplicitComponents(components);if(energy.getEnergyStored()>0)components.set(techguns.TGDataComponents.ENERGY.get(),energy.getEnergyStored());}
    @Override protected void applyImplicitComponents(net.minecraft.world.level.block.entity.BlockEntity.DataComponentInput input){super.applyImplicitComponents(input);int stored=input.getOrDefault(techguns.TGDataComponents.ENERGY.get(),0);if(stored>0&&energy.getEnergyStored()==0)energy.receiveEnergy(stored,false);}
}
