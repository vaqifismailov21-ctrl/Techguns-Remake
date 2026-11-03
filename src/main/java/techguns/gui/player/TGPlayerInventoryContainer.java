package techguns.gui.player;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import techguns.TGMenuTypes;
import techguns.api.tginventory.TGSlotType;
import techguns.capabilities.TGExtendedPlayer;
import techguns.gui.widgets.SlotFood;
import techguns.gui.widgets.SlotTG;

/** Original slot coordinates and transfer order. The server owns the inventory. */
public final class TGPlayerInventoryContainer extends AbstractContainerMenu {
    private static final int MAIN_START = 19, HOTBAR_START = 46, MAIN_END = 55;
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final Player owner;

    public TGPlayerInventoryContainer(int id, Inventory inventory) {
        super(TGMenuTypes.PLAYER_INVENTORY.get(), id);
        owner = inventory.player;
        var extra = TGExtendedPlayer.get(owner).tg_inventory;
        addSlot(new SlotTG(extra, 0, 77, 8, TGSlotType.FACESLOT));
        addSlot(new SlotTG(extra, 1, 77, 26, TGSlotType.BACKSLOT));
        addSlot(new SlotTG(extra, 2, 77, 44, TGSlotType.HANDSLOT));
        for (int i = 0; i < 3; i++) addSlot(new SlotFood(extra, 3 + i, 116 + i * 18, 24));
        addSlot(new SlotTG(extra, 6, 97, 24, TGSlotType.HEALSLOT));
        for (int row = 0; row < 2; row++) for (int col = 0; col < 4; col++) {
            addSlot(new SlotTG(extra, 7 + row * 4 + col, 98 + col * 18, 44 + row * 18, TGSlotType.AMMOSLOT));
        }
        for (int i = 0; i < ARMOR.length; i++) {
            EquipmentSlot equipment = ARMOR[i];
            addSlot(new Slot(inventory, 39 - i, 8, 8 + i * 18) {
                @Override public int getMaxStackSize() { return 1; }
                @Override public boolean mayPlace(ItemStack stack) { return stack.canEquip(equipment, owner); }
                @Override public boolean mayPickup(Player player) {
                    return (getItem().isEmpty() || player.isCreative() || !net.minecraft.world.item.enchantment.EnchantmentHelper.has(getItem(), net.minecraft.world.item.enchantment.EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) && super.mayPickup(player);
                }
                @Override public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    ResourceLocation icon = switch (equipment) {
                        case HEAD -> InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
                        case CHEST -> InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
                        case LEGS -> InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
                        default -> InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
                    };
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, icon);
                }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, 9 + row * 9 + col, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        addSlot(new Slot(inventory, 40, 77, 62) {
            @Override public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() { return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD); }
        });
    }

    @Override public boolean stillValid(Player player) { return player == owner && owner.isAlive(); }

    @Override public ItemStack quickMoveStack(Player player, int id) {
        if (id < 0 || id >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(id);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack moving = slot.getItem(), original = moving.copy();
        boolean moved;
        if (id < TGPlayerInventory.NUMSLOTS) moved = moveItemStackTo(moving, MAIN_START, MAIN_END, false);
        else if (id >= MAIN_START && moving.getItem() instanceof ArmorItem armor
                && !slots.get(15 + (3 - armor.getEquipmentSlot().getIndex())).hasItem()) {
            int target = 15 + (3 - armor.getEquipmentSlot().getIndex());
            moved = moveItemStackTo(moving, target, target + 1, false);
        } else {
            int first = -1, size = 1;
            if (id >= MAIN_START && id < MAIN_END) for (int i = 0; i < 15; i++) {
                if (slots.get(i).mayPlace(moving)) {
                    first = i;
                    if (slots.get(i) instanceof SlotFood) size = 3;
                    if (slots.get(i) instanceof SlotTG tg && tg.getType() == TGSlotType.AMMOSLOT) size = 8;
                    break;
                }
            }
            if (first >= 0) moved = moveItemStackTo(moving, first, first + size, false);
            else if (id >= MAIN_START && id < HOTBAR_START) moved = moveItemStackTo(moving, HOTBAR_START, MAIN_END, false);
            else if (id >= HOTBAR_START && id < MAIN_END) moved = moveItemStackTo(moving, MAIN_START, HOTBAR_START, false);
            else moved = moveItemStackTo(moving, MAIN_START, MAIN_END, false);
        }
        if (!moved || moving.getCount() == original.getCount()) return ItemStack.EMPTY;
        if (moving.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, moving);
        return original;
    }
}
