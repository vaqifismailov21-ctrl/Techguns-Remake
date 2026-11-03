package techguns.client.render.entities.projectiles;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import techguns.client.render.fx.LegacyFxRenderTypes;
import techguns.entities.projectiles.BioGunProjectile;
public final class RenderBioProjectile extends EntityRenderer<BioGunProjectile> {
    private static final ResourceLocation TEXTURE=new ResourceLocation("techguns","textures/entity/bioblob.png");
    public RenderBioProjectile(EntityRendererProvider.Context context){super(context);}
    @Override public ResourceLocation getTextureLocation(BioGunProjectile entity){return TEXTURE;}
    @Override public void render(BioGunProjectile entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose();pose.mulPose(entityRenderDispatcher.cameraOrientation());float size=0.15F*(entity.power==1?1.25F:entity.power==2?2.5F:entity.power==3?4:1);pose.scale(size,size,size);
        var v=buffers.getBuffer(LegacyFxRenderTypes.sprite(TEXTURE,true,false));var m=pose.last().pose();
        for(var p:new float[][]{{-0.5F,-0.25F,0,1},{0.5F,-0.25F,1,1},{0.5F,0.75F,1,0},{-0.5F,0.75F,0,0}})v.vertex(m,p[0],p[1],0).color(1F,1F,1F,1F).uv(p[2],p[3]).endVertex();
        pose.popPose();
    }
}
