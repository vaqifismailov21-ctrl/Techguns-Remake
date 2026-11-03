package techguns.client.render.entities.npcs;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.models.ModelMultipart;
public final class SpecialNpcModel<T extends LivingEntity> extends EntityModel<T> implements techguns.client.models.LegacyDeathModel {
    private final ModelMultipart geometry;private T holder;private float limb,amount,age,yaw,pitch;
    public SpecialNpcModel(ModelMultipart geometry){this.geometry=geometry;}
    @Override public void setupAnim(T entity,float limb,float amount,float age,float yaw,float pitch){holder=entity;this.limb=limb;this.amount=amount;this.age=age;this.yaw=yaw;this.pitch=pitch;}
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer vertices,int light,int overlay,int tgColor){float red=net.minecraft.util.FastColor.ARGB32.red(tgColor)/255F,green=net.minecraft.util.FastColor.ARGB32.green(tgColor)/255F,blue=net.minecraft.util.FastColor.ARGB32.blue(tgColor)/255F,alpha=net.minecraft.util.FastColor.ARGB32.alpha(tgColor)/255F;geometry.renderAnimatedModel(pose,new techguns.client.models.TintedVertexConsumer(vertices,red,green,blue,alpha),light,overlay,ItemDisplayContext.NONE,holder,limb,amount,age,yaw,pitch);}
    @Override public java.util.List<techguns.client.models.MeshSnapshot> deathParts(PoseStack pose){return geometry.captureParts(vertices->renderToBuffer(pose,vertices,0xf000f0,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,-1));}
}
