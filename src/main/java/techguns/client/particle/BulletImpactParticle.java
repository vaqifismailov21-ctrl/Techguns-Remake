package techguns.client.particle;

import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.Random;
import techguns.client.render.fx.LegacyFxRenderTypes;

/** Normal, unattached TGParticle path: original tick arithmetic, HSB color and animated sprite UVs. */
public final class BulletImpactParticle extends Particle {
    private final BulletImpactDefinition definition;
    private final RenderType renderType;
    private final float velocityDamping, sizeDamping, angleDamping, animationSpeed,groundDamping;
    private final int variation;
    private float size, previousSize, sizeRate, angle, angleRate;
    private net.minecraft.world.entity.Entity follow;
    private Vec3 followPosition;
    private BulletImpactParticle previous,next;
    private java.util.function.Supplier<Vec3> anchor;
    private java.util.function.BooleanSupplier anchorAlive;
    public void stickTo(java.util.function.Supplier<Vec3> anchor,java.util.function.BooleanSupplier alive){this.anchor=anchor;anchorAlive=alive;}
    @Override public BulletImpactParticle scale(float amount){size*=amount;sizeRate*=amount;return this;}
    public Vec3 position() {return new Vec3(x,y,z);}
    private Vec3 interpolated(float partial) {return new Vec3(Mth.lerp(partial,xo,x),Mth.lerp(partial,yo,y),Mth.lerp(partial,zo,z));}
    public void linkPrevious(BulletImpactParticle particle) {previous=particle;if(particle!=null)particle.next=this;}
    public void addStartSize(float value) {size+=value;}
    public void follow(net.minecraft.world.entity.Entity entity) {follow=entity;followPosition=entity.position();}
    public BulletImpactParticle(ClientLevel level, Vec3 position, Vec3 motion, BulletImpactDefinition definition, Random random) {
        super(level,position.x,position.y,position.z);
        this.definition=definition; this.xd=motion.x; this.yd=motion.y; this.zd=motion.z;
        lifetime=definition.range("lifetime").sampleInt(random);
        size=definition.range("size").sample(random); previousSize=0;
        sizeRate=definition.range("sizerate").sample(random); sizeDamping=definition.range("sizeratedamping").sample(random);
        animationSpeed=definition.range("animationspeed").sample(random); velocityDamping=definition.range("velocitydamping").sample(random);
        groundDamping=definition.ranges.containsKey("velocitydampingonground")?definition.range("velocitydampingonground").sample(random):1;
        angle=definition.range("angle").sample(random); angleRate=definition.range("anglerate").sample(random);
        angleDamping=definition.range("angleratedamping").sample(random); variation=random.nextInt(definition.frames);
        renderType=LegacyFxRenderTypes.sprite(definition.texture,definition.additive,false,definition.noZTest);
        hasPhysics=false;
    }
    @Override public void tick() {
        xo=x; yo=y; zo=z; previousSize=size;
        if (age++ >= lifetime) { remove(); return; }
        if(anchor!=null){if(!anchorAlive.getAsBoolean()){remove();return;}var p=anchor.get();setPos(p.x,p.y,p.z);}
        if(follow!=null) {var movement=follow.position().subtract(followPosition);setPos(x+movement.x,y+movement.y,z+movement.z);followPosition=follow.position();}
        // Original gravity is applied to this tick's movement, not accumulated into velY.
        var destination=new Vec3(x+xd,y+yd-definition.gravity,z+zd);
        if(definition.removeOnGround&&level.clip(new net.minecraft.world.level.ClipContext(position(),destination,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty())).getType()!=net.minecraft.world.phys.HitResult.Type.MISS){remove();return;}
        setPos(destination.x,destination.y,destination.z);
        xd*=velocityDamping; yd*=velocityDamping; zd*=velocityDamping;
        if(definition.ranges.containsKey("velocitydampingonground")){
            var hit=level.clip(new net.minecraft.world.level.ClipContext(new Vec3(xo,yo,zo),destination,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty()));
            if(hit.getType()!=net.minecraft.world.phys.HitResult.Type.MISS){setPos(hit.getLocation().x,hit.getLocation().y+.001,hit.getLocation().z);xd*=groundDamping;yd*=groundDamping;zd*=groundDamping;}
        }
        size=Math.max(0,size+sizeRate); sizeRate*=sizeDamping;
        angle=(angle+angleRate)%360; angleRate*=angleDamping;
    }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.CUSTOM; }
    /** Original TGParticleItemAttached geometry: weapon-local front quad and third-person crossed quads. */
    public void renderAttached(com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,float partial,boolean third) {
        float progress=(age+partial)/lifetime;
        int frame=definition.variations?variation:(int)(definition.frames*progress*animationSpeed)%definition.frames;
        float u1=frame%definition.columns/(float)definition.columns,u2=u1+1F/definition.columns,v1=frame/definition.columns/(float)definition.columns,v2=v1+1F/definition.columns;
        float radius=0.1F*Mth.lerp(partial,previousSize,size),rotation=(angle+partial*angleRate)*(float)Math.PI/180;
        var centre=interpolated(partial);Vector3f[] points={new Vector3f(-radius,-radius,0),new Vector3f(-radius,radius,0),new Vector3f(radius,radius,0),new Vector3f(radius,-radius,0)};
        if(rotation>0.0001F)for(var p:points)p.rotateZ(-rotation);
        float[][] uv={{u2,v2},{u2,v1},{u1,v1},{u1,v2}};int rgb=definition.rgb(progress);float alpha=definition.alpha(progress);
        var vertices=buffers.getBuffer(renderType);
        for(int plane=0;plane<(third?3:1);plane++)for(int i=0;i<4;i++) {
            var p=points[i];float px=(float)centre.x+p.x,py=(float)centre.y+p.y,pz=(float)centre.z+p.z;
            if(plane==1) {float swap=px;px=pz;pz=swap;}
            if(plane==2) {py=(float)centre.y;pz=-points[(i+1)%4].x+(float)centre.x;}
            vertices.addVertex(pose.last().pose(),px,py,pz).setColor((rgb>>16&255)/255F*rCol,(rgb>>8&255)/255F*gCol,(rgb&255)/255F*bCol,alpha).setUv(uv[i][0],uv[i][1]);
        }
    }
    @Override public void render(VertexConsumer unused, Camera camera, float partial) {
        float progress=(age+partial)/lifetime;
        int frame=definition.variations ? variation : (int)(definition.frames*progress*animationSpeed)%definition.frames;
        float u1=frame%definition.columns/(float)definition.columns,u2=u1+1F/definition.columns;
        // Original uses columns for both UV denominators; all normal impact sheets are square.
        float v1=frame/definition.columns/(float)definition.columns,v2=v1+1F/definition.columns;
        float radius=0.1F*Mth.lerp(partial,previousSize,size);
        var cameraPos=camera.getPosition();
        float px=(float)(Mth.lerp(partial,xo,x)-cameraPos.x),py=(float)(Mth.lerp(partial,yo,y)-cameraPos.y),pz=(float)(Mth.lerp(partial,zo,z)-cameraPos.z);
        float rotation=(angle+partial*angleRate)*(float)Math.PI/180;
        var quaternion=new Quaternionf(camera.rotation());
        // TGParticle only rotates positive angles; preserve that original branch.
        if (rotation>0.0001F) quaternion.rotateZ(-rotation);
        Vector3f[] corners={new Vector3f(-1,-1,0),new Vector3f(-1,1,0),new Vector3f(1,1,0),new Vector3f(1,-1,0)};
        if(definition.groundAligned) {
            corners=new Vector3f[]{new Vector3f(-1,0,-1),new Vector3f(1,0,-1),new Vector3f(1,0,1),new Vector3f(-1,0,1)};
            quaternion=new Quaternionf();if(rotation>0.0001F)quaternion.rotateY(-rotation);
        }
        float[][] uv={{u2,v2},{u2,v1},{u1,v1},{u1,v2}};
        int rgb=definition.rgb(progress); float alpha=definition.alpha(progress);
        if(definition.streak && (previous==null||!previous.isAlive()))return;
        if(definition.streak && next==null)alpha=0;
        Vector3f[] trail=null;
        if(definition.streak) {
            var here=interpolated(partial).subtract(cameraPos);var there=previous.interpolated(partial).subtract(cameraPos);
            var view=new Vector3f(0,0,-1).rotate(camera.rotation());
            var width=new Vec3(view.x,view.y,view.z).cross(there.subtract(here).normalize()).normalize();
            double oldRadius=0.1F*Mth.lerp(partial,previous.previousSize,previous.size);
            var points=new Vec3[]{here.add(width.scale(radius)),here.subtract(width.scale(radius)),there.subtract(width.scale(oldRadius)),there.add(width.scale(oldRadius))};
            trail=new Vector3f[4];for(int i=0;i<4;i++)trail[i]=new Vector3f((float)points[i].x,(float)points[i].y,(float)points[i].z);
        }
        renderType.setupRenderState();
        var builder=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX_COLOR);
        for(int i=0;i<4;i++) {
            var p=trail==null?corners[i].rotate(quaternion).mul(radius).add(px,py,pz):trail[i];
            builder.addVertex(p.x,p.y,p.z).setColor((rgb>>16&255)/255F*rCol,(rgb>>8&255)/255F*gCol,(rgb&255)/255F*bCol,alpha).setUv(uv[i][0],uv[i][1]);
        }
        BufferUploader.drawWithShader(builder.buildOrThrow()); renderType.clearRenderState();
    }
}
