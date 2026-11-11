package techguns.power.plant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Static geometry of the formed power plants, shared by the renderer (which draws it) and the blocks of the plant
 * (which take their collision from the solid boxes). Coordinates are in the controller frame: x to the right of the
 * controller, y up, z to the back, the origin in the middle of the controller's bottom face. The turning and glowing
 * parts (rotor, plasma, core, vents, lights) are drawn by the renderer on top of this.
 */
public final class PlantModel {
    private PlantModel(){}
    /** Own steel textures (wind turbine), and the Techguns machine textures the other plants are built from. */
    public enum Tex{CASING,CASING_LIGHT,CASING_DARK,GLASS,TOWER,COIL,BLADE,FRONT,LADDER,PIPE,VENT,HAZARD,PANEL,
        RC_HOUSING,RC_GLASS,FAB_HOUSING,FAB_GLASS,DRILL_ENGINE,DRILL_FRAME,STEELFRAME,CONCRETE,ROD,GEN_SIDE,TECHDOOR,NONE;
        public boolean translucent(){return this==GLASS||this==RC_GLASS||this==FAB_GLASS;}}
    /** Axis aligned box; detail boxes sit on others and are drawn a hair larger so their faces never flicker against them; rotY turns it around the vertical line through (px,pz) (it then collides with its turned footprint's bounds). */
    public record Box(float x0,float y0,float z0,float x1,float y1,float z1,Tex tex,int argb,boolean solid,float rotY,float px,float pz,boolean detail){}
    private static final int W=0xFFFFFFFF,GREY=0xFFB4B4B4,DARK=0xFF8C8C8C;

    private static final class B {
        final List<Box> list=new ArrayList<>();
        void s(Tex t,double x0,double y0,double z0,double x1,double y1,double z1){add(t,x0,y0,z0,x1,y1,z1,W,false);}
        void s(Tex t,int c,double x0,double y0,double z0,double x1,double y1,double z1){add(t,x0,y0,z0,x1,y1,z1,c,false);}
        void d(Tex t,double x0,double y0,double z0,double x1,double y1,double z1){add(t,x0,y0,z0,x1,y1,z1,W,true);}
        void d(Tex t,int c,double x0,double y0,double z0,double x1,double y1,double z1){add(t,x0,y0,z0,x1,y1,z1,c,true);}
        /** Collision only, not drawn. */
        void hit(double x0,double y0,double z0,double x1,double y1,double z1){add(Tex.NONE,x0,y0,z0,x1,y1,z1,0,false);}
        void rot(Tex t,int c,double x0,double y0,double z0,double x1,double y1,double z1,double deg,double px,double pz){
            list.add(new Box((float)x0,(float)y0,(float)z0,(float)x1,(float)y1,(float)z1,t,c,true,(float)deg,(float)px,(float)pz,true));}
        private void add(Tex t,double x0,double y0,double z0,double x1,double y1,double z1,int c,boolean detail){
            list.add(new Box((float)Math.min(x0,x1),(float)Math.min(y0,y1),(float)Math.min(z0,z1),(float)Math.max(x0,x1),(float)Math.max(y0,y1),(float)Math.max(z0,z1),t,c,true,0,0,0,detail));}
        /** Pipes are open tubes (Techguns style): four walls, a dark bore you can look into, flanged rims at both ends. */
        private static final int BORE=0xFF3A3A3A;
        private static double wall(double r){return Math.max(0.014,r*0.26);}
        /** Vertical pipe with flanges. */
        void pipeY(double x,double z,double r,double y0,double y1,boolean solid){
            double w=wall(r);
            add(Tex.PIPE,x-r,y0,z-r,x+r,y1,z-r+w,W,false);add(Tex.PIPE,x-r,y0,z+r-w,x+r,y1,z+r,W,false);
            add(Tex.PIPE,x-r,y0,z-r+w,x-r+w,y1,z+r-w,W,false);add(Tex.PIPE,x+r-w,y0,z-r+w,x+r,y1,z+r-w,W,false);
            d(Tex.PIPE,BORE,x-r+w,y0+0.02,z-r+w,x+r-w,y0+0.04,z+r-w);
            ringY(x,z,r,y1-0.05,y1);ringY(x,z,r,y0,y0+0.05);
            for(double y=y0+0.25;y<y1-0.1;y+=0.75)d(Tex.CASING_DARK,x-r-0.025,y,z-r-0.025,x+r+0.025,y+0.06,z+r+0.025);
        }
        void pipeX(double y,double z,double r,double x0,double x1,boolean solid){
            double w=wall(r);
            add(Tex.PIPE,x0,y-r,z-r,x1,y-r+w,z+r,W,false);add(Tex.PIPE,x0,y+r-w,z-r,x1,y+r,z+r,W,false);
            add(Tex.PIPE,x0,y-r+w,z-r,x1,y+r-w,z-r+w,W,false);add(Tex.PIPE,x0,y-r+w,z+r-w,x1,y+r-w,z+r,W,false);
            ringX(y,z,r,x0,x0+0.05);ringX(y,z,r,x1-0.05,x1);
            for(double x=x0+0.3;x<x1-0.1;x+=0.75)d(Tex.CASING_DARK,x,y-r-0.025,z-r-0.025,x+0.06,y+r+0.025,z+r+0.025);
        }
        void pipeZ(double x,double y,double r,double z0,double z1,boolean solid){
            double w=wall(r);
            add(Tex.PIPE,x-r,y-r,z0,x+r,y-r+w,z1,W,false);add(Tex.PIPE,x-r,y+r-w,z0,x+r,y+r,z1,W,false);
            add(Tex.PIPE,x-r,y-r+w,z0,x-r+w,y+r-w,z1,W,false);add(Tex.PIPE,x+r-w,y-r+w,z0,x+r,y+r-w,z1,W,false);
            ringZ(x,y,r,z0,z0+0.05);ringZ(x,y,r,z1-0.05,z1);
            for(double z=z0+0.3;z<z1-0.1;z+=0.75)d(Tex.CASING_DARK,x-r-0.025,y-r-0.025,z,x+r+0.025,y+r+0.025,z+0.06);
        }
        /** Square ring of four walls (half size h, wall t) from y0 to y1: an open shaft. */
        void hollow(Tex t,double x,double z,double h,double w,double y0,double y1){
            d(t,x-h,y0,z-h,x+h,y1,z-h+w);d(t,x-h,y0,z+h-w,x+h,y1,z+h);
            d(t,x-h,y0,z-h+w,x-h+w,y1,z+h-w);d(t,x+h-w,y0,z-h+w,x+h,y1,z+h-w);
        }
        /** Flange ring around an open pipe end. */
        void ringY(double x,double z,double r,double y0,double y1){double o=r+0.03,i=r-0.004;
            d(Tex.CASING_DARK,x-o,y0,z-o,x+o,y1,z-i);d(Tex.CASING_DARK,x-o,y0,z+i,x+o,y1,z+o);
            d(Tex.CASING_DARK,x-o,y0,z-i,x-i,y1,z+i);d(Tex.CASING_DARK,x+i,y0,z-i,x+o,y1,z+i);}
        void ringX(double y,double z,double r,double x0,double x1){double o=r+0.03,i=r-0.004;
            d(Tex.CASING_DARK,x0,y-o,z-o,x1,y-i,z+o);d(Tex.CASING_DARK,x0,y+i,z-o,x1,y+o,z+o);
            d(Tex.CASING_DARK,x0,y-i,z-o,x1,y+i,z-i);d(Tex.CASING_DARK,x0,y-i,z+i,x1,y+i,z+o);}
        void ringZ(double x,double y,double r,double z0,double z1){double o=r+0.03,i=r-0.004;
            d(Tex.CASING_DARK,x-o,y-o,z0,x+o,y-i,z1);d(Tex.CASING_DARK,x-o,y+i,z0,x+o,y+o,z1);
            d(Tex.CASING_DARK,x-o,y-i,z0,x-i,y+i,z1);d(Tex.CASING_DARK,x+i,y-i,z0,x+o,y+i,z1);}
    }

