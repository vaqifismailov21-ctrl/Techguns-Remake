package techguns.radiation;

import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

public final class RadResistancePotion extends MobEffect {
    public RadResistancePotion() { super(MobEffectCategory.BENEFICIAL, 0xff99ac2d); }
    @Override public String getDescriptionId() { return "techguns.radresistance"; }
    @Override public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new techguns.client.gui.RadiationEffectIcon(16));
    }
}
