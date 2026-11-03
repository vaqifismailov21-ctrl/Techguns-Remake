package techguns.client.render.tileentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.models.machines.ModelTurretBase;
import techguns.tileentities.TurretTileEnt;

/** Original RenderTurret: the base model per mounting side, textured by the installed turret armor tier. */
public final class TurretBaseRenderer implements BlockEntityRenderer<TurretTileEnt> {
    public static final ResourceLocation DEFAULT_TEXTURE=ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/turret_base.png");
    private static final ModelTurretBase MODEL=new ModelTurretBase();
    public TurretBaseRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(TurretTileEnt tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        renderBase(tile.getFacing(),tile.getTurretBaseTexture(),pose,buffers,light,overlay);
    }
    /** Default (item) rendering is the upright base with the tier-0 texture, as for a null tile in the original. */
    public static void renderBase(Direction facing,ResourceLocation texture,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        pose.pushPose();
        switch(facing){
            case DOWN->{pose.translate(.5,-.5,.5);pose.mulPose(Axis.YP.rotationDegrees(180));}
            case EAST->{pose.translate(1.5,.5,.5);pose.mulPose(Axis.ZP.rotationDegrees(90));}
            case NORTH->{pose.translate(.5,.5,-.5);pose.mulPose(Axis.XP.rotationDegrees(90));}
            case SOUTH->{pose.translate(.5,.5,1.5);pose.mulPose(Axis.XP.rotationDegrees(-90));}
            case WEST->{pose.translate(-.5,.5,.5);pose.mulPose(Axis.ZP.rotationDegrees(-90));}
            default->{pose.translate(.5,1.5,.5);pose.mulPose(Axis.ZP.rotationDegrees(180));pose.mulPose(Axis.YP.rotationDegrees(180));}
        }
        MODEL.renderModel(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,overlay,ItemDisplayContext.NONE);
        pose.popPose();
    }
    @Override public boolean shouldRenderOffScreen(TurretTileEnt tile){return true;}
}