    /** Wind tower half width of section i (1..n): tapers from the foundation to the nacelle. */
    public static float towerHalf(int i,int n){return (float)(0.34-0.13*(i-0.5)/n);}
    /** Centre of the rotor in the controller frame. */
    public static float[] rotorCentre(int towers){return new float[]{0,towers+1+0.42F,-0.92F};}

    public static List<Box> build(PlantController plant){
        return build(plant.kind,plant instanceof WindPlant w?Math.max(1,w.towers):WindPlant.MIN_TOWER);
    }
    /** towers: tower sections of a wind plant, ignored by the other kinds. */
    public static List<Box> build(PlantKind kind,int towers){
        var b=new B();
        switch(kind){
            case WIND->wind(b,towers);
            case GEOTHERMAL->geothermal(b);
            case FISSION->fission(b);
            case FUSION->fusion(b);
        }
        // the big plants wear the textures of the Techguns multiblocks: ore drill, reaction chamber, fabricator
        Map<Tex,Tex> skin=switch(kind){
            case GEOTHERMAL->Map.of(Tex.CASING_LIGHT,Tex.DRILL_ENGINE,Tex.CASING,Tex.DRILL_FRAME,Tex.CASING_DARK,Tex.STEELFRAME,Tex.PIPE,Tex.ROD,Tex.PANEL,Tex.GEN_SIDE,Tex.GLASS,Tex.RC_GLASS);
            case FISSION->Map.of(Tex.CASING_LIGHT,Tex.RC_HOUSING,Tex.CASING,Tex.STEELFRAME,Tex.CASING_DARK,Tex.STEELFRAME,Tex.PIPE,Tex.ROD,Tex.PANEL,Tex.GEN_SIDE,Tex.GLASS,Tex.RC_GLASS);
            case FUSION->Map.of(Tex.CASING_LIGHT,Tex.FAB_HOUSING,Tex.CASING,Tex.FAB_HOUSING,Tex.CASING_DARK,Tex.STEELFRAME,Tex.PIPE,Tex.ROD,Tex.GLASS,Tex.FAB_GLASS);
            default->Map.of();
        };
        if(skin.isEmpty())return b.list;
        var out=new ArrayList<Box>(b.list.size());
        for(var x:b.list){var t=skin.getOrDefault(x.tex(),x.tex());
            // a grey tint on white Techguns panels looks dirty: keep tints only where they mean something (red wheels, yellow rails)
            int c=t!=x.tex()&&(x.argb()==GREY||x.argb()==DARK)?W:x.argb();
            out.add(new Box(x.x0(),x.y0(),x.z0(),x.x1(),x.y1(),x.z1(),t,c,x.solid(),x.rotY(),x.px(),x.pz(),x.detail()));}
        return out;
    }

