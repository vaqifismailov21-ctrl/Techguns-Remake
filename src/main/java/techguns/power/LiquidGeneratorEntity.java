package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.TGConfig;

/**
 * Burns liquid fuel from its tank: fuels of FluidListFuel, oils of FluidListOil (including Techguns oil) and lava.
 * Filled by pipes on any side or with buckets/containers in its slot (the empty container stays in the slot).
 */
public final class LiquidGeneratorEntity extends EnergySource {
    /** FE per mB: refined fuels, crude oil, lava. */
    public static int energyPerMb(Fluid fluid){
        if(fluid==null||fluid==Fluids.EMPTY)return 0;var key=ForgeRegistries.FLUIDS.getKey(fluid);if(key==null)return 0;String name=key.getPath();
        if(fluid.isSame(Fluids.LAVA))return 100;
        for(String fuel:TGConfig.fluidListFuel)if(fuel.equals(name))return 500;
        for(String oil:TGConfig.fluidListOil)if(oil.equals(name))return 200;
        return 0;
    }
    public final FluidTank tank=new FluidTank(PowerBlocks.LIQUID_TANK,stack->energyPerMb(stack.getFluid())>0){@Override protected void onContentsChanged(){setChanged();}};
    public final ItemStackHandler container=new ItemStackHandler(1){
        @Override public boolean isItemValid(int slot,ItemStack stack){return FluidUtil.getFluidContained(stack).map(f->energyPerMb(f.getFluid())>0).orElse(false);}
        @Override public int getSlotLimit(int slot){return 1;}
        @Override protected void onContentsChanged(int slot){setChanged();}
    };
    private final LazyOptional<IFluidHandler> tankHandle=LazyOptional.of(()->tank);
    private final LazyOptional<IItemHandler> containerHandle=LazyOptional.of(()->container);
    /** Energy left from the last fluid drawn. */
    public int burnEnergy;
    private int shownLevel=-1;
    /** Fill of the tank, 0..1, for the oil level in the model. */
    public float fill(){return tank.getFluidAmount()/(float)PowerBlocks.LIQUID_TANK;}
    public LiquidGeneratorEntity(BlockPos pos,BlockState state){super(PowerBlocks.LIQUID_GENERATOR_ENTITY.get(),pos,state,PowerBlocks.LIQUID_CAPACITY,PowerBlocks.LIQUID_OUTPUT);}
    public void tick(){
        boolean wasBurning=burnEnergy>0;
        var stack=container.getStackInSlot(0);
        if(!stack.isEmpty()){var result=FluidUtil.tryEmptyContainer(stack,tank,tank.getSpace(),null,true);if(result.isSuccess())container.setStackInSlot(0,result.getResult());}
        if(burnEnergy<PowerBlocks.LIQUID_RATE&&energy.getEnergyStored()+PowerBlocks.LIQUID_RATE<=energy.getMaxEnergyStored()&&!tank.isEmpty()){
            int perMb=energyPerMb(tank.getFluid().getFluid());var drawn=tank.drain(PowerBlocks.LIQUID_DRAW,IFluidHandler.FluidAction.EXECUTE);burnEnergy+=drawn.getAmount()*perMb;
        }
        if(burnEnergy>0){int made=Math.min(PowerBlocks.LIQUID_RATE,burnEnergy);burnEnergy-=made;produce(made);}
        distribute();
        int shown=tank.getFluidAmount()*9/PowerBlocks.LIQUID_TANK;if(shown!=shownLevel){shownLevel=shown;sync();}
        boolean burning=burnEnergy>0;
        if(burning!=wasBurning&&level!=null)level.setBlock(worldPosition,getBlockState().setValue(GeneratorBlock.LIT,burning),3);
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){
        if(capability==ForgeCapabilities.FLUID_HANDLER)return tankHandle.cast();if(capability==ForgeCapabilities.ITEM_HANDLER)return containerHandle.cast();
        return super.getCapability(capability,side);
    }
    @Override public void invalidateCaps(){super.invalidateCaps();tankHandle.invalidate();containerHandle.invalidate();}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.put("Tank",tank.writeToNBT(new CompoundTag()));tag.put("Container",container.serializeNBT());tag.putInt("BurnEnergy",burnEnergy);}
    @Override public void load(CompoundTag tag){super.load(tag);tank.readFromNBT(tag.getCompound("Tank"));container.deserializeNBT(tag.getCompound("Container"));burnEnergy=tag.getInt("BurnEnergy");}
    public static FluidStack displayed(LiquidGeneratorEntity entity){return entity.tank.getFluid();}
}
