package techguns.gui.widgets;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import techguns.Techguns;

public final class SlotFood extends Slot {
    public SlotFood(Container container, int slot, int x, int y) { super(container, slot, x, y); }
    @Override public boolean mayPlace(ItemStack stack) { return stack.has(net.minecraft.core.component.DataComponents.FOOD); }
    @Override public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, ResourceLocation.fromNamespaceAndPath(Techguns.MODID, "gui/emptyslots/emptyslot_food"));
    }
}
