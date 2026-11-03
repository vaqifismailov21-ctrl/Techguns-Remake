package techguns.radiation;

import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import techguns.capabilities.TGExtendedPlayer;

public final class RadRegenerationPotion extends MobEffect {
    public RadRegenerationPotion() { super(MobEffectCategory.BENEFICIAL, 0xffa2000); }
    @Override public String getDescriptionId() { return "techguns.radregeneration"; }
    @Override public boolean isDurationEffectTick(int duration, int amplifier) { return duration % 20 == 0; }
    @Override public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) TGExtendedPlayer.get(player).addRadiation(-(amplifier + 1));
    }
    @Override public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new techguns.client.gui.RadiationEffectIcon(32));
    }
}
