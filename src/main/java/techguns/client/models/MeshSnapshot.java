package techguns.client.models;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/** Copies rendered geometry, so cached vanilla models can change pose safely after capture. */
public final class MeshSnapshot implements VertexConsumer {
    public record Vertex(float x,float y,float z,float u,float v,float nx,float ny,float nz) {}
    public final List<Vertex> vertices=new ArrayList<>();
    public float minX=Float.POSITIVE_INFINITY,minY=minX,minZ=minX,maxX=Float.NEGATIVE_INFINITY,maxY=maxX,maxZ=maxX;
    private float x,y,z,u,v,nx,ny,nz;
    @Override public VertexConsumer vertex(double x,double y,double z){this.x=(float)x;this.y=(float)y;this.z=(float)z;return this;}
    @Override public VertexConsumer color(int r,int g,int b,int a){return this;}
    @Override public VertexConsumer uv(float u,float v){this.u=u;this.v=v;return this;}
    @Override public VertexConsumer overlayCoords(int u,int v){return this;}
    @Override public VertexConsumer uv2(int u,int v){return this;}
    @Override public VertexConsumer normal(float x,float y,float z){nx=x;ny=y;nz=z;return this;}
    @Override public void defaultColor(int r,int g,int b,int a){}
    @Override public void unsetDefaultColor(){}
    @Override public void endVertex(){vertices.add(new Vertex(x,y,z,u,v,nx,ny,nz));minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);minZ=Math.min(minZ,z);maxZ=Math.max(maxZ,z);}
    public float centerX(){return (minX+maxX)/2;}
    public float centerY(){return (minY+maxY)/2;}
    public float centerZ(){return (minZ+maxZ)/2;}
    public void render(PoseStack pose,VertexConsumer output,int light,int overlay,float r,float g,float b,float a){
        for(var p:vertices)output.vertex(pose.last().pose(),p.x,p.y,p.z).color(r,g,b,a).uv(p.u,p.v).overlayCoords(overlay).uv2(light).normal(pose.last().normal(),p.nx,p.ny,p.nz).endVertex();
    }
}
