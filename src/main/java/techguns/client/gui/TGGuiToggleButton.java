package techguns.client.gui;

import net.minecraft.client.Minecraft;
import techguns.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import techguns.TGPackets;
import techguns.capabilities.TGExtendedPlayer;
import techguns.keybind.TGKeybindsID;

public final class TGGuiToggleButton extends AbstractButton {
    private static final ResourceLocation WIDGETS = ResourceLocation.withDefaultNamespace("textures/gui/widgets.png");
    private static final byte[] KEYS = {TGKeybindsID.TOGGLE_HUD, TGKeybindsID.TOGGLE_NIGHTVISION,
            TGKeybindsID.TOGGLE_SAFEMODE, TGKeybindsID.TOGGLE_STEP_ASSIST, TGKeybindsID.TOGGLE_JETPACK};
    private static final String[] LABELS = {"togglehud", "togglenv", "togglesafe", "togglestep", "togglejetpack"};
    private final int type;
    public TGGuiToggleButton(int x, int y, int type) {
        super(x, y, 11, 11, Component.translatable("techguns.tgguitooltip." + LABELS[type]));
        this.type = type;
    }
    @Override public void onPress() { TGPackets.sendKey(KEYS[type], false); }
    @Override public void renderButton(com.mojang.blaze3d.vertex.PoseStack graphics__pose, int mouseX, int mouseY, float partialTick) {techguns.client.gui.compat.GuiGraphics graphics=new techguns.client.gui.compat.GuiGraphics(graphics__pose);
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        var data = TGExtendedPlayer.find(player).orElse(null);
        if(data==null) return;
        boolean enabled = switch (type) {
            case 0 -> data.showTGHudElements; case 1 -> data.enableNightVision; case 2 -> data.enableSafemode;
            case 3 -> data.enableStepAssist; default -> data.enableJetpack;
        };
        graphics.blitNineSliced(WIDGETS, x, y, width, height, 2, 3, 2, 2, 200, 20, 0, 46 + (isHoveredOrFocused() ? 2 : 1) * 20);
        int textureY = type * 7 + (type > 3 ? 14 : 0);
        graphics.blit(TGPlayerInventoryScreen.TEXTURE, x + 2, y + 2, 242 + (enabled ? 7 : 0), textureY, 7, 7);
        if(isHovered&&Minecraft.getInstance().screen!=null)Minecraft.getInstance().screen.renderTooltip(graphics__pose,Minecraft.getInstance().font.split(Component.translatable("techguns.tgguitooltip." + LABELS[type])
                .append("\n").append(Component.translatable("techguns.tgguitooltip.current")).append(": ")
                .append(enabled ? "§a" : "§c").append(Component.translatable("techguns.tgguitooltip." + (enabled ? "on" : "off")))
                .append("\n").append(Component.translatable("techguns.tgguitooltip." + LABELS[type] + ".tooltip1"))
                .append("\n").append(Component.translatable("techguns.tgguitooltip." + LABELS[type] + ".tooltip2")),200),mouseX,mouseY);
    }
    @Override public void updateNarration(net.minecraft.client.gui.narration.NarrationElementOutput narration) { defaultButtonNarrationText(narration); }
}
