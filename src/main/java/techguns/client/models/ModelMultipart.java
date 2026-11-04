package techguns.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;

/** Rendering adapter for the original Techguns multipart geometry. Client only. */
public abstract class ModelMultipart {
    public int textureWidth = 64;
    public int textureHeight = 32;
    /** Counts cubes so coplanar faces of different boxes get a stable, sub-pixel depth order. */
    int cubeOrder;
    PoseStack pose;
    VertexConsumer vertices;
    int light;
    int overlay;
    private final java.util.Deque<Integer> savedLights=new java.util.ArrayDeque<>();
    private java.util.function.BiFunction<Boolean,Boolean,VertexConsumer> layers;
    private boolean alphaLayer,doubleSided;
    /** Render each root through the full model path, retaining its original transforms and UVs. */
    public final java.util.List<MeshSnapshot> captureParts(java.util.function.Consumer<VertexConsumer> render){
        var roots=new java.util.LinkedHashSet<LegacyModelPart>();
        for(Class<?> type=getClass();type!=null&&type!=Object.class;type=type.getSuperclass())for(var field:type.getDeclaredFields()){
            if(java.lang.reflect.Modifier.isStatic(field.getModifiers()))continue;
            if(field.getType()!=LegacyModelPart.class&&field.getType()!=LegacyModelPart[].class)continue;
            try{field.setAccessible(true);Object value=field.get(this);if(value instanceof LegacyModelPart part)roots.add(part);else if(value instanceof LegacyModelPart[] parts)for(var part:parts)if(part!=null)roots.add(part);}
            catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot capture legacy body parts",error);}
        }
        var children=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<LegacyModelPart,Boolean>());
        for(var root:roots)collectChildren(root,children);roots.removeAll(children);
        var visibility=new java.util.IdentityHashMap<LegacyModelPart,Boolean>();for(var part:roots)visibility.put(part,part.showModel);
        var meshes=new java.util.ArrayList<MeshSnapshot>();
        try{for(var selected:roots){for(var part:roots)part.showModel=part==selected&&visibility.get(part);var mesh=new MeshSnapshot();render.accept(mesh);if(!mesh.vertices.isEmpty())meshes.add(mesh);}}
        finally{visibility.forEach((part,visible)->part.showModel=visible);}
        return meshes;
    }
    private static void collectChildren(LegacyModelPart parent,java.util.Set<LegacyModelPart> children){for(var child:parent.children())if(children.add(child))collectChildren(child,children);}
    public void setLayers(java.util.function.BiFunction<Boolean,Boolean,VertexConsumer> layers){this.layers=layers;alphaLayer=doubleSided=false;}
    protected void useAlphaLayer(boolean value){alphaLayer=value;if(layers!=null)vertices=layers.apply(alphaLayer,doubleSided);}
    protected void useDoubleSidedLayer(boolean value){doubleSided=value;if(layers!=null)vertices=layers.apply(alphaLayer,doubleSided);}

    public final void renderModel(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                                  ItemDisplayContext context) {
        renderModel(pose, vertices, light, overlay, context, null, 0, 0, 0, 0, 0);
    }

    public final void renderModel(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                                  ItemDisplayContext context, net.minecraft.world.entity.LivingEntity entity,
                                  int ammo, float reload, int part, float fire, float charge) {
        this.pose = pose;
        this.vertices = vertices;
        this.light = light;
        this.overlay = overlay;
        try {
            render(entity, 0, 0, 0, 0, 0, 0.0625F, ammo, reload, context, part, fire, charge);
        } finally {
            this.pose = null;
            this.vertices = null;
        }
    }

    protected void pushMatrix() { pose.pushPose(); }
    public final void renderAnimatedModel(PoseStack pose,VertexConsumer vertices,int light,int overlay,ItemDisplayContext context,net.minecraft.world.entity.LivingEntity entity,float limb,float amount,float age,float yaw,float pitch){
        this.pose=pose;this.vertices=vertices;this.light=light;this.overlay=overlay;
        try{render(entity,limb,amount,age,yaw,pitch,.0625f,0,0,context,0,0,0);}finally{this.pose=null;this.vertices=null;}
    }
    /** A nested ModelBase used global render state in 1.12; explicitly pass that state to its adapter. */
    public final void renderInherited(ModelMultipart parent,Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale,int ammo,float reload,ItemDisplayContext context,int part,float fire,float charge) {
        var previousPose=pose;var previousVertices=vertices;int previousLight=light,previousOverlay=overlay;
        pose=parent.pose;vertices=parent.vertices;light=parent.light;overlay=parent.overlay;
        try {render(entity,limb,amount,age,yaw,pitch,scale,ammo,reload,context,part,fire,charge);}
        finally {pose=previousPose;vertices=previousVertices;light=previousLight;overlay=previousOverlay;}
    }
    protected void popMatrix() { pose.popPose(); }
    protected void translate(double x, double y, double z) { pose.translate(x, y, z); }
    protected void scale(double x,double y,double z) { pose.scale((float)x,(float)y,(float)z); }
    protected void rotate(float angle,float x,float y,float z) {
        pose.mulPose(new org.joml.Quaternionf().rotationAxis((float)Math.toRadians(angle),new org.joml.Vector3f(x,y,z).normalize()));
    }
    protected void enableFXLighting() { savedLights.push(light);light=net.minecraft.client.renderer.LightTexture.FULL_BRIGHT; }
    protected void disableFXLighting() { light=savedLights.pop(); }
    /** ModelBase's default 1.12.2 rotation hook was empty; several original item models call it. */
    protected void setRotationAngles(float limbSwing,float limbAmount,float age,float yaw,float pitch,float scale,Entity entity) {}

    public abstract void render(Entity entity, float limbSwing, float limbSwingAmount, float age,
                                float yaw, float pitch, float scale, int ammo, float reload,
                                ItemDisplayContext context, int part, float fire, float charge);

    protected void setRotation(LegacyModelPart model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
