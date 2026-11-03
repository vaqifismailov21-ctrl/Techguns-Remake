package techguns.client;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.Mod;
import techguns.items.armors.ArmorDisplay;

@EventBusSubscriber(modid="techguns",value=Dist.CLIENT)
public final class ArmorHud {
    private static final ResourceLocation FULL=ResourceLocation.withDefaultNamespace("hud/armor_full"),HALF=ResourceLocation.withDefaultNamespace("hud/armor_half"),EMPTY=ResourceLocation.withDefaultNamespace("hud/armor_empty");
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void armor(RenderGuiLayerEvent.Pre event){
        var mc=Minecraft.getInstance();
        var gui=mc.gui;
        if(!event.getName().equals(VanillaGuiLayers.ARMOR_LEVEL)||mc.player==null||mc.options.hideGui||mc.gameMode==null||!mc.gameMode.canHurtPlayer()||!techguns.damagesystem.DamageSystem.hasSuitArmor(mc.player))return;
        event.setCanceled(true);
        render(event.getGuiGraphics(),ArmorDisplay.points(mc.player),event.getGuiGraphics().guiWidth()/2-91,event.getGuiGraphics().guiHeight()-gui.leftHeight);
        gui.leftHeight+=10;
    }
    public static void render(GuiGraphics graphics,int points,int x,int y){
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        if(points<=0){RenderSystem.disableBlend();return;}
        for(int icon=0;icon<10;icon++){int value=icon*2+1;graphics.blitSprite(value<points?FULL:value==points?HALF:EMPTY,x+icon*8,y,9,9);}
        RenderSystem.disableBlend();
    }
    private ArmorHud(){}
}
