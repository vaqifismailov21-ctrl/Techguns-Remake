package techguns.client.render.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import techguns.client.ClientWeaponEvents;

/** ScreenEffect.muzzleFlash_gun and the exact pistol settings from original ClientProxy. */
public final class PistolMuzzleFlash {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("techguns", "textures/fx/muzzleflashnew_2_add_2.png");
    public static final ResourceLocation RIFLE_TEXTURE = ResourceLocation.fromNamespaceAndPath("techguns", "textures/fx/muzzleflashnew_add.png");
    public record Specification(ResourceLocation texture,int frames,float rightX,float leftX,float y,float z,float scale,
                                float jitterX,float jitterY,float jitterScale,float thirdY,float thirdZ,
                                int columns,int rows,boolean flipX,boolean flipY,float red,float green,float blue,boolean smoothFade) {
        public Specification(ResourceLocation texture,int frames,float rightX,float leftX,float y,float z,float scale,
                             float jitterX,float jitterY,float jitterScale,float thirdY,float thirdZ,int columns,int rows,boolean flipX,boolean flipY) {
            this(texture,frames,rightX,leftX,y,z,scale,jitterX,jitterY,jitterScale,thirdY,thirdZ,columns,rows,flipX,flipY,1,1,1,false);
        }
        public Specification tinted(float red,float green,float blue,boolean smooth) {return new Specification(texture,frames,rightX,leftX,y,z,scale,jitterX,jitterY,jitterScale,thirdY,thirdZ,columns,rows,flipX,flipY,red,green,blue,smooth);}
        public Specification(ResourceLocation texture,int frames,float rightX,float leftX,float y,float z,float scale,
                             float jitterX,float jitterY,float jitterScale,float thirdY,float thirdZ) {
            this(texture,frames,rightX,leftX,y,z,scale,jitterX,jitterY,jitterScale,thirdY,thirdZ,4,4,false,false);
        }
    }
    public static final Specification PISTOL = new Specification(TEXTURE,11,0.03F,-0.03F,0.2F,-0.5F,0.55F,0.01F,0.01F,0.05F,0.07F,-0.26F);
    public static void render(PoseStack pose, MultiBufferSource buffers, float progress, boolean left, boolean thirdPerson) {
        render(PISTOL,pose,buffers,progress,left,thirdPerson);
    }
    public static void render(Specification spec,PoseStack pose, MultiBufferSource buffers, float progress, boolean left, boolean thirdPerson) {
        if(spec==null) return;
        if(spec.texture.getPath().endsWith("/tfg_flare.png")){
            if(progress>0&&progress<0.75F)renderSingle(spec,pose,buffers,progress/0.75F,left,thirdPerson,1,true);
            if(progress>0&&progress<1)renderSingle(layer(spec,"lensflare1",0.75F,0.01F,1,1).tinted(0.25F,1,0.25F,true),pose,buffers,progress,left,thirdPerson,0.75F,false);
            if(progress>0&&progress<0.35F)renderSingle(layer(spec,"lensflare4",1.35F,0.02F,1,1).tinted(0.25F,1,0.25F,false),pose,buffers,progress/0.35F,left,thirdPerson,1,true);
            return;
        }
        renderSingle(spec,pose,buffers,progress,left,thirdPerson,1,false);
    }
    private static Specification layer(Specification spec,String name,float scale,float z,int frames,int columns){
        return new Specification(ResourceLocation.fromNamespaceAndPath("techguns","textures/fx/"+name+".png"),frames,spec.rightX,spec.leftX,spec.y,spec.z+z,spec.scale*scale,spec.jitterX,spec.jitterY,spec.jitterScale,spec.thirdY,spec.thirdZ+z,columns,columns,false,false);
    }
    private static void renderSingle(Specification spec,PoseStack pose,MultiBufferSource buffers,float progress,boolean left,boolean thirdPerson,float opacity,boolean fast){
        int frame = (int) (spec.frames * progress);
        if (progress < 0 || frame >= spec.frames) return;
        float alpha=opacity*(fast?(float)((1-Math.cos(Math.sqrt(progress)*2*Math.PI))*0.5):spec.smoothFade?(float)Math.pow(Math.sin(Math.PI*progress),2):1);
        float x = thirdPerson ? 0 : (left ? spec.leftX : spec.rightX) + spec.jitterX * ClientWeaponEvents.muzzleJitterX;
        float y = thirdPerson ? spec.thirdY : spec.y + spec.jitterY * ClientWeaponEvents.muzzleJitterY;
        float z = thirdPerson ? spec.thirdZ : spec.z;
        float half = (thirdPerson ? spec.scale * 0.5F : spec.scale + spec.jitterScale * ClientWeaponEvents.muzzleJitterScale) / 2;
        float u1 = frame % spec.columns / (float)spec.columns, v1 = frame / spec.columns / (float)spec.rows;
        float u2 = u1 + 1F/spec.columns, v2 = v1 + 1F/spec.rows;
        if(spec.flipX) {float swap=u1;u1=u2;u2=swap;}
        if(spec.flipY) {float swap=v1;v1=v2;v2=swap;}
        var vertices = buffers.getBuffer(LegacyFxRenderTypes.sprite(spec.texture,!spec.texture.getPath().endsWith("/fireball_4x4_alpha.png"),!thirdPerson));
        var matrix = pose.last().pose();
        vertices.vertex(matrix, x-half,y+half,z).color(spec.red,spec.green,spec.blue,alpha).uv(u1,v2).endVertex();
        vertices.vertex(matrix, x-half,y-half,z).color(spec.red,spec.green,spec.blue,alpha).uv(u1,v1).endVertex();
        vertices.vertex(matrix, x+half,y-half,z).color(spec.red,spec.green,spec.blue,alpha).uv(u2,v1).endVertex();
        vertices.vertex(matrix, x+half,y+half,z).color(spec.red,spec.green,spec.blue,alpha).uv(u2,v2).endVertex();
        if (thirdPerson) {
            vertices.vertex(matrix,x-half,y,z+half).color(spec.red,spec.green,spec.blue,alpha).uv(u1,v2).endVertex();
            vertices.vertex(matrix,x-half,y,z-half).color(spec.red,spec.green,spec.blue,alpha).uv(u1,v1).endVertex();
            vertices.vertex(matrix,x+half,y,z-half).color(spec.red,spec.green,spec.blue,alpha).uv(u2,v1).endVertex();
            vertices.vertex(matrix,x+half,y,z+half).color(spec.red,spec.green,spec.blue,alpha).uv(u2,v2).endVertex();
            vertices.vertex(matrix,x,y-half,z+half).color(spec.red,spec.green,spec.blue,alpha).uv(u1,v2).endVertex();
            vertices.vertex(matrix,x,y-half,z-half).color(spec.red,spec.green,spec.blue,alpha).uv(u1,v1).endVertex();
            vertices.vertex(matrix,x,y+half,z-half).color(spec.red,spec.green,spec.blue,alpha).uv(u2,v1).endVertex();
            vertices.vertex(matrix,x,y+half,z+half).color(spec.red,spec.green,spec.blue,alpha).uv(u2,v2).endVertex();
        }
    }
    private PistolMuzzleFlash() {}
}
