package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

/** Burns one furnace fuel at a time (furnace burn time) for GENERATOR_RATE FE per tick; waits while its buffer is full. */
public final class FuelGeneratorEntity extends EnergySource {
    public final ItemStackHandler fuel=new ItemStackHandler(1){
        @Override public boolean isItemValid(int slot,ItemStack stack){return ForgeHooks.getBurnTime(stack,RecipeType.SMELTING)>0;}
        @Override protected void onContentsChanged(int slot){setChanged();}
    };
    private final LazyOptional<IItemHandler> fuelHandle=LazyOptional.of(()->fuel);
    public int burnTime,burnTotal;
    public FuelGeneratorEntity(BlockPos pos,BlockState state){super(PowerBlocks.FUEL_GENERATOR_ENTITY.get(),pos,state,PowerBlocks.GENERATOR_CAPACITY,PowerBlocks.GENERATOR_OUTPUT);}
    public void tick(){
        boolean wasBurning=burnTime>0;
        if(burnTime<=0&&energy.getEnergyStored()+PowerBlocks.GENERATOR_RATE<=energy.getMaxEnergyStored()){
            var stack=fuel.getStackInSlot(0);int time=stack.isEmpty()?0:ForgeHooks.getBurnTime(stack,RecipeType.SMELTING);
            if(time>0){burnTime=burnTotal=time;var remainder=stack.getCraftingRemainingItem();stack.shrink(1);if(stack.isEmpty()&&!remainder.isEmpty())fuel.setStackInSlot(0,remainder);else fuel.setStackInSlot(0,stack);setChanged();}
        }
        if(burnTime>0){burnTime--;produce(PowerBlocks.GENERATOR_RATE);setChanged();}
        distribute();
        boolean burning=burnTime>0;
        if(burning!=wasBurning&&level!=null)level.setBlock(worldPosition,getBlockState().setValue(GeneratorBlock.LIT,burning),3);
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){if(capability==ForgeCapabilities.ITEM_HANDLER)return fuelHandle.cast();return super.getCapability(capability,side);}
    @Override public void invalidateCaps(){super.invalidateCaps();fuelHandle.invalidate();}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.put("Fuel",fuel.serializeNBT());tag.putInt("BurnTime",burnTime);tag.putInt("BurnTotal",burnTotal);}
    @Override public void load(CompoundTag tag){super.load(tag);fuel.deserializeNBT(tag.getCompound("Fuel"));burnTime=tag.getInt("BurnTime");burnTotal=tag.getInt("BurnTotal");}
}
