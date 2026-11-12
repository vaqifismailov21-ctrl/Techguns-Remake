package techguns.radiation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import techguns.TGSounds;
import techguns.api.radiation.TGRadiation;
import techguns.capabilities.TGExtendedPlayer;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;

public final class RadiationPotion extends MobEffect {
    public RadiationPotion() { super(MobEffectCategory.HARMFUL, 0xcff8ff00); }
    @Override public String getDescriptionId() { return "techguns.radiation"; }
    @Override public List<ItemStack> getCurativeItems() { return new ArrayList<>(); }
    /** Strong radiation (level 5 and up) works twice a second, each time with about half the amount. */
    @Override public boolean isDurationEffectTick(int duration, int amplifier) { return duration % (amplifier >= 4 ? 10 : 20) == 0; }
    @Override public void applyEffectTick(LivingEntity entity, int amplifier) {
        int resistance = entity.getAttribute(TGRadiation.RADIATION_RESISTANCE.get()) == null ? 0
                : (int) entity.getAttributeValue(TGRadiation.RADIATION_RESISTANCE.get());
        int amount = Math.max(0, Math.min(1000, amplifier + 1 - resistance));
        if (amplifier >= 4) amount = amount / 2 + 1;
        if (entity instanceof Player player) {
            if (!player.isCreative()) {
                TGExtendedPlayer.get(player).addRadiation(amount);
                if (amount > 0 && player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.playNotifySound(amplifier >= 2 ? TGSounds.GEIGER_HIGH.get() : TGSounds.GEIGER_LOW.get(), SoundSource.PLAYERS, 1, 1);
                }
            }
        } else if (entity instanceof Mob && amount * 0.5F >= 1.0F) {
            entity.hurt(TGDamageSource.causeRadiationDamage(entity.level(), null, null, DeathType.BIO), amount * 0.5F);
        }
    }
    @Override public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new techguns.client.gui.RadiationEffectIcon(0));
    }
}
