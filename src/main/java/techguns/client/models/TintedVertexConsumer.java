package techguns.client.models;
import com.mojang.blaze3d.vertex.VertexConsumer;
/** Preserves the legacy per-part colour applied to upgraded chainsaw blades. */
public final class TintedVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;private final float r,g,b,alpha;
    public TintedVertexConsumer(VertexConsumer delegate,float r,float g,float b){this(delegate,r,g,b,1);}
    public TintedVertexConsumer(VertexConsumer delegate,float r,float g,float b,float alpha){this.delegate=delegate;this.r=r;this.g=g;this.b=b;this.alpha=alpha;}
    @Override public VertexConsumer addVertex(float x,float y,float z){delegate.addVertex(x,y,z);return this;}
    @Override public VertexConsumer setColor(int r,int g,int b,int a){delegate.setColor(Math.round(r*this.r),Math.round(g*this.g),Math.round(b*this.b),Math.round(a*alpha));return this;}
    @Override public VertexConsumer setUv(float u,float v){delegate.setUv(u,v);return this;}
    @Override public VertexConsumer setUv1(int u,int v){delegate.setUv1(u,v);return this;}
    @Override public VertexConsumer setUv2(int u,int v){delegate.setUv2(u,v);return this;}
    @Override public VertexConsumer setNormal(float x,float y,float z){delegate.setNormal(x,y,z);return this;}
}
