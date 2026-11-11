package techguns.client.render.bbmodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * A Blockbench model (.bbmodel, "Generic Model" format) drawn as it is: cubes with per-face UVs, groups with pivots
 * and rotations, the textures stored inside the file, and the animations made in Blockbench.
 * Conventions for plant models: a texture whose name starts with "glow" is drawn fullbright; a group named
 * "on_only" is drawn only while the plant runs, "off_only" only while it stands still; the animation "working" loops
 * while the plant runs and "idle" while it does not.
 */
public final class BbModel {
    /** Light at a point of the model, in blocks from the model origin. */
    public interface Light {int at(float x,float y,float z);}

    private record Face(float[] uv,int texture,int rotation){}
    private record Cube(float[] from,float[] to,float[] origin,float[] rotation,Face[] faces,boolean visible){}
    private static final class Group{String name;float[] origin=new float[3],rotation=new float[3];boolean visible=true;String uuid;final List<Object> children=new ArrayList<>();}
    private record Key(float time,float[] value,boolean step){}
    private static final class Animation{String name;float length;boolean loop;final Map<String,Map<String,List<Key>>> bones=new HashMap<>();}
    private static final class Tex{ResourceLocation id;DynamicTexture texture;float w,h;boolean glow,translucent;}

    private final List<Tex> textures=new ArrayList<>();
    private final Map<String,Cube> cubes=new HashMap<>();
    private final List<Object> roots=new ArrayList<>();
    private final Map<String,Animation> animations=new HashMap<>();

    /** Reads a .bbmodel; textures are uploaded under techguns:dynamic/<key>_<n>. Call on the render thread. */
    public static BbModel read(JsonObject json,String key,Function<String,NativeImage> external){
        var m=new BbModel();
        int n=0;
        for(var e:arr(json,"textures")){
            var o=e.getAsJsonObject();var t=new Tex();String name=str(o,"name","texture");
            NativeImage image=null;
            try{
                String src=str(o,"source","");
                if(src.startsWith("data:image")){byte[] bytes=Base64.getDecoder().decode(src.substring(src.indexOf(',')+1));image=NativeImage.read(new ByteArrayInputStream(bytes));}
                else image=external.apply(name);
            }catch(Exception ex){image=null;}
            if(image==null){image=new NativeImage(1,1,false);image.setPixelRGBA(0,0,0xFFFF00FF);}
            t.translucent=translucent(image);
            t.w=o.has("uv_width")?o.get("uv_width").getAsFloat():image.getWidth();t.h=o.has("uv_height")?o.get("uv_height").getAsFloat():image.getHeight();
            t.glow=name.toLowerCase().startsWith("glow");
            t.texture=new DynamicTexture(image);
            t.id=Minecraft.getInstance().getTextureManager().register("techguns_bbmodel_"+key+"_"+(n++),t.texture);
            m.textures.add(t);
        }
        for(var e:arr(json,"elements")){
            var o=e.getAsJsonObject();if(!"cube".equals(str(o,"type","cube")))continue;
            Face[] faces=new Face[6];var fo=o.getAsJsonObject("faces");
            String[] dirs={"north","south","west","east","up","down"};
            for(int i=0;i<6;i++){
                if(fo==null||!fo.has(dirs[i]))continue;var f=fo.getAsJsonObject(dirs[i]);
                if(!f.has("texture")||f.get("texture").isJsonNull())continue;
                faces[i]=new Face(vec(f,"uv",4),f.get("texture").getAsInt(),f.has("rotation")?f.get("rotation").getAsInt():0);
            }
            m.cubes.put(str(o,"uuid",""),new Cube(vec(o,"from",3),vec(o,"to",3),vec(o,"origin",3),vec(o,"rotation",3),faces,!o.has("visibility")||o.get("visibility").getAsBoolean()));
        }
        for(var e:arr(json,"outliner"))m.roots.add(node(e));
        if(json.has("outliner")&&json.getAsJsonArray("outliner").size()==0)m.roots.addAll(m.cubes.keySet());
        for(var e:arr(json,"animations")){
            var o=e.getAsJsonObject();var a=new Animation();a.name=str(o,"name","");a.length=o.has("length")?o.get("length").getAsFloat():0;a.loop="loop".equals(str(o,"loop",""));
            var an=o.getAsJsonObject("animators");
            if(an!=null)for(var b:an.entrySet()){
                var channels=new HashMap<String,List<Key>>();
                for(var k:arr(b.getValue().getAsJsonObject(),"keyframes")){
                    var ko=k.getAsJsonObject();var dp=arr(ko,"data_points");if(dp.isEmpty())continue;var p=dp.get(0).getAsJsonObject();
                    channels.computeIfAbsent(str(ko,"channel","rotation"),c->new ArrayList<>())
                        .add(new Key(ko.get("time").getAsFloat(),new float[]{num(p,"x"),num(p,"y"),num(p,"z")},"step".equals(str(ko,"interpolation","linear"))));
                }
                channels.values().forEach(l->l.sort((x,y)->Float.compare(x.time,y.time)));
                a.bones.put(b.getKey(),channels);
            }
            m.animations.put(a.name,a);
        }
        return m;
    }
    private static Object node(JsonElement e){
        if(e.isJsonPrimitive())return e.getAsString();
        var o=e.getAsJsonObject();var g=new Group();g.name=str(o,"name","");g.uuid=str(o,"uuid","");g.origin=vec(o,"origin",3);g.rotation=vec(o,"rotation",3);
        g.visible=!o.has("visibility")||o.get("visibility").getAsBoolean();
        for(var c:arr(o,"children"))g.children.add(node(c));
        return g;
    }
    public void close(){for(var t:textures)Minecraft.getInstance().getTextureManager().release(t.id);}
    public boolean hasAnimation(String name){return animations.containsKey(name);}

