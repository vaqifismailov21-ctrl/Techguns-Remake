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
    private static final ResourceLocation STEEL=ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/wire/pylon_steel_worn.png");
    private static final ResourceLocation INSULATOR=ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/wire/insulator.png");
    public PylonRenderer(BlockEntityRendererProvider.Context ctx){}
    @Override public boolean shouldRenderOffScreen(PylonEntity be){return true;}
    @Override public int getViewDistance(){return 160;}

    /** The tower never changes: its vertices (x y z u v nx ny nz) are worked out once for each facing, steel and insulators apart. */
    private static final float[][][] BAKED=new float[4][][];
    private static final class Out{
        float[] d=new float[8192];int n;
        void add(Vec3 p,float u,float v,Vec3 nr){
            if(n+8>d.length)d=java.util.Arrays.copyOf(d,d.length*2);
            d[n++]=(float)p.x;d[n++]=(float)p.y;d[n++]=(float)p.z;d[n++]=u;d[n++]=v;d[n++]=(float)nr.x;d[n++]=(float)nr.y;d[n++]=(float)nr.z;
        }
        float[] array(){return java.util.Arrays.copyOf(d,n);}
    }

    @Override public void render(PylonEntity be,float pt,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var state=be.getBlockState();if(!state.hasProperty(PylonBlock.FACING))return;
        var f=state.getValue(PylonBlock.FACING);int fi=f.get2DDataValue();if(fi<0)return;
        var baked=BAKED[fi];if(baked==null)baked=BAKED[fi]=bake(f);
        // light of every block of height, so the top is not lit like the foot
        var level=be.getLevel();int[] lights=new int[PylonBlock.HEIGHT+1];
        for(int i=0;i<lights.length;i++)lights[i]=level==null?light:net.minecraft.client.renderer.LevelRenderer.getLightColor(level,be.getBlockPos().above(i));
        var m=pose.last().pose();var n=pose.last().normal();
        emit(buffers.getBuffer(RenderType.entityCutoutNoCull(STEEL)),m,n,baked[0],lights);
        emit(buffers.getBuffer(RenderType.entityCutoutNoCull(INSULATOR)),m,n,baked[1],lights);
    }
    private static void emit(VertexConsumer vc,Matrix4f m,Matrix3f n,float[] d,int[] lights){
        int top=lights.length-1;
        for(int o=0;o<d.length;o+=8){
            int h=(int)d[o+1];if(h<0)h=0;else if(h>top)h=top;
            vc.vertex(m,d[o],d[o+1],d[o+2]).color(255,255,255,255).uv(d[o+3],d[o+4]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lights[h]).normal(n,d[o+5],d[o+6],d[o+7]).endVertex();
        }
    }
    private static float[][] bake(net.minecraft.core.Direction f){
        var a=f.getClockWise();
        Vec3 across=new Vec3(a.getStepX(),0,a.getStepZ()),along=new Vec3(f.getStepX(),0,f.getStepZ());
        var c=new Vec3(0.5,0,0.5);beamIndex=0;
        var steel=new Out();
        int H=PylonBlock.HEIGHT;double top=H-0.05;
        // four corner legs, tapering
        for(int sx=-1;sx<=1;sx+=2)for(int sz=-1;sz<=1;sz+=2){
            Vec3 lo=leg(c,across,along,sx,sz,0),hi=leg(c,across,along,sx,sz,top);
            beam(steel,lo,hi,0.045);
        }
        // horizontal rings and X braces per block of height
        for(int y=0;y<H;y++){
            double y0=y+(y==0?0.05:0),y1=Math.min(top,y+1);
            for(int side=0;side<4;side++){
                int[][] corners={{-1,-1},{1,-1},{1,1},{-1,1}};
                int[] p=corners[side],q=corners[(side+1)%4];
                var a0=leg(c,across,along,p[0],p[1],y0);var b0=leg(c,across,along,q[0],q[1],y0);
                var a1=leg(c,across,along,p[0],p[1],y1);var b1=leg(c,across,along,q[0],q[1],y1);
                beam(steel,a0,b0,0.025);
                beam(steel,a0,b1,0.018);beam(steel,b0,a1,0.018);
            }
        }
        // cap and peak
        for(int side=0;side<4;side++){int[][] k={{-1,-1},{1,-1},{1,1},{-1,1}};
            beam(steel,leg(c,across,along,k[side][0],k[side][1],top),c.add(0,H+0.55,0),0.025);}
        // cross-arms: triangular trusses out to both sides
        arm(steel,c,across,along,PylonBlock.Y_UPPER,PylonBlock.ARM_UPPER);
        arm(steel,c,across,along,PylonBlock.Y_LOWER,PylonBlock.ARM_LOWER);
        // insulator strings: stacked glazed discs hanging from the arm tips down to the wire points
        var ins=new Out();
        for(int i=0;i<4;i++){
            double reach=i<2?PylonBlock.ARM_UPPER:PylonBlock.ARM_LOWER,y=i<2?PylonBlock.Y_UPPER:PylonBlock.Y_LOWER;int s=i%2==0?-1:1;
            var tip=c.add(across.scale(reach*s)).add(0,y,0);
            var end=tip.add(0,-PylonBlock.INSULATOR,0);
            beam(steel,tip.add(0,0.02,0),end,0.012);
            int discs=5;for(int d=0;d<discs;d++){double t=(d+0.6)/(discs+0.4);var dc=tip.lerp(end,t);
                box(ins,dc.add(-0.085,-0.025,-0.085),dc.add(0.085,0.025,0.085));}
            box(ins,end.add(-0.04,-0.05,-0.04),end.add(0.04,0.02,0.04));
        }
            return new float[][]{steel.array(),ins.array()};
    }
    private static Vec3 leg(Vec3 c,Vec3 across,Vec3 along,int sx,int sz,double y){double h=PylonBlock.half(y);return c.add(across.scale(sx*h)).add(along.scale(sz*h)).add(0,y,0);}
    private static void arm(Out vc,Vec3 c,Vec3 across,Vec3 along,double y,double reach){
        double h=PylonBlock.half(y),drop=0.42;
        for(int s=-1;s<=1;s+=2){
            var tip=c.add(across.scale(reach*s)).add(0,y,0);
            for(int z=-1;z<=1;z+=2){
                var upper=c.add(across.scale(h*s)).add(along.scale(h*z)).add(0,y,0);
                var lower=c.add(across.scale(h*s)).add(along.scale(h*z)).add(0,y-drop,0);
                beam(vc,upper,tip,0.03);beam(vc,lower,tip,0.026);
                for(int k=1;k<4;k++){double t=k/4.0;var p=upper.lerp(tip,t);var q=lower.lerp(tip,t);
                    beam(vc,p,q,0.014);if(k<3)beam(vc,q,upper.lerp(tip,(k+1)/4.0),0.012);}
            }
            beam(vc,tip.add(along.scale(-0.08)),tip.add(along.scale(0.08)),0.03);
            beam(vc,c.add(across.scale(h*s)).add(along.scale(-h)).add(0,y,0),c.add(across.scale(h*s)).add(along.scale(h)).add(0,y,0),0.025);
        }
    }
    private static int beamIndex;
    /** Square beam from p to q with half-width w, with end caps. */
    private static void beam(Out vc,Vec3 p,Vec3 q,double w){
        var d=q.subtract(p);double len=d.length();if(len<1e-4)return;d=d.scale(1/len);
        var side=d.cross(new Vec3(0,1,0));if(side.lengthSqr()<1e-6)side=new Vec3(1,0,0);side=side.normalize();
        var up=side.cross(d).normalize();
        Vec3[] o={side.scale(w).add(up.scale(w)),side.scale(-w).add(up.scale(w)),side.scale(-w).add(up.scale(-w)),side.scale(w).add(up.scale(-w))};
        // every beam takes its own strip of the worn steel texture, so no two look alike
        int bi=beamIndex++;float u0=(bi%4)*0.25F,vo=(bi*0.37F)%1F,v1=vo+(float)(len*0.25);float uw=(float)Math.max(0.03,Math.min(0.25,w*4));
        for(int k=0;k<4;k++){var o1=o[k];var o2=o[(k+1)%4];var nr=o1.add(o2).normalize();float uk=u0+(k%2)*(0.25F-uw);
            quad(vc,p.add(o1),q.add(o1),q.add(o2),p.add(o2),uk,vo,uk+uw,v1,nr);}
        quad(vc,p.add(o[3]),p.add(o[2]),p.add(o[1]),p.add(o[0]),u0,vo,u0+uw,vo+uw,d.scale(-1));
        quad(vc,q.add(o[0]),q.add(o[1]),q.add(o[2]),q.add(o[3]),u0,vo,u0+uw,vo+uw,d);
    }
    /** Axis-aligned box from a to b. */
    private static void box(Out vc,Vec3 a,Vec3 b){
        double x0=a.x,y0=a.y,z0=a.z,x1=b.x,y1=b.y,z1=b.z;
        quad(vc,new Vec3(x0,y1,z0),new Vec3(x0,y1,z1),new Vec3(x1,y1,z1),new Vec3(x1,y1,z0),0,0,1,1,new Vec3(0,1,0));
        quad(vc,new Vec3(x0,y0,z1),new Vec3(x0,y0,z0),new Vec3(x1,y0,z0),new Vec3(x1,y0,z1),0,0,1,1,new Vec3(0,-1,0));
        quad(vc,new Vec3(x0,y1,z0),new Vec3(x1,y1,z0),new Vec3(x1,y0,z0),new Vec3(x0,y0,z0),0,0,1,0.3F,new Vec3(0,0,-1));
        quad(vc,new Vec3(x1,y1,z1),new Vec3(x0,y1,z1),new Vec3(x0,y0,z1),new Vec3(x1,y0,z1),0,0,1,0.3F,new Vec3(0,0,1));
        quad(vc,new Vec3(x0,y1,z1),new Vec3(x0,y1,z0),new Vec3(x0,y0,z0),new Vec3(x0,y0,z1),0,0,1,0.3F,new Vec3(-1,0,0));
        quad(vc,new Vec3(x1,y1,z0),new Vec3(x1,y1,z1),new Vec3(x1,y0,z1),new Vec3(x1,y0,z0),0,0,1,0.3F,new Vec3(1,0,0));
    }
    private static void quad(Out vc,Vec3 a,Vec3 b,Vec3 c,Vec3 d,float u0,float v0,float u1,float v1,Vec3 nr){
        vc.add(a,u0,v0,nr);vc.add(b,u0,v1,nr);vc.add(c,u1,v1,nr);vc.add(d,u1,v0,nr);
    }
}
