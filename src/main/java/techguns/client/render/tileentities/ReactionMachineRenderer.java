package techguns.client.render.tileentities;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.models.LegacyObjModel;
import techguns.client.render.fx.LegacyFxRenderTypes;
import techguns.machines.*;
/** Original OBJ materials, suspended input, pulsating heat-ray billboard and visible tank volume. */
public final class ReactionMachineRenderer {
    private static final LegacyObjModel opaque=new LegacyObjModel(true,false,"reactionchamber_opaque"),glass=new LegacyObjModel(true,false,"reactionchamber_glass");
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/reactionchamber.png"),GLASS=ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/reactionchamberglass.png"),HEAT=ResourceLocation.fromNamespaceAndPath("techguns","textures/fx/heatray.png");
    public static void render(ProcessingMachine machine,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!machine.formed)return;var direction=machine.getBlockState().getValue(MachineBlock.FACING);pose.pushPose();pose.translate(0.5,0,0.5);pose.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));pose.translate(-0.5,0,-0.5);opaque.renderModel(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),light,overlay,ItemDisplayContext.NONE);glass.renderModel(pose,buffers.getBuffer(RenderType.entityTranslucent(GLASS)),light,overlay,ItemDisplayContext.NONE);pose.popPose();
        double centerX=direction.getStepX()+0.5,centerZ=direction.getStepZ()+0.5;
        var item=machine.reactor.running()?machine.reactor.input:machine.inventory.getStackInSlot(0);if(!item.isEmpty()){pose.pushPose();pose.translate(centerX,1.5,centerZ);pose.mulPose(Axis.YP.rotationDegrees(-direction.getOpposite().toYRot()));Minecraft.getInstance().getItemRenderer().renderStatic(item.copyWithCount(1),ItemDisplayContext.GROUND,light,overlay,pose,buffers,machine.getLevel(),0);pose.popPose();}
        if(!machine.inputTank.isEmpty())fluid(machine,pose,buffers,light,overlay,centerX,centerZ);
        if(machine.reactor.running()){double pulse=0.875+0.125*(1-Math.cos(Math.PI*((machine.progress%10+partial)/10F)*2)),width=1.875*0.5*pulse;var camera=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();double angle=Math.atan2(machine.getBlockPos().getX()+centerX-camera.x,machine.getBlockPos().getZ()+centerZ-camera.z);pose.pushPose();pose.translate(centerX,0.5,centerZ);pose.mulPose(Axis.YP.rotation((float)angle));var out=buffers.getBuffer(LegacyFxRenderTypes.sprite(HEAT,true,true));var matrix=pose.last().pose();out.vertex(matrix,(float)-width,0.5F,0).color(1F,1F,1F,1F).uv(0,1).endVertex();out.vertex(matrix,(float)-width,2.9375F,0).color(1F,1F,1F,1F).uv(0,0).endVertex();out.vertex(matrix,(float)width,2.9375F,0).color(1F,1F,1F,1F).uv(1,0).endVertex();out.vertex(matrix,(float)width,0.5F,0).color(1F,1F,1F,1F).uv(1,1).endVertex();pose.popPose();}
    }
    private static void fluid(ProcessingMachine machine,PoseStack pose,MultiBufferSource buffers,int light,int overlay,double x,double z){
        var stack=machine.inputTank.getFluid();var extension=net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(stack.getFluid());var texture=machine.reactor.running()?extension.getFlowingTexture(stack):extension.getStillTexture(stack);var sprite=Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);int tint=extension.getTintColor(stack);var out=buffers.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));float x0=(float)(x-0.9375),x1=(float)(x+0.9375),z0=(float)(z-0.9375),z1=(float)(z+0.9375),y0=0.5F,y1=y0+2.9375F*machine.inputTank.getFluidAmount()/machine.inputTank.getCapacity();float u0=sprite.getU0(),u1=sprite.getU1(),v0=sprite.getV0(),v1=sprite.getV1();
        quad(out,pose,light,overlay,tint,u0,u1,v0,v1,new float[][]{{x0,y0,z0},{x0,y1,z0},{x1,y1,z0},{x1,y0,z0}},0,0,-1);quad(out,pose,light,overlay,tint,u0,u1,v0,v1,new float[][]{{x1,y0,z1},{x1,y1,z1},{x0,y1,z1},{x0,y0,z1}},0,0,1);quad(out,pose,light,overlay,tint,u0,u1,v0,v1,new float[][]{{x0,y0,z1},{x0,y1,z1},{x0,y1,z0},{x0,y0,z0}},-1,0,0);quad(out,pose,light,overlay,tint,u0,u1,v0,v1,new float[][]{{x1,y0,z0},{x1,y1,z0},{x1,y1,z1},{x1,y0,z1}},1,0,0);quad(out,pose,light,overlay,tint,u0,u1,v0,v1,new float[][]{{x0,y1,z0},{x0,y1,z1},{x1,y1,z1},{x1,y1,z0}},0,1,0);
    }
    private static void quad(VertexConsumer out,PoseStack pose,int light,int overlay,int color,float u0,float u1,float v0,float v1,float[][] points,float nx,float ny,float nz){for(int i=0;i<4;i++){var p=points[i];out.vertex(pose.last().pose(),p[0],p[1],p[2]).color(color>>16&255,color>>8&255,color&255,color>>>24).uv(i<2?u0:u1,i==0||i==3?v1:v0).overlayCoords(overlay).uv2(light).normal(pose.last().normal(),nx,ny,nz).endVertex();}}
    private ReactionMachineRenderer(){}
}
