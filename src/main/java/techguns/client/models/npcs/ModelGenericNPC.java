package techguns.client.models.npcs;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import techguns.client.models.*;
/** Original 64x64 ModelBiped UVs, with modern humanoid animation supplied by the renderer. */
public class ModelGenericNPC extends LegacyHumanoidGeometry {
    public ModelGenericNPC(){this(0,false);}
    public ModelGenericNPC(float inflation,boolean armor){super(inflation,0,64,armor?32:64);}
    private void angles(LegacyModelPart to,ModelPart from){to.rotateAngleX=from.xRot;to.rotateAngleY=from.yRot;to.rotateAngleZ=from.zRot;to.showModel=from.visible;}
    public void syncNpc(HumanoidModel<?> model){
        angles(bipedHead,model.head);angles(bipedHeadwear,model.hat);angles(bipedBody,model.body);
        angles(bipedRightArm,model.rightArm);angles(bipedLeftArm,model.leftArm);angles(bipedRightLeg,model.rightLeg);angles(bipedLeftLeg,model.leftLeg);
    }
}
