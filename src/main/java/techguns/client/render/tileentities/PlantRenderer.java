package techguns.client.render.tileentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import techguns.power.plant.*;
import techguns.power.plant.PlantModel.Tex;

/**
 * Draws a formed power plant as one model (its blocks are hidden): the static geometry of PlantModel (which also
 * gives the plant its collision) plus the moving parts: the turning rotor and blinking lights of the wind turbine,
 * the glowing intakes of the geothermal plant, the fuel rods of the fission reactor and the plasma of the tokamak.
 */
public final class PlantRenderer implements BlockEntityRenderer<PlantController> {
    /** A texture of the Techguns blocks themselves (their multiblocks: ore drill, reaction chamber, fabricator). */
    private static ResourceLocation tg(String id){return ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/"+id+".png");}
    private static ResourceLocation tex(String id){return ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/plant/"+id+".png");}
    private static final Map<Tex,ResourceLocation> TEX=new EnumMap<>(Tex.class),TOP=new EnumMap<>(Tex.class);
    static{
        TEX.put(Tex.CASING,tex("plant_casing"));TEX.put(Tex.CASING_LIGHT,tex("plant_casing_light"));TOP.put(Tex.CASING,tex("plant_casing_top"));TOP.put(Tex.CASING_LIGHT,tex("plant_casing_light_top"));TEX.put(Tex.CASING_DARK,tex("plant_casing_dark"));TEX.put(Tex.GLASS,tex("plant_glass"));
        TEX.put(Tex.TOWER,tex("turbine_tower"));TEX.put(Tex.COIL,tex("fusion_coil"));TEX.put(Tex.BLADE,tex("turbine_blade"));
        TEX.put(Tex.LADDER,ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/metal_ladder.png"));
        // the plants' own textures, painted in the Techguns manner (tools/plant_style.py)
        TEX.put(Tex.RC_HOUSING,tex("hall_white"));TEX.put(Tex.RC_GLASS,tex("hall_glass"));
        TEX.put(Tex.FAB_HOUSING,tex("hall_light"));TEX.put(Tex.FAB_GLASS,tex("hall_glass_dark"));
        TEX.put(Tex.DRILL_ENGINE,tex("machine"));TEX.put(Tex.DRILL_FRAME,tex("frame_rust"));TEX.put(Tex.STEELFRAME,tex("frame_dark"));
        TEX.put(Tex.CONCRETE,tex("concrete"));TEX.put(Tex.ROD,tex("rod"));TEX.put(Tex.GEN_SIDE,tex("gen_side"));TEX.put(Tex.TECHDOOR,tex("door"));
        TEX.put(Tex.PIPE,tex("plant_pipe"));TEX.put(Tex.VENT,tex("plant_vent"));TEX.put(Tex.HAZARD,tex("plant_hazard"));TEX.put(Tex.PANEL,tex("plant_panel"));
    }
    private static final ResourceLocation GLOW=tex("plant_glow");
    private static final Map<PlantController,float[]> angles=new WeakHashMap<>();
    public PlantRenderer(BlockEntityRendererProvider.Context context){}

    /** Axis aligned box; texture repeats once per block. */
    static void box(PoseStack pose,VertexConsumer vc,float x0,float y0,float z0,float x1,float y1,float z1,int light,int argb){box(pose,vc,x0,y0,z0,x1,y1,z1,light,argb,false);}
    /** Axis aligned box; fit stretches the whole texture over every face instead of repeating it per block. */
    static void box(PoseStack pose,VertexConsumer vc,float x0,float y0,float z0,float x1,float y1,float z1,int light,int argb,boolean fit){box(pose,vc,x0,y0,z0,x1,y1,z1,light,argb,fit,ALL);}
    static final int ALL=63,SIDES=15,TOPS=48;
    /** Shift of the repeating textures so Techguns panels line up with the blocks of the plant (render thread only; the wind turbine keeps its look). */
    private static float uvShift=0;
    /** faces: bit mask of the faces to draw, in the order -z, +z, -x, +x, +y, -y. */
    static void box(PoseStack pose,VertexConsumer vc,float x0,float y0,float z0,float x1,float y1,float z1,int light,int argb,boolean fit,int faces){
        var m=pose.last().pose();var n=pose.last().normal();int a=argb>>>24,r=argb>>16&255,g=argb>>8&255,b=argb&255;
        float[][] quads={
            {x0,y0,z0, x0,y1,z0, x1,y1,z0, x1,y0,z0, 0,0,-1},{x1,y0,z1, x1,y1,z1, x0,y1,z1, x0,y0,z1, 0,0,1},
            {x0,y0,z1, x0,y1,z1, x0,y1,z0, x0,y0,z0, -1,0,0},{x1,y0,z0, x1,y1,z0, x1,y1,z1, x1,y0,z1, 1,0,0},
            {x0,y1,z0, x0,y1,z1, x1,y1,z1, x1,y1,z0, 0,1,0},{x0,y0,z1, x0,y0,z0, x1,y0,z0, x1,y0,z1, 0,-1,0}};
        for(int fi=0;fi<6;fi++){
            if((faces&(1<<fi))==0)continue;var f=quads[fi];
            float nx=f[12],ny=f[13],nz=f[14];
            for(int i=0;i<4;i++){
                float px=f[i*3],py=f[i*3+1],pz=f[i*3+2];
                float u=nx!=0?pz:px,v=ny!=0?pz:py;
                if(fit||faceRects!=null){u=nx!=0?(pz-z0)/(z1-z0):(px-x0)/(x1-x0);v=ny!=0?(pz-z0)/(z1-z0):1-(py-y0)/(y1-y0);if(nx<0||nz>0)u=1-u;
                    if(faceRects!=null){var fr=faceRects[fi];u=fr[0]+u*(fr[2]-fr[0]);v=fr[1]+v*(fr[3]-fr[1]);}}
                else{u=nx!=0?pz+uvShift:px+uvShift;v=ny!=0?pz+uvShift:py;u=nx<0||nz>0?-u:u;v=-v;}
                vc.vertex(m,px,py,pz).color(r,g,b,a).uv(u,v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n,nx,ny,nz).endVertex();
            }
        }
    }
    private static ResourceLocation front(PlantKind kind){return tex(kind.id()+"_front");}
    private static boolean fit(Tex t){return t==Tex.FRONT||t==Tex.PANEL||t==Tex.VENT||t==Tex.LADDER||t==Tex.TECHDOOR;}
    /** Packed light at a point of the controller frame, so a tall tower is not lit like its foot. */
    private static int lightAt(PlantController p,float x,float y,float z,int fallback){
        var f=p.facing();var r=f.getClockWise();var b=f.getOpposite();var o=p.getBlockPos();
        var pos=BlockPos.containing(o.getX()+0.5+r.getStepX()*x+b.getStepX()*z,o.getY()+y,o.getZ()+0.5+r.getStepZ()*x+b.getStepZ()*z);
        int light=LevelRenderer.getLightColor(p.getLevel(),pos);
        return light==0?fallback:light;
    }

    @Override public void render(PlantController plant,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!plant.formed||plant.getLevel()==null)return;
        float time=plant.getLevel().getGameTime()+partial;
        float[] a=angles.computeIfAbsent(plant,p->new float[]{0,time});
        float speed=plant instanceof WindPlant?plant.rate/(float)WindPlant.RATE*0.12F:0;
        a[0]=(a[0]+(time-a[1])*speed)%(float)(Math.PI*2);a[1]=time;
        pose.pushPose();pose.translate(0.5,0,0.5);pose.mulPose(Axis.YP.rotationDegrees(180-plant.facing().toYRot()));
        // an own model made in Blockbench replaces the built-in one (see PlantCustomModels)
        var custom=techguns.client.render.bbmodel.PlantCustomModels.get(plant.kind);
        if(custom!=null){
            boolean running=plant instanceof FusionReactor f?f.ignited:plant.rate>0;
            pose.pushPose();pose.translate(-0.5F,0,-0.5F);
            custom.render(pose,buffers,running,time/20F,(x,y,z)->lightAt(plant,x-0.5F,y,z-0.5F,light));
            pose.popPose();pose.popPose();return;
        }
        // the static model never changes while the plant stands: its vertices are worked out once and only sent again
        var model=plant.model();var atlas=PlantAtlas.get(plant.kind);
        var baked=BAKED.get(plant);
        if(baked==null||baked.model!=model||baked.atlas!=atlas){baked=bake(plant,model,atlas);BAKED.put(plant,baked);}
        long now=plant.getLevel().getGameTime();
        if(now-baked.lightTime>=10||now<baked.lightTime){
            for(int i=0;i<model.size();i++){var b=model.get(i);baked.light[i]=lightAt(plant,(b.x0()+b.x1())/2,(b.y0()+b.y1())/2+0.01F,(b.z0()+b.z1())/2,light);}
            baked.lightTime=now;
        }
        var m=pose.last().pose();var n=pose.last().normal();
        for(var batch:baked.batches)if(!batch.translucent)batch.emit(buffers.getBuffer(RenderType.entityCutout(batch.texture)),m,n,baked.light);
        uvShift=plant.kind==PlantKind.WIND?0:0.5F;parts(plant,pose,buffers,light,a[0],time);uvShift=0;
        for(var batch:baked.batches)if(batch.translucent)batch.emit(buffers.getBuffer(RenderType.entityTranslucent(batch.texture)),m,n,baked.light);
        pose.popPose();
    }
    /** One texture's share of a plant's static model: x y z u v nx ny nz per vertex, its colour and the box it belongs to (for its light). */
    private static final class Batch{
        final ResourceLocation texture;final boolean translucent;final float[] d;final int[] colour,box;
        Batch(ResourceLocation texture,boolean translucent,Recorder r){this.texture=texture;this.translucent=translucent;d=java.util.Arrays.copyOf(r.d,r.n*8);colour=java.util.Arrays.copyOf(r.colour,r.n);box=java.util.Arrays.copyOf(r.boxOf,r.n);}
        void emit(VertexConsumer vc,org.joml.Matrix4f m,org.joml.Matrix3f n,int[] light){
            for(int i=0,o=0;i<colour.length;i++,o+=8){int c=colour[i];
                vc.vertex(m,d[o],d[o+1],d[o+2]).color(c>>16&255,c>>8&255,c&255,c>>>24).uv(d[o+3],d[o+4]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light[box[i]]).normal(n,d[o+5],d[o+6],d[o+7]).endVertex();}
        }
    }
    private static final class Baked{
        final java.util.List<PlantModel.Box> model;final PlantAtlas.Atlas atlas;final java.util.List<Batch> batches=new java.util.ArrayList<>();final int[] light;long lightTime=Long.MIN_VALUE;
        Baked(java.util.List<PlantModel.Box> model,PlantAtlas.Atlas atlas){this.model=model;this.atlas=atlas;light=new int[model.size()];}
    }
    private static final Map<PlantController,Baked> BAKED=new WeakHashMap<>();
    /** Takes the vertices the box drawing code makes instead of sending them to the screen. */
    private static final class Recorder implements VertexConsumer{
        float[] d=new float[4096];int[] colour=new int[512],boxOf=new int[512];int n,box;
        private float x,y,z,u,v,nx,ny,nz;private int c;
        @Override public VertexConsumer vertex(double px,double py,double pz){x=(float)px;y=(float)py;z=(float)pz;return this;}
        @Override public VertexConsumer color(int r,int g,int b,int a){c=a<<24|r<<16|g<<8|b;return this;}
        @Override public VertexConsumer uv(float pu,float pv){u=pu;v=pv;return this;}
        @Override public VertexConsumer overlayCoords(int pu,int pv){return this;}
        @Override public VertexConsumer uv2(int pu,int pv){return this;}
        @Override public VertexConsumer normal(float px,float py,float pz){nx=px;ny=py;nz=pz;return this;}
        @Override public void endVertex(){
            if(n==colour.length){d=java.util.Arrays.copyOf(d,d.length*2);colour=java.util.Arrays.copyOf(colour,n*2);boxOf=java.util.Arrays.copyOf(boxOf,n*2);}
            int o=n*8;d[o]=x;d[o+1]=y;d[o+2]=z;d[o+3]=u;d[o+4]=v;d[o+5]=nx;d[o+6]=ny;d[o+7]=nz;colour[n]=c;boxOf[n]=box;n++;
        }
        @Override public void defaultColor(int r,int g,int b,int a){}
        @Override public void unsetDefaultColor(){}
    }
    private static Baked bake(PlantController plant,java.util.List<PlantModel.Box> model,PlantAtlas.Atlas atlas){
        var baked=new Baked(model,atlas);var pose=new PoseStack();
        uvShift=plant.kind==PlantKind.WIND?0:0.5F;
        try{
            if(atlas!=null&&atlas.count()==model.size()){
                // the painted atlas: one texture, every face its own place in it
                for(int pass=0;pass<2;pass++){
                    var r=new Recorder();
                    for(int i=0;i<model.size();i++){var b=model.get(i);if(b.tex()==Tex.NONE||b.tex().translucent()!=(pass==1)||atlas.rects()[i]==null)continue;faceRects=atlas.rects()[i];draw(pose,r,b,ALL,i);}
                    if(r.n>0)baked.batches.add(new Batch(atlas.texture(),pass==1,r));
                }
                return baked;
            }
            for(Tex t:Tex.values()){
                if(t==Tex.NONE)continue;
                // steel boxes: the riveted side texture on the sides, the plain top texture up and down (one pass each)
                boolean split=!t.translucent()&&TOP.containsKey(t);
                for(int pass=0;pass<(split?2:1);pass++){
                    var r=new Recorder();int faces=split?(pass==0?SIDES:TOPS):ALL;
                    for(int i=0;i<model.size();i++){var box=model.get(i);if(box.tex()==t)draw(pose,r,box,faces,i);}
                    if(r.n>0)baked.batches.add(new Batch(t==Tex.FRONT?front(plant.kind):pass==1?TOP.get(t):TEX.get(t),t.translucent(),r));
                }
            }
            return baked;
        }finally{faceRects=null;uvShift=0;}
    }
    /** The moving and glowing parts drawn on top of the static model. */
    private void parts(PlantController plant,PoseStack pose,MultiBufferSource buffers,int light,float angle,float time){
        switch(plant.kind){
            case WIND->wind((WindPlant)plant,pose,buffers,light,angle,time);
            case GEOTHERMAL->geothermal(plant,pose,buffers,light,time);
            case FISSION->fission((FissionReactor)plant,pose,buffers,light,time);
            case FUSION->fusion((FusionReactor)plant,pose,buffers,light,time);
        }
    }
    /** Atlas places of the faces of the box being drawn (render thread only); null for the repeating textures. */
    private static float[][] faceRects;
    /**
     * Every box is drawn a tiny bit larger, by an amount that differs from box to box (and is larger for detail boxes),
     * so two faces never lie in the same plane and nothing flickers where boxes touch or overlap.
     */
    private static float inflate(PlantModel.Box b,int index){return 0.0003F*((index*7)%23)+(b.detail()?0.002F:0);}
    private static void draw(PoseStack pose,Recorder vc,PlantModel.Box b,int faces,int index){
        int l=0;vc.box=index;
        if(b.rotY()!=0){pose.pushPose();pose.translate(b.px(),0,b.pz());pose.mulPose(Axis.YP.rotationDegrees(b.rotY()));pose.translate(-b.px(),0,-b.pz());}
        float e=inflate(b,index);
        box(pose,vc,b.x0()-e,b.y0()-e,b.z0()-e,b.x1()+e,b.y1()+e,b.z1()+e,l,b.argb(),fit(b.tex()),faces);
        if(b.rotY()!=0)pose.popPose();
    }

    private static final int WHITE=0xFFFFFFFF,RED_TIP=0xFFD8402E;
    private void wind(WindPlant p,PoseStack pose,MultiBufferSource buffers,int light,float angle,float time){
        int n=Math.max(1,p.towers);float[] c=PlantModel.rotorCentre(n);
        int l=lightAt(p,c[0],c[1],c[2],light);
        pose.pushPose();pose.translate(c[0],c[1],c[2]);rotor(pose,buffers,l,angle,3,true);pose.popPose();
        // aviation warning lights on the nacelle roof and halfway up the tower
        if(((int)(time/20))%2==0){
            var glow=buffers.getBuffer(RenderType.eyes(GLOW));int red=0xFFFF2010;float y=n+1;
            box(pose,glow,-0.3F,y+0.78F,0.76F,-0.24F,y+0.85F,0.82F,l,red);box(pose,glow,0.24F,y+0.78F,0.76F,0.3F,y+0.85F,0.82F,l,red);
            float h=PlantModel.towerHalf(n/2+1,n)+0.01F,my=n/2+1.5F;
            box(pose,glow,-0.05F,my,-h-0.04F,0.05F,my+0.08F,-h,l,red);
        }
    }
    /** Rotor of the wind turbine around the current origin (the rotor centre, facing -z): hub, spinner and blades. */
    static void rotor(PoseStack pose,MultiBufferSource buffers,int l,float angle,int blades,boolean hub){
        pose.pushPose();pose.mulPose(Axis.ZP.rotation(angle));
        if(hub){
        var casing=buffers.getBuffer(RenderType.entityCutout(TEX.get(Tex.CASING)));
        box(pose,casing,-0.3F,-0.3F,-0.12F,0.3F,0.3F,0.12F,l,WHITE);
        box(pose,casing,-0.24F,-0.24F,-0.2F,0.24F,0.24F,-0.12F,l,WHITE);
        box(pose,casing,-0.17F,-0.17F,-0.26F,0.17F,0.17F,-0.2F,l,WHITE);
        box(pose,casing,-0.1F,-0.1F,-0.3F,0.1F,0.1F,-0.26F,l,WHITE);
        box(pose,casing,-0.04F,-0.04F,-0.32F,0.04F,0.04F,-0.3F,l,RED_TIP);
        }
        var dark=buffers.getBuffer(RenderType.entityCutout(TEX.get(Tex.CASING_DARK)));
        for(int k=0;k<blades;k++){pose.pushPose();pose.mulPose(Axis.ZP.rotationDegrees(120*k));box(pose,dark,-0.16F,0.24F,-0.11F,0.16F,0.4F,0.11F,l,WHITE);pose.popPose();}
        var tower=buffers.getBuffer(RenderType.entityCutout(TEX.get(Tex.TOWER)));
        for(int k=0;k<blades;k++){pose.pushPose();pose.mulPose(Axis.ZP.rotationDegrees(120*k));box(pose,tower,-0.11F,0.4F,-0.09F,0.11F,0.62F,0.09F,l,WHITE);pose.popPose();}
        // blades: airfoil segments, widest near the root, twisted, red tips
        var blade=buffers.getBuffer(RenderType.entityCutout(TEX.get(Tex.BLADE)));
        int segments=10;float r0=0.62F,r1=4.4F;
        for(int k=0;k<blades;k++){
            pose.pushPose();pose.mulPose(Axis.ZP.rotationDegrees(120*k));
            for(int s=0;s<segments;s++){
                float t0=s/(float)segments,t1=(s+1)/(float)segments,t=(t0+t1)/2;
                float chord=t<0.15F?0.24F+t/0.15F*0.12F:0.36F-(t-0.15F)/0.85F*0.29F,thick=0.07F-0.05F*t,twist=16-15*t;
                float y0=r0+(r1-r0)*t0,y1=r0+(r1-r0)*t1;
                pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(twist));
                box(pose,blade,-0.35F*chord,y0,-thick/2,0.65F*chord,y1+0.01F,thick/2,l,t>0.86F&&s%2==1||s==segments-1?RED_TIP:WHITE);
                pose.popPose();
            }
            pose.popPose();
        }
        pose.popPose();
    }
    private void geothermal(PlantController p,PoseStack pose,MultiBufferSource buffers,int light,float time){
        boolean on=p.rate>0;
        int glow=on?(int)(200+40*Math.sin(time*0.15))<<24|0xFF7A20:0xFF301008;
        var vents=on?buffers.getBuffer(RenderType.eyes(GLOW)):buffers.getBuffer(RenderType.entityCutout(GLOW));
        for(int sx=-1;sx<=1;sx+=2)box(pose,vents,Math.min(sx*0.56F,sx*1.3F),0.3F,-0.2005F,Math.max(sx*0.56F,sx*1.3F),0.86F,-0.2F,light,glow);
        // warning lamps over the door and the beacon on the separator
        if(on){var lamp=buffers.getBuffer(RenderType.eyes(GLOW));int amber=((int)(time/10))%2==0?0xFFFFA020:0x60FF8010;
            for(int sx=-1;sx<=1;sx+=2)box(pose,lamp,sx*0.4F-0.025F,0.905F,-0.255F,sx*0.4F+0.025F,0.955F,-0.25F,light,amber);
            box(pose,lamp,0.86F,2.49F,0.14F,0.94F,2.55F,0.22F,lightAt(p,0.9F,2.5F,0.18F,light),amber);}
        // roof fans: five pitched blades turning inside the housing, under the grille
        var dark=buffers.getBuffer(RenderType.entityCutout(TEX.get(Tex.CASING_DARK)));float spin=on?time*18:0;
        for(float fz:new float[]{0.95F,1.95F}){
            int l=lightAt(p,0.86F,2.2F,fz,light);
            pose.pushPose();pose.translate(0.86F,2.03F,fz);pose.mulPose(Axis.YP.rotationDegrees(spin));
            box(pose,dark,-0.06F,-0.03F,-0.06F,0.06F,0.03F,0.06F,l,WHITE);
            for(int k=0;k<5;k++){pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(k*72));pose.mulPose(Axis.ZP.rotationDegrees(25));
                box(pose,dark,-0.07F,-0.006F,0.05F,0.07F,0.006F,0.34F,l,WHITE);pose.popPose();}
            pose.popPose();
        }
    }
    private void fission(FissionReactor p,PoseStack pose,MultiBufferSource buffers,int light,float time){
        boolean on=p.rate>0;
        float pulse=on?0.75F+0.25F*(float)Math.sin(time*0.2):0.12F;
        var core=buffers.getBuffer(RenderType.eyes(GLOW));
        int green=(int)(pulse*255)<<24|0x40FF60,blue=(int)(pulse*200)<<24|0x3080FF;
        for(float rx:new float[]{-0.36F,0,0.36F})for(float rz:new float[]{0.64F,1.0F,1.36F})box(pose,core,rx-0.06F,-0.7F,rz-0.06F,rx+0.06F,1.5F,rz+0.06F,light,green);
        box(pose,core,-0.58F,-0.715F,0.42F,0.58F,-0.71F,1.58F,light,blue);
        // status strip: green running, red overheated, dim when idle
        int strip=p.overheated?(((int)(time/6))%2==0?0xFFFF3020:0x40FF3020):on?0xFF40FF60:0x30406040;
        box(pose,core,-1.08F,1.44F,-0.535F,1.08F,1.5F,-0.53F,light,strip);
        // control rods: drawn out while the reactor runs, dropped in when it stops
        float target=on?1.05F:-0.55F;float[] r=rods.computeIfAbsent(p,k->new float[]{target});r[0]+=(target-r[0])*0.02F;
        var dark=buffers.getBuffer(RenderType.entityCutout(TEX.get(Tex.CASING_DARK)));
        for(float rx:new float[]{-0.18F,0.18F})for(float rz:new float[]{0.82F,1.18F})box(pose,dark,rx-0.04F,r[0],rz-0.04F,rx+0.04F,r[0]+1.1F,rz+0.04F,light,WHITE);
    }
    private static final Map<PlantController,float[]> rods=new WeakHashMap<>();
    private void fusion(FusionReactor p,PoseStack pose,MultiBufferSource buffers,int light,float time){
        if(!p.ignited)return;
        var plasma=buffers.getBuffer(RenderType.eyes(GLOW));float radius=1.45F;
        for(int i=0;i<32;i++){
            pose.pushPose();pose.translate(0,0.5F,2);pose.mulPose(Axis.YP.rotationDegrees(i*11.25F+time*9));
            float wave=0.5F+0.5F*(float)Math.sin(time*0.3+i*0.6);
            int colour=(int)(140+100*wave)<<24|(int)(160+90*wave)<<16|0x40<<8|0xFF;
            box(pose,plasma,-0.15F,-0.17F,radius-0.16F,0.15F,0.17F,radius+0.16F,light,colour);
            pose.popPose();
        }
    }
    @Override public boolean shouldRenderOffScreen(PlantController plant){return true;}
    @Override public int getViewDistance(){return 160;}
}
