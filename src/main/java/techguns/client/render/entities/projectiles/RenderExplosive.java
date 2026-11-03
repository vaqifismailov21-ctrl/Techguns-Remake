package techguns.client.render.entities.projectiles;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import techguns.TGEntities;
import techguns.client.models.*;
import techguns.client.models.guns.*;
import techguns.client.models.projectiles.ModelRocket;
import techguns.entities.projectiles.*;
/** Original rocket, thrown-grenade and 40 mm mesh transforms. */
public final class RenderExplosive<T extends GenericProjectile> extends EntityRenderer<T> {
    private final ModelMultipart rocket=new ModelRocket(),stick=new ModelStielgranate(),frag=new ModelFragGrenade(false),shell=new LegacyObjModel(false,false,"grenade40mm");
    public RenderExplosive(EntityRendererProvider.Context context) {super(context);}
    @Override public ResourceLocation getTextureLocation(T entity) {
        String texture=entity instanceof RocketProjectile r?r.isNuclear()?"guns/rocket_nuke":r.isHighVelocity()?"guns/rocket_hv":"guns/rocket":entity.getType()==TGEntities.GRENADE_40MM.get()?"entity/launchergrenade":entity.getType()==TGEntities.FRAG_GRENADE.get()?"guns/frag_grenade_texture":"guns/stielgranate";
        return new ResourceLocation("techguns","textures/"+texture+".png");
    }
    @Override public void render(T entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose();boolean launched=entity.getType()==TGEntities.GRENADE_40MM.get(),isRocket=entity instanceof RocketProjectile;
        pose.mulPose(Vector3f.YP.rotationDegrees((launched?entity.getYRot():Mth.lerp(partial,entity.yRotO,entity.getYRot()))-90));
        pose.mulPose(Vector3f.ZP.rotationDegrees(launched?entity.getXRot():Mth.lerp(partial,entity.xRotO,entity.getXRot())));
        ModelMultipart model;
        if(isRocket) {pose.scale(0.9F,0.9F,0.9F);model=rocket;}
        else if(launched) {pose.mulPose(Vector3f.XP.rotationDegrees(180));pose.scale(0.5F,0.5F,0.5F);model=shell;}
        else {
            pose.mulPose(Vector3f.ZP.rotationDegrees(-360*((entity.tickCount%20+partial)/20F)));pose.scale(0.8F,0.8F,0.8F);
            pose.mulPose(Vector3f.XP.rotationDegrees(-180));pose.mulPose(Vector3f.YP.rotationDegrees(180));pose.translate(-0.03125,0.83,-0.0625);
            model=entity.getType()==TGEntities.FRAG_GRENADE.get()?frag:stick;
        }
        model.renderModel(pose,buffers.getBuffer(RenderType.entityCutout(getTextureLocation(entity))),light,OverlayTexture.NO_OVERLAY,ItemTransforms.TransformType.NONE);
        pose.popPose();
    }
}
