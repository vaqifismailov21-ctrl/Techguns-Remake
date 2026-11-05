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
/** Original RenderArmorItem transforms and full armor geometry for the powered suit icons. GUI uses the same slot space as 1.12 (16 units, y up). */
public final class ArmorItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static ArmorItemRenderer instance;
    private final java.util.Map<TGSuitArmor,LegacyHumanoidGeometry> models=new java.util.HashMap<>();
    private ArmorItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    public static ArmorItemRenderer get(){if(instance==null)instance=new ArmorItemRenderer();return instance;}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!(stack.getItem() instanceof TGSuitArmor armor)||!armor.use3dIcon)return;
        var model=models.computeIfAbsent(armor,a->newModel(family(a.modelKey),a.slot==EquipmentSlot.LEGS?1:0));
        var slot=armor.slot;
        model.bipedHead.showModel=model.bipedHeadwear.showModel=slot==EquipmentSlot.HEAD;
        model.bipedBody.showModel=slot==EquipmentSlot.CHEST||slot==EquipmentSlot.LEGS;
        model.bipedRightArm.showModel=model.bipedLeftArm.showModel=slot==EquipmentSlot.CHEST;
        model.bipedRightLeg.showModel=model.bipedLeftLeg.showModel=slot==EquipmentSlot.LEGS||slot==EquipmentSlot.FEET;
        var texture=ResourceLocation.fromNamespaceAndPath("techguns","textures/models/armor/"+armor.textures[0]+".png");
        pose.pushPose();
        if(context==ItemDisplayContext.GUI){
            // Each piece fills the slot, centred, as the 1.12 icons did (the raw 1.12 numbers overflowed under the 1.20 GUI).
            var mesh=new techguns.client.models.MeshSnapshot();var capture=new PoseStack();transforms(capture,context,slot);model.child=false;model.renderModel(capture,mesh,light,overlay,context);
            if(!mesh.vertices.isEmpty()){
                float fit=.92F/Math.max(1e-4F,Math.max(mesh.maxX-mesh.minX,mesh.maxY-mesh.minY));pose.translate(.5,.5,.5);pose.scale(fit,fit,fit);pose.translate(-mesh.centerX(),-mesh.centerY(),-mesh.centerZ());
                mesh.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,overlay,1,1,1,1);
            }
            pose.popPose();return;
        }
        transforms(pose,context,slot);
        // 1.12 drew the item model with ModelBase.isChild left at its default (true); the original scales and translateBase values are for that layout
        model.child=true;
        model.renderModel(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,overlay,context);pose.popPose();
    }
    private final java.util.Map<String,Float> fits=new java.util.HashMap<>();
    private static String family(String key){return key.startsWith("steam")?"steam":key.startsWith("power_armor_mk2")?"mk2":"t3";}
    private static LegacyHumanoidGeometry newModel(String family,int type){return family.equals("steam")?new ModelSteamArmor(type):family.equals("mk2")?new ModelT4PowerArmorMk2(type):new ModelT3PowerArmor(type);}
    /** Slot scale of a suit: its chestplate fills 0.95 of the slot. */
    private float familyFit(TGSuitArmor armor){return fits.computeIfAbsent(family(armor.modelKey),f->{
        var chest=newModel(f,0);chest.bipedHead.showModel=chest.bipedHeadwear.showModel=false;chest.bipedRightLeg.showModel=chest.bipedLeftLeg.showModel=false;
        chest.bipedBody.showModel=chest.bipedRightArm.showModel=chest.bipedLeftArm.showModel=true;
        var mesh=new techguns.client.models.MeshSnapshot();var capture=new PoseStack();transforms(capture,ItemDisplayContext.GUI,EquipmentSlot.CHEST);
        chest.renderModel(capture,mesh,15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,ItemDisplayContext.GUI);
        float size=Math.max(mesh.maxX-mesh.minX,mesh.maxY-mesh.minY);return size>0?.95F/size:1F;});}
    private static void transforms(PoseStack pose,ItemDisplayContext context,EquipmentSlot slot){
        pose.translate(0.5,0.5,0.5);
        if(context.firstPerson())pose.translate(0,0.1,0);else if(context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)pose.translate(0,0.04,0);else if(context==ItemDisplayContext.FIXED)pose.translate(0,0,0.07);
        if(context==ItemDisplayContext.GUI)pose.mulPose(Axis.YP.rotationDegrees(180));
        float scale=switch(context){case GUI,FIXED->1.5F;case GROUND->slot==EquipmentSlot.HEAD?1.25F:1.5F;default->1;};
        pose.scale(scale,scale,scale);pose.mulPose(Axis.XP.rotationDegrees(-180));pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.translate(0,switch(slot){case HEAD->-0.54;case CHEST->-0.9;case LEGS->-1.29;case FEET->-1.42;default->0;},0);
    }
}