    private static void wind(B b,int n){
        // foundation with the control panel
        b.s(Tex.CASING,-0.5,0,-0.5,0.5,0.16,0.5);
        b.d(Tex.HAZARD,-0.5,0.1,-0.52,0.5,0.16,-0.5);
        b.s(Tex.CASING_LIGHT,-0.43,0.16,-0.43,0.43,0.9,0.43);
        for(int sx=-1;sx<=1;sx+=2)for(int sz=-1;sz<=1;sz+=2)b.d(Tex.CASING_DARK,sx*0.5,0.16,sz*0.5,sx*0.41,0.9,sz*0.41);
        b.d(Tex.FRONT,-0.3,0.24,-0.45,0.3,0.84,-0.43);
        b.d(Tex.PANEL,0.43,0.16,-0.18,0.45,0.78,0.18);
        b.d(Tex.VENT,-0.45,0.4,-0.25,-0.43,0.7,0.25);
        b.pipeY(0.18,0.47,0.05,0.16,1.4,false);
        b.s(Tex.CASING_DARK,-0.38,0.9,-0.38,0.38,1,0.38);
        // tower: tapered tube, flanges, ladder with cage on the back, cable on the side
        for(int i=1;i<=n;i++){
            float h=towerHalf(i,n);
            b.s(Tex.TOWER,-h,i,-h,h,i+1,h);
            b.d(Tex.CASING,GREY,-h-0.03,i,-h-0.03,h+0.03,i+0.07,h+0.03);
            b.d(Tex.LADDER,-0.13,i,h+0.005,0.13,i+1,h+0.03);
            for(double y:new double[]{i+0.15,i+0.65}){
                b.d(Tex.CASING_DARK,-0.2,y,h+0.2,0.2,y+0.04,h+0.23);
                b.d(Tex.CASING_DARK,-0.2,y,h,-0.17,y+0.04,h+0.23);b.d(Tex.CASING_DARK,0.17,y,h,0.2,y+0.04,h+0.23);
            }
            b.d(Tex.CASING_DARK,-0.2,i,h+0.2,-0.18,i+1,h+0.22);b.d(Tex.CASING_DARK,0.18,i,h+0.2,0.2,i+1,h+0.22);
            b.d(Tex.PIPE,DARK,h,i,-0.04,h+0.05,i+1,0.04);
        }
        double y=n+1;
        // nacelle: yaw bearing, housing with roof, hatches, vents, radiator, anemometer and vane
        b.s(Tex.CASING_DARK,-0.27,y,-0.27,0.27,y+0.08,0.27);
        b.s(Tex.CASING_LIGHT,-0.36,y+0.08,-0.5,0.36,y+0.72,0.5);
        b.d(Tex.CASING_LIGHT,-0.31,y+0.12,0.5,0.31,y+0.67,0.9);
        b.d(Tex.CASING,GREY,-0.22,y+0.2,0.9,0.22,y+0.58,1.0);
        b.d(Tex.CASING_LIGHT,-0.33,y+0.72,-0.46,0.33,y+0.78,0.86);
        b.d(Tex.HAZARD,-0.365,y+0.08,-0.505,0.365,y+0.13,-0.495);
        for(int k=0;k<5;k++)b.d(Tex.VENT,-0.24,y+0.78,0.34+k*0.1,0.24,y+0.92,0.38+k*0.1);
        for(int sx=-1;sx<=1;sx+=2){
            b.d(Tex.PANEL,sx*0.36,y+0.2,-0.32,sx*0.38,y+0.62,0.12);
            b.d(Tex.VENT,sx*0.36,y+0.28,0.22,sx*0.375,y+0.56,0.44);
            b.d(Tex.CASING_DARK,sx*0.31,y+0.6,0.55,sx*0.33,y+0.64,0.88);
        }
        b.d(Tex.CASING_DARK,0.16,y+0.78,0.7,0.2,y+1.08,0.74);b.d(Tex.CASING_DARK,0.04,y+1.06,0.7,0.32,y+1.08,0.74);
        b.d(Tex.BLADE,GREY,0.02,y+1.03,0.69,0.07,y+1.09,0.75);b.d(Tex.BLADE,GREY,0.29,y+1.03,0.69,0.34,y+1.09,0.75);
        b.d(Tex.CASING_DARK,-0.2,y+0.78,0.7,-0.16,y+1.05,0.74);b.d(Tex.BLADE,-0.19,y+0.95,0.74,-0.17,y+1.05,0.98);
        // gearbox and main shaft into the rotor cell; the turning rotor is drawn by the renderer
        b.s(Tex.CASING_DARK,-0.28,y+0.14,-0.64,0.28,y+0.68,-0.5);
        b.d(Tex.TOWER,-0.16,y+0.26,-0.78,0.16,y+0.58,-0.64);
        b.hit(-0.32,y+0.1,-1.2,0.32,y+0.74,-0.64);
    }

