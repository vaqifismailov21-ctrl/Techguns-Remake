package techguns.client.render.tileentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import techguns.power.plant.PlantPartBlock;
import techguns.power.plant.RotorHubBlock;
import techguns.power.plant.RotorHubEntity;

/** Blades hung on a rotor hub that is not part of a running plant yet (the formed plant draws its own turning rotor). */
public final class RotorHubRenderer implements BlockEntityRenderer<RotorHubEntity> {
    private static final ResourceLocation BLADE=ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/plant/turbine_blade.png");
    public RotorHubRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(RotorHubEntity hub,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var state=hub.getBlockState();
        if(!(state.getBlock() instanceof RotorHubBlock)||state.getValue(PlantPartBlock.FORMED))return;
        int blades=state.getValue(RotorHubBlock.BLADES);if(blades==0)return;
        pose.pushPose();pose.translate(0.5,0,0.5);pose.mulPose(Axis.YP.rotationDegrees(180-state.getValue(RotorHubBlock.FACING).toYRot()));
        pose.translate(0,0.42F,0.25F);
        var vc=buffers.getBuffer(RenderType.entityCutout(BLADE));
        for(int k=0;k<blades;k++){
            pose.pushPose();pose.mulPose(Axis.ZP.rotationDegrees(120*k));
            PlantRenderer.box(pose,vc,-0.17F,0.18F,-0.06F,0.17F,1.6F,0.06F,light,0xFFFFFFFF);
            PlantRenderer.box(pose,vc,-0.11F,1.6F,-0.04F,0.11F,4.3F,0.04F,light,0xFFFFFFFF);
            pose.popPose();
        }
        pose.popPose();
    }
    @Override public boolean shouldRenderOffScreen(RotorHubEntity hub){return true;}
}
