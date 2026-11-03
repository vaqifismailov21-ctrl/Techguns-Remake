package techguns.client.models;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
/** Direct adapter for the original OBJ mesh, retaining its coordinates, UVs and drum pivot. */
public final class LegacyObjModel extends ModelMultipart {
    private record Vertex(float x,float y,float z,float u,float v,float nx,float ny,float nz) {}
    private final String[] files;
    private final boolean flipV,drum;
    private List<List<Vertex>> meshes;
    public LegacyObjModel(boolean flipV,boolean drum,String... files) {this.files=files;this.flipV=flipV;this.drum=drum;}
    private void load() {
        var loaded=new ArrayList<List<Vertex>>();
        for(var file:files) {
            var positions=new ArrayList<float[]>();var uvs=new ArrayList<float[]>();var normals=new ArrayList<float[]>();var mesh=new ArrayList<Vertex>();
            var location=ResourceLocation.fromNamespaceAndPath("techguns","models/item/"+file+".obj");
            try(var reader=new BufferedReader(new InputStreamReader(Minecraft.getInstance().getResourceManager().open(location),StandardCharsets.UTF_8))) {
                for(String line;(line=reader.readLine())!=null;) {
                    var a=line.trim().split("\\s+");
                    switch(a[0]) {
                        case "v","vn" -> {var values=new float[]{Float.parseFloat(a[1]),Float.parseFloat(a[2]),Float.parseFloat(a[3])};(a[0].equals("v")?positions:normals).add(values);}
                        case "vt" -> uvs.add(new float[]{Float.parseFloat(a[1]),Float.parseFloat(a[2])});
                        case "f" -> {
                            var first=positions.get(index(a[1].split("/",-1)[0],positions.size()));var second=positions.get(index(a[2].split("/",-1)[0],positions.size()));var third=positions.get(index(a[3].split("/",-1)[0],positions.size()));
                            var faceNormal=new org.joml.Vector3f(second[0]-first[0],second[1]-first[1],second[2]-first[2]).cross(third[0]-first[0],third[1]-first[1],third[2]-first[2]);if(faceNormal.lengthSquared()>1e-12f)faceNormal.normalize();else faceNormal.set(0,1,0);
                            // OBJ faces are triangulated into degenerate quads for the entity buffer format.
                            for(int i=2;i<a.length-1;i++)for(var index:new String[]{a[1],a[i],a[i+1],a[i+1]}) {
                                var indices=index.split("/",-1);var p=positions.get(index(indices[0],positions.size()));var uv=indices.length>1&&!indices[1].isEmpty()?uvs.get(index(indices[1],uvs.size())):new float[]{0,0};var n=indices.length>2&&!indices[2].isEmpty()?normals.get(index(indices[2],normals.size())):new float[]{faceNormal.x,faceNormal.y,faceNormal.z};
                                mesh.add(new Vertex(p[0],p[1],p[2],uv[0],flipV?1-uv[1]:uv[1],n[0],n[1],n[2]));
                            }
                        }
                    }
                }
            } catch(IOException error) {throw new IllegalStateException("Missing original mesh "+location,error);}
            loaded.add(mesh);
        }
        meshes=loaded;
    }
    private static int index(String value,int length){int index=Integer.parseInt(value);return index<0?length+index:index-1;}
    @Override public void render(Entity entity,float a,float b,float c,float d,float e,float scale,int ammo,float reload,ItemDisplayContext context,int part,float fire,float charge) {
        if(meshes==null)load();
        for(int i=0;i<meshes.size();i++) {
            pushMatrix();if(drum&&i==1) {translate(0,-2,0);rotate(60*fire,1,0,0);translate(0,2,0);}
            for(var v:meshes.get(i))vertices.vertex(pose.last().pose(),v.x,v.y,v.z).color(255,255,255,255).uv(v.u,v.v).overlayCoords(overlay).uv2(light).normal(pose.last().normal(),v.nx,v.ny,v.nz).endVertex();
            popMatrix();
        }
    }
}