    private static final int CONCRETE=0xFFE6E2D6,RED=0xFFC8382A;
    /** Upright tank: two crossed boxes give it a rounded look, with steel bands and a stepped dome. */
    private static void tank(B b,Tex t,int c,double cx,double cz,double r,double y0,double y1){
        b.d(t,c,cx-r,y0,cz-r*0.72,cx+r,y1,cz+r*0.72);b.d(t,c,cx-r*0.72,y0,cz-r,cx+r*0.72,y1,cz+r);
        for(double y=y0+0.2;y<y1-0.1;y+=0.4)b.d(Tex.CASING,cx-r-0.02,y,cz-r*0.72-0.02,cx+r+0.02,y+0.05,cz+r*0.72+0.02);
        b.d(t,c,cx-r*0.75,y1,cz-r*0.55,cx+r*0.75,y1+0.08,cz+r*0.55);b.d(t,c,cx-r*0.45,y1+0.08,cz-r*0.35,cx+r*0.45,y1+0.14,cz+r*0.35);
    }
    /** Valve with a red hand wheel on a horizontal pipe. */
    private static void valve(B b,double x,double y,double z){
        b.d(Tex.CASING_DARK,x-0.07,y-0.07,z-0.07,x+0.07,y+0.07,z+0.07);b.d(Tex.CASING_DARK,x-0.015,y+0.07,z-0.015,x+0.015,y+0.16,z+0.015);
        b.d(Tex.CASING,RED,x-0.1,y+0.16,z-0.1,x+0.1,y+0.18,z+0.1);
    }
    /** Railing along x at height y on the edge z. */
    private static void railX(B b,double x0,double x1,double y,double z){
        for(double x=x0;x<=x1+1e-6;x+=Math.max(0.2,(x1-x0)/Math.max(1,Math.round((x1-x0)/0.4))))b.d(Tex.CASING_DARK,x-0.015,y,z-0.015,x+0.015,y+0.28,z+0.015);
        b.d(Tex.CASING,0xFFE0B020,x0-0.015,y+0.26,z-0.02,x1+0.015,y+0.29,z+0.02);b.d(Tex.CASING_DARK,x0,y+0.13,z-0.012,x1,y+0.15,z+0.012);
    }

