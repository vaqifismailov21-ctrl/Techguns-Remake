package techguns.client.render.tileentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import techguns.power.wire.PylonBlock;
import techguns.power.wire.PylonEntity;

/** Lattice steel tower with two cross-arms and hanging insulator strings, drawn from beams. */
public class PylonRenderer implements BlockEntityRenderer<PylonEntity> {
    private static final ResourceLocation STEEL=ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/wire/pylon_steel.png");
    private static final ResourceLocation INSULATOR=ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/wire/insulator.png");
    public PylonRenderer(BlockEntityRendererProvider.Context ctx){}
    @Override public boolean shouldRenderOffScreen(PylonEntity be){return true;}
    @Override public int getViewDistance(){return 160;}

    @Override public void render(PylonEntity be,float pt,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var state=be.getBlockState();if(!state.hasProperty(PylonBlock.FACING))return;
        var f=state.getValue(PylonBlock.FACING);var a=f.getClockWise();
        Vec3 across=new Vec3(a.getStepX(),0,a.getStepZ()),along=new Vec3(f.getStepX(),0,f.getStepZ());
        var c=new Vec3(0.5,0,0.5);
        var steel=buffers.getBuffer(RenderType.entityCutoutNoCull(STEEL));
        var m=pose.last().pose();var n=pose.last().normal();
        int H=PylonBlock.HEIGHT;double top=H-0.05;
        // four corner legs, tapering
        for(int sx=-1;sx<=1;sx+=2)for(int sz=-1;sz<=1;sz+=2){
            Vec3 lo=leg(c,across,along,sx,sz,0),hi=leg(c,across,along,sx,sz,top);
            beam(steel,m,n,lo,hi,0.045,light);
        }
        // horizontal rings and X braces per block of height
        for(int y=0;y<H;y++){
            double y0=y+(y==0?0.05:0),y1=Math.min(top,y+1);
            for(int side=0;side<4;side++){
                int[][] corners={{-1,-1},{1,-1},{1,1},{-1,1}};
                int[] p=corners[side],q=corners[(side+1)%4];
                var a0=leg(c,across,along,p[0],p[1],y0);var b0=leg(c,across,along,q[0],q[1],y0);
                var a1=leg(c,across,along,p[0],p[1],y1);var b1=leg(c,across,along,q[0],q[1],y1);
                beam(steel,m,n,a0,b0,0.025,light);
                beam(steel,m,n,a0,b1,0.018,light);beam(steel,m,n,b0,a1,0.018,light);
            }
        }
        // cap and peak
        for(int side=0;side<4;side++){int[][] k={{-1,-1},{1,-1},{1,1},{-1,1}};
            beam(steel,m,n,leg(c,across,along,k[side][0],k[side][1],top),c.add(0,H+0.55,0),0.025,light);}
        // cross-arms: triangular trusses out to both sides
        arm(steel,m,n,c,across,along,PylonBlock.Y_UPPER,PylonBlock.ARM_UPPER,light);
        arm(steel,m,n,c,across,along,PylonBlock.Y_LOWER,PylonBlock.ARM_LOWER,light);
        // insulator strings: stacked glazed discs hanging from the arm tips down to the wire points
        var ins=buffers.getBuffer(RenderType.entityCutoutNoCull(INSULATOR));
        for(int i=0;i<4;i++){
            double reach=i<2?PylonBlock.ARM_UPPER:PylonBlock.ARM_LOWER,y=i<2?PylonBlock.Y_UPPER:PylonBlock.Y_LOWER;int s=i%2==0?-1:1;
            var tip=c.add(across.scale(reach*s)).add(0,y,0);
            var end=tip.add(0,-PylonBlock.INSULATOR,0);
            beam(steel,m,n,tip.add(0,0.02,0),end,0.012,light);
            int discs=5;for(int d=0;d<discs;d++){double t=(d+0.6)/(discs+0.4);var dc=tip.lerp(end,t);
                box(ins,m,n,dc.add(-0.085,-0.025,-0.085),dc.add(0.085,0.025,0.085),light);}
            box(ins,m,n,end.add(-0.04,-0.05,-0.04),end.add(0.04,0.02,0.04),light);
        }
    }
    private static Vec3 leg(Vec3 c,Vec3 across,Vec3 along,int sx,int sz,double y){double h=PylonBlock.half(y);return c.add(across.scale(sx*h)).add(along.scale(sz*h)).add(0,y,0);}
    private static void arm(VertexConsumer vc,Matrix4f m,Matrix3f n,Vec3 c,Vec3 across,Vec3 along,double y,double reach,int light){
        double h=PylonBlock.half(y),drop=0.42;
        for(int s=-1;s<=1;s+=2){
            var tip=c.add(across.scale(reach*s)).add(0,y,0);
            for(int z=-1;z<=1;z+=2){
                var upper=c.add(across.scale(h*s)).add(along.scale(h*z)).add(0,y,0);
                var lower=c.add(across.scale(h*s)).add(along.scale(h*z)).add(0,y-drop,0);
                beam(vc,m,n,upper,tip,0.03,light);beam(vc,m,n,lower,tip,0.026,light);
                for(int k=1;k<4;k++){double t=k/4.0;var p=upper.lerp(tip,t);var q=lower.lerp(tip,t);
                    beam(vc,m,n,p,q,0.014,light);if(k<3)beam(vc,m,n,q,upper.lerp(tip,(k+1)/4.0),0.012,light);}
            }
            beam(vc,m,n,tip.add(along.scale(-0.08)),tip.add(along.scale(0.08)),0.03,light);
            beam(vc,m,n,c.add(across.scale(h*s)).add(along.scale(-h)).add(0,y,0),c.add(across.scale(h*s)).add(along.scale(h)).add(0,y,0),0.025,light);
        }
    }
    /** Square beam from p to q with half-width w, with end caps. */
    public static void beam(VertexConsumer vc,Matrix4f m,Matrix3f n,Vec3 p,Vec3 q,double w,int light){
        var d=q.subtract(p);double len=d.length();if(len<1e-4)return;d=d.scale(1/len);
        var side=d.cross(new Vec3(0,1,0));if(side.lengthSqr()<1e-6)side=new Vec3(1,0,0);side=side.normalize();
        var up=side.cross(d).normalize();
        Vec3[] o={side.scale(w).add(up.scale(w)),side.scale(-w).add(up.scale(w)),side.scale(-w).add(up.scale(-w)),side.scale(w).add(up.scale(-w))};
        float v1=(float)(len*2);
        for(int k=0;k<4;k++){var o1=o[k];var o2=o[(k+1)%4];var nr=o1.add(o2).normalize();
            quad(vc,m,n,p.add(o1),q.add(o1),q.add(o2),p.add(o2),0,0,0.25F,v1,nr,light);}
        quad(vc,m,n,p.add(o[3]),p.add(o[2]),p.add(o[1]),p.add(o[0]),0,0,0.25F,0.25F,d.scale(-1),light);
        quad(vc,m,n,q.add(o[0]),q.add(o[1]),q.add(o[2]),q.add(o[3]),0,0,0.25F,0.25F,d,light);
    }
    /** Axis-aligned box from a to b. */
    public static void box(VertexConsumer vc,Matrix4f m,Matrix3f n,Vec3 a,Vec3 b,int light){
        double x0=a.x,y0=a.y,z0=a.z,x1=b.x,y1=b.y,z1=b.z;
        quad(vc,m,n,new Vec3(x0,y1,z0),new Vec3(x0,y1,z1),new Vec3(x1,y1,z1),new Vec3(x1,y1,z0),0,0,1,1,new Vec3(0,1,0),light);
        quad(vc,m,n,new Vec3(x0,y0,z1),new Vec3(x0,y0,z0),new Vec3(x1,y0,z0),new Vec3(x1,y0,z1),0,0,1,1,new Vec3(0,-1,0),light);
        quad(vc,m,n,new Vec3(x0,y1,z0),new Vec3(x1,y1,z0),new Vec3(x1,y0,z0),new Vec3(x0,y0,z0),0,0,1,0.3F,new Vec3(0,0,-1),light);
        quad(vc,m,n,new Vec3(x1,y1,z1),new Vec3(x0,y1,z1),new Vec3(x0,y0,z1),new Vec3(x1,y0,z1),0,0,1,0.3F,new Vec3(0,0,1),light);
        quad(vc,m,n,new Vec3(x0,y1,z1),new Vec3(x0,y1,z0),new Vec3(x0,y0,z0),new Vec3(x0,y0,z1),0,0,1,0.3F,new Vec3(-1,0,0),light);
        quad(vc,m,n,new Vec3(x1,y1,z0),new Vec3(x1,y1,z1),new Vec3(x1,y0,z1),new Vec3(x1,y0,z0),0,0,1,0.3F,new Vec3(1,0,0),light);
    }
    private static void quad(VertexConsumer vc,Matrix4f m,Matrix3f n,Vec3 a,Vec3 b,Vec3 c,Vec3 d,float u0,float v0,float u1,float v1,Vec3 nr,int light){
        v(vc,m,n,a,u0,v0,nr,light);v(vc,m,n,b,u0,v1,nr,light);v(vc,m,n,c,u1,v1,nr,light);v(vc,m,n,d,u1,v0,nr,light);
    }
    private static void v(VertexConsumer vc,Matrix4f m,Matrix3f n,Vec3 p,float u,float vv,Vec3 nr,int light){
        vc.vertex(m,(float)p.x,(float)p.y,(float)p.z).color(255,255,255,255).uv(u,vv).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n,(float)nr.x,(float)nr.y,(float)nr.z).endVertex();
    }
}
