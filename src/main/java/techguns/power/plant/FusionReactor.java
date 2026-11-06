package techguns.power.plant;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Fusion reactor (tokamak): 5x5x3, top and bottom layers casing, the middle layer a ring of magnet coils around an
 * empty 3x3 chamber, the controller in the middle of the front of the ring. It takes IGNITION FE from outside to
 * light the plasma, then makes RATE FE per tick from the deuterium in water (WATER_USE mB per tick). Without water
 * the plasma goes out and has to be lit again.
 */
public final class FusionReactor extends PlantController {
    public static final int RATE=8000,IGNITION=2_000_000,CAPACITY=4_000_000,OUTPUT=16_000,INPUT=20_000,TANK=32_000,WATER_USE=20,WATER_TO_IGNITE=1000;
    public final FluidTank water=new FluidTank(TANK,f->f.getFluid().isSame(Fluids.WATER)){@Override protected void onContentsChanged(){setChanged();}};
    private final LazyOptional<IFluidHandler> fluids=LazyOptional.of(()->water);
    public boolean ignited;
    public FusionReactor(BlockPos pos,BlockState state){super(PlantBlocks.FUSION_ENTITY.get(),PlantKind.FUSION,pos,state,CAPACITY,OUTPUT);}
    @Override protected Map<BlockPos,Need> structure(){
        var map=map();var casing=PlantBlocks.CASING.get();var coil=PlantBlocks.COIL.get();
        for(int y=-1;y<=1;y++)for(int r=-2;r<=2;r++)for(int b=0;b<=4;b++){
            if(r==0&&y==0&&b==0)continue;
            boolean ring=Math.abs(r)==2||b==0||b==4;
            map.put(at(r,y,b),y!=0?part(casing):ring?part(coil):AIR);
        }
        return map;
    }
    @Override protected boolean takes(Direction side){return formed&&!ignited;}
    @Override protected int inputLimit(){return INPUT;}
    @Override protected boolean pushes(){return ignited;}
    @Override protected void work(){
        rate=0;
        if(!ignited){
            problem=water.getFluidAmount()<WATER_TO_IGNITE?2:energy.getEnergyStored()<IGNITION?1:0;
            if(problem==0){energy.extractEnergy(IGNITION,false);ignited=true;resetHandles();sync();}
            return;
        }
        if(water.getFluidAmount()<WATER_USE){ignited=false;problem=2;resetHandles();sync();return;}
        water.drain(WATER_USE,IFluidHandler.FluidAction.EXECUTE);
        if(energy.getEnergyStored()+RATE<=energy.getMaxEnergyStored()){produce(RATE);rate=RATE;}
        problem=rate==0?3:0;
        setChanged();
    }
    @Override protected void onUnformed(){ignited=false;}
    @Override public int value(int i){return switch(i){case 0->water.getFluidAmount();case 1->TANK;case 3->ignited?1000:(int)Math.min(999,(long)energy.getEnergyStored()*1000/IGNITION);default->0;};}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){
        if(capability==ForgeCapabilities.FLUID_HANDLER)return fluids.cast();
        return super.getCapability(capability,side);
    }
    @Override public void invalidateCaps(){super.invalidateCaps();fluids.invalidate();}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.put("Water",water.writeToNBT(new CompoundTag()));tag.putBoolean("Ignited",ignited);}
    @Override public void load(CompoundTag tag){super.load(tag);water.readFromNBT(tag.getCompound("Water"));ignited=tag.getBoolean("Ignited");}
}