    private static void geothermal(B b){
        // plinth and lower hall of light steel with posts and a belt course
        b.s(Tex.CASING,-1.5,0,-0.5,1.5,0.14,2.5);
        b.d(Tex.HAZARD,-1.5,0.08,-0.51,1.5,0.14,-0.5);
        b.s(Tex.CASING_LIGHT,-1.42,0.14,-0.18,1.42,1.2,2.42);
        for(double x:new double[]{-1.42,-0.47,0.47,1.36}){b.d(Tex.CASING,x,0.14,-0.22,x+0.06,1.2,-0.18);b.d(Tex.CASING,x,0.14,2.42,x+0.06,1.2,2.46);}
        b.d(Tex.CASING,-1.46,1.2,-0.22,1.46,1.28,2.46);
        // front: controller panel, door, glowing intakes (glow drawn behind the grilles), warning lamps
        b.d(Tex.FRONT,-0.3,0.24,-0.2,0.3,0.84,-0.18);
        for(int sx=-1;sx<=1;sx+=2){b.d(Tex.VENT,Math.min(sx*0.56,sx*1.3),0.3,-0.215,Math.max(sx*0.56,sx*1.3),0.86,-0.205);
            b.d(Tex.CASING_DARK,sx*0.4-0.03,0.9,-0.24,sx*0.4+0.03,0.96,-0.2);}
        b.d(Tex.HAZARD,-1.42,0.98,-0.215,1.42,1.05,-0.2);
        // upper level: turbine hall at the back, control room with windows front left, open deck with a railing
        b.s(Tex.CASING_LIGHT,-1.42,1.28,0.55,1.42,1.82,2.42);
        b.s(Tex.CASING,-1.46,1.82,0.51,1.46,1.9,2.46);
        for(int k=0;k<4;k++)b.s(Tex.GLASS,0xC8FFFFFF,-1.2+k*0.7,1.4,0.53,-0.8+k*0.7,1.7,0.55);
        b.s(Tex.CASING_LIGHT,-1.42,1.28,-0.15,-0.25,1.78,0.55);
        b.s(Tex.GLASS,0xC8FFFFFF,-1.36,1.4,-0.17,-0.31,1.68,-0.15);b.s(Tex.GLASS,0xC8FFFFFF,-1.44,1.4,-0.08,-1.42,1.68,0.48);
        b.d(Tex.CASING,-1.46,1.78,-0.19,-0.21,1.86,0.55);
        b.d(Tex.PANEL,-0.27,1.28,0.1,-0.25,1.7,0.45);
        railX(b,-0.2,0.5,1.28,-0.16);
        // steam separator on the deck (front right) and its pipes into the turbine hall
        tank(b,Tex.FAB_HOUSING,W,0.9,0.18,0.32,1.28,2.35);
        b.pipeX(2.2,0.18,0.07,0.2,0.9,true);b.pipeZ(0.2,2.2,0.07,0.18,0.62,true);b.pipeY(0.2,0.62,0.07,1.9,2.27,true);
        b.pipeX(1.62,0.18,0.06,0.15,0.58,true);valve(b,0.32,1.62,0.18);
        // lava pipes in front, a manifold and a valve
        b.pipeY(-1.3,-0.36,0.1,0,1.25,true);b.pipeY(1.3,-0.36,0.1,0,1.25,true);
        b.pipeX(1.12,-0.36,0.07,-1.3,1.3,true);valve(b,-0.9,1.12,-0.36);valve(b,0.9,1.12,-0.36);
        // roof: cooling tower (stepped hyperboloid of concrete) and two fan housings (fans turn in the renderer)
        double cx=-0.55,cz=1.55;double[] r={0.82,0.72,0.64,0.58,0.55,0.56,0.6,0.66};
        // open at the top (steam comes out of it): every ring is four walls round a hollow core with a dark floor
        for(int k=0;k<r.length;k++){double y0=1.9+k*0.22;b.hollow(Tex.CONCRETE,cx,cz,r[k],0.09,y0,y0+0.22);}
        b.d(Tex.CASING_DARK,cx-0.72,1.92,cz-0.72,cx+0.72,1.94,cz+0.72);
        double top=1.9+r.length*0.22;b.hollow(Tex.CASING,cx,cz,0.7,0.06,top,top+0.05);
        for(double fz:new double[]{0.95,1.95}){
            b.d(Tex.CASING,0.4,1.9,fz-0.42,0.45,2.14,fz+0.42);b.d(Tex.CASING,1.27,1.9,fz-0.42,1.32,2.14,fz+0.42);
            b.d(Tex.CASING,0.45,1.9,fz-0.42,1.27,2.14,fz-0.37);b.d(Tex.CASING,0.45,1.9,fz+0.37,1.27,2.14,fz+0.42);
            b.d(Tex.CASING_DARK,0.45,1.9,fz-0.37,1.27,1.92,fz+0.37);b.d(Tex.CASING_DARK,0.82,1.92,fz-0.04,0.9,2.0,fz+0.04);
            b.d(Tex.VENT,0.45,2.12,fz-0.37,1.27,2.13,fz+0.37);}
        // left: heat exchangers with headers, right: radiator fins; ladder at the back up to the roof
        for(int k=0;k<3;k++)b.d(Tex.PANEL,-1.5,0.3,0.15+k*0.75,-1.42,1.12,0.8+k*0.75);
        b.pipeZ(-1.48,1.16,0.045,-0.15,2.4,true);b.pipeZ(-1.48,0.22,0.045,-0.15,2.4,true);
        for(int k=0;k<3;k++)b.d(Tex.VENT,1.42,0.3,0.15+k*0.75,1.46,1.12,0.8+k*0.75);
        b.pipeZ(1.48,1.16,0.045,-0.15,2.4,true);b.pipeZ(1.48,0.22,0.045,-0.15,2.4,true);
        b.d(Tex.LADDER,-0.2,0.14,2.46,0.2,1.9,2.49);
    }

    /**
     * Wall with windows: the rectangle u0..u1 x v0..v1 (u along x or z, v up) between f0 and f1 across it is cut along
     * the window edges; cells inside a window get a glass pane in the middle of the wall, the rest is wall.
     */
    private static void wall(B b,boolean alongX,double f0,double f1,double u0,double u1,double v0,double v1,double[][] holes,Tex wallTex,Tex glass){
        var us=new java.util.TreeSet<Double>(List.of(u0,u1));var vs=new java.util.TreeSet<Double>(List.of(v0,v1));
        for(var h:holes){us.add(h[0]);us.add(h[1]);vs.add(h[2]);vs.add(h[3]);}
        Double[] U=us.toArray(new Double[0]),V=vs.toArray(new Double[0]);double gm=(f0+f1)/2;
        for(int a=0;a+1<U.length;a++)for(int c=0;c+1<V.length;c++){
            double cu=(U[a]+U[a+1])/2,cv=(V[c]+V[c+1])/2;boolean hole=false;
            for(var h:holes)if(cu>h[0]&&cu<h[1]&&cv>h[2]&&cv<h[3])hole=true;
            double g0=hole?gm-0.03:f0,g1=hole?gm+0.03:f1;
            if(alongX)b.s(hole?glass:wallTex,hole?0xC8FFFFFF:W,U[a],V[c],g0,U[a+1],V[c+1],g1);
            else b.s(hole?glass:wallTex,hole?0xC8FFFFFF:W,g0,V[c],U[a],g1,V[c+1],U[a+1]);
        }
    }
    /** Double door (Techguns tech door) in a steel frame, on a wall facing -z at z. */
    private static void door(B b,double xa,double xb,double y0,double y1,double z){
        b.d(Tex.CASING_DARK,xa-0.06,y0,z-0.03,xb+0.06,y1+0.06,z);
        b.d(Tex.TECHDOOR,xa,y0,z-0.04,xb,y1,z-0.03);
    }

