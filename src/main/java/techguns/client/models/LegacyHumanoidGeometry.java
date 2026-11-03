package techguns.client.models;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.client.renderer.block.model.ItemTransforms;
/** ModelBiped geometry adapter. The actual animation pose comes from the modern humanoid model. */
public class LegacyHumanoidGeometry extends ModelMultipart {
    public LegacyModelPart bipedHead,bipedHeadwear,bipedBody,bipedRightArm,bipedLeftArm,bipedRightLeg,bipedLeftLeg;
    public LegacyHumanoidGeometry(){this(0);}
    public LegacyHumanoidGeometry(float scale){this(scale,0,64,32);}
    public LegacyHumanoidGeometry(float scale,float offset,int width,int height){
        textureWidth=width;textureHeight=height;
        bipedHead=part(0,0,0,offset,0,-4,-8,-4,8,8,8,scale,false);bipedHeadwear=part(32,0,0,offset,0,-4,-8,-4,8,8,8,scale+0.5F,false);
        bipedBody=part(16,16,0,offset,0,-4,0,-2,8,12,4,scale,false);
        bipedRightArm=part(40,16,-5,2+offset,0,-3,-2,-2,4,12,4,scale,false);bipedLeftArm=part(40,16,5,2+offset,0,-1,-2,-2,4,12,4,scale,true);
        bipedRightLeg=part(0,16,-1.9F,12+offset,0,-2,0,-2,4,12,4,scale,false);bipedLeftLeg=part(0,16,1.9F,12+offset,0,-2,0,-2,4,12,4,scale,true);
    }
    private LegacyModelPart part(int u,int v,float x,float y,float z,float bx,float by,float bz,int sx,int sy,int sz,float scale,boolean mirror){var p=new LegacyModelPart(this,u,v);p.mirror=mirror;p.setRotationPoint(x,y,z);p.addBox(bx,by,bz,sx,sy,sz,scale);return p;}
    private void copy(LegacyModelPart target,ModelPart from,boolean visible){target.rotationPointX=from.x;target.rotationPointY=from.y;target.rotationPointZ=from.z;target.rotateAngleX=from.xRot;target.rotateAngleY=from.yRot;target.rotateAngleZ=from.zRot;target.showModel=visible;}
    public void sync(HumanoidModel<?> model,EquipmentSlot slot){
        copy(bipedHead,model.head,slot==EquipmentSlot.HEAD);copy(bipedHeadwear,model.hat,slot==EquipmentSlot.HEAD);
        copy(bipedBody,model.body,slot==EquipmentSlot.CHEST||slot==EquipmentSlot.LEGS);
        copy(bipedRightArm,model.rightArm,slot==EquipmentSlot.CHEST);copy(bipedLeftArm,model.leftArm,slot==EquipmentSlot.CHEST);
        copy(bipedRightLeg,model.rightLeg,slot==EquipmentSlot.LEGS||slot==EquipmentSlot.FEET);copy(bipedLeftLeg,model.leftLeg,slot==EquipmentSlot.LEGS||slot==EquipmentSlot.FEET);
    }
    @Override public void render(Entity entity,float a,float b,float c,float d,float e,float scale,int ammo,float reload,ItemTransforms.TransformType context,int part,float fire,float charge){
        render(entity,a,b,c,d,e,scale);
    }
    public void render(Entity entity,float a,float b,float c,float d,float e,float scale){
        for(var bone:new LegacyModelPart[]{bipedHead,bipedHeadwear,bipedBody,bipedRightArm,bipedLeftArm,bipedRightLeg,bipedLeftLeg})bone.render(scale);
    }
}
