package techguns.power;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import techguns.capabilities.compat.Capability;
import techguns.capabilities.compat.Capabilities;
import techguns.capabilities.compat.LazyOptional;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import techguns.TGConfig;

/**
 * Burns liquid fuel from its tank: fuels of FluidListFuel, oils of FluidListOil (including Techguns oil) and lava.
 * Filled by pipes on any side or with buckets/containers in its slot (the empty container stays in the slot).
 */
public final class LiquidGeneratorEntity extends EnergySource {
    /** FE per mB: refined fuels, crude oil, lava. */
    public static int energyPerMb(Fluid fluid){
        if(fluid==null||fluid==Fluids.EMPTY)return 0;var key=BuiltInRegistries.FLUID.getKey(fluid);if(key==null)return 0;String name=key.getPath();
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
    public LiquidGeneratorEntity(BlockPos pos,BlockState state){super(PowerBlocks.LIQUID_GENERATOR_ENTITY.get(),pos,state,PowerBlocks.LIQUID_CAPACITY,PowerBlocks.LIQUID_OUTPUT);}
    public void tick(){
        boolean wasBurning=burnEnergy>0;
        var stack=container.getStackInSlot(0);
        if(!stack.isEmpty()){var result=FluidUtil.tryEmptyContainer(stack,tank,tank.getSpace(),null,true);if(result.isSuccess())container.setStackInSlot(0,result.getResult());}
        if(burnEnergy<=0&&energy.getEnergyStored()+PowerBlocks.LIQUID_RATE<=energy.getMaxEnergyStored()&&!tank.isEmpty()){
            int perMb=energyPerMb(tank.getFluid().getFluid());var drawn=tank.drain(PowerBlocks.LIQUID_DRAW,IFluidHandler.FluidAction.EXECUTE);burnEnergy+=drawn.getAmount()*perMb;
        }
        if(burnEnergy>0){int made=Math.min(PowerBlocks.LIQUID_RATE,burnEnergy);burnEnergy-=made;produce(made);}
        distribute();
        boolean burning=burnEnergy>0;
        if(burning!=wasBurning&&level!=null)level.setBlock(worldPosition,getBlockState().setValue(GeneratorBlock.LIT,burning),3);
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){
        if(capability==Capabilities.FLUID_HANDLER)return tankHandle.cast();if(capability==Capabilities.ITEM_HANDLER)return containerHandle.cast();
        return super.getCapability(capability,side);
    }
    @Override public void invalidateCaps(){invalidateCapabilities();tankHandle.invalidate();containerHandle.invalidate();}
    @Override protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(tag, registries);tag.put("Tank",tank.writeToNBT(techguns.util.TGItemNbt.registries(), new CompoundTag()));tag.put("Container",container.serializeNBT(techguns.util.TGItemNbt.registries()));tag.putInt("BurnEnergy",burnEnergy);}
    @Override protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(tag, registries);tank.readFromNBT(techguns.util.TGItemNbt.registries(), tag.getCompound("Tank"));container.deserializeNBT(techguns.util.TGItemNbt.registries(), tag.getCompound("Container"));burnEnergy=tag.getInt("BurnEnergy");}
    public static FluidStack displayed(LiquidGeneratorEntity entity){return entity.tank.getFluid();}
}
