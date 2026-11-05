package techguns.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import techguns.capabilities.TGExtendedPlayer;
import techguns.items.MedkitItem;

/** Healing slot: a medkit or bandages there is used by itself once the player has lost at least what it heals. */
public final class TGAutoHealHandler {
    public static final int SLOT = 6;
    public static void tick(ServerPlayer player, TGExtendedPlayer data) {
        if (!player.isAlive() || player.isCreative() || player.isSpectator()) return;
        var stack = data.tg_inventory.getItem(SLOT);
        if (!(stack.getItem() instanceof MedkitItem medkit)) return;
        float missing = player.getMaxHealth() - player.getHealth();
        // wait for a previous kit's regeneration to run out, so one hit does not use up the whole stack
        if (missing < Math.min(medkit.heal, player.getMaxHealth() * 0.5F) || player.hasEffect(MobEffects.REGENERATION)) return;
        medkit.applyTo(player);
        stack.shrink(1);
        data.tg_inventory.setItem(SLOT, stack.isEmpty() ? net.minecraft.world.item.ItemStack.EMPTY : stack);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 1, 1.2F);
        data.markDirty();
    }
    private TGAutoHealHandler() {}
}
