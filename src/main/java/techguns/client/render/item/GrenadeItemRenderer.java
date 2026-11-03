package techguns.client.render.item;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import techguns.TGuns;
import techguns.client.models.*;
import techguns.client.models.guns.*;
import techguns.items.guns.GenericGrenade;
public final class GrenadeItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static GrenadeItemRenderer instance;
    public static GrenadeItemRenderer get() {if(instance==null)instance=new GrenadeItemRenderer();return instance;}
    private final Renderer stick=new Renderer(new ModelStielgranate(),"stielgranate",false),frag=new Renderer(new ModelFragGrenade(true),"frag_grenade_texture",true);
    private GrenadeItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    @Override public void renderByItem(ItemStack stack,ItemTransforms.TransformType context,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
        var renderer=stack.is(TGuns.fraggrenade.get())?frag:stick;renderer.stack=stack;renderer.render(context,pose,buffers,light,overlay);
    }
    private static final class Renderer extends RenderItemBase {
        final boolean frag;ItemStack stack;float charge;
        Renderer(ModelMultipart model,String texture,boolean frag) {
            super(model,new ResourceLocation("techguns","textures/guns/"+texture+".png"));this.frag=frag;
            baseScale=frag?1.25F:1;guiScale=frag?1.35F:0.8F;
            baseTranslation=frag?new float[]{-0.02F,0.65F,-0.0425F}:new float[]{-0.03125F,0.48F,-0.0625F};
            translations=frag?new float[][]{{0,-0.06F,0},{0,-0.11F,-0.01F},{-0.05F,-0.49F,0},{0,0,0},{0,0,-0.05F}}:new float[][]{{0,-0.06F,0},{0,-0.15F,0.06F},{-0.05F,-0.49F,0},{0,-0.15F,0},{0,0,-0.05F}};
        }
        @Override protected void setBaseRotation(PoseStack pose,ItemTransforms.TransformType context){pose.mulPose(Vector3f.XP.rotationDegrees(-180));pose.mulPose(Vector3f.YP.rotationDegrees(frag?90:180));}
        @Override protected void applyAnimation(PoseStack pose,ItemTransforms.TransformType context) {
            charge=0;var player=Minecraft.getInstance().player;
            if(context.firstPerson()&&player!=null&&player.getUseItem()==stack) {
                charge=Math.min(1,player.getTicksUsingItem()/((GenericGrenade)stack.getItem()).fullChargeTime);pose.mulPose(Vector3f.XP.rotationDegrees(25*charge));
            }
        }
        @Override protected void renderContents(ItemTransforms.TransformType context,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
            model.renderModel(pose,buffers.getBuffer(RenderType.entityCutout(texture)),light,overlay,context,Minecraft.getInstance().player,0,0,0,charge,charge);
        }
    }
}
