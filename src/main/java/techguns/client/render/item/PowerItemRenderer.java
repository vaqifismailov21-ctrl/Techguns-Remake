package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import techguns.client.render.tileentities.PowerRenderer;
import techguns.power.EnergyCellBlock;
import techguns.power.EnergyCellItem;

/** Power block items: the block's box model, front turned toward the viewer in the inventory. */
public final class PowerItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static PowerItemRenderer instance;
    private PowerItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    public static PowerItemRenderer get(){if(instance==null)instance=new PowerItemRenderer();return instance;}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!(stack.getItem() instanceof BlockItem item))return;
        var state=item.getBlock().defaultBlockState();
        int charge=stack.getItem() instanceof EnergyCellItem cell?EnergyCellBlock.chargeLevel(EnergyCellItem.stored(stack),cell.capacity):0;
        pose.pushPose();pose.translate(0.5,0.5,0.5);
        if(context==ItemDisplayContext.GUI){pose.mulPose(Axis.XP.rotationDegrees(30));pose.mulPose(Axis.YP.rotationDegrees(225));}
        else if(context!=ItemDisplayContext.FIXED)pose.mulPose(Axis.YP.rotationDegrees(180));
        float scale=context==ItemDisplayContext.GUI?0.625F:context==ItemDisplayContext.GROUND?0.25F:context==ItemDisplayContext.FIXED?0.5F:0.4F;
        pose.scale(scale,scale,scale);pose.translate(-0.5,-0.5,-0.5);
        PowerRenderer.draw(state,pose,buffers,light,overlay,false,charge,0.6F,0.6F);
        pose.popPose();
    }
}
