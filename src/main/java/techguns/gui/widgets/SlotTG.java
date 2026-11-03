package techguns.gui.widgets;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import techguns.Techguns;
import techguns.api.tginventory.ITGSpecialSlot;
import techguns.api.tginventory.TGSlotType;

public final class SlotTG extends Slot {
    private final TGSlotType type;
    public SlotTG(Container container, int slot, int x, int y, TGSlotType type) { super(container, slot, x, y); this.type = type; }
    public TGSlotType getType() { return type; }
    @Override public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ITGSpecialSlot special ? special.getSlot(stack) == type : type == TGSlotType.NORMAL;
    }
    @Override public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        String icon = switch (type) {
            case FACESLOT -> "face"; case BACKSLOT -> "back"; case HANDSLOT -> "hand";
            case AMMOSLOT -> "bullet"; case FOODSLOT -> "food"; case HEALSLOT -> "heal";
            default -> null;
        };
        return icon == null ? null : Pair.of(InventoryMenu.BLOCK_ATLAS,
                new ResourceLocation(Techguns.MODID, "gui/emptyslots/emptyslot_" + icon));
    }
}
