package techguns.client.render.entities.npcs;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import techguns.Techguns;
import techguns.entities.npcs.*;
import techguns.client.models.npcs.*;

public final class NpcRenderer extends HumanoidMobRenderer<GenericNPC,NpcModel> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"npc"),"main");
    public NpcRenderer(EntityRendererProvider.Context context,String id){
        super(context,new NpcModel(context.bakeLayer(LAYER),id.equals("cyberdemon")?new ModelCyberDemon():id.startsWith("supermutant")?new ModelSuperMutant():id.equals("skeletonsoldier")?new ModelSkeletonSoldier():new ModelGenericNPC()),.5f);
        layers.removeIf(layer->layer instanceof ItemInHandLayer<?,?>);
        addLayer(new ItemInHandLayer<GenericNPC,NpcModel>(this,context.getItemInHandRenderer()){
            @Override protected void renderArmWithItem(LivingEntity entity,ItemStack stack,ItemDisplayContext display,HumanoidArm hand,PoseStack pose,MultiBufferSource buffers,int light){
                if(stack.isEmpty())return;
                pose.pushPose();getParentModel().translateToHand(hand,pose);
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
                boolean left=hand==HumanoidArm.LEFT;pose.translate((left?-1:1)/16f,.125,-.625);
                var shooter=(GenericNPC)entity;pose.translate(left?-shooter.getWeaponPosX():shooter.getWeaponPosX(),shooter.getWeaponPosY(),shooter.getWeaponPosZ());
                float scale=shooter.getGunScale();pose.scale(scale,scale,scale);
                context.getItemInHandRenderer().renderItem(entity,stack,display,left,pose,buffers,light);pose.popPose();
            }
        });
        if(!id.equals("cyberdemon"))addLayer(new HumanoidArmorLayer<>(this,new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),context.getModelManager()));
    }
    public static LayerDefinition layer(){return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE,0),64,64);}
    @Override public ResourceLocation getTextureLocation(GenericNPC npc){
        String id=BuiltInRegistries.ENTITY_TYPE.getKey(npc.getType()).getPath();
        var textures=NpcTextures.VALUES.get(id);int index=npc instanceof SuperMutantBasic mutant?mutant.gettype():0;
        return ResourceLocation.parse(textures[index]);
    }
    @Override public void render(GenericNPC npc,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();if(npc instanceof SuperMutantBasic mutant)pose.translate(0,mutant.getModelHeightOffset(),0);
        super.render(npc,yaw,partial,pose,buffers,light);pose.popPose();
    }
}
