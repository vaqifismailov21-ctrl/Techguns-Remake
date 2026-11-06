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
    private static ResourceLocation tex(String id){return ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/plant/"+id+".png");}
    private static final Map<Tex,ResourceLocation> TEX=new EnumMap<>(Tex.class),TOP=new EnumMap<>(Tex.class);
    static{
        TEX.put(Tex.CASING,tex("plant_casing"));TEX.put(Tex.CASING_LIGHT,tex("plant_casing_light"));TOP.put(Tex.CASING,tex("plant_casing_top"));TOP.put(Tex.CASING_LIGHT,tex("plant_casing_light_top"));TEX.put(Tex.CASING_DARK,tex("plant_casing_dark"));TEX.put(Tex.GLASS,tex("plant_glass"));
        TEX.put(Tex.TOWER,tex("turbine_tower"));TEX.put(Tex.COIL,tex("fusion_coil"));TEX.put(Tex.BLADE,tex("turbine_blade"));
        TEX.put(Tex.LADDER,ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/metal_ladder.png"));
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
                if(fit){u=nx!=0?(pz-z0)/(z1-z0):(px-x0)/(x1-x0);v=ny!=0?(pz-z0)/(z1-z0):1-(py-y0)/(y1-y0);if(nx<0||nz>0)u=1-u;}
                else{u=nx<0||nz>0?-u:u;v=-v;}
                vc.vertex(m,px,py,pz).color(r,g,b,a).uv(u,v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n,nx,ny,nz).endVertex();
            }
        }
    }
    private static ResourceLocation front(PlantKind kind){return tex(kind.id()+"_front");}
    private static boolean fit(Tex t){return t==Tex.FRONT||t==Tex.PANEL||t==Tex.VENT||t==Tex.LADDER;}
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
        var model=plant.model();
        for(Tex t:Tex.values()){
            if(t==Tex.NONE||t==Tex.GLASS)continue;
            // steel boxes: the riveted side texture on the sides, the plain top texture up and down (one pass each)
            boolean split=TOP.containsKey(t);
            for(int pass=0;pass<(split?2:1);pass++){
                VertexConsumer vc=null;int faces=split?(pass==0?SIDES:TOPS):ALL;
                for(int i=0;i<model.size();i++){
                    var box=model.get(i);if(box.tex()!=t)continue;
                    if(vc==null)vc=buffers.getBuffer(RenderType.entityCutout(t==Tex.FRONT?front(plant.kind):pass==1?TOP.get(t):TEX.get(t)));
                    draw(plant,pose,vc,box,light,faces,i);
                }
            }
        }
        switch(plant.kind){
            case WIND->wind((WindPlant)plant,pose,buffers,light,a[0],time);
            case GEOTHERMAL->geothermal(plant,pose,buffers,light,time);
            case FISSION->fission((FissionReactor)plant,pose,buffers,light,time);
            case FUSION->fusion((FusionReactor)plant,pose,buffers,light,time);
        }
        VertexConsumer glass=null;
        for(int i=0;i<model.size();i++)if(model.get(i).tex()==Tex.GLASS){if(glass==null)glass=buffers.getBuffer(RenderType.entityTranslucent(TEX.get(Tex.GLASS)));draw(plant,pose,glass,model.get(i),light,ALL,i);}
        pose.popPose();
    }
    /**
     * Every box is drawn a tiny bit larger, by an amount that differs from box to box (and is larger for detail boxes),
     * so two faces never lie in the same plane and nothing flickers where boxes touch or overlap.
     */
    private static float inflate(PlantModel.Box b,int index){return 0.0003F*((index*7)%23)+(b.detail()?0.002F:0);}
    private static void draw(PlantController plant,PoseStack pose,VertexConsumer vc,PlantModel.Box b,int light,int faces,int index){
        int l=lightAt(plant,(b.x0()+b.x1())/2,(b.y0()+b.y1())/2+0.01F,(b.z0()+b.z1())/2,light);
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
