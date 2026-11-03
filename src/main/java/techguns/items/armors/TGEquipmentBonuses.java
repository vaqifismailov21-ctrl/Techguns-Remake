package techguns.items.armors;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.api.tginventory.ITGSpecialSlot;
import techguns.capabilities.TGExtendedPlayer;

public final class TGEquipmentBonuses {
    public static float getBonus(Player player, TGArmorBonus type, boolean consume) {
        float result = 0;
        for(var stack:player.getArmorSlots())if(stack.getItem() instanceof TGSuitArmor armor)result+=armor.bonus(type,stack,consume,player);
        var data=player.level().isClientSide ? TGExtendedPlayer.find(player).orElse(null) : TGExtendedPlayer.get(player);
        if(data==null) return 0;
        var inventory = data.tg_inventory;
        for (int slot = 0; slot < 3; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof ITGSpecialSlot item) result += item.getBonus(type, stack, consume, player);
        }
        return result;
    }
    private TGEquipmentBonuses() {}
}
