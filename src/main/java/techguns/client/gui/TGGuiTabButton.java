package techguns.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Original 28x26 tab shape, item icon, and selected-tab extension. */
public final class TGGuiTabButton extends AbstractButton {
    private static final ResourceLocation TABS = ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tabs.png");
    private final ItemStack icon;
    private final Runnable action;
    private final boolean selected;
    public TGGuiTabButton(int x, int y, boolean selected, ItemStack icon, Runnable action) {
        super(x, y, 28, 26, Component.empty());
        this.selected = selected; this.icon = icon; this.action = action; active = !selected;
    }
    @Override public void onPress() { action.run(); }
    @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.flush();
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        // Vanilla narrowed each atlas tab from 28 to 26 pixels. Preserve original 28px borders
        // by stretching only its flat centre, rather than sampling into the neighbouring tab.
        drawLegacyWidth(graphics,getX(),getY(),0,selected?32:2,26);
        if (selected) drawLegacyWidth(graphics,getX()+1,getY()+26,27,60,4);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        graphics.renderItem(icon, getX() + 6, getY() + 6);
    }
    private static void drawLegacyWidth(GuiGraphics graphics,int x,int y,int u,int v,int height) {
        graphics.blit(TABS,x,y,3,height,(float)u,v,3,height,256,256);
        graphics.blit(TABS,x+3,y,22,height,(float)u+3,v,20,height,256,256);
        graphics.blit(TABS,x+25,y,3,height,(float)u+23,v,3,height,256,256);
    }
    @Override protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narration) { defaultButtonNarrationText(narration); }
}
