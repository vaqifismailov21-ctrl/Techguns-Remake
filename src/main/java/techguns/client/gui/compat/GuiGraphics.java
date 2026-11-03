package techguns.client.gui.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/**
 * The part of the 1.20 GuiGraphics API that Techguns uses, implemented with the 1.19.2 PoseStack/GuiComponent calls,
 * so the screens keep the code of the 1.20.1 version. Created at the 1.19.2 render entry points from their PoseStack.
 */
public class GuiGraphics {
    private final PoseStack pose;
    private final Minecraft minecraft = Minecraft.getInstance();
    public GuiGraphics(PoseStack pose) { this.pose = pose; }
    public GuiGraphics(net.minecraft.client.Minecraft mc, net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers) { this(new PoseStack()); }
    public PoseStack pose() { return pose; }
    public MultiBufferSource.BufferSource bufferSource() { return minecraft.renderBuffers().bufferSource(); }
    public void flush() { RenderSystem.disableDepthTest(); bufferSource().endBatch(); RenderSystem.enableDepthTest(); }
    public int guiWidth() { return minecraft.getWindow().getGuiScaledWidth(); }
    public int guiHeight() { return minecraft.getWindow().getGuiScaledHeight(); }
    public void setColor(float r, float g, float b, float a) { RenderSystem.setShaderColor(r, g, b, a); }

    private static void bind(ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }
    public void blit(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        bind(texture); GuiComponent.blit(pose, x, y, 0, (float) u, (float) v, width, height, 256, 256);
    }
    public void blit(ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        bind(texture); GuiComponent.blit(pose, x, y, u, v, width, height, textureWidth, textureHeight);
    }
    public void blit(ResourceLocation texture, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        bind(texture); GuiComponent.blit(pose, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight);
    }
    public void blit(ResourceLocation texture, int x, int y, int blitOffset, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        bind(texture); GuiComponent.blit(pose, x, y, blitOffset, u, v, width, height, textureHeight, textureWidth);
    }
    public void blit(int x, int y, int blitOffset, int width, int height, TextureAtlasSprite sprite) {
        bind(sprite.atlas().location()); GuiComponent.blit(pose, x, y, blitOffset, width, height, sprite);
    }
    /** 1.20 blitNineSliced: corners stay, edges and the middle are stretched. */
    public void blitNineSliced(ResourceLocation texture, int x, int y, int width, int height, int left, int top, int right, int bottom,
                               int sourceWidth, int sourceHeight, int u, int v) {
        int midW = width - left - right, midH = height - top - bottom, srcMidW = sourceWidth - left - right, srcMidH = sourceHeight - top - bottom;
        int[][] cols = {{x, left, u, left}, {x + left, midW, u + left, srcMidW}, {x + width - right, right, u + sourceWidth - right, right}};
        int[][] rows = {{y, top, v, top}, {y + top, midH, v + top, srcMidH}, {y + height - bottom, bottom, v + sourceHeight - bottom, bottom}};
        bind(texture);
        for (int[] c : cols) for (int[] r : rows) if (c[1] > 0 && r[1] > 0)
            GuiComponent.blit(pose, c[0], r[0], c[1], r[1], c[2], r[2], c[3], r[3], 256, 256);
    }
    public void fill(int x1, int y1, int x2, int y2, int color) { GuiComponent.fill(pose, x1, y1, x2, y2, color); }
    public int drawString(Font font, String text, int x, int y, int color) { return drawString(font, text, x, y, color, true); }
    public int drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }
    public int drawString(Font font, Component text, int x, int y, int color) { return drawString(font, text, x, y, color, true); }
    public int drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }
    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color) { return drawString(font, text, x, y, color, true); }
    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }
    public void drawCenteredString(Font font, Component text, int x, int y, int color) { GuiComponent.drawCenteredString(pose, font, text, x, y, color); }
    public void drawCenteredString(Font font, String text, int x, int y, int color) { GuiComponent.drawCenteredString(pose, font, text, x, y, color); }

    /** The 1.19.2 item renderer draws through the global model-view stack, so the GUI pose is applied there. */
    public void renderItem(ItemStack stack, int x, int y) {
        var view = RenderSystem.getModelViewStack();
        view.pushPose(); view.mulPoseMatrix(pose.last().pose()); RenderSystem.applyModelViewMatrix();
        minecraft.getItemRenderer().renderAndDecorateItem(stack, x, y);
        view.popPose(); RenderSystem.applyModelViewMatrix();
    }
    public void renderItemDecorations(Font font, ItemStack stack, int x, int y) {
        var view = RenderSystem.getModelViewStack();
        view.pushPose(); view.mulPoseMatrix(pose.last().pose()); RenderSystem.applyModelViewMatrix();
        minecraft.getItemRenderer().renderGuiItemDecorations(font, stack, x, y);
        view.popPose(); RenderSystem.applyModelViewMatrix();
    }
    private Screen screen() { return minecraft.screen; }
    public void renderTooltip(Font font, Component text, int x, int y) { if (screen() != null) screen().renderTooltip(pose, text, x, y); }
    public void renderTooltip(Font font, ItemStack stack, int x, int y) { if (screen() != null) screen().renderTooltip(pose, screen().getTooltipFromItem(stack), Optional.empty(), x, y); }
    public void renderComponentTooltip(Font font, List<Component> lines, int x, int y) { if (screen() != null) screen().renderComponentTooltip(pose, lines, x, y); }
    public void renderComponentTooltip(Font font, List<? extends net.minecraft.network.chat.FormattedText> lines, int x, int y, ItemStack stack) {
        if (screen() != null) screen().renderComponentTooltip(pose, lines, x, y, stack);
    }
    /** GuiGraphics.enableScissor in GUI coordinates; RenderSystem scissor is in window pixels from the bottom. */
    public void enableScissor(int x1, int y1, int x2, int y2) {
        var window = minecraft.getWindow(); double scale = window.getGuiScale();
        RenderSystem.enableScissor((int) (x1 * scale), (int) (window.getHeight() - y2 * scale), Math.max(0, (int) ((x2 - x1) * scale)), Math.max(0, (int) ((y2 - y1) * scale)));
    }
    public void disableScissor() { RenderSystem.disableScissor(); }
}