    private static void fission(B b){
        double x0=-1.5,x1=1.5,z0=-0.5,z1=2.5,y0=-1,y1=2,mid=1.0,c=0.34,t=0.14,wy0=y0+0.2,wy1=y1-0.25;
        // plinth and corner columns of the reactor hall (Techguns reaction chamber panels and glass)
        b.s(Tex.CASING,x0,y0,z0,x1,wy0,z1);
        b.d(Tex.HAZARD,x0,y0+0.14,z0-0.01,x1,wy0,z0);
        for(double sx:new double[]{x0,x1-c})for(double sz:new double[]{z0,z1-c})b.s(Tex.CASING,sx,wy0,sz,sx+c,wy1,sz+c);
        // sides and back: big windows in two rows, so the core can be seen from three sides
        double[][] sideHoles={{z0+c+0.12,mid-0.05,-0.55,0.82},{mid+0.05,z1-c-0.12,-0.55,0.82},{z0+c+0.12,mid-0.05,1.0,1.5},{mid+0.05,z1-c-0.12,1.0,1.5}};
        wall(b,false,x0,x0+t,z0+c,z1-c,wy0,wy1,sideHoles,Tex.CASING_LIGHT,Tex.GLASS);
        wall(b,false,x1-t,x1,z0+c,z1-c,wy0,wy1,sideHoles,Tex.CASING_LIGHT,Tex.GLASS);
        wall(b,true,z1-t,z1,x0+c,x1-c,wy0,wy1,new double[][]{{x0+c+0.12,-0.05,-0.55,0.82},{0.05,x1-c-0.12,-0.55,0.82},{x0+c+0.12,-0.05,1.0,1.5},{0.05,x1-c-0.12,1.0,1.5}},Tex.CASING_LIGHT,Tex.GLASS);
        // front: two doors, the controller between them, a row of windows above, status strip (lit by the renderer)
        wall(b,true,z0,z0+t,x0+c,x1-c,wy0,wy1,new double[][]{{-1.08,-0.5,1.0,1.32},{0.5,1.08,1.0,1.32}},Tex.CASING_LIGHT,Tex.GLASS);
        b.d(Tex.CASING_DARK,-0.42,0.04,z0-0.02,0.42,0.9,z0);b.d(Tex.FRONT,-0.34,0.12,z0-0.03,0.34,0.86,z0-0.02);
        door(b,-1.08,-0.55,wy0,0.7,z0);door(b,0.55,1.08,wy0,0.7,z0);
        b.d(Tex.CASING_DARK,-1.1,1.4,z0-0.02,1.1,1.54,z0);
        // inside: coolant pool and the rod grid frame (rods glow, control rods move in the renderer)
        b.d(Tex.CASING_DARK,-0.62,-0.8,0.38,0.62,-0.72,1.62);
        for(double sx:new double[]{-0.6,0.54})for(double sz:new double[]{0.4,1.54})b.d(Tex.CASING_DARK,sx,-0.72,sz,sx+0.06,1.6,sz+0.06);
        b.d(Tex.CASING_DARK,-0.62,1.55,0.38,0.62,1.62,1.62);
        // sides: coolant pumps with their pipes up into the hall
        for(int side=-1;side<=1;side+=2){
            double px=side<0?x0-0.24:x1+0.02;
            b.d(Tex.CASING,px,y0+0.2,0.35,px+0.22,y0+0.78,1.65);b.d(Tex.VENT,side<0?px-0.005:px+0.22,y0+0.3,0.45,side<0?px:px+0.225,y0+0.7,1.55);
            b.pipeY(side*1.62,1.0,0.07,y0+0.78,1.55,true);b.pipeX(1.55,1.0,0.07,Math.min(side*1.62,side*1.5),Math.max(side*1.62,side*1.5),true);
            valve(b,side*1.62,0.2,1.0);
        }
        // cornice, roof slab, containment dome (stepped octagon), steam lines and stacks, coolant pipes down the back
        b.s(Tex.CASING,x0-0.03,wy1,z0-0.03,x1+0.03,y1-0.12,z1+0.03);
        b.s(Tex.CASING_LIGHT,x0,y1-0.12,z0,x1,y1,z1);
        double[] dr={1.15,1.02,0.86,0.66,0.42};
        for(int k=0;k<dr.length;k++){double y=y1+k*0.13,r=dr[k];b.d(Tex.CASING_LIGHT,-r,y,mid-r*0.7,r,y+0.13,mid+r*0.7);b.d(Tex.CASING_LIGHT,-r*0.7,y,mid-r,r*0.7,y+0.13,mid+r);}
        b.d(Tex.CASING,-0.12,y1+0.65,mid-0.12,0.12,y1+0.75,mid+0.12);
        for(int side=-1;side<=1;side+=2){
            b.pipeX(y1+0.18,mid,0.08,Math.min(side*1.1,side*1.62),Math.max(side*1.1,side*1.62),true);
            b.pipeY(side*1.62,mid,0.08,y1-0.6,y1+0.26,true);
        }
        b.pipeY(-0.7,z1+0.08,0.08,y0+0.2,y1,true);b.pipeY(0.7,z1+0.08,0.08,y0+0.2,y1,true);
        b.pipeX(y1+0.08,z1+0.08,0.08,-0.7,0.7,true);valve(b,0,y1+0.08,z1+0.08);
        b.pipeY(-1.15,0.15,0.15,y1,y1+0.8,true);b.ringY(-1.15,0.15,0.18,y1+0.8,y1+0.86);
        b.pipeY(1.15,1.85,0.15,y1,y1+0.8,true);b.ringY(1.15,1.85,0.18,y1+0.8,y1+0.86);
    }

