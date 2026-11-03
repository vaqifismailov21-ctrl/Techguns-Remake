package techguns.client;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import techguns.TGItems;
import techguns.TGPackets;
import techguns.Techguns;
import techguns.client.gui.TGGuiTabButton;
import techguns.keybind.TGKeybindsID;

@EventBusSubscriber(modid = Techguns.MODID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    @SubscribeEvent public static void inventoryTabs(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen screen) {
            event.addListener(new TGGuiTabButton(screen.getGuiLeft() + 5, screen.getGuiTop() - 26, true, Items.CRAFTING_TABLE.getDefaultInstance(), () -> {}));
            event.addListener(new TGGuiTabButton(screen.getGuiLeft() + 33, screen.getGuiTop() - 26, false, TGItems.PISTOL_ROUNDS.get().getDefaultInstance(), TGPackets::requestInventory));
        }
    }
    @SubscribeEvent public static void keys(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (false || minecraft.player == null || minecraft.screen != null) return;
        while (ClientModEvents.NIGHT_VISION.consumeClick()) TGPackets.sendKey(TGKeybindsID.TOGGLE_NIGHTVISION, true);
        while (ClientModEvents.SAFE_MODE.consumeClick()) TGPackets.sendKey(TGKeybindsID.TOGGLE_SAFEMODE, true);
        while (ClientModEvents.STEP_ASSIST.consumeClick()) TGPackets.sendKey(TGKeybindsID.TOGGLE_STEP_ASSIST, true);
        while (ClientModEvents.JETPACK.consumeClick()) TGPackets.sendKey(TGKeybindsID.TOGGLE_JETPACK, true);
    }
    private ClientForgeEvents() {}
}
