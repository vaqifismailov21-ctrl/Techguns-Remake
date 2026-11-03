package techguns.tileentities;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * The original InventoryUtil helpers for ItemStackHandlerPlus that the turret uses. They bypass slot validity
 * (insertItemNoCheck/extractWithoutCheck) and match on item identity only, ignoring NBT, as the 1.12.2 code did
 * (item + metadata; flattened metadata variants are separate items now).
 */
public final class TurretInventory {
    private TurretInventory() {}

    private static boolean matches(ItemStack slot,ItemStack wanted){return !slot.isEmpty()&&slot.getItem()==wanted.getItem();}

    /** Returns the number of items still missing; 0 means the whole amount can be consumed. */
    public static int canConsumeItem(ItemStackHandler inv,ItemStack item,int start,int end){
        int needed=item.getCount();
        for(int i=start;i<end;i++)if(matches(inv.getStackInSlot(i),item)){needed-=inv.getStackInSlot(i).getCount();if(needed<=0)return 0;}
        return needed;
    }

    /** Removes one item from the first matching slot. */
    public static boolean consumeAmmo(ItemStackHandler inv,ItemStack ammo,int start,int end){
        for(int i=start;i<end;i++)if(matches(inv.getStackInSlot(i),ammo)){
            var stack=inv.getStackInSlot(i).copy();stack.shrink(1);inv.setStackInSlot(i,stack.isEmpty()?ItemStack.EMPTY:stack);return true;
        }
        return false;
    }

    /** First merges into equal stacks (OreDictionary.itemMatches strict: item and metadata), then fills one empty slot. Returns the rest. */
    public static int addItemToInventory(ItemStackHandler inv,ItemStack incoming,int start,int end){
        ItemStack item=incoming.copy();
        for(int i=start;i<end;i++){
            ItemStack slot=inv.getStackInSlot(i);
            if(!slot.isEmpty()&&slot.getItem()==item.getItem()&&slot.getCount()<item.getMaxStackSize()){
                int total=slot.getCount()+item.getCount();
                if(total<=item.getMaxStackSize()){inv.setStackInSlot(i,slot.copyWithCount(total));return 0;}
                inv.setStackInSlot(i,slot.copyWithCount(item.getMaxStackSize()));item.setCount(total-item.getMaxStackSize());
            }
        }
        if(item.getCount()>0)for(int i=start;i<end;i++)if(inv.getStackInSlot(i).isEmpty()){inv.setStackInSlot(i,item.copy());return 0;}
        return item.getCount();
    }
}
