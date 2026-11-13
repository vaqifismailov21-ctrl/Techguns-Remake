package techguns.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

/** Shared pieces of the Techguns CE style screens of the energy blocks: the green read-out screen and tinted tanks. */
final class CePowerGui {
    static final int SCREEN_TEXT=0xFF7CFC9C,SCREEN_WARN=0xFFFF7A5C,SCREEN_DIM=0xFF4FA86A;
    private CePowerGui(){}

    /** Lines inside the read-out screen, shrunk until they fit (long translations, big fonts). */
    static void screen(GuiGraphics g,Font font,List<Component> lines,int x0,int y0,int x1,int y1){
        int width=x1-x0,height=y1-y0;float scale=1;List<FormattedCharSequence> parts=List.of();
        // the largest size at which every line fits on one row; otherwise the largest at which the wrapped text fits
        boolean found=false;
        for(float s:new float[]{1F,0.85F,0.75F,0.66F,0.58F}){
            var list=new ArrayList<FormattedCharSequence>();
            for(var line:lines)list.addAll(font.split(line,(int)(width/s)));
            if(list.size()==lines.size()&&list.size()*(font.lineHeight+1)*s<=height){scale=s;parts=list;found=true;break;}
        }
        if(!found)for(float s:new float[]{1F,0.85F,0.75F,0.66F,0.58F,0.5F}){
            scale=s;var list=new ArrayList<FormattedCharSequence>();
            for(var line:lines)list.addAll(font.split(line,(int)(width/s)));
            parts=list;if(list.size()*(font.lineHeight+1)*s<=height)break;
        }
        g.pose().pushPose();g.pose().translate(x0,y0,0);g.pose().scale(scale,scale,1);
        int ly=0;for(var part:parts){if((ly+font.lineHeight)*scale>height)break;g.drawString(font,part,0,ly,SCREEN_TEXT,false);ly+=font.lineHeight+1;}
        g.pose().popPose();
    }
    /** A fluid column rising from the bottom of the given area, tinted like the fluid in the world. */
    static void fluid(GuiGraphics g,Fluid fluid,int x,int y,int width,int height,int filled){
        if(filled<=0)return;filled=Math.min(height,filled);
        var ext=IClientFluidTypeExtensions.of(fluid);var sprite=Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ext.getStillTexture());int color=ext.getTintColor();
        RenderSystem.setShaderColor((color>>16&255)/255F,(color>>8&255)/255F,(color&255)/255F,1F);
        for(int dx=0;dx<width;dx+=16)for(int drawn=0;drawn<filled;drawn+=16){int h=Math.min(16,filled-drawn),w=Math.min(16,width-dx);g.blit(x+dx,y+height-drawn-h,0,w,h,sprite);}
        RenderSystem.setShaderColor(1F,1F,1F,1F);
    }
    /** A heat or charge column: a gradient rising from the bottom. */
    static void column(GuiGraphics g,int x,int y,int width,int height,int value,int max,int top,int bottom){
        int h=max<=0?0:(int)Math.min(height,(long)value*height/max);
        if(h>0)g.fillGradient(x,y+height-h,x+width,y+height,top,bottom);
    }
    static boolean in(int x,int y,int rx,int ry,int w,int h){return x>=rx&&x<rx+w&&y>=ry&&y<ry+h;}
}