    /**
     * Draws the model with its origin (block corner, 16 px per block) at the current pose origin.
     * running picks the on_only/off_only groups and the working/idle animation; seconds is the animation clock.
     */
    public void render(PoseStack pose,MultiBufferSource buffers,boolean running,float seconds,Light light){
        var anim=animations.get(running?"working":"idle");
        pose.pushPose();pose.scale(1/16F,1/16F,1/16F);
        // one pass per texture, so a buffer is never left half written for another one (translucent ones last)
        for(int pass=0;pass<2;pass++)for(int i=0;i<textures.size();i++){
            var t=textures.get(i);if(t.translucent!=(pass==1))continue;
            RenderType type=t.glow?RenderType.eyes(t.id):t.translucent?RenderType.entityTranslucent(t.id):RenderType.entityCutout(t.id);
            var vc=buffers.getBuffer(type);
            for(var r:roots)draw(r,pose,vc,i,t,running,anim,seconds,light);
        }
        pose.popPose();
    }
    private void draw(Object node,PoseStack pose,VertexConsumer vc,int tex,Tex t,boolean running,Animation anim,float seconds,Light light){
        if(node instanceof String id){var c=cubes.get(id);if(c!=null&&c.visible)cube(c,pose,vc,tex,t,light);return;}
        var g=(Group)node;
        if(!g.visible||"on_only".equals(g.name)&&!running||"off_only".equals(g.name)&&running)return;
        pose.pushPose();
        pose.translate(g.origin[0],g.origin[1],g.origin[2]);
        if(anim!=null){
            var ch=anim.bones.get(g.uuid);
            if(ch!=null){
                float time=anim.loop&&anim.length>0?seconds%anim.length:Math.min(seconds,anim.length);
                float[] p=sample(ch.get("position"),time,0);if(p!=null)pose.translate(-p[0],p[1],p[2]);
                float[] r=sample(ch.get("rotation"),time,0);
                if(r!=null){pose.mulPose(Axis.ZP.rotationDegrees(r[2]));pose.mulPose(Axis.YP.rotationDegrees(-r[1]));pose.mulPose(Axis.XP.rotationDegrees(-r[0]));}
                float[] s=sample(ch.get("scale"),time,1);if(s!=null)pose.scale(s[0],s[1],s[2]);
            }
        }
        pose.mulPose(Axis.ZP.rotationDegrees(g.rotation[2]));pose.mulPose(Axis.YP.rotationDegrees(g.rotation[1]));pose.mulPose(Axis.XP.rotationDegrees(g.rotation[0]));
        pose.translate(-g.origin[0],-g.origin[1],-g.origin[2]);
        for(var c:g.children)draw(c,pose,vc,tex,t,running,anim,seconds,light);
        pose.popPose();
    }
    private static float[] sample(List<Key> keys,float time,float rest){
        if(keys==null||keys.isEmpty())return null;
        if(time<=keys.get(0).time)return keys.get(0).value;
        for(int i=0;i+1<keys.size();i++){
            var a=keys.get(i);var b=keys.get(i+1);
            if(time<b.time){
                if(b.step||a.step)return a.value;
                float k=(time-a.time)/Math.max(1e-6F,b.time-a.time);
                return new float[]{a.value[0]+(b.value[0]-a.value[0])*k,a.value[1]+(b.value[1]-a.value[1])*k,a.value[2]+(b.value[2]-a.value[2])*k};
            }
        }
        return keys.get(keys.size()-1).value;
    }
    private void cube(Cube c,PoseStack pose,VertexConsumer vc,int tex,Tex t,Light light){
        boolean any=false;for(var f:c.faces)if(f!=null&&f.texture==tex)any=true;
        if(!any)return;
        pose.pushPose();
        if(c.rotation[0]!=0||c.rotation[1]!=0||c.rotation[2]!=0){
            pose.translate(c.origin[0],c.origin[1],c.origin[2]);
            pose.mulPose(Axis.ZP.rotationDegrees(c.rotation[2]));pose.mulPose(Axis.YP.rotationDegrees(c.rotation[1]));pose.mulPose(Axis.XP.rotationDegrees(c.rotation[0]));
            pose.translate(-c.origin[0],-c.origin[1],-c.origin[2]);
        }
        float x0=c.from[0],y0=c.from[1],z0=c.from[2],x1=c.to[0],y1=c.to[1],z1=c.to[2];
        int l=t.glow?0xF000F0:light.at((x0+x1)/32F,(y0+y1)/32F+0.01F,(z0+z1)/32F);
        var m=pose.last().pose();var nm=pose.last().normal();
        // corners of each face as Minecraft block models lay them out (u to the right, v down, seen from outside)
        float[][][] q={
            {{x1,y1,z0},{x0,y1,z0},{x0,y0,z0},{x1,y0,z0}},{{x0,y1,z1},{x1,y1,z1},{x1,y0,z1},{x0,y0,z1}},
            {{x0,y1,z0},{x0,y1,z1},{x0,y0,z1},{x0,y0,z0}},{{x1,y1,z1},{x1,y1,z0},{x1,y0,z0},{x1,y0,z1}},
            {{x0,y1,z0},{x1,y1,z0},{x1,y1,z1},{x0,y1,z1}},{{x0,y0,z1},{x1,y0,z1},{x1,y0,z0},{x0,y0,z0}}};
        float[][] normals={{0,0,-1},{0,0,1},{-1,0,0},{1,0,0},{0,1,0},{0,-1,0}};
        for(int i=0;i<6;i++){
            var f=c.faces[i];if(f==null||f.texture!=tex)continue;
            float u0=f.uv[0]/t.w,v0=f.uv[1]/t.h,u1=f.uv[2]/t.w,v1=f.uv[3]/t.h;
            float[][] uv={{u0,v0},{u1,v0},{u1,v1},{u0,v1}};
            int rot=((f.rotation%360)+360)%360/90;
            // the corners above run clockwise seen from outside: emit them backwards (front faces are counter-clockwise)
            for(int k=3;k>=0;k--){
                var p=q[i][k];var w=uv[(k+rot)%4];
                vc.vertex(m,p[0],p[1],p[2]).color(255,255,255,255).uv(w[0],w[1]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(l).normal(nm,normals[i][0],normals[i][1],normals[i][2]).endVertex();
            }
        }
        pose.popPose();
    }
    private static boolean translucent(NativeImage image){
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int a=image.getPixelRGBA(x,y)>>>24;if(a>8&&a<247)return true;}
        return false;
    }
    private static List<JsonElement> arr(JsonObject o,String key){
        var list=new ArrayList<JsonElement>();if(o!=null&&o.has(key)&&o.get(key).isJsonArray())for(var e:(JsonArray)o.get(key))list.add(e);return list;
    }
    private static String str(JsonObject o,String key,String def){return o.has(key)&&o.get(key).isJsonPrimitive()?o.get(key).getAsString():def;}
    private static float[] vec(JsonObject o,String key,int n){
        float[] v=new float[n];if(!o.has(key)||!o.get(key).isJsonArray())return v;var a=o.getAsJsonArray(key);
        for(int i=0;i<n&&i<a.size();i++)v[i]=a.get(i).getAsFloat();return v;
    }
    /** A keyframe value: a number, or a Molang text that is a plain number (anything else counts as 0). */
    private static float num(JsonObject o,String key){
        if(!o.has(key))return 0;
        try{return Float.parseFloat(o.get(key).getAsString().trim());}catch(Exception e){return 0;}
    }
}
