package techguns.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Original 28x26 tab shape, item icon, and selected-tab extension. */
public final class TGGuiTabButton extends AbstractButton {
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
        // 1.21.1: the creative tab atlas (tabs.png) is gone; use the vanilla tab sprites at the original 28px width.
        graphics.blitSprite(ResourceLocation.withDefaultNamespace(selected?"container/creative_inventory/tab_top_selected_1":"container/creative_inventory/tab_top_unselected_1"),getX(),getY(),28,selected?30:26);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        graphics.renderItem(icon, getX() + 6, getY() + 6);
    }
    @Override protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narration) { defaultButtonNarrationText(narration); }
}
