package techguns.client.render.tileentities;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import techguns.client.models.*;
import techguns.client.models.machines.*;
import techguns.machines.*;
public final class ProcessingMachineRenderer implements BlockEntityRenderer<ProcessingMachine> {
    private static final ModelMachine ammo=new ModelAmmoPress(),metal=new ModelMetalPress(),chem=new ModelChemLab();
    private static final ModelMachine fabricator=new ModelFabricator();
    private static final LegacyObjModel charger=new LegacyObjModel(true,false,"charging_station");
    public ProcessingMachineRenderer(BlockEntityRendererProvider.Context context){}
    public static void renderMachine(MachineKind kind,PoseStack pose,MultiBufferSource buffers,int light,int overlay,float progress){
        if(kind==MachineKind.BLAST_FURNACE||kind==MachineKind.GRINDER||kind==MachineKind.FABRICATOR)return;
        pose.pushPose();
        if(kind==MachineKind.CHARGING_STATION){charger.renderModel(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation("techguns","textures/blocks/charging_station.png"))),light,overlay,ItemTransforms.TransformType.NONE);}
        else {pose.translate(0.5,1.5,0.5);pose.mulPose(Vector3f.ZP.rotationDegrees(180));pose.mulPose(Vector3f.YP.rotationDegrees(180));var model=kind==MachineKind.AMMO_PRESS?ammo:kind==MachineKind.CHEM_LAB?chem:metal;var texture=new ResourceLocation("techguns","textures/blocks/"+(kind==MachineKind.AMMO_PRESS?"ammopress":kind==MachineKind.CHEM_LAB?"chemlab":"metalpress")+".png");model.setLayers((alpha,doubleSided)->buffers.getBuffer(alpha?RenderType.entityTranslucent(texture):RenderType.entityCutoutNoCull(texture)));model.renderModel(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,overlay,ItemTransforms.TransformType.NONE,null,0,progress,0,0,0);}
        pose.popPose();
    }
    @Override public void render(ProcessingMachine machine,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(machine.kind==MachineKind.REACTION){ReactionMachineRenderer.render(machine,partial,pose,buffers,light,overlay);return;}
        if(machine.kind==MachineKind.FABRICATOR){renderFabricator(machine,pose,buffers,light,overlay);return;}
        pose.pushPose();pose.translate(0.5,0,0.5);pose.mulPose(Vector3f.YP.rotationDegrees(-machine.getBlockState().getValue(MachineBlock.FACING).toYRot()+180));pose.translate(-0.5,0,-0.5);
        renderMachine(machine.kind,pose,buffers,light,overlay,machine.totalTime>0?machine.progress/(float)machine.totalTime:0);
        if(machine.kind==MachineKind.CHARGING_STATION){var item=machine.running()?machine.pending():machine.inventory.getStackInSlot(0);if(!item.isEmpty()){pose.translate(0.5,0.4,0.5);Minecraft.getInstance().getItemRenderer().renderStatic(item,ItemTransforms.TransformType.GROUND,light,overlay,pose,buffers,0);}}
        pose.popPose();
    }
    private static void renderFabricator(ProcessingMachine machine,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!machine.formed)return;float progress=machine.totalTime>0?machine.progress/(float)machine.totalTime:0;
        pose.pushPose();pose.translate(0.5,1.5,0.5);pose.mulPose(Vector3f.ZP.rotationDegrees(180));pose.mulPose(Vector3f.YP.rotationDegrees(180+machine.getBlockState().getValue(MachineBlock.FACING).toYRot()));
        var texture=new ResourceLocation("techguns","textures/blocks/fabricator.png");fabricator.setLayers((alpha,doubleSided)->buffers.getBuffer(alpha?RenderType.entityTranslucent(texture):RenderType.entityCutoutNoCull(texture)));fabricator.renderModel(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,overlay,ItemTransforms.TransformType.NONE,null,0,progress,0,0,0);
        ItemStack item=progress<0.8F?machine.consumed(3):machine.pending();if(item.isEmpty()&&progress<0.8F)item=machine.consumed(0);if(!item.isEmpty()){pose.pushPose();pose.mulPose(Vector3f.XP.rotationDegrees(90));pose.translate(-0.5,0,-0.6);pose.mulPose(Vector3f.YP.rotationDegrees(93.5F));Minecraft.getInstance().getItemRenderer().renderStatic(techguns.util.Compat.copyWithCount(item, 1),ItemTransforms.TransformType.GROUND,light,overlay,pose,buffers,0);pose.popPose();}pose.popPose();
    }
}
