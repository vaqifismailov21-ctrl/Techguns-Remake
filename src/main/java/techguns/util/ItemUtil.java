package techguns.util;

import net.minecraft.world.item.ItemStack;

public final class ItemUtil {
    private ItemUtil() {}

    /** Original OreDictionary.itemMatches(..., true) ignored NBT and compared item+metadata.
     * Metadata variants are separate registry items in this port. */
    public static boolean isItemEqual(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) return first.isEmpty() && second.isEmpty();
        return first.getItem() == second.getItem();
    }
}
