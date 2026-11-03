package techguns.client.render.entities.npcs;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.models.npcs.*;
import techguns.entities.npcs.*;

public final class NpcModel extends HumanoidModel<GenericNPC> implements techguns.client.models.LegacyDeathModel {
    private final ModelGenericNPC geometry;
    private GenericNPC holder;
    public NpcModel(ModelPart root,ModelGenericNPC geometry){super(root);this.geometry=geometry;}
    @Override public void setupAnim(GenericNPC npc,float limb,float amount,float age,float yaw,float pitch){
        holder=npc;leftArmPose=rightArmPose=ArmPose.EMPTY;
        if(!npc.getMainHandItem().isEmpty()){
            var pose=npc.getMainHandItem().getItem() instanceof techguns.items.guns.GenericGun&&npc.getHasAimedBowAnim()?ArmPose.BOW_AND_ARROW:ArmPose.ITEM;
            if(npc.isLeftHanded())leftArmPose=pose;else rightArmPose=pose;
        }
        super.setupAnim(npc,limb,amount,age,yaw,pitch);geometry.syncNpc(this);
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer vertices,int light,int overlay,int tgColor){float r=net.minecraft.util.FastColor.ARGB32.red(tgColor)/255F,g=net.minecraft.util.FastColor.ARGB32.green(tgColor)/255F,b=net.minecraft.util.FastColor.ARGB32.blue(tgColor)/255F,alpha=net.minecraft.util.FastColor.ARGB32.alpha(tgColor)/255F;
        geometry.renderModel(pose,new techguns.client.models.TintedVertexConsumer(vertices,r,g,b,alpha),light,overlay,ItemDisplayContext.NONE,holder,0,0,0,0,0);
    }
    @Override public java.util.List<techguns.client.models.MeshSnapshot> deathParts(PoseStack pose){return geometry.captureParts(vertices->renderToBuffer(pose,vertices,0xf000f0,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,-1));}
    @Override public void translateToHand(HumanoidArm hand,PoseStack pose){
        // Mutants retain the original limb pivots and model scale for held items as well as their body.
        float scale=holder instanceof SuperMutantBasic mutant?mutant.getModelScale():1;
        var arm=hand==HumanoidArm.RIGHT?geometry.bipedRightArm:geometry.bipedLeftArm;
        pose.translate(arm.rotationPointX*scale/16,arm.rotationPointY*scale/16,arm.rotationPointZ*scale/16);
        if(arm.rotateAngleZ!=0)pose.mulPose(com.mojang.math.Axis.ZP.rotation(arm.rotateAngleZ));
        if(arm.rotateAngleY!=0)pose.mulPose(com.mojang.math.Axis.YP.rotation(arm.rotateAngleY));
        if(arm.rotateAngleX!=0)pose.mulPose(com.mojang.math.Axis.XP.rotation(arm.rotateAngleX));
    }
}
