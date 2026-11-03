package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import techguns.client.models.ModelMultipart;

public final class RenderItemLMGMag extends RenderItemBase {
    public RenderItemLMGMag(ModelMultipart model, ResourceLocation texture) { super(model, texture); }

    @Override
    protected void setBaseRotation(PoseStack pose, ItemTransforms.TransformType context) {
        pose.mulPose(Vector3f.XP.rotationDegrees(-180));
        pose.mulPose(Vector3f.YP.rotationDegrees(context == ItemTransforms.TransformType.GUI || context == ItemTransforms.TransformType.FIXED ? 90 : 180));
    }
}
