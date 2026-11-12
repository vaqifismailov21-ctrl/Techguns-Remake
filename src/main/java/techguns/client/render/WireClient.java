package techguns.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;
import techguns.power.wire.WireConnection;
import techguns.power.wire.WireHost;
import techguns.power.wire.WireTier;

/** Client copy of the wires of the current dimension, drawn as sagging cables between their points. */
@Mod.EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class WireClient {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/wire/wire.png");
    private static ResourceKey<Level> dimension;
    private static final List<WireConnection> WIRES=new ArrayList<>();
    private WireClient(){}

    /** Geometry of one hanging wire, built once: 16 vertices a segment, positions relative to its first end. */
    private static final class Mesh{
        final Vec3 a,b;final float[] data;final BlockPos[] lightAt;final int[] light;final net.minecraft.world.phys.AABB box;long lightTime=Long.MIN_VALUE;
        Mesh(Vec3 a,Vec3 b,WireTier tier,boolean insulated){
            this.a=a;this.b=b;
            double length=a.distanceTo(b),sag=sag(tier,length);int n=Math.max(6,Math.min(48,(int)(length*2.5)));
            float w=tier.thickness+(insulated?0.008F:0);
            data=new float[n*16*8];lightAt=new BlockPos[n];light=new int[n];
            Vec3 prev=a;double v0=0;int o=0;double y0=Math.min(a.y,b.y);
            for(int i=1;i<=n;i++){
                double t=i/(double)n;var p=a.lerp(b,t).add(0,-sag*4*t*(1-t),0);y0=Math.min(y0,p.y);
                lightAt[i-1]=BlockPos.containing(prev.add(p).scale(0.5));
                double v1=v0+prev.distanceTo(p);
                o=tube(data,o,prev.subtract(a),p.subtract(a),w,(float)v0,(float)v1);
                prev=p;v0=v1;
            }
            box=new net.minecraft.world.phys.AABB(Math.min(a.x,b.x),y0,Math.min(a.z,b.z),Math.max(a.x,b.x),Math.max(a.y,b.y),Math.max(a.z,b.z)).inflate(0.1);
        }
    }
    private static final java.util.Map<WireConnection,Mesh> MESHES=new java.util.IdentityHashMap<>();

    public static void receive(ResourceKey<Level> dim,int mode,List<WireConnection> list){
        MESHES.clear();
        if(mode==0){dimension=dim;WIRES.clear();WIRES.addAll(list);return;}
        if(!dim.equals(dimension))return;
        if(mode==1)WIRES.addAll(list);
        else WIRES.removeIf(w->list.stream().anyMatch(w::sameEnds));
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){WIRES.clear();MESHES.clear();dimension=null;}

    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS||WIRES.isEmpty())return;
        var mc=Minecraft.getInstance();var level=mc.level;if(level==null||!level.dimension().equals(dimension))return;
        var cam=event.getCamera().getPosition();var pose=event.getPoseStack();var frustum=event.getFrustum();
        double far=mc.options.getEffectiveRenderDistance()*16+32;far*=far;long time=level.getGameTime();
        VertexConsumer vc=null;RenderType type=null;org.joml.Matrix4f m=null;org.joml.Matrix3f nm=null;
        for(var w:WIRES){
            Vec3 a=point(level,w.a().pos(),w.a().point()),b=point(level,w.b().pos(),w.b().point());
            if(a==null||b==null)continue;
            var mesh=MESHES.get(w);
            if(mesh==null||!mesh.a.equals(a)||!mesh.b.equals(b)){mesh=new Mesh(a,b,w.tier(),w.insulated());MESHES.put(w,mesh);}
            if(mesh.box.distanceToSqr(cam)>far||frustum!=null&&!frustum.isVisible(mesh.box))continue;
            // light along the wire changes slowly: look it up twice a second
            if(time-mesh.lightTime>=10||time<mesh.lightTime){for(int i=0;i<mesh.light.length;i++)mesh.light[i]=LevelRenderer.getLightColor(level,mesh.lightAt[i]);mesh.lightTime=time;}
            if(vc==null){type=RenderType.entityCutoutNoCull(TEXTURE);vc=mc.renderBuffers().bufferSource().getBuffer(type);m=pose.last().pose();nm=pose.last().normal();}
            int col=w.insulated()?(w.tier()==WireTier.COPPER?0xFF3B2E26:0xFF2C3034):w.tier().colour;
            int r=col>>16&255,g=col>>8&255,bl=col&255;
            float ox=(float)(a.x-cam.x),oy=(float)(a.y-cam.y),oz=(float)(a.z-cam.z);
            var d=mesh.data;
            for(int o=0,k=0;o<d.length;o+=8,k++)
                vc.vertex(m,d[o]+ox,d[o+1]+oy,d[o+2]+oz).color(r,g,bl,255).uv(d[o+3],d[o+4]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(mesh.light[k>>4]).normal(nm,d[o+5],d[o+6],d[o+7]).endVertex();
        }
        if(vc!=null)mc.renderBuffers().bufferSource().endBatch(type);
    }
    private static Vec3 point(Level level,BlockPos pos,int index){
        if(!level.isLoaded(pos))return null;
        var state=level.getBlockState(pos);
        if(!(state.getBlock() instanceof WireHost host)||index>=host.points(state))return null;
        return host.point(state,pos,index);
    }
    /** Sag of the middle of a wire: thin wires hang more, a little more the longer they are. */
    public static double sag(WireTier tier,double length){return WireTier.sag(tier,length);}

    /** Square tube from p to q, turned 45 degrees so it looks round enough at this thickness; writes 16 vertices (x y z u v nx ny nz). */
    private static int tube(float[] out,int o,Vec3 p,Vec3 q,float w,float v0,float v1){
        var d=q.subtract(p).normalize();
        var side=d.cross(new Vec3(0,1,0));if(side.lengthSqr()<1e-6)side=new Vec3(1,0,0);side=side.normalize();
        var up=side.cross(d).normalize();
        Vec3[] c=new Vec3[4];
        for(int k=0;k<4;k++){double ang=Math.PI/4+k*Math.PI/2;c[k]=side.scale(Math.cos(ang)*w).add(up.scale(Math.sin(ang)*w));}
        for(int k=0;k<4;k++){
            var o1=c[k];var o2=c[(k+1)%4];var nrm=o1.add(o2).normalize();
            float u0=k/4F,u1=(k+1)/4F;
            o=put(out,o,p.add(o1),u0,v0,nrm);o=put(out,o,q.add(o1),u0,v1,nrm);
            o=put(out,o,q.add(o2),u1,v1,nrm);o=put(out,o,p.add(o2),u1,v0,nrm);
        }
        return o;
    }
    private static int put(float[] out,int o,Vec3 v,float u,float vv,Vec3 n){
        out[o]=(float)v.x;out[o+1]=(float)v.y;out[o+2]=(float)v.z;out[o+3]=u;out[o+4]=vv;out[o+5]=(float)n.x;out[o+6]=(float)n.y;out[o+7]=(float)n.z;return o+8;
    }
}
