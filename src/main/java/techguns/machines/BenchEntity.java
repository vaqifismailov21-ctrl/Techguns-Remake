package techguns.machines;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import techguns.capabilities.compat.*;
import techguns.capabilities.compat.LazyOptional;
import net.neoforged.neoforge.items.*;
import techguns.*;
import techguns.items.armors.*;
public final class BenchEntity extends BlockEntity implements MenuProvider, techguns.capabilities.compat.ICapabilityHost {
    public final BenchKind kind;
    public final ItemStackHandler inventory;
    public final MachineSecurity access=new MachineSecurity(this::changed);
    private LazyOptional<IItemHandler> handler;
    public BenchEntity(BlockPos pos,BlockState state){super(TGBenches.ENTITY.get(),pos,state);kind=((BenchBlock)state.getBlock()).kind;inventory=new ItemStackHandler(kind.slots){@Override protected void onContentsChanged(int slot){changed();}@Override public boolean isItemValid(int slot,ItemStack stack){return accepts(slot,stack);}};handler=LazyOptional.of(()->inventory);}
    public boolean accepts(int slot,ItemStack stack){return switch(kind){case CAMO->true;case REPAIR->slot<9||stack.getItem() instanceof TGSuitArmor||stack.getItem() instanceof GenericShield;case UPGRADE->slot==0?stack.getItem() instanceof TGSuitArmor:ArmorUpgrades.recipe(stack)!=null;};}
    public List<ItemStack> repairMaterials(ItemStack stack){return stack.getItem() instanceof TGSuitArmor armor?armor.repairMaterials(stack):stack.getItem() instanceof GenericShield shield?shield.getRepairMats(stack):List.of();}
    public boolean repair(ItemStack target){
        var materials=repairMaterials(target);if(target.isEmpty()||target.getDamageValue()==0||materials.isEmpty())return false;
        // Reserve from a working copy so overlapping material requirements can never spend the same item twice.
        var remaining=new ArrayList<ItemStack>();for(int i=0;i<9;i++)remaining.add(inventory.getStackInSlot(i).copy());
        for(var material:materials){int needed=material.getCount();for(var stack:remaining)if(ItemStack.isSameItemSameComponents(stack,material)){int count=Math.min(needed,stack.getCount());stack.shrink(count);needed-=count;}if(needed>0)return false;}
        for(int i=0;i<9;i++)inventory.setStackInSlot(i,remaining.get(i));target.setDamageValue(0);changed();return true;
    }
    public void changed(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(tag, registries);tag.put("inventory",inventory.serializeNBT(techguns.util.TGItemNbt.registries()));access.save(tag);}
    @Override protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(tag, registries);inventory.deserializeNBT(techguns.util.TGItemNbt.registries(), tag.getCompound("inventory"));access.load(tag);}
    @Override public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries){return saveWithoutMetadata(techguns.util.TGItemNbt.registries());}@Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){return capability==Capabilities.ITEM_HANDLER?handler.cast():LazyOptional.empty();}
    @Override public void invalidateCaps(){invalidateCapabilities();handler.invalidate();}@Override public void reviveCaps(){handler=LazyOptional.of(()->inventory);}
    @Override public Component getDisplayName(){return Component.translatable(kind.titleKey());}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new BenchMenu(id,inventory,this);}
}
