package techguns.gui.player;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Original 15 slots, in their original NBT and menu order. */
public final class TGPlayerInventory implements Container {
    public static final int NUMSLOTS = 15;
    public static final int SLOT_FACE = 0, SLOT_BACK = 1, SLOT_HAND = 2;
    public static final int SLOTS_AUTOFOOD_START = 3, SLOTS_AUTOFOOD_END = 5;
    public static final int SLOT_AUTOHEAL = 6;
    public static final int SLOTS_AMMO_START = 7, SLOTS_AMMO_END = 14;
    public final NonNullList<ItemStack> inventory = NonNullList.withSize(NUMSLOTS, ItemStack.EMPTY);
    private final Player owner;
    public boolean dirty;

    public TGPlayerInventory(Player owner) { this.owner = owner; }
    public void saveNBTData(CompoundTag tag) { ContainerHelper.saveAllItems(tag, inventory); }
    public void loadNBTData(CompoundTag tag) {
        inventory.clear();
        ContainerHelper.loadAllItems(tag, inventory);
        setChanged();
    }
    @Override public int getContainerSize() { return NUMSLOTS; }
    @Override public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return slot >= 0 && slot < NUMSLOTS ? inventory.get(slot) : ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(inventory, slot, count);
        if (!result.isEmpty()) setChanged();
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(inventory, slot);
        if (!result.isEmpty()) setChanged();
        return result;
    }
    @Override public void setItem(int slot, ItemStack stack) { inventory.set(slot, stack); setChanged(); }
    @Override public void setChanged() { dirty = true; }
    @Override public boolean stillValid(Player player) { return owner.isAlive() && player == owner; }
    @Override public void clearContent() { inventory.clear(); setChanged(); }
}
