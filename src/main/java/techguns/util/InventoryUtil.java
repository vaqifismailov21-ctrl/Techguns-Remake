package techguns.util;

import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.capabilities.TGExtendedPlayer;
import techguns.gui.player.TGPlayerInventory;

/** Original ammo-slot priority, with transfers limited to the remaining item count. */
public final class InventoryUtil {
    private InventoryUtil() {}

    public static int addItemToInventory(List<ItemStack> inventory, ItemStack incoming, int start, int end) {
        ItemStack remaining = incoming.copy();
        for (int i = start; i < end && !remaining.isEmpty(); i++) {
            ItemStack existing = inventory.get(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, remaining)) {
                int count = Math.min(remaining.getCount(), Math.max(0, existing.getMaxStackSize() - existing.getCount()));
                existing.grow(count); remaining.shrink(count);
            }
        }
        for (int i = start; i < end && !remaining.isEmpty(); i++) if (inventory.get(i).isEmpty()) {
            int count = Math.min(remaining.getCount(), remaining.getMaxStackSize());
            inventory.set(i, techguns.util.Compat.copyWithCount(remaining, count));
            remaining.shrink(count);
        }
        return remaining.getCount();
    }
    public static int addAmmoToAmmoInventory(Player player, ItemStack stack) {
        var inventory = TGExtendedPlayer.get(player).tg_inventory;
        int remaining = addItemToInventory(inventory.inventory, stack, TGPlayerInventory.SLOTS_AMMO_START, TGPlayerInventory.SLOTS_AMMO_END + 1);
        if (remaining != stack.getCount()) inventory.setChanged();
        return remaining;
    }
    public static int addAmmoToPlayerInventory(Player player, ItemStack stack) {
        int remaining = addAmmoToAmmoInventory(player, stack);
        if (remaining > 0) remaining = addItemToInventory(player.getInventory().items, techguns.util.Compat.copyWithCount(stack, remaining), 0, player.getInventory().items.size());
        player.getInventory().setChanged();
        return remaining;
    }
    public static int countItemInInv(List<ItemStack> inventory, ItemStack wanted, int start, int end) {
        int count = 0;
        for (int i = start; i < end; i++) if (ItemUtil.isItemEqual(inventory.get(i), wanted)) count += inventory.get(i).getCount();
        return count;
    }
    public static int canConsumeItem(List<ItemStack> inventory, ItemStack wanted, int start, int end) {
        return Math.max(0, wanted.getCount() - countItemInInv(inventory, wanted, start, end));
    }
    public static int consumeItem(List<ItemStack> inventory, ItemStack wanted, int start, int end) {
        int remaining = wanted.getCount();
        for (int i = start; i < end && remaining > 0; i++) if (ItemUtil.isItemEqual(inventory.get(i), wanted)) {
            ItemStack stack = inventory.get(i);
            int count = Math.min(remaining, stack.getCount());
            stack.shrink(count); remaining -= count;
            if (stack.isEmpty()) inventory.set(i, ItemStack.EMPTY);
        }
        return remaining;
    }
    public static boolean canConsumeAmmoPlayer(Player player, ItemStack wanted) {
        if (wanted.isEmpty()) return true;
        var extra = TGExtendedPlayer.get(player).tg_inventory;
        return countItemInInv(extra.inventory, wanted, 7, 15)
                + countItemInInv(player.getInventory().items, wanted, 0, player.getInventory().items.size()) >= wanted.getCount();
    }
    public static boolean consumeAmmoPlayer(Player player, ItemStack wanted) {
        if (!canConsumeAmmoPlayer(player, wanted)) return false;
        var extra = TGExtendedPlayer.get(player).tg_inventory;
        int remaining = consumeItem(extra.inventory, wanted, 7, 15);
        if (remaining > 0) consumeItem(player.getInventory().items, techguns.util.Compat.copyWithCount(wanted, remaining), 0, player.getInventory().items.size());
        extra.setChanged(); player.getInventory().setChanged();
        return true;
    }
    public static boolean consumeAmmoPlayer(Player player, ItemStack[] requirements) {
        // Aggregate equal entries before checking, so multi-component reloads are atomic.
        var combined = new java.util.ArrayList<ItemStack>();
        for (ItemStack requirement : requirements) {
            if (requirement.isEmpty()) continue;
            ItemStack existing = combined.stream().filter(s -> ItemUtil.isItemEqual(s, requirement)).findFirst().orElse(null);
            if (existing == null) combined.add(requirement.copy()); else existing.grow(requirement.getCount());
        }
        if (combined.stream().anyMatch(s -> !canConsumeAmmoPlayer(player, s))) return false;
        combined.forEach(s -> consumeAmmoPlayer(player, s));
        return true;
    }
    public static ItemStack consumeFood(TGPlayerInventory inventory) {
        for (int i = 3; i <= 5; i++) if (inventory.getItem(i).isEdible()) return inventory.removeItem(i, 1);
        return ItemStack.EMPTY;
    }
}
