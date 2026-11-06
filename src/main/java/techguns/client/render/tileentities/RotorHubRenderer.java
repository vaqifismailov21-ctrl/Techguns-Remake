package techguns.client.render.tileentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import techguns.power.plant.PlantPartBlock;
import techguns.power.plant.RotorHubBlock;
import techguns.power.plant.RotorHubEntity;

/** Blades hung on a rotor hub that is not part of a running plant yet (the formed plant draws its own turning rotor). */
public final class RotorHubRenderer implements BlockEntityRenderer<RotorHubEntity> {
    public RotorHubRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(RotorHubEntity hub,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var state=hub.getBlockState();
        if(!(state.getBlock() instanceof RotorHubBlock)||state.getValue(PlantPartBlock.FORMED))return;
        int blades=state.getValue(RotorHubBlock.BLADES);if(blades==0)return;
        pose.pushPose();pose.translate(0.5,0,0.5);pose.mulPose(Axis.YP.rotationDegrees(180-state.getValue(RotorHubBlock.FACING).toYRot()));
        pose.translate(0,0.42F,0.08F);
        PlantRenderer.rotor(pose,buffers,light,0,blades,false);
        pose.popPose();
    }
    @Override public boolean shouldRenderOffScreen(RotorHubEntity hub){return true;}
}
