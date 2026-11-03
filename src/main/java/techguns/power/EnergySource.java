package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import techguns.capabilities.compat.Capability;
import techguns.capabilities.compat.Capabilities;
import techguns.capabilities.compat.LazyOptional;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Energy buffer of the power blocks. Each side either gives energy (pushed to the neighbour every tick and drainable by
 * cables) or takes it; generators only give, the energy cell takes on all sides but its front.
 */
public abstract class EnergySource extends BlockEntity implements techguns.capabilities.compat.ICapabilityHost{
    protected final EnergyStorage energy;
    private final int output;
    @SuppressWarnings("unchecked") private final LazyOptional<IEnergyStorage>[] handles=new LazyOptional[7];
    protected EnergySource(BlockEntityType<?> type,BlockPos pos,BlockState state,int capacity,int output){super(type,pos,state);this.output=output;energy=new EnergyStorage(capacity,capacity,capacity);}
    /** Side (null: unsided access) that gives energy. */
    protected boolean gives(Direction side){return true;}
    /** Side that takes energy from outside. */
    protected boolean takes(Direction side){return false;}
    protected int inputLimit(){return 0;}
    public int energyStored(){return energy.getEnergyStored();}
    public int energyCapacity(){return energy.getMaxEnergyStored();}
    protected void produce(int amount){if(amount>0&&energy.receiveEnergy(amount,false)>0)setChanged();}
    /** Gives stored energy to adjacent blocks that accept it on giving sides, at most {@code output} per tick in total. */
    protected void distribute(){
        if(level==null||energy.getEnergyStored()<=0)return;int budget=Math.min(output,energy.getEnergyStored());
        for(var side:Direction.values()){
            if(budget<=0)break;if(!gives(side))continue;var neighbour=level.getBlockEntity(worldPosition.relative(side));if(neighbour==null)continue;
            if(neighbour instanceof EnergySource source&&!source.takes(side.getOpposite()))continue;
            var target=Capabilities.block(neighbour,Capabilities.ENERGY,side.getOpposite()).resolve();if(target.isEmpty()||!target.get().canReceive())continue;
            int given=target.get().receiveEnergy(budget,false);if(given>0){energy.extractEnergy(given,false);budget-=given;setChanged();}
        }
    }
    private IEnergyStorage handle(Direction side){
        boolean gives=gives(side),takes=takes(side);
        return new IEnergyStorage(){
            @Override public int receiveEnergy(int max,boolean simulate){if(!takes)return 0;int got=energy.receiveEnergy(Math.min(max,inputLimit()),simulate);if(got>0&&!simulate)setChanged();return got;}
            @Override public int extractEnergy(int max,boolean simulate){if(!gives)return 0;int taken=energy.extractEnergy(Math.min(max,output),simulate);if(taken>0&&!simulate)setChanged();return taken;}
            @Override public int getEnergyStored(){return energy.getEnergyStored();}
            @Override public int getMaxEnergyStored(){return energy.getMaxEnergyStored();}
            @Override public boolean canExtract(){return gives;}
            @Override public boolean canReceive(){return takes;}
        };
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){
        if(capability==Capabilities.ENERGY){int i=side==null?6:side.ordinal();if(handles[i]==null)handles[i]=LazyOptional.of(()->handle(side));return handles[i].cast();}
        return LazyOptional.empty();
    }
    /** Sided handles depend on the block's facing. */
    protected void resetHandles(){for(int i=0;i<handles.length;i++)if(handles[i]!=null){handles[i].invalidate();handles[i]=null;}}
    @Override public void invalidateCaps(){invalidateCapabilities();resetHandles();}
    @Override public void setBlockState(BlockState state){super.setBlockState(state);resetHandles();}
    @Override protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(tag, registries);tag.putInt("Energy",energy.getEnergyStored());}
    @Override protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(tag, registries);if(tag.contains("Energy"))energy.deserializeNBT(techguns.util.TGItemNbt.registries(), net.minecraft.nbt.IntTag.valueOf(Math.min(tag.getInt("Energy"),energy.getMaxEnergyStored())));}
}
