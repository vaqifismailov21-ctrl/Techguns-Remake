package techguns.client.render.entities.npcs;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import techguns.client.models.LegacyObjModel;
import techguns.entities.npcs.AttackHelicopter;
public final class HelicopterRenderer extends EntityRenderer<AttackHelicopter> {
    private static final ResourceLocation TEXTURE=new ResourceLocation("techguns:textures/entity/apache.png");
    private final LegacyObjModel body=new LegacyObjModel(true,false,"npc/helicopter0"),rotor=new LegacyObjModel(true,false,"npc/helicopter1"),gun=new LegacyObjModel(true,false,"npc/helicopter2");
    public HelicopterRenderer(EntityRendererProvider.Context context){super(context);shadowRadius=5;}
    @Override public ResourceLocation getTextureLocation(AttackHelicopter entity){return TEXTURE;}
    private void mesh(LegacyObjModel model,PoseStack pose,MultiBufferSource buffers,int light){model.renderModel(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),light,OverlayTexture.NO_OVERLAY,ItemTransforms.TransformType.NONE);}
    @Override public void render(AttackHelicopter entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();pose.mulPose(Vector3f.YP.rotationDegrees(-Mth.lerp(partial,entity.yRotO,entity.getYRot())-90));pose.scale(2.5f,2.5f,2.5f);
        float ticks=(entity.level.getGameTime()%60)+partial;
        if(entity.deathTime==0){
            mesh(body,pose,buffers,light);pose.pushPose();pose.mulPose(Vector3f.YP.rotationDegrees(ticks*24));mesh(rotor,pose,buffers,light);pose.popPose();
            pose.pushPose();pose.translate(1.2,0,0);pose.mulPose(Vector3f.ZP.rotationDegrees(-Mth.lerp(partial,entity.xRotO,entity.getXRot())));pose.translate(-1.2,0,0);mesh(gun,pose,buffers,light);pose.popPose();
        }else{
            float progress=(entity.deathTime+partial)/AttackHelicopter.MAX_DEATH_TIME,squared=progress*progress;pose.translate(0,-4*squared,0);
            // The source applies the same 720-degree death rotation twice.
            pose.mulPose(Vector3f.YP.rotationDegrees(squared*1440));mesh(body,pose,buffers,light);mesh(gun,pose,buffers,light);pose.mulPose(Vector3f.YP.rotationDegrees(ticks*12));mesh(rotor,pose,buffers,light);
        }
        pose.popPose();super.render(entity,yaw,partial,pose,buffers,light);
    }
}
