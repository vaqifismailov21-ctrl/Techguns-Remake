package techguns.client.gui;

import net.minecraft.client.gui.Gui;
import techguns.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

/** Read the exact original icon rectangles, without editing their source texture. */
public final class RadiationEffectIcon implements IClientMobEffectExtensions {
    private final int textureX;
    public RadiationEffectIcon(int textureX) { this.textureX = textureX; }
    @Override public boolean renderInventoryIcon(MobEffectInstance effect, EffectRenderingInventoryScreen<?> screen,
                                                com.mojang.blaze3d.vertex.PoseStack pose, int x, int y, int z) {
        new GuiGraphics(pose).blit(TGPlayerInventoryScreen.TEXTURE, x + 8, y + 8, textureX, 168, 16, 16);
        return true;
    }
    @Override public boolean renderGuiIcon(MobEffectInstance effect, Gui gui, com.mojang.blaze3d.vertex.PoseStack pose,
                                          int x, int y, float z, float alpha) {
        new GuiGraphics(pose).blit(TGPlayerInventoryScreen.TEXTURE, x + 4, y + 4, textureX, 168, 16, 16);
        return true;
    }
}
