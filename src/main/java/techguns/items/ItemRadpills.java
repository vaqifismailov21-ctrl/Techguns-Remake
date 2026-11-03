package techguns.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import techguns.TGRadiationSystem;

public final class ItemRadpills extends GenericItemConsumable {
    public ItemRadpills(Properties properties) { super(32, properties); }
    @Override protected void onConsumed(ItemStack stack, Level level, Player player) {
        player.addEffect(new MobEffectInstance(TGRadiationSystem.REGENERATION.get(), 500, 1, false, false));
        player.addEffect(new MobEffectInstance(TGRadiationSystem.RESISTANCE.get(), 3600, 2, false, false));
        ItemStack bottle = Items.GLASS_BOTTLE.getDefaultInstance();
        if (!player.getInventory().add(bottle)) player.drop(bottle, false);
    }
    @Override protected SoundEvent getConsumedSound() { return SoundEvents.PLAYER_BREATH; }
    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flags) {
        super.appendHoverText(stack, level, tooltip, flags);
        tooltip.add(Component.translatable("techguns.radresistance").append(" ").append(Component.translatable("potion.potency.2")).append(" (180s)").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("techguns.radregeneration").append(" ").append(Component.translatable("potion.potency.1")).append(" (-50)").withStyle(ChatFormatting.BLUE));
    }
}
