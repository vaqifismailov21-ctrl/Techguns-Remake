package techguns.client.render.entities.projectiles;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import techguns.client.render.fx.LegacyFxRenderTypes;
import techguns.entities.projectiles.EnergyProjectile;
import java.util.Random;

/** Original energy textures and beam/wave equations through buffered rendering. */
public final class RenderEnergyProjectile extends EntityRenderer<EnergyProjectile> {
    public RenderEnergyProjectile(EntityRendererProvider.Context context){super(context);}
    private static ResourceLocation texture(String name){return new ResourceLocation("techguns","textures/"+name+".png");}
    @Override public ResourceLocation getTextureLocation(EnergyProjectile entity){return texture(entity.mode==EnergyProjectile.Mode.TESLA?"fx/laser_blue":entity.mode==EnergyProjectile.Mode.NDR?"fx/nukebeam":"fx/laser3");}
    @Override public void render(EnergyProjectile e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        if(e.mode==EnergyProjectile.Mode.ALIEN||e.mode==EnergyProjectile.Mode.NETHER||e.mode==EnergyProjectile.Mode.DEATOMIZER||e.mode==EnergyProjectile.Mode.TFG)return;
        pose.pushPose();
        var dir=e.getDeltaMovement().normalize();
        pose.mulPose(Vector3f.YP.rotationDegrees((float)Math.toDegrees(Math.atan2(dir.x,dir.z))-90));
        pose.mulPose(Vector3f.ZP.rotationDegrees((float)Math.toDegrees(Math.asin(dir.y))));
        float progress=Math.max(0,Math.min(1,(e.tickCount+partial)/Math.max(1F,e.beamLifetime)));
        if(e.mode==EnergyProjectile.Mode.TESLA)tesla(e,pose,buffers,progress);
        else if(e.mode==EnergyProjectile.Mode.SONIC)sonic(e,pose,buffers,partial);
        else if(e.isBeam())beam(e,pose,buffers,progress,partial);
        else if(e.tickCount+partial>2.5F/e.speed){
            pose.mulPose(Vector3f.XP.rotationDegrees(45));pose.scale(0.0125F,0.0125F,0.0125F);
            var vertices=buffers.getBuffer(LegacyFxRenderTypes.sprite(texture(e.mode==EnergyProjectile.Mode.GAUSS?"entity/bullet_blue":"fx/laser3"),true,false));
            float length=e.mode==EnergyProjectile.Mode.GAUSS?16:8;
            for(int i=0;i<4;i++){pose.mulPose(Vector3f.XP.rotationDegrees(90));quad(vertices,pose,-length,length,2,0,1,0,1,1);}
        }
        pose.popPose();
    }
    private void beam(EnergyProjectile e,PoseStack pose,MultiBufferSource buffers,float progress,float partial){
        boolean nuclear=e.mode==EnergyProjectile.Mode.NDR;
        float brightness=(float)Math.sin(Math.sqrt(progress)*Math.PI),width=3*brightness*2;
        float distance=(float)e.beamDistance*80,start=Math.min(80,distance);
        pose.mulPose(Vector3f.XP.rotationDegrees(135+progress*(nuclear?360:180)));pose.scale(0.0125F,0.0125F,0.0125F);
        var vertices=buffers.getBuffer(LegacyFxRenderTypes.sprite(getTextureLocation(e),true,false));
        if(nuclear){
            int frame=(int)(((e.tickCount+partial)*0.5F)%17);float v1=frame/17F,v2=(frame+1)/17F,u=distance/(3*8*2);
            for(int i=0;i<2;i++){pose.mulPose(Vector3f.XP.rotationDegrees(90));quad(vertices,pose,0,distance,width,i,u+i,v1,v2,brightness);}
            float length=Math.min(distance,400);int segments=Math.max(1,(int)(length/8));var random=new Random(e.getId());
            double angle=Math.PI/8,offset=random.nextDouble()*Math.PI*2,cos2=Math.cos((Math.PI-angle)*0.5)*2;
            double px=0,py=0,pz=0,pu=0;
            for(int i=0;i<segments;i++){
                double x=length/segments*i,r=0.5*(1-Math.cos(2*Math.sqrt((double)i/segments)*Math.PI))*8,y=Math.cos(offset-angle*i)*r,z=Math.sin(offset-angle*i)*r,u2=pu+cos2*r/8;
                vertex(vertices,pose,px-4,py,pz,pu,v2,brightness);vertex(vertices,pose,x-4,y,z,u2,v2,brightness);vertex(vertices,pose,x+4,y,z,u2,v1,brightness);vertex(vertices,pose,px+4,py,pz,pu,v1,brightness);
                px=x;py=y;pz=z;pu=u2;
            }
        }else{
            for(int i=0;i<2;i++){pose.mulPose(Vector3f.XP.rotationDegrees(90));if(distance>start)quad(vertices,pose,start,distance,width,progress,distance/3*2+progress,0,1,brightness);}
            var beginning=buffers.getBuffer(LegacyFxRenderTypes.sprite(texture("fx/laser3_start"),true,false));
            for(int i=0;i<2;i++){pose.mulPose(Vector3f.XP.rotationDegrees(90));quad(beginning,pose,0,start,width,0,1,0,1,brightness);}
        }
    }
    private void tesla(EnergyProjectile e,PoseStack pose,MultiBufferSource buffers,float progress){
        var random=new Random(e.seed);double[] dy=new double[5],dz=new double[5];for(int j=0;j<5;j++){dy[j]=0.5-random.nextDouble();dz[j]=0.5-random.nextDouble();}
        int count=Math.max(1,(int)Math.round(e.beamDistance/0.2)),repetitions=Math.max(1,(int)Math.round(e.beamDistance/10));
        double px=0,py=0,pz=0,pw=0.5,pa=1;var v=buffers.getBuffer(LegacyFxRenderTypes.sprite(getTextureLocation(e),true,false));
        pose.mulPose(Vector3f.XP.rotationDegrees(45));
        for(int i=0;i<=count;i++){
            double d=(double)i/count,x=d*e.beamDistance,y=0,z=0;
            if(i>1)for(int j=1;j<=5;j++){y+=Math.sin(d*Math.PI*j*repetitions)*((random.nextDouble()-0.5+progress*dy[j-1])*2/j);z+=Math.sin(d*Math.PI*j*repetitions)*((random.nextDouble()-0.5+progress*dz[j-1])*2/j);}
            y*=0.5;z*=0.5;double pulse=1-Math.sqrt(Math.abs(progress-d)*2),w=Math.max(0,0.25*pulse);
            if(i>0){
                vertex(v,pose,px,py-pw,pz,0,0,pa);vertex(v,pose,px,py+pw,pz,0,1,pa);vertex(v,pose,x,y+w,z,1,1,pa);vertex(v,pose,x,y-w,z,1,0,pa);
                vertex(v,pose,px,py,pz-pw,0,0,pa);vertex(v,pose,px,py,pz+pw,0,1,pa);vertex(v,pose,x,y,z+w,1,1,pa);vertex(v,pose,x,y,z-w,1,0,pa);
            }px=x;py=y;pz=z;pw=w;pa=pulse;
        }
    }
    private void sonic(EnergyProjectile e,PoseStack pose,MultiBufferSource buffers,float partial){
        if(!e.mainProjectile||e.tickCount<3)return;
        var random=new Random(e.getId());float d=(e.tickCount+partial)*0.3F,prog=Math.min(1,(e.tickCount+partial)/20F),opacity=1-prog*prog;
        var v=buffers.getBuffer(LegacyFxRenderTypes.sprite(texture("fx/shockwave"),true,false));
        pose.mulPose(Vector3f.XP.rotationDegrees(90));
        for(int i=0;i<=5;i++){
            float r=opacity*random.nextFloat(),g=opacity*random.nextFloat(),b=opacity*(0.5F+random.nextFloat()*0.5F),x=2.5F-i,size=d*(0.5F+random.nextFloat());
            var m=pose.last().pose();
            v.vertex(m,x,-size,-size).color(r,g,b,opacity).uv(0,0).endVertex();v.vertex(m,x,-size,size).color(r,g,b,opacity).uv(0,1).endVertex();v.vertex(m,x,size,size).color(r,g,b,opacity).uv(1,1).endVertex();v.vertex(m,x,size,-size).color(r,g,b,opacity).uv(1,0).endVertex();
        }
    }
    private static void quad(VertexConsumer v,PoseStack p,float start,float end,float width,float u1,float u2,float v1,float v2,float alpha){vertex(v,p,start,-width,0,u1,v1,alpha);vertex(v,p,start,width,0,u1,v2,alpha);vertex(v,p,end,width,0,u2,v2,alpha);vertex(v,p,end,-width,0,u2,v1,alpha);}
    private static void vertex(VertexConsumer v,PoseStack p,double x,double y,double z,double u,double w,double alpha){v.vertex(p.last().pose(),(float)x,(float)y,(float)z).color(1F,1F,1F,(float)Math.max(0,Math.min(1,alpha))).uv((float)u,(float)w).endVertex();}
}
