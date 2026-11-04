package techguns.api.damagesystem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum DamageType {
    PHYSICAL, PROJECTILE, FIRE, EXPLOSION, ENERGY, POISON, UNRESISTABLE, ICE, LIGHTNING, RADIATION, DARK;

    public Component displayName() {
        ChatFormatting color = switch (this) {
            case PHYSICAL -> ChatFormatting.DARK_GRAY; case PROJECTILE -> ChatFormatting.GRAY;
            case FIRE -> ChatFormatting.RED; case EXPLOSION -> ChatFormatting.DARK_RED;
            case ENERGY -> ChatFormatting.DARK_AQUA; case POISON -> ChatFormatting.DARK_GREEN;
            case UNRESISTABLE, DARK -> ChatFormatting.DARK_GRAY; case ICE -> ChatFormatting.AQUA;
            case LIGHTNING -> ChatFormatting.YELLOW; case RADIATION -> ChatFormatting.GREEN;
        };
        return Component.translatable("techguns.TGDamageType." + name()).withStyle(color);
    }
}
