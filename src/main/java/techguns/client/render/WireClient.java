package techguns.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
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

    public static void receive(ResourceKey<Level> dim,int mode,List<WireConnection> list){
        if(mode==0){dimension=dim;WIRES.clear();WIRES.addAll(list);return;}
        if(!dim.equals(dimension))return;
        if(mode==1)WIRES.addAll(list);
        else WIRES.removeIf(w->list.stream().anyMatch(w::sameEnds));
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){WIRES.clear();dimension=null;}

    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS||WIRES.isEmpty())return;
        var mc=Minecraft.getInstance();var level=mc.level;if(level==null||!level.dimension().equals(dimension))return;
        var cam=event.getCamera().getPosition();var pose=event.getPoseStack();var buffers=mc.renderBuffers().bufferSource();
        var type=RenderType.entityCutoutNoCull(TEXTURE);var vc=buffers.getBuffer(type);
        pose.pushPose();pose.translate(-cam.x,-cam.y,-cam.z);
        for(var w:WIRES){
            Vec3 a=point(level,w.a().pos(),w.a().point()),b=point(level,w.b().pos(),w.b().point());
            if(a==null||b==null)continue;
            var mid=a.add(b).scale(0.5);if(mid.distanceToSqr(cam)>200*200)continue;
            cable(pose,vc,level,a,b,w.tier());
        }
        pose.popPose();
        buffers.endBatch(type);
    }
    private static Vec3 point(Level level,BlockPos pos,int index){
        if(!level.isLoaded(pos))return null;
        var state=level.getBlockState(pos);
        if(!(state.getBlock() instanceof WireHost host)||index>=host.points(state))return null;
        return host.point(state,pos,index);
    }
    /** Sag of the middle of a wire: thin wires hang more, a little more the longer they are. */
    public static double sag(WireTier tier,double length){return WireTier.sag(tier,length);}

    private static void cable(PoseStack pose,VertexConsumer vc,Level level,Vec3 a,Vec3 b,WireTier tier){
        double length=a.distanceTo(b),sag=sag(tier,length);
        int n=Math.max(6,Math.min(48,(int)(length*2.5)));
        int r=tier.colour>>16&255,g=tier.colour>>8&255,bl=tier.colour&255;float w=tier.thickness;
        Vec3 prev=a;double v0=0;
        for(int i=1;i<=n;i++){
            double t=i/(double)n;
            var p=a.lerp(b,t).add(0,-sag*4*t*(1-t),0);
            int light=LevelRenderer.getLightColor(level,BlockPos.containing(prev.add(p).scale(0.5)));
            double v1=v0+prev.distanceTo(p);
            tube(pose,vc,prev,p,w,(float)v0,(float)v1,light,r,g,bl);
            prev=p;v0=v1;
        }
    }
    /** Square tube from p to q, turned 45 degrees so it looks round enough at this thickness. */
    static void tube(PoseStack pose,VertexConsumer vc,Vec3 p,Vec3 q,float w,float v0,float v1,int light,int r,int g,int b){
        var d=q.subtract(p).normalize();
        var side=d.cross(new Vec3(0,1,0));if(side.lengthSqr()<1e-6)side=new Vec3(1,0,0);side=side.normalize();
        var up=side.cross(d).normalize();
        var m=pose.last().pose();var nm=pose.last().normal();
        Vec3[] o=new Vec3[4];
        for(int k=0;k<4;k++){double ang=Math.PI/4+k*Math.PI/2;o[k]=side.scale(Math.cos(ang)*w).add(up.scale(Math.sin(ang)*w));}
        for(int k=0;k<4;k++){
            var o1=o[k];var o2=o[(k+1)%4];var nrm=o1.add(o2).normalize();
            float u0=k/4F,u1=(k+1)/4F;
            put(vc,m,nm,p.add(o1),u0,v0,light,r,g,b,nrm);put(vc,m,nm,q.add(o1),u0,v1,light,r,g,b,nrm);
            put(vc,m,nm,q.add(o2),u1,v1,light,r,g,b,nrm);put(vc,m,nm,p.add(o2),u1,v0,light,r,g,b,nrm);
        }
    }
    private static void put(VertexConsumer vc,org.joml.Matrix4f m,org.joml.Matrix3f nm,Vec3 v,float u,float vv,int light,int r,int g,int b,Vec3 n){
        vc.vertex(m,(float)v.x,(float)v.y,(float)v.z).color(r,g,b,255).uv(u,vv).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nm,(float)n.x,(float)n.y,(float)n.z).endVertex();
    }
}
