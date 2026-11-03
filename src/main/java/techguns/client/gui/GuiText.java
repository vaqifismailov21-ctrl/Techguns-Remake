package techguns.client.gui;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
/** Keep translated labels inside their allocated area without moving the slots. */
final class GuiText {
    static void centered(GuiGraphics graphics,Font font,Component text,int start,int end,int y,int color){
        float scale=Math.min(1,(end-start)/(float)Math.max(1,font.width(text)));
        float x=(start+end-font.width(text)*scale)/2F;
        graphics.pose().pushPose();graphics.pose().translate(x,y,0);graphics.pose().scale(scale,scale,1);graphics.drawString(font,text,0,0,color,false);graphics.pose().popPose();
    }
    static void fitted(GuiGraphics graphics,Font font,Component text,int x,int y,int width,int color){
        float scale=Math.min(1,width/(float)Math.max(1,font.width(text)));graphics.pose().pushPose();graphics.pose().translate(x,y,0);graphics.pose().scale(scale,scale,1);graphics.drawString(font,text,0,0,color,false);graphics.pose().popPose();
    }
    private GuiText(){}
}
