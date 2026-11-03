package techguns.client.render.entities.projectiles;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import techguns.client.render.fx.LegacyFxRenderTypes;
import techguns.entities.projectiles.AdvancedBulletProjectile;

/** Original blue additive tracer, 32 by 4 model units and speed-dependent visibility delay. */
public final class RenderAdvancedBulletProjectile extends EntityRenderer<AdvancedBulletProjectile> {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("techguns","textures/entity/bullet_blue.png");
    public RenderAdvancedBulletProjectile(EntityRendererProvider.Context context){super(context);}
    @Override public ResourceLocation getTextureLocation(AdvancedBulletProjectile entity){return TEXTURE;}
    @Override public void render(AdvancedBulletProjectile entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        if(entity.tickCount+partial<=2.5F/entity.speed)return;
        pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(entity.getYRot()-90));pose.mulPose(Axis.ZP.rotationDegrees(entity.getXRot()));pose.mulPose(Axis.XP.rotationDegrees(45));pose.scale(.0125F,.0125F,.0125F);
        var vertices=buffers.getBuffer(LegacyFxRenderTypes.sprite(TEXTURE,true,false));
        for(int i=0;i<4;i++){pose.mulPose(Axis.XP.rotationDegrees(90));vertex(vertices,pose,-16,-2,0,0);vertex(vertices,pose,16,-2,1,0);vertex(vertices,pose,16,2,1,1);vertex(vertices,pose,-16,2,0,1);}
        pose.popPose();
    }
    private static void vertex(VertexConsumer vertices,PoseStack pose,float x,float y,float u,float v){vertices.vertex(pose.last().pose(),x,y,0).color(1F,1F,1F,1F).uv(u,v).endVertex();}
}
