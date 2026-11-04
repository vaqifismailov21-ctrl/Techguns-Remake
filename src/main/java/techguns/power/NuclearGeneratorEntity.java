package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import techguns.TGConfig;
import techguns.TGItems;
import techguns.TGRadiationSystem;

/**
 * Burns one nuclear micro reactor at a time (NUCLEAR_CELL_ENERGY at NUCLEAR_RATE FE per tick); the spent reactor goes
 * to the second slot. Running heats it up; water on any side cools it faster than it heats. At NUCLEAR_HEAT_MAX it shuts
 * down until it has cooled to half (or explodes when nuclearGeneratorExplodes is set).
 */
public final class NuclearGeneratorEntity extends EnergySource {
    public final ItemStackHandler slots=new ItemStackHandler(2){
        @Override public boolean isItemValid(int slot,ItemStack stack){return slot==0&&stack.is(TGItems.NUCLEAR_POWERCELL.get());}
        @Override protected void onContentsChanged(int slot){setChanged();}
    };
    /** Pipes and hoppers put reactors into the first slot and take spent ones from the second. */
    private final IItemHandler automation=new IItemHandler(){
        @Override public int getSlots(){return 2;}
        @Override public ItemStack getStackInSlot(int slot){return slots.getStackInSlot(slot);}
        @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return slot==0?slots.insertItem(0,stack,simulate):stack;}
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate){return slot==1?slots.extractItem(1,amount,simulate):ItemStack.EMPTY;}
        @Override public int getSlotLimit(int slot){return 64;}
        @Override public boolean isItemValid(int slot,ItemStack stack){return slots.isItemValid(slot,stack);}
    };
    private final LazyOptional<IItemHandler> automationHandle=LazyOptional.of(()->automation);
    public int fuel,heat;
    public boolean overheated,cooled;
    public NuclearGeneratorEntity(BlockPos pos,BlockState state){super(PowerBlocks.NUCLEAR_GENERATOR_ENTITY.get(),pos,state,PowerBlocks.NUCLEAR_CAPACITY,PowerBlocks.NUCLEAR_OUTPUT);}
    public static boolean waterAround(Level level,BlockPos pos){for(var side:Direction.values())if(level.getFluidState(pos.relative(side)).is(FluidTags.WATER))return true;return false;}
    public boolean running(){return fuel>0&&!overheated;}
    public void tick(){
        if(level==null)return;
        cooled=waterAround(level,worldPosition);
        if(!overheated&&fuel<=0&&energy.getEnergyStored()+PowerBlocks.NUCLEAR_RATE<=energy.getMaxEnergyStored()){
            var input=slots.getStackInSlot(0);var output=slots.getStackInSlot(1);var spent=new ItemStack(TGItems.NUCLEAR_POWERCELL_EMPTY.get());
            if(!input.isEmpty()&&(output.isEmpty()||ItemStack.isSameItemSameTags(output,spent)&&output.getCount()<output.getMaxStackSize())){
                input.shrink(1);slots.setStackInSlot(0,input);fuel=PowerBlocks.NUCLEAR_CELL_ENERGY;
                if(output.isEmpty())slots.setStackInSlot(1,spent);else{output.grow(1);slots.setStackInSlot(1,output);}
            }
        }
        boolean working=running()&&energy.getEnergyStored()+PowerBlocks.NUCLEAR_RATE<=energy.getMaxEnergyStored();
        if(working){int made=Math.min(PowerBlocks.NUCLEAR_RATE,fuel);fuel-=made;produce(made);heat+=1;}
        if(cooled)heat-=2;else if(!working)heat-=1;
        heat=Math.max(0,heat);
        if(heat>=PowerBlocks.NUCLEAR_HEAT_MAX){
            if(TGConfig.nuclearGeneratorExplodes){level.removeBlock(worldPosition,false);level.explode(null,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,6,Level.ExplosionInteraction.BLOCK);return;}
            overheated=true;
        }
        if(overheated&&heat<=PowerBlocks.NUCLEAR_HEAT_MAX/2)overheated=false;
        if(working&&level.getGameTime()%20==0&&TGRadiationSystem.isEnabled())TGRadiationSystem.applyRadToEntities(this,4,100,2,1.5,1);
        setChanged();
        distribute();
        FacingMachineBlock.setLit(level,worldPosition,getBlockState(),working);
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){if(capability==ForgeCapabilities.ITEM_HANDLER)return automationHandle.cast();return super.getCapability(capability,side);}
    @Override public void invalidateCaps(){super.invalidateCaps();automationHandle.invalidate();}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.put("Slots",slots.serializeNBT());tag.putInt("Fuel",fuel);tag.putInt("Heat",heat);tag.putBoolean("Overheated",overheated);}
    @Override public void load(CompoundTag tag){super.load(tag);slots.deserializeNBT(tag.getCompound("Slots"));fuel=tag.getInt("Fuel");heat=tag.getInt("Heat");overheated=tag.getBoolean("Overheated");}
}
