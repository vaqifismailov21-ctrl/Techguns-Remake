package techguns.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Techguns CE TGCreativeTabButton: a creative inventory tab turned onto the left side that opens the Techguns inventory. */
public final class TGCreativeTabButton extends AbstractButton {
    private static final ResourceLocation TABS=ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tabs.png");
    private final ItemStack icon;private final Runnable action;
    public TGCreativeTabButton(int x,int y,ItemStack icon,Runnable action){super(x,y,28,28,Component.empty());this.icon=icon;this.action=action;}
    @Override public void onPress(){action.run();}
    @Override protected void renderWidget(GuiGraphics graphics,int mouseX,int mouseY,float partial){
        graphics.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableCull();
        var pose=graphics.pose();pose.pushPose();pose.translate(getX(),getY(),0);pose.scale(1,-1,1);pose.mulPose(Axis.ZP.rotationDegrees(-90));
        // the unselected top tab, its 26 px width widened back to 28 like the other Techguns tabs
        graphics.blit(TABS,0,0,3,28,0F,2,3,28,256,256);graphics.blit(TABS,3,0,22,28,3F,2,20,28,256,256);graphics.blit(TABS,25,0,3,28,23F,2,3,28,256,256);
        pose.popPose();graphics.flush();RenderSystem.enableCull();RenderSystem.disableBlend();
        graphics.renderItem(icon,getX()+6,getY()+6);
    }
    @Override protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narration){defaultButtonNarrationText(narration);}
}
