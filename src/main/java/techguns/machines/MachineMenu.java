package techguns.machines;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import techguns.TGMenuTypes;
public final class MachineMenu extends AbstractContainerMenu {
    public final ProcessingMachine machine;
    public MachineMenu(int id,Inventory inventory,FriendlyByteBuf buffer){this(id,inventory,require(inventory,buffer));}
    private static ProcessingMachine require(Inventory inventory,FriendlyByteBuf buffer){var tile=inventory.player.level().getBlockEntity(buffer.readBlockPos());if(!(tile instanceof ProcessingMachine machine))throw new IllegalArgumentException("Machine menu requires the actual block entity");return machine;}
    public MachineMenu(int id,Inventory inventory,ProcessingMachine machine){
        super(TGMenuTypes.MACHINE.get(),id);this.machine=machine;
        for(int i=0;i<machine.kind.slots.length;i++){int slot=i;var xy=machine.kind.slots[i];addSlot(new SlotItemHandler(machine.inventory,i,xy[0],xy[1]){@Override public boolean mayPlace(ItemStack stack){return machine.accepts(slot,stack);}});}
        int[] origin=machine.kind.inventoryOrigin();int x=origin[0],y=origin[1];
        for(int row=0;row<3;row++)for(int column=0;column<9;column++)addSlot(new Slot(inventory,column+row*9+9,x+column*18,y+row*18));
        for(int column=0;column<9;column++)addSlot(new Slot(inventory,column,x+column*18,y+58));addDataSlots(machine.data);
    }
    @Override public boolean stillValid(Player player){return machine.getLevel()!=null&&machine.getLevel().getBlockEntity(machine.getBlockPos())==machine&&player.distanceToSqr(machine.getBlockPos().getCenter())<=64&&machine.access.allows(player);}
    @Override public boolean clickMenuButton(Player player,int id){if(!stillValid(player))return false;if(id==1)return player.level().isClientSide||machine.access.toggle(player);if(id<2||id>switch(machine.kind){case REACTION->120;case FABRICATOR->9+MachineRecipes.fabricator().size();case CHEM_LAB->5;default->4;})return false;if(!player.level().isClientSide)machine.button(id);return true;}
    @Override public ItemStack quickMoveStack(Player player,int index){if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var original=stack.copy();int count=machine.kind.slots.length;
        if(index<count){if(!moveItemStackTo(stack,count,slots.size(),true))return ItemStack.EMPTY;}
        else {boolean moved=false;for(int target=0;target<count&&!stack.isEmpty();target++)if(machine.accepts(target,stack))moved=moveItemStackTo(stack,target,target+1,false)||moved;if(!moved)return ItemStack.EMPTY;}
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return original;
    }
}
