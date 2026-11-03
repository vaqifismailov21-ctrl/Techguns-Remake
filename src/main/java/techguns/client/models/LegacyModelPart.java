package techguns.client.models;

import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;

/** Preserves ModelRenderer addBox UVs, pivots and Z/Y/X rotation order. */
public final class LegacyModelPart {
    private final ModelMultipart model;
    private final int textureU;
    private final int textureV;
    private float textureWidth;
    private float textureHeight;
    private final List<ModelPart.Cube> cubes = new ArrayList<>();
    public final List<ModelPart.Cube> cubeList=cubes;
    public boolean showModel=true,isHidden;
    private final List<LegacyModelPart> children=new ArrayList<>();
    public void addChild(LegacyModelPart child){children.add(child);}
    public List<LegacyModelPart> children(){return java.util.Collections.unmodifiableList(children);}
    public float rotateAngleX, rotateAngleY, rotateAngleZ;
    public float rotationPointX, rotationPointY, rotationPointZ;
    public float offsetX,offsetY,offsetZ;
    public boolean mirror;

    public LegacyModelPart(ModelMultipart model, int u, int v) {
        this.model = model;
        textureU = u;
        textureV = v;
        textureWidth = model.textureWidth;
        textureHeight = model.textureHeight;
    }

    public void addBox(float x, float y, float z, int width, int height, int depth) {
        addBox(x, y, z, width, height, depth, 0);
    }

    public void addBox(float x, float y, float z, int width, int height, int depth, float inflation) {
        // Capture UV dimensions/mirror when addBox is called, as ModelRenderer did in 1.12.2.
        cubes.add(new ModelPart.Cube(textureU, textureV, x, y, z, width, height, depth,
                inflation, inflation, inflation, mirror, textureWidth, textureHeight, EnumSet.allOf(Direction.class)));
    }

    public void setTextureSize(int width, int height) {
        textureWidth = width;
        textureHeight = height;
    }

    public void setRotationPoint(float x, float y, float z) {
        rotationPointX = x;
        rotationPointY = y;
        rotationPointZ = z;
    }

    public void render(float scale) {
        if(!showModel||isHidden)return;
        model.pose.pushPose();
        model.pose.translate(offsetX,offsetY,offsetZ);
        model.pose.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
        if (rotateAngleZ != 0) model.pose.mulPose(Axis.ZP.rotation(rotateAngleZ));
        if (rotateAngleY != 0) model.pose.mulPose(Axis.YP.rotation(rotateAngleY));
        if (rotateAngleX != 0) model.pose.mulPose(Axis.XP.rotation(rotateAngleX));
        float factor = scale * 16.0F;
        model.pose.pushPose();
        model.pose.scale(factor, factor, factor);
        for (ModelPart.Cube cube : cubes) {
            cube.compile(model.pose.last(), model.vertices, model.light, model.overlay, 1, 1, 1, 1);
        }
        model.pose.popPose();
        for(var child:children)child.render(scale);
        model.pose.popPose();
    }
}
