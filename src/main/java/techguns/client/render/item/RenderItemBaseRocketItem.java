package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.models.ModelMultipart;

public final class RenderItemBaseRocketItem extends RenderItemBase {
    public RenderItemBaseRocketItem(ModelMultipart model, ResourceLocation texture) { super(model, texture); }

    @Override
    protected void setBaseRotation(PoseStack pose, ItemDisplayContext context) {
        pose.mulPose(Axis.XP.rotationDegrees(-180));
        pose.mulPose(Axis.YP.rotationDegrees(-90));
        pose.mulPose(Axis.ZP.rotationDegrees(-90));
    }
}
