package techguns.client;
import java.nio.file.Files;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import techguns.*;
/** Synthetic item rendering only; never reads a player save or captures the desktop. */
@Mod.EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class ItemVisualVerifier {
    private static boolean done;
    @SubscribeEvent public static void verify(ScreenEvent.Render.Post event){
        if(done||!Boolean.getBoolean("techguns.verifyAllItems")||!(event.getScreen() instanceof net.minecraft.client.gui.screens.TitleScreen))return;done=true;
        var mc=Minecraft.getInstance();org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());var target=new TextureTarget(960,540,true,Minecraft.ON_OSX);var view=RenderSystem.getModelViewStack();RenderSystem.backupProjectionMatrix();view.pushPose();view.setIdentity();RenderSystem.applyModelViewMatrix();
        try{
            var output=mc.gameDirectory.toPath().resolve("verification");Files.createDirectories(output);var stacks=new java.util.ArrayList<ItemStack>();
            for(var registered:TGItems.ITEMS.getEntries())stacks.add(registered.get().getDefaultInstance());
            for(var armor:TGEquipment.ARMOR.values())for(int camo=1;camo<armor.get().getCamoCount();camo++){var stack=armor.get().getDefaultInstance();stack.getOrCreateTag().putByte("camo",(byte)camo);stacks.add(stack);}
            int pageSize=126;var index=new java.util.ArrayList<String>();
            for(int offset=0;offset<stacks.size();offset+=pageSize){
                target.setClearColor(.12f,.15f,.18f,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,960,540,0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());g.pose().translate(0,0,-11000);
                int page=offset/pageSize;for(int i=offset;i<Math.min(stacks.size(),offset+pageSize);i++){int slot=i-offset,x=8+(slot%18)*52,y=10+(slot/18)*74;var stack=stacks.get(i);g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(2,2,2);g.renderItem(stack,0,0);g.pose().popPose();g.drawString(mc.font,""+i,x,y+36,0xffeeeeee,false);index.add(page+":"+slot+" "+net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem())+(stack.hasTag()?" "+stack.getTag():""));}g.flush();try(var pixels=Screenshot.takeScreenshot(target)){pixels.writeToFile(output.resolve("all-items-"+page+".png"));}
            }
            Files.write(output.resolve("all-items-index.txt"),index);
            for(int variant=0;variant<4;variant++)for(int frame=0;frame<5;frame++){
                target.setClearColor(.12f,.15f,.18f,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.setProjectionMatrix(new Matrix4f().perspective((float)Math.toRadians(50),960f/540,.05f,100),VertexSorting.DISTANCE_TO_ORIGIN);RenderSystem.enableDepthTest();var pose=new com.mojang.blaze3d.vertex.PoseStack();pose.translate(0,-.4,-8);pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(12));pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-25));pose.translate(-.5,0,-.5);var buffers=mc.renderBuffers().bufferSource();techguns.client.render.tileentities.LargeDoorRenderer.renderGeometry(TGDoors.BLOCKS.get(variant).get().defaultBlockState(),variant,false,frame/4f,pose,buffers,0xf000f0,0);buffers.endBatch();try(var pixels=Screenshot.takeScreenshot(target)){pixels.writeToFile(output.resolve("door-"+variant+"-"+frame+".png"));}
            }
            int glError=org.lwjgl.opengl.GL11.glGetError();if(glError!=0)throw new IllegalStateException("OpenGL error in full item rendering: "+glError);Techguns.LOGGER.info("TECHGUNS_ALL_ITEMS_RENDER_PASSED: {} registered items and armor camouflage stacks rendered; twenty door animation frames saved",stacks.size());
        }catch(Exception error){throw new IllegalStateException("Full item GPU verification failed",error);}finally{target.destroyBuffers();mc.getMainRenderTarget().bindWrite(true);view.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.restoreProjectionMatrix();RenderSystem.disableDepthTest();}
    }
    private ItemVisualVerifier(){}
}
