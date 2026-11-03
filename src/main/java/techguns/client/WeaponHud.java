package techguns.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;
import techguns.capabilities.TGExtendedPlayer;
import techguns.items.guns.GenericGun;
import techguns.util.InventoryUtil;

/** Pistol subset of original TGGuiEvents, using original sprites, spacing and coordinates. */
@Mod.EventBusSubscriber(modid = Techguns.MODID, value = Dist.CLIENT)
public final class WeaponHud {
    private static final ResourceLocation CROSSHAIRS = ResourceLocation.fromNamespaceAndPath("techguns", "textures/gui/tg_crosshairs.png");
    private static final ResourceLocation INVENTORY = techguns.client.gui.TGPlayerInventoryScreen.TEXTURE;
    @SubscribeEvent public static void crosshair(RenderGuiOverlayEvent.Pre event) {
        var game = Minecraft.getInstance();
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id()) || game.player == null || game.player.isSpectator()
                || !game.options.getCameraType().isFirstPerson() || !(game.player.getMainHandItem().getItem() instanceof GenericGun gun)) return;
        var style=gun.getCrossHairStyle();
        if(ClientZoom.isZoomed(game.player,gun)&&techguns.client.render.item.GunItemRenderer.get().hasScope(gun)) {event.setCanceled(true);return;}
        if(style==techguns.items.guns.EnumCrosshairStyle.VANILLA) return;
        event.setCanceled(true);
        var graphics = event.getGuiGraphics();
        int x = graphics.guiWidth() / 2, y = graphics.guiHeight() / 2;
        int spacing = Mth.clamp(Math.round(100 * gun.getSpread() * (ClientZoom.isZoomed(game.player,gun)?gun.getZoomBonus():1) * (game.player.isBlocking() ?
                gun.getGunHandType()==techguns.api.guns.GunHandType.ONE_HANDED?4:8 : 1)), 1, 10);
        graphics.flush(); RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        if(style==techguns.items.guns.EnumCrosshairStyle.GUN_DYNAMIC) {
        graphics.blit(CROSSHAIRS, x-4-spacing,y,3,7,4,1);
        graphics.blit(CROSSHAIRS, x+1+spacing,y,8,7,4,1);
        graphics.blit(CROSSHAIRS, x,y,7,7,1,1);
        graphics.blit(CROSSHAIRS, x,y-4-spacing,7,3,1,4);
        graphics.blit(CROSSHAIRS, x,y+1+spacing,7,8,1,4);
        } else {
            int u=style.getLocX(),v=style.getLocY();
            if(style.dynamicType!=techguns.items.guns.EnumCrosshairDynamicType.NONE)
                graphics.blit(CROSSHAIRS,x-1,y-1,u+6,v+6,3,3);
            switch(style.dynamicType) {
                case BOTH -> {
                    graphics.blit(CROSSHAIRS,x-6-spacing,y-2,u,v+5,6,5);
                    graphics.blit(CROSSHAIRS,x+1+spacing,y-2,u+9,v+5,6,5);
                    graphics.blit(CROSSHAIRS,x-2,y-6-spacing,u+5,v,5,6);
                    graphics.blit(CROSSHAIRS,x-2,y+1+spacing,u+5,v+9,5,6);
                }
                case HORIZONTAL -> {
                    graphics.blit(CROSSHAIRS,x-6-spacing,y-7,u,v,6,16);
                    graphics.blit(CROSSHAIRS,x+1+spacing,y-7,u+9,v,6,16);
                }
                case X -> {
                    graphics.blit(CROSSHAIRS,x-6-spacing,y-6-spacing,u,v,6,6);
                    graphics.blit(CROSSHAIRS,x+1+spacing,y-6-spacing,u+9,v,6,6);
                    graphics.blit(CROSSHAIRS,x-6-spacing,y+1+spacing,u,v+9,6,6);
                    graphics.blit(CROSSHAIRS,x+1+spacing,y+1+spacing,u+9,v+9,6,6);
                }
                case TRI -> {
                    graphics.blit(CROSSHAIRS,x-7,y-6-spacing,u,v,16,6);
                    graphics.blit(CROSSHAIRS,x-6-spacing,y+1+spacing,u,v+9,6,6);
                    graphics.blit(CROSSHAIRS,x+1+spacing,y+1+spacing,u+9,v+9,6,6);
                }
                case TRI_INV -> {
                    graphics.blit(CROSSHAIRS,x-6-spacing,y-6-spacing,u,v,6,6);
                    graphics.blit(CROSSHAIRS,x+1+spacing,y-6-spacing,u+9,v,6,6);
                    graphics.blit(CROSSHAIRS,x-7,y+1+spacing,u,v+9,16,6);
                }
                default -> graphics.blit(CROSSHAIRS,x-7,y-7,u,v,16,16);
            }
        }
        graphics.flush(); RenderSystem.defaultBlendFunc(); RenderSystem.disableBlend();
    }
    @SubscribeEvent public static void ammo(RenderGuiEvent.Post event) {
        var game = Minecraft.getInstance();
        if (game.player == null || game.player.isSpectator() || game.options.hideGui) return;
        var state = TGExtendedPlayer.find(game.player).orElse(null);
        if(state==null) return;
        var graphics = event.getGuiGraphics();
        boolean held = drawAmmo(graphics, game.player.getMainHandItem(), 0, state);
        if(techguns.api.guns.GunManager.canUseOffhand(game.player)) held |= drawAmmo(graphics, game.player.getOffhandItem(), -12, state);
        if (held && state.showTGHudElements)
            graphics.blit(INVENTORY, graphics.guiWidth()-10, graphics.guiHeight()-32, 242+7*(state.enableSafemode ? 1 : 0),14,7,7);
    }
    private static boolean drawAmmo(GuiGraphics graphics, ItemStack stack, int offsetY, TGExtendedPlayer state) {
        if (!(stack.getItem() instanceof GenericGun gun)) return false;
        var game = Minecraft.getInstance(); var inventory = game.player.getInventory().items;
        var extra = state.tg_inventory.inventory;
        int count = 0;
        for (var magazine : gun.getAmmoType().getAmmo(gun.getCurrentAmmoVariant(stack))) {
            int available = InventoryUtil.countItemInInv(inventory, magazine, 0, inventory.size())
                    + InventoryUtil.countItemInInv(extra, magazine, 7, 15);
            if(gun.getAmmoCount()>1) available/=gun.getAmmoCount();
            if (available > count) count = available; // Original uses max for multiple components.
        }
        String text = gun.getAmmoDisplayCount(stack) + "/" + gun.getClipDisplayCount() + ChatFormatting.YELLOW + "x" + count;
        graphics.drawString(game.font, text, graphics.guiWidth()+1-text.length()*6,
                graphics.guiHeight()-game.font.lineHeight-2+offsetY, 0xFFFFFFFF, false);
        return true;
    }
    private WeaponHud() {}
}
