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
    public enum Tex{CASING,CASING_DARK,GLASS,TOWER,COIL,BLADE,FRONT,LADDER,PIPE,VENT,HAZARD,PANEL,NONE}
    /** Axis aligned box; rotY turns it around the vertical line through (px,pz), such boxes are never solid. */
    public record Box(float x0,float y0,float z0,float x1,float y1,float z1,Tex tex,int argb,boolean solid,float rotY,float px,float pz){}
    private static final int W=0xFFFFFFFF,GREY=0xFFB4B4B4,DARK=0xFF8C8C8C;

    private static final class B {
        final List<Box> list=new ArrayList<>();
        void s(Tex t,double x0,double y0,double z0,double x1,double y1,double z1){add(t,x0,y0,z0,x1,y1,z1,W,true);}
        void s(Tex t,int c,double x0,double y0,double z0,double x1,double y1,double z1){add(t,x0,y0,z0,x1,y1,z1,c,true);}
        void d(Tex t,double x0,double y0,double z0,double x1,double y1,double z1){add(t,x0,y0,z0,x1,y1,z1,W,false);}
        void d(Tex t,int c,double x0,double y0,double z0,double x1,double y1,double z1){add(t,x0,y0,z0,x1,y1,z1,c,false);}
        /** Collision only, not drawn. */
        void hit(double x0,double y0,double z0,double x1,double y1,double z1){add(Tex.NONE,x0,y0,z0,x1,y1,z1,0,true);}
        void rot(Tex t,int c,double x0,double y0,double z0,double x1,double y1,double z1,double deg,double px,double pz){
            list.add(new Box((float)x0,(float)y0,(float)z0,(float)x1,(float)y1,(float)z1,t,c,false,(float)deg,(float)px,(float)pz));}
        private void add(Tex t,double x0,double y0,double z0,double x1,double y1,double z1,int c,boolean solid){
            list.add(new Box((float)Math.min(x0,x1),(float)Math.min(y0,y1),(float)Math.min(z0,z1),(float)Math.max(x0,x1),(float)Math.max(y0,y1),(float)Math.max(z0,z1),t,c,solid,0,0,0));}
        /** Vertical pipe with flanges. */
        void pipeY(double x,double z,double r,double y0,double y1,boolean solid){
            add(Tex.PIPE,x-r,y0,z-r,x+r,y1,z+r,W,solid);
            for(double y=y0+0.25;y<y1-0.1;y+=0.75)d(Tex.CASING_DARK,x-r-0.025,y,z-r-0.025,x+r+0.025,y+0.06,z+r+0.025);
        }
        void pipeX(double y,double z,double r,double x0,double x1,boolean solid){
            add(Tex.PIPE,x0,y-r,z-r,x1,y+r,z+r,W,solid);
            for(double x=x0+0.3;x<x1-0.1;x+=0.75)d(Tex.CASING_DARK,x,y-r-0.025,z-r-0.025,x+0.06,y+r+0.025,z+r+0.025);
        }
        void pipeZ(double x,double y,double r,double z0,double z1,boolean solid){
            add(Tex.PIPE,x-r,y-r,z0,x+r,y+r,z1,W,solid);
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
        b.s(Tex.CASING_DARK,-0.5,0,-0.5,0.5,0.16,0.5);
        b.d(Tex.HAZARD,-0.5,0.1,-0.52,0.5,0.16,-0.5);
        b.s(Tex.CASING,-0.43,0.16,-0.43,0.43,0.9,0.43);
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
            if(i%4==1)b.d(Tex.CASING,GREY,-h-0.03,i,-h-0.03,h+0.03,i+0.07,h+0.03);
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
        b.s(Tex.CASING,-0.36,y+0.08,-0.5,0.36,y+0.72,0.5);
        b.d(Tex.CASING,-0.31,y+0.12,0.5,0.31,y+0.67,0.9);
        b.d(Tex.CASING,GREY,-0.22,y+0.2,0.9,0.22,y+0.58,1.0);
        b.d(Tex.CASING,0xFFDADADA,-0.33,y+0.72,-0.46,0.33,y+0.78,0.86);
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

    private static void geothermal(B b){
        b.s(Tex.CASING_DARK,-1.5,0,-0.5,1.5,0.15,2.5);
        b.s(Tex.CASING,-1.4,0.15,-0.2,1.4,1.55,2.4);
        b.s(Tex.CASING,0xFFD0D0D0,-1.46,1.55,-0.26,1.46,1.7,2.46);
        b.d(Tex.CASING_DARK,-1.46,1.7,-0.26,1.46,1.78,-0.18);b.d(Tex.CASING_DARK,-1.46,1.7,2.38,1.46,1.78,2.46);
        // front: control panel, glowing intakes (glow drawn behind the grilles by the renderer), hazard band
        b.d(Tex.FRONT,-0.3,0.25,-0.22,0.3,0.85,-0.2);
        for(int sx=-1;sx<=1;sx+=2){b.d(Tex.VENT,sx*0.5,0.28,-0.215,sx*1.25,0.82,-0.205);}
        for(int k=-1;k<=1;k++)b.d(Tex.VENT,k*0.85-0.35,1.02,-0.215,k*0.85+0.35,1.42,-0.205);
        b.d(Tex.HAZARD,-1.4,0.88,-0.215,1.4,0.96,-0.2);
        // walkway and the pipes down into the lava
        b.s(Tex.CASING_DARK,-1.5,0.15,-0.5,1.5,0.2,-0.2);
        b.pipeY(-1.28,-0.36,0.1,0,1.62,true);b.pipeY(1.28,-0.36,0.1,0,1.62,true);
        b.pipeX(1.5,-0.36,0.08,-1.28,1.28,false);
        b.d(Tex.CASING_DARK,-0.08,0.2,-0.44,0.08,1.42,-0.28);
        // left: heat exchanger panels, right: radiator fins and a ladder to the roof
        for(int k=0;k<3;k++)b.d(Tex.PANEL,-1.48,0.3,0.15+k*0.75,-1.4,1.35,0.8+k*0.75);
        b.pipeZ(-1.46,1.42,0.05,-0.2,2.4,false);
        for(int k=0;k<9;k++)b.d(Tex.VENT,1.4,0.35,0.1+k*0.2,1.5,1.3,0.16+k*0.2);
        b.d(Tex.LADDER,-0.2,0.15,2.4,0.2,1.7,2.43);
        // roof: pump house, cooling tower (stepped hyperboloid), pipe between them
        b.d(Tex.CASING,-1.25,1.7,0.15,-0.45,2.05,1.0);
        b.d(Tex.VENT,-1.25,1.78,-0.0,-0.45,1.98,0.15);
        b.d(Tex.CASING_DARK,-1.28,2.05,0.12,-0.42,2.1,1.03);
        double cx=0.45,cz=1.3;
        double[] r={0.85,0.74,0.65,0.59,0.56,0.57,0.61,0.66};
        for(int k=0;k<r.length;k++){double y0=1.7+k*0.2;b.d(Tex.TOWER,0xFFDCD8CC,cx-r[k],y0,cz-r[k],cx+r[k],y0+0.2,cz+r[k]);}
        b.d(Tex.CASING_DARK,cx-0.62,3.3,cz-0.62,cx+0.62,3.34,cz+0.62);
        b.pipeX(1.9,0.6,0.06,-0.45,cx-0.6,false);
        b.hit(-1.5,1.7,-0.5,1.5,2.0,2.5);
    }

    private static void fission(B b){
        double x0=-1.5,x1=1.5,z0=-0.5,z1=2.5,y0=-1,y1=2;
        b.s(Tex.CASING_DARK,x0,y0,z0,x1,y0+0.22,z1);
        // corner columns and the walls between them, with a window in the middle of the sides and the back
        for(double sx:new double[]{x0,x1-0.36})for(double sz:new double[]{z0,z1-0.36})b.s(Tex.CASING,GREY,sx,y0+0.22,sz,sx+0.36,y1-0.25,sz+0.36);
        double w0=-0.15,w1=1.15,wa=-0.8,wb=0.8,t=0.16;
        b.s(Tex.CASING,x0+0.36,y0+0.22,z0+0.02,x1-0.36,y1-0.25,z0+t);
        b.d(Tex.FRONT,-0.34,0.12,z0,0.34,0.86,z0+0.02);
        b.d(Tex.HAZARD,x0+0.36,y0+0.22,z0,x1-0.36,y0+0.36,z0+0.02);
        b.d(Tex.PANEL,-1.05,0.0,z0,-0.5,1.0,z0+0.02);b.d(Tex.PANEL,0.5,0.0,z0,1.05,1.0,z0+0.02);
        // side walls (x) and back wall (z) with windows
        for(int side=-1;side<=1;side+=2){
            double xa=side<0?x0+0.02:x1-t,xb=side<0?x0+t:x1-0.02,mid=1.0;
            b.s(Tex.CASING,xa,y0+0.22,z0+0.36,xb,w0,z1-0.36);b.s(Tex.CASING,xa,w1,z0+0.36,xb,y1-0.25,z1-0.36);
            b.s(Tex.CASING,xa,w0,z0+0.36,xb,w1,mid+wa);b.s(Tex.CASING,xa,w0,mid+wb,xb,w1,z1-0.36);
            b.s(Tex.GLASS,0xC0FFFFFF,xa+0.04,w0,mid+wa,xb-0.04,w1,mid+wb);
            b.pipeZ(side*1.53,1.5,0.06,z0+0.36,z1-0.36,false);
        }
        b.s(Tex.CASING,x0+0.36,y0+0.22,z1-t,x1-0.36,w0,z1-0.02);b.s(Tex.CASING,x0+0.36,w1,z1-t,x1-0.36,y1-0.25,z1-0.02);
        b.s(Tex.CASING,x0+0.36,w0,z1-t,wa,w1,z1-0.02);b.s(Tex.CASING,wb,w0,z1-t,x1-0.36,w1,z1-0.02);
        b.s(Tex.GLASS,0xC0FFFFFF,wa,w0,z1-t+0.04,wb,w1,z1-0.06);
        // inside: fuel rod cage over the coolant pool (the glow of the rods is drawn by the renderer)
        b.d(Tex.CASING_DARK,-0.62,-0.78,0.38,0.62,-0.7,1.62);
        for(double sx:new double[]{-0.6,0.54})for(double sz:new double[]{0.4,1.54})b.d(Tex.CASING_DARK,sx,-0.7,sz,sx+0.06,1.6,sz+0.06);
        b.d(Tex.CASING_DARK,-0.62,1.55,0.38,0.62,1.62,1.62);
        // roof: slab, stepped dome, steam stacks and the coolant pipes down the back
        b.s(Tex.CASING,0xFFD8D8D8,x0,y1-0.25,z0,x1,y1,z1);
        b.d(Tex.CASING,-0.95,y1,0.05,0.95,y1+0.14,1.95);b.d(Tex.CASING,GREY,-0.72,y1+0.14,0.28,0.72,y1+0.28,1.72);b.d(Tex.CASING_DARK,-0.42,y1+0.28,0.58,0.42,y1+0.38,1.42);
        b.d(Tex.HAZARD,-0.955,y1+0.02,0.045,0.955,y1+0.08,1.955);
        b.pipeY(-1.15,0.15,0.16,y1,y1+0.75,false);b.d(Tex.CASING_DARK,-1.35,y1+0.75,-0.05,-0.95,y1+0.82,0.35);
        b.pipeY(1.15,1.85,0.16,y1,y1+0.75,false);b.d(Tex.CASING_DARK,0.95,y1+0.75,1.65,1.35,y1+0.82,2.05);
        b.pipeY(-0.7,z1+0.06,0.06,y0+0.22,y1,false);b.pipeY(0.7,z1+0.06,0.06,y0+0.22,y1,false);
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
            // two corners in world space (exact: the frame axes are world axes)
            double ax=origin.getX()+0.5+right.getStepX()*box.x0()+back.getStepX()*box.z0(),az=origin.getZ()+0.5+right.getStepZ()*box.x0()+back.getStepZ()*box.z0();
            double bx=origin.getX()+0.5+right.getStepX()*box.x1()+back.getStepX()*box.z1(),bz=origin.getZ()+0.5+right.getStepZ()*box.x1()+back.getStepZ()*box.z1();
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
