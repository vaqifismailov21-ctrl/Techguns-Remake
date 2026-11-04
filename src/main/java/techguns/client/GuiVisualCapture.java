package techguns.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Matrix4f;

/** Deterministic rendering of the isolated QA screen, independent of a hidden window's framebuffer. */
final class GuiVisualCapture {
    static NativeImage capture(Minecraft mc,Screen screen){
        var window=mc.getWindow();int oldWidth=window.getWidth(),oldHeight=window.getHeight(),oldGuiWidth=window.getGuiScaledWidth(),oldGuiHeight=window.getGuiScaledHeight();double oldScale=window.getGuiScale();
        int oldScreenWidth=screen.width,oldScreenHeight=screen.height;
        var target=new TextureTarget(1280,720,true,Minecraft.ON_OSX);var view=RenderSystem.getModelViewStack();RenderSystem.backupProjectionMatrix();view.pushPose();
        try{
            window.setWidth(1280);window.setHeight(720);window.setGuiScale(2);screen.resize(mc,640,360);
            target.setClearColor(.12f,.15f,.18f,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);
            view.setIdentity();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,640,360,0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);
            var graphics=new GuiGraphics(mc,mc.renderBuffers().bufferSource()){@Override public int guiWidth(){return 640;}@Override public int guiHeight(){return 360;}};
            graphics.pose().translate(0,0,-11000);RenderSystem.setShaderColor(1,1,1,1);screen.render(graphics,-1,-1,0);graphics.flush();
            NativeImage image=Screenshot.takeScreenshot(target);
            if(image.getWidth()!=1280||image.getHeight()!=720){image.close();throw new IllegalStateException("Invalid isolated GUI capture dimensions");}
            var colors=new java.util.HashSet<Integer>();for(int x=240;x<1040;x+=5)for(int y=120;y<620;y+=5)colors.add(image.getPixelRGBA(x,y));
            if(colors.size()<12){image.close();throw new IllegalStateException("Isolated GUI capture contains no rendered interface");}
            return image;
        }finally{
            // Restore both the screen and possibly stale logical dimensions used by the hidden client.
            screen.resize(mc,oldScreenWidth,oldScreenHeight);
            window.setWidth((int)Math.round(oldGuiWidth*oldScale));window.setHeight((int)Math.round(oldGuiHeight*oldScale));window.setGuiScale(oldScale);window.setWidth(oldWidth);window.setHeight(oldHeight);
            target.destroyBuffers();mc.getMainRenderTarget().bindWrite(true);view.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.restoreProjectionMatrix();RenderSystem.disableDepthTest();RenderSystem.disableBlend();
        }
    }
    private GuiVisualCapture(){}
}
