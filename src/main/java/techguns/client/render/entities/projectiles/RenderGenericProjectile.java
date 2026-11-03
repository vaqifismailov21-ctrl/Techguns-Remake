package techguns.client.render.entities.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import techguns.Techguns;
import techguns.entities.projectiles.GenericProjectile;

/** Original four crossed bullet1 quads, size, orientation and initial visibility delay. */
public final class RenderGenericProjectile<T extends GenericProjectile> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Techguns.MODID, "textures/entity/bullet1.png");
    public RenderGenericProjectile(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(T entity) { return TEXTURE; }
    @Override public void render(T entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        if (entity.tickCount < 2 && partialTick <= 0.25F) return;
        pose.pushPose();
        pose.mulPose(Vector3f.YP.rotationDegrees(entity.getYRot() - 90));
        pose.mulPose(Vector3f.ZP.rotationDegrees(entity.getXRot()));
        pose.mulPose(Vector3f.XP.rotationDegrees(45));
        pose.scale(0.0125F, 0.0125F, 0.0125F);
        var consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        for (int i = 0; i < 4; i++) {
            pose.mulPose(Vector3f.XP.rotationDegrees(90));
            vertex(consumer, pose, -8, -2, 0, 0); vertex(consumer, pose, 8, -2, 1, 0);
            vertex(consumer, pose, 8, 2, 1, 1); vertex(consumer, pose, -8, 2, 0, 1);
        }
        pose.popPose();
    }
    private static void vertex(VertexConsumer consumer, PoseStack pose, float x, float y, float u, float v) {
        consumer.vertex(pose.last().pose(), x, y, 0).color(255, 255, 255, 255).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(pose.last().normal(), 0, 0, 1).endVertex();
    }
}
