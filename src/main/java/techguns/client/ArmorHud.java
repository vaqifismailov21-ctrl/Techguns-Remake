package techguns.client;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import techguns.items.armors.ArmorDisplay;

@Mod.EventBusSubscriber(modid="techguns",value=Dist.CLIENT)
public final class ArmorHud {
    private static final ResourceLocation ICONS=ResourceLocation.withDefaultNamespace("textures/gui/icons.png");
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void armor(RenderGuiOverlayEvent.Pre event){
        var mc=Minecraft.getInstance();
        if(event.getOverlay()!=VanillaGuiOverlay.ARMOR_LEVEL.type()||mc.player==null||mc.options.hideGui||!(mc.gui instanceof ForgeGui gui)||!gui.shouldDrawSurvivalElements()||!techguns.damagesystem.DamageSystem.hasSuitArmor(mc.player))return;
        event.setCanceled(true);gui.setupOverlayRenderState(true,false);
        render(event.getGuiGraphics(),ArmorDisplay.points(mc.player),event.getGuiGraphics().guiWidth()/2-91,event.getGuiGraphics().guiHeight()-gui.leftHeight);
        gui.leftHeight+=10;
    }
    public static void render(GuiGraphics graphics,int points,int x,int y){
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        if(points<=0){RenderSystem.disableBlend();return;}
        for(int icon=0;icon<10;icon++){int value=icon*2+1;int u=value<points?34:value==points?25:16;graphics.blit(ICONS,x+icon*8,y,u,9,9,9);}
        RenderSystem.disableBlend();
    }
    private ArmorHud(){}
}
