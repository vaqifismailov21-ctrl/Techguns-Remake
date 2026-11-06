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
    public enum Tex{CASING,CASING_LIGHT,CASING_DARK,GLASS,TOWER,COIL,BLADE,FRONT,LADDER,PIPE,VENT,HAZARD,PANEL,NONE}
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
        /** Vertical pipe with flanges. */
        void pipeY(double x,double z,double r,double y0,double y1,boolean solid){
            add(Tex.PIPE,x-r,y0,z-r,x+r,y1,z+r,W,false);
            for(double y=y0+0.25;y<y1-0.1;y+=0.75)d(Tex.CASING_DARK,x-r-0.025,y,z-r-0.025,x+r+0.025,y+0.06,z+r+0.025);
        }
        void pipeX(double y,double z,double r,double x0,double x1,boolean solid){
            add(Tex.PIPE,x0,y-r,z-r,x1,y+r,z+r,W,false);
            for(double x=x0+0.3;x<x1-0.1;x+=0.75)d(Tex.CASING_DARK,x,y-r-0.025,z-r-0.025,x+0.06,y+r+0.025,z+r+0.025);
        }
        void pipeZ(double x,double y,double r,double z0,double z1,boolean solid){
            add(Tex.PIPE,x-r,y-r,z0,x+r,y+r,z1,W,false);
            for(double z=z0+0.3;z<z1-0.1;z+=0.75)d(Tex.CASING_DARK,x-r-0.025,y-r-0.025,z,x+r+0.025,y+r+0.025,z+0.06);
        }
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
        return b.list;
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
        tank(b,Tex.TOWER,0xFFF0F0F0,0.9,0.18,0.32,1.28,2.35);
        b.pipeX(2.2,0.18,0.07,0.2,0.9,true);b.pipeZ(0.2,2.2,0.07,0.18,0.62,true);b.pipeY(0.2,0.62,0.07,1.9,2.27,true);
        b.pipeX(1.62,0.18,0.06,0.15,0.58,true);valve(b,0.32,1.62,0.18);
        // lava pipes in front, a manifold and a valve
        b.pipeY(-1.3,-0.36,0.1,0,1.25,true);b.pipeY(1.3,-0.36,0.1,0,1.25,true);
        b.pipeX(1.12,-0.36,0.07,-1.3,1.3,true);valve(b,-0.9,1.12,-0.36);valve(b,0.9,1.12,-0.36);
        // roof: cooling tower (stepped hyperboloid of concrete) and two fan housings (fans turn in the renderer)
        double cx=-0.55,cz=1.55;double[] r={0.82,0.72,0.64,0.58,0.55,0.56,0.6,0.66};
        for(int k=0;k<r.length;k++){double y0=1.9+k*0.22;b.d(Tex.TOWER,CONCRETE,cx-r[k],y0,cz-r[k],cx+r[k],y0+0.22,cz+r[k]);}
        b.d(Tex.CASING,cx-0.7,1.9+r.length*0.22,cz-0.7,cx+0.7,1.95+r.length*0.22,cz+0.7);
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

    private static void fission(B b){
        double x0=-1.5,x1=1.5,z0=-0.5,z1=2.5,y0=-1,y1=2,t=0.14;
        b.s(Tex.CASING,x0,y0,z0,x1,y0+0.2,z1);
        b.d(Tex.HAZARD,x0,y0+0.14,z0-0.01,x1,y0+0.2,z0);
        // corner columns with caps, light walls with large framed windows (sides and back)
        for(double sx:new double[]{x0,x1-0.34})for(double sz:new double[]{z0,z1-0.34}){b.s(Tex.CASING,sx,y0+0.2,sz,sx+0.34,y1-0.25,sz+0.34);b.d(Tex.CASING_DARK,sx-0.02,y1-0.32,sz-0.02,sx+0.36,y1-0.25,sz+0.36);}
        double wy0=-0.35,wy1=1.3,wa=-0.9,wb=0.9,mid=1.0;
        b.s(Tex.CASING_LIGHT,x0+0.34,y0+0.2,z0+0.02,x1-0.34,y1-0.25,z0+t);
        for(int side=-1;side<=1;side+=2){
            double xa=side<0?x0+0.02:x1-t,xb=side<0?x0+t:x1-0.02,xo=side<0?x0:x1-0.02,xi=side<0?x0+0.02:x1;
            b.s(Tex.CASING_LIGHT,xa,y0+0.2,z0+0.34,xb,wy0,z1-0.34);b.s(Tex.CASING_LIGHT,xa,wy1,z0+0.34,xb,y1-0.25,z1-0.34);
            b.s(Tex.CASING_LIGHT,xa,wy0,z0+0.34,xb,wy1,mid+wa);b.s(Tex.CASING_LIGHT,xa,wy0,mid+wb,xb,wy1,z1-0.34);
            b.s(Tex.GLASS,0xC0FFFFFF,xa+0.04,wy0,mid+wa,xb-0.04,wy1,mid+wb);
            b.d(Tex.CASING,Math.min(xo,xi)-0.01,wy0,mid-0.03,Math.max(xo,xi)+0.01,wy1,mid+0.03);b.d(Tex.CASING,Math.min(xo,xi)-0.01,(wy0+wy1)/2-0.03,mid+wa,Math.max(xo,xi)+0.01,(wy0+wy1)/2+0.03,mid+wb);
            // coolant pump on the side with its pipe up into the wall
            double px=side<0?x0-0.24:x1+0.02;
            b.d(Tex.CASING,px,y0+0.2,0.35,px+0.22,y0+0.78,1.65);b.d(Tex.VENT,side<0?px-0.005:px+0.22,y0+0.3,0.45,side<0?px:px+0.225,y0+0.7,1.55);
            b.pipeY(side*1.62,1.0,0.07,y0+0.78,1.55,true);b.pipeX(1.55,1.0,0.07,Math.min(side*1.62,side*1.5),Math.max(side*1.62,side*1.5),true);
            valve(b,side*1.62,0.2,1.0);
        }
        b.s(Tex.CASING_LIGHT,x0+0.34,y0+0.2,z1-t,x1-0.34,wy0,z1-0.02);b.s(Tex.CASING_LIGHT,x0+0.34,wy1,z1-t,x1-0.34,y1-0.25,z1-0.02);
        b.s(Tex.CASING_LIGHT,x0+0.34,wy0,z1-t,wa,wy1,z1-0.02);b.s(Tex.CASING_LIGHT,wb,wy0,z1-t,x1-0.34,wy1,z1-0.02);
        b.s(Tex.GLASS,0xC0FFFFFF,wa,wy0,z1-t+0.04,wb,wy1,z1-0.06);
        b.d(Tex.CASING,-0.03,wy0,z1-0.02,0.03,wy1,z1+0.01);b.d(Tex.CASING,wa,(wy0+wy1)/2-0.03,z1-0.02,wb,(wy0+wy1)/2+0.03,z1+0.01);
        // front: controller panel between two hatches, radiation warning, status strip (lit by the renderer)
        b.d(Tex.FRONT,-0.34,0.12,z0,0.34,0.86,z0+0.02);
        b.d(Tex.PANEL,-1.08,-0.3,z0,-0.5,0.95,z0+0.02);b.d(Tex.PANEL,0.5,-0.3,z0,1.08,0.95,z0+0.02);
        b.d(Tex.HAZARD,-0.3,1.0,z0-0.005,0.3,1.3,z0+0.02);
        b.d(Tex.CASING_DARK,-1.1,1.42,z0-0.02,1.1,1.52,z0+0.02);
        // inside: coolant pool and the rod grid frame (rods glow, control rods move in the renderer)
        b.d(Tex.CASING_DARK,-0.62,-0.8,0.38,0.62,-0.72,1.62);
        for(double sx:new double[]{-0.6,0.54})for(double sz:new double[]{0.4,1.54})b.d(Tex.CASING_DARK,sx,-0.72,sz,sx+0.06,1.6,sz+0.06);
        b.d(Tex.CASING_DARK,-0.62,1.55,0.38,0.62,1.62,1.62);
        // roof: slab, containment dome (stepped octagon), steam lines with valves down the back
        b.s(Tex.CASING_LIGHT,x0,y1-0.25,z0,x1,y1,z1);
        b.d(Tex.CASING,x0-0.02,y1-0.05,z0-0.02,x1+0.02,y1,z1+0.02);
        double[] dr={1.15,1.02,0.86,0.66,0.42};
        for(int k=0;k<dr.length;k++){double y=y1+k*0.13,r=dr[k];b.d(Tex.CASING_LIGHT,-r,y,mid-r*0.7,r,y+0.13,mid+r*0.7);b.d(Tex.CASING_LIGHT,-r*0.7,y,mid-r,r*0.7,y+0.13,mid+r);}
        b.d(Tex.CASING,-0.12,y1+0.65,mid-0.12,0.12,y1+0.75,mid+0.12);
        for(int side=-1;side<=1;side+=2){
            b.pipeX(y1+0.18,mid,0.08,Math.min(side*1.1,side*1.62),Math.max(side*1.1,side*1.62),true);
            b.pipeY(side*1.62,mid,0.08,y1-0.6,y1+0.26,true);
        }
        b.pipeY(-0.7,z1+0.08,0.08,y0+0.2,y1,true);b.pipeY(0.7,z1+0.08,0.08,y0+0.2,y1,true);
        b.pipeX(y1+0.08,z1+0.08,0.08,-0.7,0.7,true);valve(b,0,y1+0.08,z1+0.08);
        b.pipeY(-1.15,0.15,0.15,y1,y1+0.8,true);b.d(Tex.CASING,-1.33,y1+0.8,-0.03,-0.97,y1+0.86,0.33);
        b.pipeY(1.15,1.85,0.15,y1,y1+0.8,true);b.d(Tex.CASING,0.97,y1+0.8,1.67,1.33,y1+0.86,2.03);
    }

    private static void fusion(B b){
        double x0=-2.5,x1=2.5,z0=-0.5,z1=4.5,cz=2;
        // deck with grating and the support pillars
        b.s(Tex.CASING_DARK,x0,-1,z0,x1,-0.62,z1);
        b.d(Tex.VENT,-1.6,-0.62,0.4,1.6,-0.6,3.6);
        b.d(Tex.HAZARD,x0,-0.66,z0-0.01,x1,-0.62,z0+0.01);
        for(double sx:new double[]{x0,x1-0.42})for(double sz:new double[]{z0,z1-0.42})b.s(Tex.CASING,GREY,sx,-0.62,sz,sx+0.42,1.62,sz+0.42);
        // top: frame with a glass dome over the chamber
        b.s(Tex.CASING,x0,1.62,z0,x1,2,z0+0.9);b.s(Tex.CASING,x0,1.62,z1-0.9,x1,2,z1);
        b.s(Tex.CASING,x0,1.62,z0+0.9,x0+0.9,2,z1-0.9);b.s(Tex.CASING,x1-0.9,1.62,z0+0.9,x1,2,z1-0.9);
        b.s(Tex.GLASS,0xB0FFFFFF,x0+0.9,1.66,z0+0.9,x1-0.9,1.96,z1-0.9);
        for(int k=-1;k<=1;k++)b.d(Tex.CASING_DARK,k*0.75-0.04,1.96,z0+0.9,k*0.75+0.04,2.02,z1-0.9);
        b.d(Tex.HAZARD,x0,1.62,z0-0.01,x1,1.68,z0+0.01);
        // ring layer: controller panel, vacuum vessel (torus of boxes), toroidal field coils, central solenoid
        b.s(Tex.CASING,-0.46,0,z0+0.02,0.46,1,z0+0.24);
        b.d(Tex.FRONT,-0.36,0.1,z0,0.36,0.88,z0+0.02);
        b.hit(x0,0,z0,x1,1,z0+1);b.hit(x0,0,z1-1,x1,1,z1);b.hit(x0,0,z0+1,x0+1,1,z1-1);b.hit(x1-1,0,z0+1,x1,1,z1-1);
        // vessel: upper and lower walls of the torus, the plasma runs between them
        for(int i=0;i<16;i++){double a=i*22.5+11.25;
            b.rot(Tex.CASING,GREY,-0.34,0.12,cz+1.18,0.34,0.28,cz+1.7,a,0,cz);b.rot(Tex.CASING,GREY,-0.34,0.72,cz+1.18,0.34,0.88,cz+1.7,a,0,cz);
            b.rot(Tex.CASING_DARK,W,-0.34,0.28,cz+1.66,0.34,0.72,cz+1.72,a,0,cz);}
        // toroidal field coils: open frames around the vessel
        for(int i=0;i<12;i++){double a=i*30;
            b.rot(Tex.COIL,W,-0.12,-0.45,cz+1.8,0.12,1.45,cz+1.96,a,0,cz);
            b.rot(Tex.COIL,W,-0.12,-0.45,cz+0.98,0.12,1.45,cz+1.08,a,0,cz);
            b.rot(Tex.CASING_DARK,W,-0.14,-0.6,cz+0.96,0.14,-0.45,cz+1.98,a,0,cz);
            b.rot(Tex.CASING_DARK,W,-0.14,1.45,cz+0.96,0.14,1.6,cz+1.98,a,0,cz);
        }
        b.d(Tex.COIL,-0.32,-0.6,cz-0.32,0.32,1.62,cz+0.32);
        b.d(Tex.CASING_DARK,-0.38,-0.6,cz-0.38,0.38,-0.4,cz+0.38);b.d(Tex.CASING_DARK,-0.38,1.42,cz-0.38,0.38,1.62,cz+0.38);
        // cryogenic lines along the sides
        for(int side=-1;side<=1;side+=2){b.pipeZ(side*2.42,-0.45,0.07,z0+0.42,z1-0.42,false);b.pipeZ(side*2.42,1.45,0.07,z0+0.42,z1-0.42,false);
            b.pipeY(side*2.42,cz,0.07,-0.45,1.45,false);}
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
