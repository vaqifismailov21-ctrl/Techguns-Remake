package techguns.power.plant;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import techguns.TGConfig;
import techguns.TGItems;
import techguns.TGRadiationSystem;

/**
 * Fission reactor: 3x3x3 of casing (reactor glass allowed in the middle of each side) with an empty core, the
 * controller in the middle of the front. Burns nuclear micro reactors (three times their energy compared with the
 * small generator) at RATE FE per tick. Running heats it; water from the tank (WATER_USE mB per tick) cools it.
 * At HEAT_MAX it shuts down until it has cooled to half, or explodes when nuclearGeneratorExplodes is set.
 */
public final class FissionReactor extends PlantController {
    public static final int RATE=3000,CELL_ENERGY=14_400_000,CAPACITY=1_000_000,OUTPUT=6000,TANK=16_000,WATER_USE=10,HEAT_MAX=1000;
    public final ItemStackHandler slots=new ItemStackHandler(2){
        @Override public boolean isItemValid(int slot,ItemStack stack){return slot==0&&stack.is(TGItems.NUCLEAR_POWERCELL.get());}
        @Override protected void onContentsChanged(int slot){setChanged();}
    };
    private final IItemHandler automation=new IItemHandler(){
        @Override public int getSlots(){return 2;}
        @Override public ItemStack getStackInSlot(int slot){return slots.getStackInSlot(slot);}
        @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return slot==0?slots.insertItem(0,stack,simulate):stack;}
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate){return slot==1?slots.extractItem(1,amount,simulate):ItemStack.EMPTY;}
        @Override public int getSlotLimit(int slot){return 64;}
        @Override public boolean isItemValid(int slot,ItemStack stack){return slots.isItemValid(slot,stack);}
    };
    public final FluidTank water=new FluidTank(TANK,f->f.getFluid().isSame(Fluids.WATER)){@Override protected void onContentsChanged(){setChanged();}};
    private final LazyOptional<IItemHandler> items=LazyOptional.of(()->automation);
    private final LazyOptional<net.minecraftforge.fluids.capability.IFluidHandler> fluids=LazyOptional.of(()->water);
    public int fuel,heat;public boolean overheated;
    public FissionReactor(BlockPos pos,BlockState state){super(PlantBlocks.FISSION_ENTITY.get(),PlantKind.FISSION,pos,state,CAPACITY,OUTPUT);}
    @Override protected Map<BlockPos,Need> structure(){
        var map=map();var casing=PlantBlocks.CASING.get();var glass=PlantBlocks.GLASS.get();
        for(int y=-1;y<=1;y++)for(int r=-1;r<=1;r++)for(int b=0;b<=2;b++){
            if(r==0&&y==0&&b==0)continue;
            int outer=(Math.abs(r)==1?1:0)+(Math.abs(y)==1?1:0)+(b!=1?1:0);
            map.put(at(r,y,b),outer==0?AIR:outer==1?either(casing,glass):part(casing));
        }
        return map;
    }
    public boolean running(){return fuel>0&&!overheated;}
    @Override protected void work(){
        if(!overheated&&fuel<=0&&energy.getEnergyStored()+RATE<=energy.getMaxEnergyStored()){
            var input=slots.getStackInSlot(0);var output=slots.getStackInSlot(1);var spent=new ItemStack(TGItems.NUCLEAR_POWERCELL_EMPTY.get());
            if(!input.isEmpty()&&(output.isEmpty()||ItemStack.isSameItemSameTags(output,spent)&&output.getCount()<output.getMaxStackSize())){
                input.shrink(1);slots.setStackInSlot(0,input);fuel=CELL_ENERGY;
                if(output.isEmpty())slots.setStackInSlot(1,spent);else{output.grow(1);slots.setStackInSlot(1,output);}
            }
        }
        boolean working=running()&&energy.getEnergyStored()+RATE<=energy.getMaxEnergyStored();
        rate=0;
        if(working){int made=Math.min(RATE,fuel);fuel-=made;produce(made);rate=made;heat+=2;}
        boolean cooled=water.getFluidAmount()>=WATER_USE&&(working||heat>0);
        if(cooled){water.drain(WATER_USE,net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);heat-=3;}else if(!working)heat-=1;
        heat=Math.max(0,heat);
        if(heat>=HEAT_MAX){
            if(TGConfig.nuclearGeneratorExplodes){var c=at(0,0,1);unform();level.explode(null,c.getX()+.5,c.getY()+.5,c.getZ()+.5,12,Level.ExplosionInteraction.BLOCK);return;}
            overheated=true;
        }
        if(overheated&&heat<=HEAT_MAX/2)overheated=false;
        problem=overheated?3:fuel<=0&&slots.getStackInSlot(0).isEmpty()?1:water.getFluidAmount()<WATER_USE&&working?2:0;
        if(working&&level.getGameTime()%20==0&&TGRadiationSystem.isEnabled())TGRadiationSystem.applyRadToEntities(this,6,200,3,2,1);
        setChanged();
    }
    @Override public void effects(RandomSource random){
        if(level==null||random.nextInt(4)!=0)return;var top=at(random.nextInt(3)-1,2,random.nextInt(3));
        level.addParticle(ParticleTypes.CLOUD,top.getX()+random.nextDouble(),top.getY()+0.1,top.getZ()+random.nextDouble(),0,0.05,0);
    }
    @Override public int value(int i){return switch(i){case 0->water.getFluidAmount();case 1->TANK;case 2->heat;case 3->(int)((long)fuel*1000/CELL_ENERGY);default->0;};}
    @Override public ItemStackHandler slots(){return slots;}
    @Override public void dropContents(){if(level==null)return;for(int i=0;i<slots.getSlots();i++)net.minecraft.world.Containers.dropItemStack(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),slots.getStackInSlot(i));}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){
        if(capability==ForgeCapabilities.ITEM_HANDLER)return items.cast();
        if(capability==ForgeCapabilities.FLUID_HANDLER)return fluids.cast();
        return super.getCapability(capability,side);
    }
    @Override public void invalidateCaps(){super.invalidateCaps();items.invalidate();fluids.invalidate();}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.put("Slots",slots.serializeNBT());tag.put("Water",water.writeToNBT(new CompoundTag()));tag.putInt("Fuel",fuel);tag.putInt("Heat",heat);tag.putBoolean("Overheated",overheated);}
    @Override public void load(CompoundTag tag){super.load(tag);slots.deserializeNBT(tag.getCompound("Slots"));water.readFromNBT(tag.getCompound("Water"));fuel=tag.getInt("Fuel");heat=tag.getInt("Heat");overheated=tag.getBoolean("Overheated");}
}
