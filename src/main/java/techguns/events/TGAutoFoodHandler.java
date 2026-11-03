package techguns.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import techguns.TGConfig;
import techguns.capabilities.TGExtendedPlayer;
import techguns.util.InventoryUtil;

/** Original buffered nutrition: unused food points survive until hunger is lost again. */
public final class TGAutoFoodHandler {
    public static void tick(ServerPlayer player, TGExtendedPlayer data) {
        if (TGConfig.disableAutofeeder || player.getFoodData().getFoodLevel() >= 20) return;
        int needed = 20 - player.getFoodData().getFoodLevel();
        if (data.foodleft > 0) {
            int used = Math.min(data.foodleft, needed);
            player.getFoodData().eat(used, data.lastSaturation);
            data.foodleft -= used;
            if (data.foodleft == 0) data.lastSaturation = 0;
            data.markDirty();
            return;
        }
        ItemStack consumed = InventoryUtil.consumeFood(data.tg_inventory);
        if (consumed.isEmpty()) return;
        var food = consumed.getFoodProperties(player);
        if (food == null) return;
        // This is the modern equivalent of the original protected ItemFood.onFoodEaten call:
        // effects are applied once, without also granting all nutrition via finishUsingItem.
        for (var effect : food.getEffects()) if (player.getRandom().nextFloat() < effect.getSecond()) {
            player.addEffect(new MobEffectInstance(effect.getFirst()));
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1, 1);
        int used = Math.min(needed, food.getNutrition());
        player.getFoodData().eat(used, food.getSaturationModifier());
        data.foodleft = (short) Math.max(0, food.getNutrition() - needed);
        data.lastSaturation = data.foodleft > 0 ? food.getSaturationModifier() : 0;
        data.markDirty();
    }
    private TGAutoFoodHandler() {}
}
