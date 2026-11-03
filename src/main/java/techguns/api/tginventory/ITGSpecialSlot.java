package techguns.api.tginventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.items.armors.TGArmorBonus;

public interface ITGSpecialSlot {
    TGSlotType getSlot(ItemStack stack);

    default void onPlayerTick(ItemStack stack, Player player) {}

    default float getBonus(TGArmorBonus type, ItemStack stack, boolean consume, Player player) {
        return 0.0F;
    }
}
