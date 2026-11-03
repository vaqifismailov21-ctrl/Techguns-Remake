package techguns.client.render.tileentities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;
import techguns.tileentities.LargeDoorEntity;
/** Original door meshes and movement distances; the frame stays fixed during opening. */
public final class LargeDoorRenderer implements BlockEntityRenderer<LargeDoorEntity> {
    public static final String[] PARTS={"techdoor3x3_frame","techdoor3x3_left","techdoor3x3_right","techdoor3x3_frame_nether","techdoor3x3_left_nether","techdoor3x3_right_nether","hangar_door_frame","hangar_door_down_frame","hangar_door_mid1j","hangar_door_mid2j","hangar_door_mid3j","hangar_door_lower","hangar_door_down_upper"};
    public LargeDoorRenderer(BlockEntityRendererProvider.Context context){}
    public static void registerModels(ModelEvent.RegisterAdditional event){for(String part:PARTS)event.register(id(part));}
    private static ResourceLocation id(String part){return new ResourceLocation("techguns","block/"+part);}
    @Override public void render(LargeDoorEntity door,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        renderGeometry(door.getBlockState(),door.variant(),door.zplane(),door.openness(partial),pose,buffers,light,overlay);
    }
    public static void renderGeometry(net.minecraft.world.level.block.state.BlockState state,int type,boolean zplane,float openness,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        pose.pushPose();if(zplane){pose.translate(0,0,1);pose.mulPose(Vector3f.YP.rotationDegrees(90));}
        if(type==0||type==3){String suffix=type==3?"_nether":"";draw(state,"techdoor3x3_frame"+suffix,pose,buffers,light,overlay,0,0);draw(state,"techdoor3x3_left"+suffix,pose,buffers,light,overlay,-1.125*openness,0);draw(state,"techdoor3x3_right"+suffix,pose,buffers,light,overlay,1.125*openness,0);}
        else{draw(state,type==1?"hangar_door_frame":"hangar_door_down_frame",pose,buffers,light,overlay,0,0);double distance=3*openness,dy=type==1?distance:-distance;
            draw(state,type==1?"hangar_door_lower":"hangar_door_down_upper",pose,buffers,light,overlay,0,dy);
            // Hide the same telescoping sections at the same distances as RenderDoor3x3Fast.
            if(openness<1){draw(state,type==1?"hangar_door_mid1j":"hangar_door_mid3j",pose,buffers,light,overlay,0,dy);if(distance<=2)draw(state,"hangar_door_mid2j",pose,buffers,light,overlay,0,dy);if(distance<=1)draw(state,type==1?"hangar_door_mid3j":"hangar_door_mid1j",pose,buffers,light,overlay,0,dy);}
        }pose.popPose();
    }
    private static void draw(net.minecraft.world.level.block.state.BlockState state,String part,PoseStack pose,MultiBufferSource buffers,int light,int overlay,double dx,double dy){pose.pushPose();pose.translate(dx,dy,0);var mc=Minecraft.getInstance();var model=mc.getModelManager().getModel(id(part));var renderType=RenderType.cutout();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(renderType),state,model,1,1,1,light,overlay,ModelData.EMPTY,renderType);pose.popPose();}
    @Override public boolean shouldRenderOffScreen(LargeDoorEntity door){return true;}
}
