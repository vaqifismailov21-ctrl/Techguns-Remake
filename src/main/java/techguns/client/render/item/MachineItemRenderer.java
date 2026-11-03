package techguns.client.render.item;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.item.*;
import techguns.client.render.tileentities.ProcessingMachineRenderer;
import techguns.machines.MachineBlock;
public final class MachineItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static MachineItemRenderer instance;
    private MachineItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    public static MachineItemRenderer get(){if(instance==null)instance=new MachineItemRenderer();return instance;}
    @Override public void renderByItem(ItemStack stack,ItemTransforms.TransformType context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){if(stack.getItem() instanceof BlockItem item&&item.getBlock() instanceof MachineBlock block){pose.pushPose();pose.translate(0.5,0.5,0.5);if(context==ItemTransforms.TransformType.GUI){pose.mulPose(Vector3f.XP.rotationDegrees(30));pose.mulPose(Vector3f.YP.rotationDegrees(45));}float scale=context==ItemTransforms.TransformType.GUI?0.625F:context==ItemTransforms.TransformType.GROUND?0.25F:0.4F;pose.scale(scale,scale,scale);pose.translate(-0.5,-0.5,-0.5);ProcessingMachineRenderer.renderMachine(block.kind,pose,buffers,light,overlay,0);pose.popPose();}}
}
