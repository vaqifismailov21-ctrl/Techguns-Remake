package techguns.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Buttons drawn the way Techguns looked on 1.12: a bevelled face a little darker than the vanilla button, blue when the
 * mouse is over it, yellow text.
 * The 1.20 buttons have a black frame and keep a white focus outline after a click, which looked wrong in the machine GUIs.
 */
public class LegacyButton extends Button {
    private static final int[] FACE = {0xFF262626, 0xFF5C5C5C, 0xFF68719F};
    private static final int[] HIGHLIGHT = {0xFF333333, 0xFF8A8A8A, 0xFF9CA6D6};
    private static final int[] SHADOW = {0xFF1C1C1C, 0xFF424242, 0xFF4A5180};
    private static final int[] TEXT = {0xA0A0A0, 0xE0E0E0, 0xFFFFA0};

    protected LegacyButton(int x, int y, int width, int height, Component message, OnPress press) {
        super(x, y, width, height, message, press, DEFAULT_NARRATION);
    }

    public static LegacyBuilder legacy(Component message, OnPress press) { return new LegacyBuilder(message, press); }

    /** 0 disabled, 1 normal, 2 hovered (keyboard focus counts as hovered only while using the keyboard). */
    public static int state(AbstractWidget widget) {
        if (!widget.active) return 0;
        boolean keyboard = widget.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
        return widget.isHovered() || keyboard ? 2 : 1;
    }

    public static void drawBackground(GuiGraphics g, int x, int y, int w, int h, int state) {
        g.fill(x, y, x + w, y + h, 0xFF000000);
        if (w <= 2 || h <= 2) return;
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, SHADOW[state]);
        g.fill(x + 1, y + 1, x + w - 2, y + h - 2, HIGHLIGHT[state]);
        if (w > 4 && h > 4) g.fill(x + 2, y + 2, x + w - 2, y + h - 2, FACE[state]);
    }

    @Override protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int s = state(this);
        drawBackground(g, getX(), getY(), width, height, s);
        renderString(g, Minecraft.getInstance().font, TEXT[s] | 0xFF000000);
    }

    public static final class LegacyBuilder {
        private final Component message; private final OnPress press;
        private int x, y, width = 150, height = 20;
        private LegacyBuilder(Component message, OnPress press) { this.message = message; this.press = press; }
        public LegacyBuilder bounds(int x, int y, int width, int height) { this.x = x; this.y = y; this.width = width; this.height = height; return this; }
        public LegacyButton build() { return new LegacyButton(x, y, width, height, message, press); }
    }
}
