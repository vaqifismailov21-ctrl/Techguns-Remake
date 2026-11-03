package techguns.client.models;
import com.mojang.blaze3d.vertex.VertexConsumer;
/** Preserves the legacy per-part colour applied to upgraded chainsaw blades. */
public final class TintedVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;private final float r,g,b,alpha;
    public TintedVertexConsumer(VertexConsumer delegate,float r,float g,float b){this(delegate,r,g,b,1);}
    public TintedVertexConsumer(VertexConsumer delegate,float r,float g,float b,float alpha){this.delegate=delegate;this.r=r;this.g=g;this.b=b;this.alpha=alpha;}
    @Override public VertexConsumer vertex(double x,double y,double z){delegate.vertex(x,y,z);return this;}
    @Override public VertexConsumer color(int r,int g,int b,int a){delegate.color(Math.round(r*this.r),Math.round(g*this.g),Math.round(b*this.b),Math.round(a*alpha));return this;}
    @Override public VertexConsumer uv(float u,float v){delegate.uv(u,v);return this;}
    @Override public VertexConsumer overlayCoords(int u,int v){delegate.overlayCoords(u,v);return this;}
    @Override public VertexConsumer uv2(int u,int v){delegate.uv2(u,v);return this;}
    @Override public VertexConsumer normal(float x,float y,float z){delegate.normal(x,y,z);return this;}
    @Override public void endVertex(){delegate.endVertex();}
    @Override public void defaultColor(int r,int g,int b,int a){delegate.defaultColor(Math.round(r*this.r),Math.round(g*this.g),Math.round(b*this.b),Math.round(a*alpha));}
    @Override public void unsetDefaultColor(){delegate.unsetDefaultColor();}
}
