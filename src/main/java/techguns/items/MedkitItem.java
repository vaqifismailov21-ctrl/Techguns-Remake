package techguns.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Bandages and medkit from Techguns 1.7.10 (ItemMedkit): instant heal plus regeneration. */
public final class MedkitItem extends GenericItemConsumable {
    public final float heal;
    public final int regenTicks, regenLevel;

    public MedkitItem(int useTicks, float heal, int regenTicks, int regenLevel, Properties properties) {
        super(useTicks, properties.stacksTo(16));
        this.heal = heal; this.regenTicks = regenTicks; this.regenLevel = regenLevel;
    }

    /** Usable only when hurt, as in 1.7.10. */
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.getHealth() < player.getMaxHealth()) { player.startUsingItem(hand); return InteractionResultHolder.consume(player.getItemInHand(hand)); }
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    @Override public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (remaining % 7 == 0) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.2F);
            if (!level.isClientSide) entity.swing(entity.getUsedItemHand(), true);
        }
    }

    @Override protected void onConsumed(ItemStack stack, Level level, Player player) {
        player.heal(heal);
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenTicks, regenLevel, true, true));
    }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }

    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(Component.translatable("techguns.tooltip.medkit.heal", (int) (heal / 2F)).withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("effect.minecraft.regeneration").append(regenLevel > 0 ? " II" : "")
                .append(" (" + regenTicks / 20 + "s)").withStyle(ChatFormatting.BLUE));
    }
}
