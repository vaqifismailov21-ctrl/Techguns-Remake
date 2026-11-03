package techguns.client.render.entities.projectiles;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import techguns.entities.projectiles.StoneBulletProjectile;
/** Original camera-facing handgunbullet quad, including its asymmetric vertical origin. */
public final class RenderStoneBullet extends EntityRenderer<StoneBulletProjectile> {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("techguns","textures/entity/handgunbullet.png");
    public RenderStoneBullet(EntityRendererProvider.Context context) {super(context);}
    @Override public ResourceLocation getTextureLocation(StoneBulletProjectile entity) {return TEXTURE;}
    @Override public void render(StoneBulletProjectile entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose();pose.mulPose(entityRenderDispatcher.cameraOrientation());pose.scale(0.1F,0.1F,0.1F);
        var v=buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));var m=pose.last();
        float[][] points={{-0.5F,-0.25F,0,1},{0.5F,-0.25F,1,1},{0.5F,0.75F,1,0},{-0.5F,0.75F,0,0}};
        for(var p:points)v.addVertex(m.pose(),p[0],p[1],0).setColor(255,255,255,255).setUv(p[2],p[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(m,0,0,1);
        pose.popPose();
    }
}
