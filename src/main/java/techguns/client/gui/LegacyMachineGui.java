package techguns.client.gui;

import java.util.List;
import java.util.UUID;
import java.util.function.IntSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import techguns.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fluids.FluidStack;
import techguns.TGConfig;

/** Shared pieces of the original OwnedTileEntGui/RedstoneTileEntGui/PoweredTileEntGui/TGBaseGui layers. */
public final class LegacyMachineGui {
    public static final ResourceLocation MACHINE=new ResourceLocation("techguns","textures/gui/ammo_press_gui.png");
    public static final int SECURITY_Y=40;
    private LegacyMachineGui(){}

    /** TGBaseGui.isInRect: inclusive on both edges. */
    public static boolean in(int x,int y,int rx,int ry,int w,int h){return x>=rx&&x<=rx+w&&y>=ry&&y<=ry+h;}

    /** GuiButtonExt with a 16x16 icon over the vanilla button (GuiButtonRedstoneState/Security/TargetAnimals/PvPSetting). */
    public static Button iconButton(int x,int y,Runnable press,ResourceLocation texture,int u,IntSupplier v){
        return new Button(x,y,20,20,Component.empty(),b->press.run()){
            @Override public void renderButton(com.mojang.blaze3d.vertex.PoseStack graphics__pose,int mouseX,int mouseY,float partial){techguns.client.gui.compat.GuiGraphics graphics=new techguns.client.gui.compat.GuiGraphics(graphics__pose);super.renderButton(graphics.pose(),mouseX,mouseY,partial);graphics.blit(texture,x+2,y+2,u,v.getAsInt(),16,16);}
        };
    }

    /** Side panels, redstone lamps and the 48 px power bar drawn by the original layers. */
    public static void drawPowered(GuiGraphics graphics,int k,int l,boolean redstoneEnabled,int energy,int maxEnergy){
        drawRedstone(graphics,k,l,redstoneEnabled);
        int power=(int)((long)energy*48/maxEnergy),diff=TGConfig.machinesNeedNoPower?0:48-power;
        graphics.blit(MACHINE,k+8,l+17+diff,251,1+diff,4,48);
    }

    /** OwnedTileEntGui + RedstoneTileEntGui backgrounds: both side panels and the two redstone lamps. */
    public static void drawRedstone(GuiGraphics graphics,int k,int l,boolean redstoneEnabled){
        drawSecurityPanel(graphics,k,l,SECURITY_Y);
        graphics.blit(MACHINE,k-22-5,l+10-5,195,0,22+5,30);
        if(redstoneEnabled){graphics.blit(MACHINE,k+6,l+5,240,0,5,5);graphics.blit(MACHINE,k+6+5,l+5,245,5,5,5);}
        else{graphics.blit(MACHINE,k+6,l+5,240,5,5,5);graphics.blit(MACHINE,k+6+5,l+5,245,0,5,5);}
    }

    /** Tooltips of the power bar, redstone and security controls, or null. */
    public static List<Component> poweredTooltip(int mx,int my,boolean signal,int redstoneMode,byte security,UUID owner,int energy,int maxEnergy){
        if(in(mx,my,7,17,5,49))return List.of(TGConfig.machinesNeedNoPower?Component.translatable("techguns.container.power"):Component.translatable("techguns.container.power").append(": "+energy+"/"+maxEnergy+" FE"));
        var redstone=redstoneTooltip(mx,my,signal,redstoneMode);if(redstone!=null)return redstone;
        return securityTooltip(mx,my,SECURITY_Y,security,owner);
    }
    /** RedstoneTileEntGui tooltip over the two lamps or the redstone button, or null. */
    public static List<Component> redstoneTooltip(int mx,int my,boolean signal,int redstoneMode){
        if(!in(mx,my,6,5,10,5)&&!in(mx,my,-22,10,20,20))return null;
        return List.of(Component.translatable("techguns.container.redstone"),
            Component.translatable("techguns.container.redstone.mode").append(": ").append(Component.translatable("techguns.redstonemode."+switch(redstoneMode){case 1->"high";case 2->"low";default->"ignore";})),
            Component.translatable("techguns.container.redstone.signal").append(": ").append(Component.translatable(signal?"techguns.redstonemode.signal.high":"techguns.redstonemode.signal.low")));
    }
    /** OwnedTileEntGui security tooltip for a button at (-22, buttonY), or null. */
    public static List<Component> securityTooltip(int mx,int my,int buttonY,byte security,UUID owner){
        if(!in(mx,my,-22,buttonY,20,20))return null;
        ChatFormatting color=switch(security){case 1->ChatFormatting.YELLOW;case 2->ChatFormatting.GOLD;case 3->ChatFormatting.RED;default->ChatFormatting.GREEN;};
        Component own=owner==null?Component.translatable("techguns.container.security.tooltip.unowned"):Component.translatable("techguns.container.security.tooltip.owner").append(": "+name(owner));
        return List.of(Component.translatable("techguns.container.security.owner").append(": ").append(own),
            Component.translatable("techguns.container.security.tooltip").append(": ").append(Component.translatable("techguns.container.security.tooltip."+security).withStyle(color)),
            Component.translatable("techguns.container.security.tooltip.description."+security),
            Component.translatable("techguns.container.security.tooltip.descr2"));
    }
    /** OwnedTileEntGui side panel behind the security button. */
    public static void drawSecurityPanel(GuiGraphics graphics,int k,int l,int buttonY){graphics.blit(MACHINE,k-22-5,l+buttonY-5,195,0,22+5,30);}
    public static String name(UUID uuid){String name=UsernameCache.getLastKnownUsername(uuid);return name!=null?name:"UNKNOW_PLAYERNAME";}

    /** TGBaseGui.drawFluidWithTesselator: untinted still sprite stretched to the tank width, stacked from the bottom. */
    public static void drawFluid(GuiGraphics graphics,FluidStack fluid,int x,int y,int width,int height,int px){
        if(fluid.isEmpty()||px<=0)return;
        var still=IClientFluidTypeExtensions.of(fluid.getFluid()).getStillTexture(fluid);
        TextureAtlasSprite tex=Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
        int iconHeight=tex.getHeight();int offset=0,len=iconHeight;
        var pose=graphics.pose().last().pose();
        while(offset<px){
            if(offset+len>px)len=px-offset;
            int y0=y+height-offset-len,h=len;
            float v0=tex.getV0()+(tex.getV1()-tex.getV0())*(iconHeight-h)/(float)iconHeight;
            var buffer=graphics.bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.text(InventoryMenu.BLOCK_ATLAS));
            buffer.vertex(pose,x,y0+h,0).color(255,255,255,255).uv(tex.getU0(),tex.getV1()).uv2(0xF000F0).endVertex();
            buffer.vertex(pose,x+width,y0+h,0).color(255,255,255,255).uv(tex.getU1(),tex.getV1()).uv2(0xF000F0).endVertex();
            buffer.vertex(pose,x+width,y0,0).color(255,255,255,255).uv(tex.getU1(),v0).uv2(0xF000F0).endVertex();
            buffer.vertex(pose,x,y0,0).color(255,255,255,255).uv(tex.getU0(),v0).uv2(0xF000F0).endVertex();
            offset+=iconHeight;
        }
        graphics.flush();
    }
}
