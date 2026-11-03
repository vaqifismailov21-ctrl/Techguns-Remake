package techguns.client.render.entities.npcs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.client.models.npcs.ModelTurret;
import techguns.entities.npcs.NPCTurret;
import techguns.items.guns.TurretGunOffsets;

/**
 * Original RenderNPCTurret: stand (part 0) turned by the head yaw, mount (part 1) additionally pitched around 0.9,
 * the base's gun rendered in third-person right hand with its per-gun turret offset. No shadow, hurt tint or name tag.
 */
public final class NPCTurretRenderer extends EntityRenderer<NPCTurret> {
    private static final ModelTurret MODEL=new ModelTurret();
    private static final float GUN_OFFSET_Y=0.85f;
    public NPCTurretRenderer(EntityRendererProvider.Context context){super(context);shadowRadius=0f;}
    @Override public ResourceLocation getTextureLocation(NPCTurret turret){return turret.getTexture();}
    @Override public void render(NPCTurret turret,float entityYaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();
        var tile=turret.getTile();
        Direction facing=tile!=null?tile.getFacing():Direction.UP;
        float mirrorYaw=1,mirrorPitch=1,yawOffset=0;
        if(facing==Direction.DOWN){mirrorYaw=-1;mirrorPitch=-1;yawOffset=180;}
        if(facing==Direction.UP)pose.translate(0,1.5,0);else if(facing==Direction.DOWN)pose.translate(0,-0.6,0);
        if(facing==Direction.UP)pose.mulPose(Vector3f.XP.rotationDegrees(180));
        pose.mulPose(Vector3f.YP.rotationDegrees(turret.getYHeadRot()*mirrorYaw+yawOffset));
        var vertices=buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(turret)));
        MODEL.renderModel(pose,vertices,light,OverlayTexture.NO_OVERLAY,ItemTransforms.TransformType.FIXED,turret,0,0,0,0,0);
        float rotoffset=0.9f;
        pose.translate(0,rotoffset,0);
        pose.mulPose(Vector3f.XP.rotationDegrees(turret.getXRot()*mirrorPitch));
        pose.translate(0,-rotoffset,0);
        MODEL.renderModel(pose,vertices,light,OverlayTexture.NO_OVERLAY,ItemTransforms.TransformType.FIXED,turret,0,0,1,0,0);
        var gun=turret.getMainHandItem();
        if(!gun.isEmpty()){
            float[] offset=TurretGunOffsets.get(ForgeRegistries.ITEMS.getKey(gun.getItem()).getPath());
            pose.pushPose();
            pose.translate(offset[0],offset[1]+GUN_OFFSET_Y,offset[2]);
            pose.mulPose(Vector3f.XP.rotationDegrees(180));
            pose.mulPose(Vector3f.YP.rotationDegrees(180));
            Minecraft.getInstance().getItemRenderer().renderStatic(turret,gun,ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND,false,pose,buffers,turret.level,light,OverlayTexture.NO_OVERLAY,turret.getId());
            pose.popPose();
        }
        pose.popPose();
    }
}
