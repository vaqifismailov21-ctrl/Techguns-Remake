package techguns.client.gui;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import techguns.TGItems;
import techguns.Techguns;
import techguns.capabilities.TGExtendedPlayer;
import techguns.gui.player.TGPlayerInventoryContainer;

/** Original inventory texture, preview, radiation bar, and five toggle positions. */
public final class TGPlayerInventoryScreen extends AbstractContainerScreen<TGPlayerInventoryContainer> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Techguns.MODID, "textures/gui/tgplayerinventory.png");
    public TGPlayerInventoryScreen(TGPlayerInventoryContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
    @Override protected void init() {
        super.init();
        addRenderableWidget(new TGGuiTabButton(leftPos + 5, topPos - 26, false, Items.CRAFTING_TABLE.getDefaultInstance(), () -> {
            var player = minecraft.player;
            if (player != null) {
                // closeContainer opens a null screen first, grabbing/recentring the mouse between tabs.
                player.connection.send(new net.minecraft.network.protocol.game.ServerboundContainerClosePacket(player.containerMenu.containerId));
                player.containerMenu=player.inventoryMenu;
                minecraft.setScreen(new InventoryScreen(player));
            }
        }));
        addRenderableWidget(new TGGuiTabButton(leftPos + 33, topPos - 26, true, TGItems.PISTOL_ROUNDS.get().getDefaultInstance(), () -> {}));
        for (int i = 0; i < 5; i++) addRenderableWidget(new TGGuiToggleButton(leftPos - 8, topPos + 7 + i * 11, i));
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        var player = minecraft.player;
        if (player == null) return;
        var state=TGExtendedPlayer.find(player).orElse(null);
        if(state==null) return;
        int radiation = state.radlevel;
        if (radiation > 0) graphics.blit(TEXTURE, leftPos + 116, topPos + 8, 177, 0, (int) Math.ceil(radiation / 1000.0 * 52), 4);
        graphics.enableScissor(leftPos+26,topPos+8,leftPos+75,topPos+79);
        try{InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, leftPos + 51, topPos + 75, 30,
                leftPos + 51 - mouseX, topPos + 25 - mouseY, player);}finally{graphics.disableScissor();}
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        // Techguns CE: total armour values of the worn suit, with the share of 25 points as a percentage.
        if (minecraft.player != null && mouseX >= leftPos + 98 && mouseX <= leftPos + 112 && mouseY >= topPos + 7 && mouseY <= topPos + 21) {
            var lines = new ArrayList<Component>();
            lines.add(Component.translatable("techguns.tgguitooltip.totalarmorvalues").withStyle(net.minecraft.ChatFormatting.UNDERLINE));
            var colors = new net.minecraft.ChatFormatting[]{net.minecraft.ChatFormatting.DARK_GRAY,net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.DARK_RED,net.minecraft.ChatFormatting.DARK_AQUA,net.minecraft.ChatFormatting.RED,net.minecraft.ChatFormatting.AQUA,net.minecraft.ChatFormatting.YELLOW,net.minecraft.ChatFormatting.DARK_GRAY,net.minecraft.ChatFormatting.DARK_GREEN,net.minecraft.ChatFormatting.GREEN};
            var types = new techguns.api.damagesystem.DamageType[]{techguns.api.damagesystem.DamageType.PHYSICAL,techguns.api.damagesystem.DamageType.PROJECTILE,techguns.api.damagesystem.DamageType.EXPLOSION,techguns.api.damagesystem.DamageType.ENERGY,techguns.api.damagesystem.DamageType.FIRE,techguns.api.damagesystem.DamageType.ICE,techguns.api.damagesystem.DamageType.LIGHTNING,techguns.api.damagesystem.DamageType.DARK,techguns.api.damagesystem.DamageType.POISON,techguns.api.damagesystem.DamageType.RADIATION};
            var format = new java.text.DecimalFormat("#.##", java.text.DecimalFormatSymbols.getInstance(java.util.Locale.ENGLISH));
            for (int i = 0; i < types.length; i++) {
                float value = techguns.damagesystem.DamageSystem.getTotalArmorAgainstType(minecraft.player, types[i]);
                lines.add(Component.translatable("techguns.TGDamageType." + types[i].name()).append(": " + format.format(value) + " (" + format.format(value / 25 * 100) + "%)").withStyle(colors[i]));
            }
            graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
        }
        if (minecraft.player != null && mouseX >= leftPos + 115 && mouseX < leftPos + 169 && mouseY >= topPos + 7 && mouseY < topPos + 13) {
            var state=TGExtendedPlayer.find(minecraft.player).orElse(null);
            if(state==null) return;
            int radiation = state.radlevel;
            var tooltip = new ArrayList<Component>();
            tooltip.add(Component.translatable("techguns.tgguitooltip.currentrad"));
            tooltip.add(Component.literal(radiation + "/1000"));
            tooltip.add(Component.translatable("techguns.tgguitooltip.radeffects")
                    .append(": ").append(radiation < 500 ? Component.translatable("techguns.tgguitooltip.radeffects.none") : Component.empty()));
            if (radiation >= 500) tooltip.add(Component.translatable("effect.minecraft.hunger").append(radiation >= 750 ? " II" : ""));
            if (radiation >= 750) { tooltip.add(Component.translatable("effect.minecraft.mining_fatigue").append(" II")); tooltip.add(Component.translatable("effect.minecraft.weakness").append(" II")); }
            if (radiation >= 1000) { tooltip.add(Component.translatable("effect.minecraft.nausea").append(" II")); tooltip.add(Component.translatable("effect.minecraft.slowness").append(" II")); tooltip.add(Component.translatable("techguns.gun.tooltip.damage")); }
            graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }
}
