package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import techguns.client.models.ModelMultipart;

/** Original material renderer transforms, expressed with PoseStack instead of OpenGL globals. */
public class RenderItemBase {
    protected final ModelMultipart model;
    protected final ResourceLocation texture;
    protected float baseScale = 1.0F;
    protected float firstPersonScale = 0.5F;
    protected float guiScale = 0.4F;
    protected float groundScale = 0.5F;
    protected float frameScale=0.5F;
    protected float[] baseTranslation = {0, 0, 0};
    protected float[][] translations = {{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,-0.05F}};

    public RenderItemBase(ModelMultipart model, ResourceLocation texture) {
        this.model = model;
        this.texture = texture;
    }

    public RenderItemBase setBaseScale(float scale) { baseScale = scale; return this; }
    public RenderItemBase setGUIScale(float scale) { guiScale = scale; return this; }
    public RenderItemBase setFirstPersonScale(float scale) { firstPersonScale = scale; return this; }
    public RenderItemBase setBaseTranslation(float x, float y, float z) {
        baseTranslation = new float[]{x, y, z};
        return this;
    }
    public RenderItemBase setTransformTranslations(float[][] translations) {
        this.translations = translations;
        return this;
    }

    protected void setBaseRotation(PoseStack pose, ItemTransforms.TransformType context) {
        pose.mulPose(Vector3f.XP.rotationDegrees(-180));
        pose.mulPose(Vector3f.YP.rotationDegrees(180));
    }

    public final void render(ItemTransforms.TransformType context, PoseStack pose, MultiBufferSource buffers,
                              int light, int overlay) {
        pose.pushPose();
        // ItemRenderer has moved the item origin to the lower corner; legacy renderers use its centre.
        pose.translate(0.5, 0.5, 0.5);
        int index = switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> 0;
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> 1;
            case GUI -> 2;
            case GROUND -> 3;
            case FIXED -> 4;
            default -> -1;
        };
        boolean left = context == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND || context == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
        if (index >= 0) pose.translate(translations[index][0] * (left ? -1 : 1), translations[index][1], translations[index][2]);
        applyAnimation(pose, context);
        if (context == ItemTransforms.TransformType.GUI) {
            pose.mulPose(Vector3f.YP.rotationDegrees(40));
            pose.mulPose(Vector3f.XP.rotationDegrees(20));
        } else if (context == ItemTransforms.TransformType.FIXED) {
            pose.mulPose(Vector3f.YP.rotationDegrees(-90));
        }
        float scale = baseScale * switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> firstPersonScale;
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> 0.35F;
            case GUI -> guiScale;
            case GROUND -> groundScale;
            case FIXED -> frameScale;
            default -> 1.0F;
        };
        pose.scale(scale, scale, scale);
        setBaseRotation(pose, context);
        pose.translate(baseTranslation[0], baseTranslation[1], baseTranslation[2]);
        renderContents(context, pose, buffers, light, overlay);
        pose.popPose();
    }
    protected void applyAnimation(PoseStack pose, ItemTransforms.TransformType context) {}
    protected void renderContents(ItemTransforms.TransformType context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        model.renderModel(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), light, overlay, context);
    }
}
