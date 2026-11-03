package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;
import techguns.client.render.tileentities.TurretBaseRenderer;

/** The turret item is the tile renderer without a tile (upright, default texture); transforms come from the original basicmachine item model. */
public final class TurretItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static TurretItemRenderer instance;
    private TurretItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    public static TurretItemRenderer get(){if(instance==null)instance=new TurretItemRenderer();return instance;}
    @Override public void renderByItem(ItemStack stack,ItemTransforms.TransformType context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        TurretBaseRenderer.renderBase(Direction.UP,TurretBaseRenderer.DEFAULT_TEXTURE,pose,buffers,light,overlay);
    }
}
