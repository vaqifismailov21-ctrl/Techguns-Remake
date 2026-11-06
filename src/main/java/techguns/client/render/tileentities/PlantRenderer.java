package techguns.client.render.tileentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import techguns.power.plant.*;

/**
 * Draws a formed power plant as one model (its blocks are hidden): wind tower with turning rotor, geothermal station
 * with glowing vents and cooling tower, fission reactor with a pulsing core behind windows, tokamak with plasma ring.
 * Model space: x to the right of the controller, y up, z back from its front; origin at the controller's bottom centre.
 */
public final class PlantRenderer implements BlockEntityRenderer<PlantController> {
    private static ResourceLocation tex(String id){return ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/plant/"+id+".png");}
    private static final ResourceLocation CASING=tex("plant_casing"),GLASS=tex("plant_glass"),TOWER=tex("turbine_tower"),COIL=tex("fusion_coil"),BLADE=tex("turbine_blade"),GLOW=tex("plant_glow");
    private static final Map<PlantController,float[]> angles=new WeakHashMap<>();
    public PlantRenderer(BlockEntityRendererProvider.Context context){}

    /** Axis aligned box; texture repeats once per block. */
    static void box(PoseStack pose,VertexConsumer vc,float x0,float y0,float z0,float x1,float y1,float z1,int light,int argb){
        var m=pose.last().pose();var n=pose.last().normal();int a=argb>>>24,r=argb>>16&255,g=argb>>8&255,b=argb&255;
        float[][] faces={
            {x0,y0,z0, x0,y1,z0, x1,y1,z0, x1,y0,z0, 0,0,-1},{x1,y0,z1, x1,y1,z1, x0,y1,z1, x0,y0,z1, 0,0,1},
            {x0,y0,z1, x0,y1,z1, x0,y1,z0, x0,y0,z0, -1,0,0},{x1,y0,z0, x1,y1,z0, x1,y1,z1, x1,y0,z1, 1,0,0},
            {x0,y1,z0, x0,y1,z1, x1,y1,z1, x1,y1,z0, 0,1,0},{x0,y0,z1, x0,y0,z0, x1,y0,z0, x1,y0,z1, 0,-1,0}};
        for(var f:faces){
            float nx=f[12],ny=f[13],nz=f[14];
            for(int i=0;i<4;i++){
                float px=f[i*3],py=f[i*3+1],pz=f[i*3+2];
                float u=nx!=0?pz:px,v=ny!=0?pz:py;
                vc.vertex(m,px,py,pz).color(r,g,b,a).uv(nx<0||nz>0?-u:u,-v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n,nx,ny,nz).endVertex();
            }
        }
    }
    @Override public void render(PlantController plant,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!plant.formed||plant.getLevel()==null)return;
        float time=plant.getLevel().getGameTime()+partial;
        float[] a=angles.computeIfAbsent(plant,p->new float[]{0,time});
        float speed=plant instanceof WindPlant?plant.rate/(float)WindPlant.RATE*0.12F:0;
        a[0]=(a[0]+(time-a[1])*speed)%(float)(Math.PI*2);a[1]=time;
        pose.pushPose();pose.translate(0.5,0,0.5);pose.mulPose(Axis.YP.rotationDegrees(180-plant.facing().toYRot()));
        var casing=buffers.getBuffer(RenderType.entityCutout(CASING));
        switch(plant.kind){
            case WIND->wind((WindPlant)plant,pose,buffers,casing,light,a[0]);
            case GEOTHERMAL->geothermal(plant,pose,buffers,casing,light,time);
            case FISSION->fission((FissionReactor)plant,pose,buffers,casing,light,time);
            case FUSION->fusion((FusionReactor)plant,pose,buffers,casing,light,time);
        }
        pose.popPose();
    }
    private static final int WHITE=0xFFFFFFFF;
    private void wind(WindPlant p,PoseStack pose,MultiBufferSource buffers,VertexConsumer casing,int light,float angle){
        int n=Math.max(1,p.towers);
        box(pose,casing,-0.5F,0,-0.5F,0.5F,1,0.5F,light,WHITE);
        var tower=buffers.getBuffer(RenderType.entityCutout(TOWER));
        for(int i=1;i<=n;i++){float bottom=0.42F-0.17F*(i-1)/n,top=0.42F-0.17F*i/n;float hw=(bottom+top)/2;box(pose,tower,-hw,i,-hw,hw,i+1,hw,light,WHITE);}
        float y=n+1;
        casing=buffers.getBuffer(RenderType.entityCutout(CASING));
        box(pose,casing,-0.4F,y+0.05F,-0.55F,0.4F,y+0.8F,1.15F,light,WHITE);
        box(pose,casing,-0.25F,y+0.15F,1.15F,0.25F,y+0.6F,1.45F,light,0xFFB0B0B0);
        tower=buffers.getBuffer(RenderType.entityCutout(TOWER));
        box(pose,tower,-0.22F,y+0.2F,-0.85F,0.22F,y+0.64F,-0.55F,light,WHITE);
        var blade=buffers.getBuffer(RenderType.entityCutout(BLADE));
        pose.pushPose();pose.translate(0,y+0.42F,-0.75F);pose.mulPose(Axis.ZP.rotation(angle));
        for(int k=0;k<3;k++){
            pose.mulPose(Axis.ZP.rotationDegrees(120));
            box(pose,blade,-0.17F,0.18F,-0.06F,0.17F,1.6F,0.06F,light,WHITE);
            box(pose,blade,-0.11F,1.6F,-0.04F,0.11F,4.3F,0.04F,light,WHITE);
        }
        pose.popPose();
    }
    private void geothermal(PlantController p,PoseStack pose,MultiBufferSource buffers,VertexConsumer casing,int light,float time){
        box(pose,casing,-1.48F,0,-0.48F,1.48F,1.7F,2.48F,light,WHITE);
        box(pose,casing,-1.4F,1.7F,-0.4F,1.4F,2.0F,2.4F,light,0xFF909090);
        box(pose,casing,-1.6F,0.2F,0.4F,-1.48F,1.5F,0.7F,light,0xFFA0A0A0);box(pose,casing,1.48F,0.2F,1.3F,1.6F,1.5F,1.6F,light,0xFFA0A0A0);
        var tower=buffers.getBuffer(RenderType.entityCutout(TOWER));
        box(pose,tower,-0.75F,2.0F,0.25F,0.75F,2.6F,1.75F,light,0xFFD0D0D0);
        box(pose,tower,-0.6F,2.6F,0.4F,0.6F,3.2F,1.6F,light,0xFFD0D0D0);
        box(pose,tower,-0.68F,3.2F,0.32F,0.68F,3.8F,1.68F,light,0xFFD0D0D0);
        int glow=p.rate>0?(int)(200+40*Math.sin(time*0.15))<<24|0xFF7A20:0xFF301008;
        var vents=p.rate>0?buffers.getBuffer(RenderType.eyes(GLOW)):buffers.getBuffer(RenderType.entityCutout(GLOW));
        for(int i=-1;i<=1;i++)box(pose,vents,i*0.8F-0.3F,0.3F,-0.52F,i*0.8F+0.3F,0.9F,-0.49F,light,glow);
    }
    private void fission(FissionReactor p,PoseStack pose,MultiBufferSource buffers,VertexConsumer casing,int light,float time){
        float y0=-1,y1=2,x0=-1.5F,x1=1.5F,z0=-0.5F,z1=2.5F,e=0.02F;
        box(pose,casing,x0+e,y0+e,z0+e,x1-e,y0+0.4F,z1-e,light,WHITE);
        box(pose,casing,x0+e,y1-0.4F,z0+e,x1-e,y1-e,z1-e,light,WHITE);
        for(float sx:new float[]{x0+e,x1-0.5F})for(float sz:new float[]{z0+e,z1-0.5F})box(pose,casing,sx,y0+0.4F,sz,sx+0.48F,y1-0.4F,sz+0.48F,light,WHITE);
        // walls with a window in the middle of each side, the front carries the controller panel
        float wy0=y0+0.4F,wy1=y1-0.4F,win0=-0.1F,win1=1.1F;
        for(int side=0;side<4;side++){
            pose.pushPose();pose.translate(0,0,1);pose.mulPose(Axis.YP.rotationDegrees(side*90));pose.translate(0,0,-1);
            float a=-1.0F,b=1.0F,z=-0.5F+e;
            if(side==0)box(pose,casing,a,wy0,z,b,wy1,z+0.1F,light,WHITE);
            else{box(pose,casing,a,wy0,z,b,win0,z+0.1F,light,WHITE);box(pose,casing,a,win1,z,b,wy1,z+0.1F,light,WHITE);
                box(pose,casing,a,win0,z,-0.8F,win1,z+0.1F,light,WHITE);box(pose,casing,0.8F,win0,z,b,win1,z+0.1F,light,WHITE);}
            pose.popPose();
        }
        float pulse=p.rate>0?0.75F+0.25F*(float)Math.sin(time*0.2):0.15F;
        var core=buffers.getBuffer(RenderType.eyes(GLOW));int colour=(int)(pulse*255)<<24|0x40FF60;
        for(float rx:new float[]{-0.35F,0.35F})for(float rz:new float[]{0.65F,1.35F})box(pose,core,rx-0.09F,-0.55F,rz-0.09F,rx+0.09F,1.55F,rz+0.09F,light,colour);
        var glass=buffers.getBuffer(RenderType.entityTranslucent(GLASS));
        for(int side=1;side<4;side++){pose.pushPose();pose.translate(0,0,1);pose.mulPose(Axis.YP.rotationDegrees(side*90));pose.translate(0,0,-1);
            box(pose,glass,-0.8F,win0,-0.45F,0.8F,win1,-0.42F,light,0xB0FFFFFF);pose.popPose();}
        casing=buffers.getBuffer(RenderType.entityCutout(CASING));
        box(pose,casing,-0.9F,y1-e,0.3F,-0.5F,y1+0.6F,0.7F,light,0xFFB0B0B0);box(pose,casing,0.5F,y1-e,1.3F,0.9F,y1+0.6F,1.7F,light,0xFFB0B0B0);
    }
    private void fusion(FusionReactor p,PoseStack pose,MultiBufferSource buffers,VertexConsumer casing,int light,float time){
        float e=0.02F;
        box(pose,casing,-2.5F+e,-1+e,-0.5F+e,2.5F-e,-0.6F,4.5F-e,light,WHITE);
        for(float sx:new float[]{-2.5F+e,2.1F})for(float sz:new float[]{-0.5F+e,4.1F})box(pose,casing,sx,-0.6F,sz,sx+0.38F,1.6F,sz+0.38F,light,WHITE);
        box(pose,casing,-2.5F+e,1.6F,-0.5F+e,2.5F-e,1.98F,-0.12F,light,WHITE);box(pose,casing,-2.5F+e,1.6F,4.12F,2.5F-e,1.98F,4.5F-e,light,WHITE);
        box(pose,casing,-2.5F+e,1.6F,-0.12F,-2.12F,1.98F,4.12F,light,WHITE);box(pose,casing,2.12F,1.6F,-0.12F,2.5F-e,1.98F,4.12F,light,WHITE);
        var coil=buffers.getBuffer(RenderType.entityCutout(COIL));float radius=1.7F;
        for(int i=0;i<16;i++){pose.pushPose();pose.translate(0,0.5F,2);pose.mulPose(Axis.YP.rotationDegrees(i*22.5F));
            box(pose,coil,-0.3F,-0.75F,radius-0.32F,0.3F,0.75F,radius+0.32F,light,WHITE);pose.popPose();}
        box(pose,coil,-0.35F,-0.6F,1.65F,0.35F,1.6F,2.35F,light,WHITE);
        if(p.ignited){
            var plasma=buffers.getBuffer(RenderType.eyes(GLOW));
            for(int i=0;i<32;i++){
                pose.pushPose();pose.translate(0,0.5F,2);pose.mulPose(Axis.YP.rotationDegrees(i*11.25F+time*9));
                float wave=0.5F+0.5F*(float)Math.sin(time*0.3+i*0.6);
                int colour=(int)(140+100*wave)<<24|(int)(160+90*wave)<<16|0x40<<8|0xFF;
                box(pose,plasma,-0.17F,-0.2F,radius-0.55F,0.17F,0.2F,radius-0.2F,light,colour);
                pose.popPose();
            }
        }
    }
    @Override public boolean shouldRenderOffScreen(PlantController plant){return true;}
    @Override public int getViewDistance(){return 160;}
}
