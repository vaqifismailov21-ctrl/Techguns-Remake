package techguns.client.render.item;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
        var renderer=stack.is(TGuns.fraggrenade.get())?frag:stick;renderer.stack=stack;renderer.render(context,pose,buffers,light,overlay);
    }
    private static final class Renderer extends RenderItemBase {
        final boolean frag;ItemStack stack;float charge;
        Renderer(ModelMultipart model,String texture,boolean frag) {
            super(model,ResourceLocation.fromNamespaceAndPath("techguns","textures/guns/"+texture+".png"));this.frag=frag;
            baseScale=frag?1.25F:1;guiScale=frag?1.35F:0.8F;
            baseTranslation=frag?new float[]{-0.02F,0.65F,-0.0425F}:new float[]{-0.03125F,0.48F,-0.0625F};
            translations=frag?new float[][]{{0,-0.06F,0},{0,-0.11F,-0.01F},{-0.05F,-0.49F,0},{0,0,0},{0,0,-0.05F}}:new float[][]{{0,-0.06F,0},{0,-0.15F,0.06F},{-0.05F,-0.49F,0},{0,-0.15F,0},{0,0,-0.05F}};
        }
        @Override protected void setBaseRotation(PoseStack pose,ItemDisplayContext context){pose.mulPose(Axis.XP.rotationDegrees(-180));pose.mulPose(Axis.YP.rotationDegrees(frag?90:180));}
        @Override protected void applyAnimation(PoseStack pose,ItemDisplayContext context) {
            charge=0;var player=Minecraft.getInstance().player;
            if(context.firstPerson()&&player!=null&&player.getUseItem()==stack) {
                charge=Math.min(1,player.getTicksUsingItem()/((GenericGrenade)stack.getItem()).fullChargeTime);pose.mulPose(Axis.XP.rotationDegrees(25*charge));
            }
        }
        @Override protected void renderContents(ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
            model.renderModel(pose,buffers.getBuffer(RenderType.entityCutout(texture)),light,overlay,context,Minecraft.getInstance().player,0,0,0,charge,charge);
        }
    }
}
