package techguns.client.render.item;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import techguns.client.models.LegacyHumanoidGeometry;
import techguns.client.models.armor.*;
import techguns.items.armors.TGSuitArmor;
/** Original RenderArmorItem transforms and full armor geometry for the twelve powered suit icons. */
public final class ArmorItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static ArmorItemRenderer instance;
    private final java.util.Map<TGSuitArmor,LegacyHumanoidGeometry> models=new java.util.HashMap<>();
    private ArmorItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    public static ArmorItemRenderer get(){if(instance==null)instance=new ArmorItemRenderer();return instance;}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!(stack.getItem() instanceof TGSuitArmor armor)||!armor.use3dIcon)return;
        var model=models.computeIfAbsent(armor,a->{int type=a.slot==EquipmentSlot.LEGS?1:0;return a.modelKey.startsWith("steam")?new ModelSteamArmor(type):a.modelKey.startsWith("power_armor_mk2")?new ModelT4PowerArmorMk2(type):new ModelT3PowerArmor(type);});
        var slot=armor.slot;
        model.bipedHead.showModel=model.bipedHeadwear.showModel=slot==EquipmentSlot.HEAD;
        model.bipedBody.showModel=slot==EquipmentSlot.CHEST||slot==EquipmentSlot.LEGS;
        model.bipedRightArm.showModel=model.bipedLeftArm.showModel=slot==EquipmentSlot.CHEST;
        model.bipedRightLeg.showModel=model.bipedLeftLeg.showModel=slot==EquipmentSlot.LEGS||slot==EquipmentSlot.FEET;
        pose.pushPose();
        if(context==ItemDisplayContext.GUI){
            // Legacy GUI scaling ran under a different projection. Fit the actual mesh to a 14px icon.
            var mesh=new techguns.client.models.MeshSnapshot();var capture=new PoseStack();
            transforms(capture,context,slot);model.renderModel(capture,mesh,light,overlay,context);
            if(!mesh.vertices.isEmpty()){
                float fit=.875F/Math.max(mesh.maxX-mesh.minX,mesh.maxY-mesh.minY);
                pose.translate(.5,.5,.5);pose.scale(fit,fit,fit);pose.translate(-mesh.centerX(),-mesh.centerY(),-mesh.centerZ());
                var texture=ResourceLocation.fromNamespaceAndPath("techguns","textures/models/armor/"+armor.textures[0]+".png");
                mesh.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,overlay,1,1,1,1);
            }
            pose.popPose();return;
        }
        transforms(pose,context,slot);
        var texture=ResourceLocation.fromNamespaceAndPath("techguns","textures/models/armor/"+armor.textures[0]+".png");
        model.renderModel(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,overlay,context);pose.popPose();
    }
    private static void transforms(PoseStack pose,ItemDisplayContext context,EquipmentSlot slot){
        pose.translate(0.5,0.5,0.5);
        if(context.firstPerson())pose.translate(0,0.1,0);else if(context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)pose.translate(0,0.04,0);else if(context==ItemDisplayContext.FIXED)pose.translate(0,0,0.07);
        if(context==ItemDisplayContext.GUI)pose.mulPose(Axis.YP.rotationDegrees(180));
        // The item models are builtin/entity without a "ground" display transform, so dropped suit parts were drawn at
        // full body size (x1.5). On the ground they now have the size of an ordinary dropped item.
        float scale=switch(context){case GUI,FIXED->1.5F;case GROUND->slot==EquipmentSlot.HEAD?.5F:.6F;default->1;};
        pose.scale(scale,scale,scale);pose.mulPose(Axis.XP.rotationDegrees(-180));pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.translate(0,switch(slot){case HEAD->-0.54;case CHEST->-0.9;case LEGS->-1.29;case FEET->-1.42;default->0;},0);
    }
}
