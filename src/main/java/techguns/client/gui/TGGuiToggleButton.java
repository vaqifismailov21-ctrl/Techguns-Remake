package techguns.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import techguns.TGPackets;
import techguns.capabilities.TGExtendedPlayer;
import techguns.keybind.TGKeybindsID;

public final class TGGuiToggleButton extends AbstractButton {
    private static final byte[] KEYS = {TGKeybindsID.TOGGLE_HUD, TGKeybindsID.TOGGLE_NIGHTVISION,
            TGKeybindsID.TOGGLE_SAFEMODE, TGKeybindsID.TOGGLE_STEP_ASSIST, TGKeybindsID.TOGGLE_JETPACK};
    private static final String[] LABELS = {"togglehud", "togglenv", "togglesafe", "togglestep", "togglejetpack"};
    private final int type;
    public TGGuiToggleButton(int x, int y, int type) {
        super(x, y, 11, 11, Component.translatable("techguns.tgguitooltip." + LABELS[type]));
        this.type = type;
    }
    @Override public void onPress() { TGPackets.sendKey(KEYS[type], false); }
    @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        var data = TGExtendedPlayer.find(player).orElse(null);
        if(data==null) return;
        boolean enabled = switch (type) {
            case 0 -> data.showTGHudElements; case 1 -> data.enableNightVision; case 2 -> data.enableSafemode;
            case 3 -> data.enableStepAssist; default -> data.enableJetpack;
        };
        graphics.blitSprite(net.minecraft.resources.ResourceLocation.withDefaultNamespace(isHoveredOrFocused() ? "widget/button_highlighted" : "widget/button"), getX(), getY(), width, height);
        int textureY = type * 7 + (type > 3 ? 14 : 0);
        graphics.blit(TGPlayerInventoryScreen.TEXTURE, getX() + 2, getY() + 2, 242 + (enabled ? 7 : 0), textureY, 7, 7);
        setTooltip(Tooltip.create(Component.translatable("techguns.tgguitooltip." + LABELS[type])
                .append("\n").append(Component.translatable("techguns.tgguitooltip.current")).append(": ")
                .append(enabled ? "§a" : "§c").append(Component.translatable("techguns.tgguitooltip." + (enabled ? "on" : "off")))
                .append("\n").append(Component.translatable("techguns.tgguitooltip." + LABELS[type] + ".tooltip1"))
                .append("\n").append(Component.translatable("techguns.tgguitooltip." + LABELS[type] + ".tooltip2"))));
    }
    @Override protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narration) { defaultButtonNarrationText(narration); }
}
