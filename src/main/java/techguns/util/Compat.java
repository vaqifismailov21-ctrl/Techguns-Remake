package techguns.util;

import net.minecraft.world.item.ItemStack;

/** Small 1.20 helpers that 1.19.2 does not have yet, so the shared code keeps its 1.20.1 shape. */
public final class Compat {
    /** ItemStack.copyWithCount (1.19.4). */
    public static ItemStack copyWithCount(ItemStack stack, int count) { ItemStack copy = stack.copy(); if (!copy.isEmpty()) copy.setCount(count); return copy; }
    /** ItemStack.isSameItem (1.19.4): same item, damage and tags ignored. */
    public static boolean isSameItem(ItemStack a, ItemStack b) { return a.getItem() == b.getItem(); }
    private Compat() {}
}
