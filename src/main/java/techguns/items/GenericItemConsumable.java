package techguns.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public abstract class GenericItemConsumable extends Item {
    public final int itemUseDuration;
    protected boolean ignoreHunger = true;
    protected GenericItemConsumable(int duration, Properties properties) { super(properties); itemUseDuration = duration; }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), getConsumedSound(), SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
            onConsumed(stack, level, player);
            player.awardStat(Stats.ITEM_USED.get(this));
            if (player instanceof ServerPlayer serverPlayer) CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            stack.shrink(1);
        }
        return stack;
    }
    @Override public int getUseDuration(ItemStack stack, LivingEntity tgUser) { return itemUseDuration; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.EAT; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.canEat(ignoreHunger)) { player.startUsingItem(hand); return InteractionResultHolder.consume(player.getItemInHand(hand)); }
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }
    protected SoundEvent getConsumedSound() { return SoundEvents.PLAYER_BURP; }
    protected abstract void onConsumed(ItemStack stack, Level level, Player player);
}
