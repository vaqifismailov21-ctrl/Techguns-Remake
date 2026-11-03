package techguns.api.guns;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum GunHandType {
    TWO_HANDED, ONE_HANDED, ONE_POINT_FIVE_HANDED;
    public Component displayName() {
        return Component.translatable("techguns.gunhandtype." + name().toLowerCase(java.util.Locale.ROOT))
                .withStyle(this == ONE_HANDED ? ChatFormatting.GREEN : this == TWO_HANDED ? ChatFormatting.GOLD : ChatFormatting.YELLOW);
    }
}