    private static void fusion(B b){
        double x0=-2.5,x1=2.5,z0=-0.5,z1=4.5,cz=2,y0=-1,y1=2,c=0.42,t=0.16,wy0=-0.62,wy1=1.62;
        // deck and corner pillars of the tokamak hall (Techguns fabricator housing and glass)
        b.s(Tex.CASING_DARK,x0,y0,z0,x1,wy0,z1);
        b.d(Tex.VENT,-1.6,wy0,0.4,1.6,wy0+0.02,3.6);
        b.d(Tex.HAZARD,x0,wy0-0.04,z0-0.01,x1,wy0,z0);
        for(double sx:new double[]{x0,x1-c})for(double sz:new double[]{z0,z1-c})b.s(Tex.CASING,sx,wy0,sz,sx+c,wy1,sz+c);
        // glass walls on a low parapet, split by mullions, so the coils and the plasma are on show
        double[][] side={{z0+c+0.06,1.25,-0.32,1.5},{1.35,2.65,-0.32,1.5},{2.75,z1-c-0.06,-0.32,1.5}};
        wall(b,false,x0,x0+t,z0+c,z1-c,wy0,wy1,side,Tex.CASING,Tex.GLASS);
        wall(b,false,x1-t,x1,z0+c,z1-c,wy0,wy1,side,Tex.CASING,Tex.GLASS);
        wall(b,true,z1-t,z1,x0+c,x1-c,wy0,wy1,new double[][]{{x0+c+0.06,-1.05,-0.32,1.5},{-0.95,-0.05,-0.32,1.5},{0.05,0.95,-0.32,1.5},{1.05,x1-c-0.06,-0.32,1.5}},Tex.CASING,Tex.GLASS);
        wall(b,true,z0,z0+t,x0+c,x1-c,wy0,wy1,new double[][]{{x0+c+0.06,-1.05,1.0,1.5},{-0.95,-0.52,1.0,1.5},{0.52,0.95,1.0,1.5},{1.05,x1-c-0.06,1.0,1.5}},Tex.CASING,Tex.GLASS);
        // front: controller in its frame, two hall doors
        b.d(Tex.CASING_DARK,-0.46,0,z0-0.02,0.46,1,z0);b.d(Tex.FRONT,-0.36,0.1,z0-0.03,0.36,0.88,z0-0.02);
        door(b,-1.9,-0.8,wy0,0.8,z0);door(b,0.8,1.9,wy0,0.8,z0);
        // inside: vacuum vessel (upper and lower walls of the torus), toroidal field coils (open frames), central solenoid
        for(int i=0;i<16;i++){double a=i*22.5+11.25;
            b.rot(Tex.CASING,GREY,-0.34,0.12,cz+1.18,0.34,0.28,cz+1.7,a,0,cz);b.rot(Tex.CASING,GREY,-0.34,0.72,cz+1.18,0.34,0.88,cz+1.7,a,0,cz);
            b.rot(Tex.CASING_DARK,W,-0.34,0.28,cz+1.66,0.34,0.72,cz+1.72,a,0,cz);}
        for(int i=0;i<12;i++){double a=i*30;
            b.rot(Tex.COIL,W,-0.12,-0.45,cz+1.8,0.12,1.45,cz+1.96,a,0,cz);b.rot(Tex.COIL,W,-0.12,-0.45,cz+0.98,0.12,1.45,cz+1.08,a,0,cz);
            b.rot(Tex.CASING_DARK,W,-0.14,-0.6,cz+0.96,0.14,-0.45,cz+1.98,a,0,cz);b.rot(Tex.CASING_DARK,W,-0.14,1.45,cz+0.96,0.14,1.6,cz+1.98,a,0,cz);}
        b.d(Tex.COIL,-0.32,-0.6,cz-0.32,0.32,1.62,cz+0.32);
        b.d(Tex.CASING_DARK,-0.38,-0.6,cz-0.38,0.38,-0.4,cz+0.38);b.d(Tex.CASING_DARK,-0.38,1.42,cz-0.38,0.38,1.62,cz+0.38);
        // cornice and roof with a glass skylight over the chamber
        b.s(Tex.CASING_DARK,x0-0.03,wy1,z0-0.03,x1+0.03,wy1+0.12,z1+0.03);
        b.s(Tex.CASING,x0,wy1+0.12,z0,x1,y1,z0+0.9);b.s(Tex.CASING,x0,wy1+0.12,z1-0.9,x1,y1,z1);
        b.s(Tex.CASING,x0,wy1+0.12,z0+0.9,x0+0.9,y1,z1-0.9);b.s(Tex.CASING,x1-0.9,wy1+0.12,z0+0.9,x1,y1,z1-0.9);
        b.s(Tex.GLASS,0xC8FFFFFF,x0+0.9,y1-0.1,z0+0.9,x1-0.9,y1-0.04,z1-0.9);
        for(int k=-1;k<=1;k++)b.d(Tex.CASING_DARK,k*0.8-0.05,y1-0.12,z0+0.9,k*0.8+0.05,y1,z1-0.9);
        b.d(Tex.CASING_DARK,x0+0.9,y1-0.12,cz-0.05,x1-0.9,y1,cz+0.05);
        // cryogenic lines along the outside of the side walls
        for(int s2=-1;s2<=1;s2+=2){b.pipeZ(s2*2.6,-0.4,0.07,z0+0.42,z1-0.42,true);b.pipeZ(s2*2.6,1.3,0.07,z0+0.42,z1-0.42,true);
            b.pipeY(s2*2.6,0.2,0.07,-0.4,1.3,true);b.pipeY(s2*2.6,3.8,0.07,-0.4,1.3,true);}
    }

