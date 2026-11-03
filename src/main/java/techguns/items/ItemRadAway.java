package techguns.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import techguns.TGItems;
import techguns.TGRadiationSystem;

public final class ItemRadAway extends GenericItemConsumable {
    public ItemRadAway(Properties properties) { super(32, properties); }
    @Override protected void onConsumed(ItemStack stack, Level level, Player player) {
        player.addEffect(new MobEffectInstance(TGRadiationSystem.REGENERATION.get(), 400, 14, false, false));
        ItemStack bag = TGItems.INFUSION_BAG.get().getDefaultInstance();
        if (!player.getInventory().add(bag)) player.drop(bag, false);
    }
    @Override protected SoundEvent getConsumedSound() { return SoundEvents.PLAYER_BREATH; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flags) {
        super.appendHoverText(stack, level, tooltip, flags);
        tooltip.add(Component.translatable("techguns.radregeneration").append(" ")
                .append(Component.translatable("potion.potency.9")).append(" (-300)").withStyle(ChatFormatting.BLUE));
    }
}
