package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/**
 * One piece of cable holds up to CABLE_TRANSFER FE. Every tick it evens out its charge with neighbouring cables and
 * gives the rest to the blocks around it that take energy. No losses.
 */
public class EnergyCableEntity extends EnergySource {
    private final int transfer;
    public EnergyCableEntity(BlockPos pos,BlockState state){this(PowerBlocks.ENERGY_CABLE_ENTITY.get(),pos,state,state.getBlock() instanceof EnergyCableBlock b?b.transfer:PowerBlocks.CABLE_TRANSFER);}
    public EnergyCableEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,BlockPos pos,BlockState state,int transfer){super(type,pos,state,transfer,transfer);this.transfer=transfer;}
    @Override protected boolean takes(Direction side){return true;}
    @Override protected int inputLimit(){return transfer;}
    public void tick(){
        if(level==null||energy.getEnergyStored()<=0)return;
        for(var side:Direction.values()){
            int stored=energy.getEnergyStored();if(stored<=0)break;
            var other=level.getBlockEntity(worldPosition.relative(side));if(other==null)continue;
            if(other instanceof EnergyCableEntity cable){
                int diff=(stored-cable.energy.getEnergyStored())/2;
                if(diff>0){int moved=cable.energy.receiveEnergy(diff,false);if(moved>0){energy.extractEnergy(moved,false);cable.setChanged();setChanged();}}
                continue;
            }
            if(other instanceof EnergySource source&&!source.takes(side.getOpposite()))continue;
            var target=other.getCapability(ForgeCapabilities.ENERGY,side.getOpposite()).resolve();
            if(target.isEmpty()||!target.get().canReceive())continue;
            int given=target.get().receiveEnergy(stored,false);if(given>0){energy.extractEnergy(given,false);setChanged();}
        }
    }
}