    /** Collision shape of every block of a formed plant, keyed by world position. */
    public static Map<BlockPos,VoxelShape> shapes(BlockPos origin,Direction facing,List<Box> boxes){
        var right=facing.getClockWise();var back=facing.getOpposite();
        Map<BlockPos,VoxelShape> map=new HashMap<>();
        for(var box:boxes){
            if(!box.solid())continue;
            double fx0=box.x0(),fz0=box.z0(),fx1=box.x1(),fz1=box.z1();
            if(box.rotY()!=0){
                // bounds of the turned footprint (same turn as the renderer: around +Y through (px,pz))
                double a=Math.toRadians(box.rotY()),c=Math.cos(a),sn=Math.sin(a);fx0=fz0=Double.MAX_VALUE;fx1=fz1=-Double.MAX_VALUE;
                for(double cx:new double[]{box.x0(),box.x1()})for(double cz:new double[]{box.z0(),box.z1()}){
                    double dx=cx-box.px(),dz=cz-box.pz(),rx=box.px()+dx*c+dz*sn,rz=box.pz()-dx*sn+dz*c;
                    fx0=Math.min(fx0,rx);fx1=Math.max(fx1,rx);fz0=Math.min(fz0,rz);fz1=Math.max(fz1,rz);}
            }
            // two corners in world space (exact: the frame axes are world axes)
            double ax=origin.getX()+0.5+right.getStepX()*fx0+back.getStepX()*fz0,az=origin.getZ()+0.5+right.getStepZ()*fx0+back.getStepZ()*fz0;
            double bx=origin.getX()+0.5+right.getStepX()*fx1+back.getStepX()*fz1,bz=origin.getZ()+0.5+right.getStepZ()*fx1+back.getStepZ()*fz1;
            double minX=Math.min(ax,bx),maxX=Math.max(ax,bx),minZ=Math.min(az,bz),maxZ=Math.max(az,bz),minY=origin.getY()+box.y0(),maxY=origin.getY()+box.y1();
            for(int x=(int)Math.floor(minX);x<Math.ceil(maxX);x++)for(int y=(int)Math.floor(minY);y<Math.ceil(maxY);y++)for(int z=(int)Math.floor(minZ);z<Math.ceil(maxZ);z++){
                double x0=Math.max(0,minX-x),x1=Math.min(1,maxX-x),y0=Math.max(0,minY-y),y1=Math.min(1,maxY-y),z0=Math.max(0,minZ-z),z1=Math.min(1,maxZ-z);
                if(x1-x0<1e-3||y1-y0<1e-3||z1-z0<1e-3)continue;
                map.merge(new BlockPos(x,y,z),Shapes.box(x0,y0,z0,x1,y1,z1),(p,q)->Shapes.joinUnoptimized(p,q,BooleanOp.OR));
            }
        }
        map.replaceAll((p,s)->s.optimize());
        return map;
    }
}
