package techguns.client.render.tileentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import techguns.oredrill.OreDrillTileEntMaster;

/**
 * Original RenderOreDrill: the spinning drill head (four tapering cubes per rod block, every second one turned 45°)
 * in steel, obsidian or carbon, or a thin rod without a head, aligned with the drill direction. Spins at 10° per tick
 * while it has a head, redstone permission and power.
 */
public final class OreDrillRenderer implements BlockEntityRenderer<OreDrillTileEntMaster> {
    private static final int DRILLERCUBESPERBLOCK=4;
    private static final ResourceLocation[] HEADS={
        new ResourceLocation("techguns","textures/blocks/drillhead.png"),
        new ResourceLocation("techguns","textures/blocks/drillhead_obsidian.png"),
        new ResourceLocation("techguns","textures/blocks/drillhead_carbon.png")};
    public OreDrillRenderer(BlockEntityRendererProvider.Context context){}

    public static int[] offset(Direction dir,int distance){int[] pos=new int[3];int axis=dir.getAxis().ordinal();pos[axis]+=dir.getAxisDirection()==Direction.AxisDirection.POSITIVE?distance:-distance;return pos;}

    @Override public void render(OreDrillTileEntMaster tile,float partialTick,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!tile.isFormed()||tile.getDrill_direction()==null)return;
        Direction direction=tile.getDrill_direction();
        int[] vec=offset(direction,tile.getEngines()+1),dir=offset(direction,1);
        pose.pushPose();
        pose.translate(0.5f-vec[0],1.25f-vec[1],0.5f-vec[2]);
        pose.pushPose();
        int rot=(int)(tile.getLevel().getGameTime()%360);
        if(dir[1]<0)pose.mulPose(Vector3f.ZP.rotationDegrees(180));
        else if(dir[1]>0)pose.translate(0,-1.5f,0);
        else if(dir[0]!=0){pose.mulPose(Vector3f.ZP.rotationDegrees(-90f*dir[0]));pose.translate(dir[0]*0.75f,0,0);pose.translate(0,-0.75f,0);}
        else if(dir[2]!=0){pose.mulPose(Vector3f.XP.rotationDegrees(90f*dir[2]));pose.translate(0,0,dir[2]*0.75f);pose.translate(0,-0.75f,0);}
        pose.translate(0,2*(tile.getEngines()+1),0);
        if(tile.hasDrill()&&tile.isRedstoneEnabled()&&tile.hasPower())pose.mulPose(Vector3f.YP.rotationDegrees(rot*10f+((1f/36f)*partialTick)));
        if(tile.hasDrill()){
            int type=tile.getDrillType();
            if(type>=1&&type<=3)drawDriller(tile,buffers.getBuffer(RenderType.entityCutoutNoCull(HEADS[type-1])),pose,light);
        }else drawDrillRod(tile,buffers.getBuffer(RenderType.entityCutoutNoCull(HEADS[0])),pose,light);
        pose.popPose();
        pose.popPose();
    }

    private static void drawDriller(OreDrillTileEntMaster tile,VertexConsumer vertices,PoseStack pose,int light){
        int length=tile.getRods();
        pose.pushPose();
        float height=1.0f/DRILLERCUBESPERBLOCK,pos=height;
        float max=(float)Math.log(length*DRILLERCUBESPERBLOCK+1.0f);
        float radiusfactor=switch(tile.getDrillRadius()){case 0,1->0.35f;case 2->0.5f;case 3->0.55f;case 4->0.60f;default->1.0f;};
        for(int i=0;i<length*DRILLERCUBESPERBLOCK;i++){
            boolean rotate=i%2==1;
            float factor=(float)Math.log((length*DRILLERCUBESPERBLOCK)-i+1.0f);
            float size=((tile.getDrillRadius()==0?1.0f:tile.getDrillRadius()*1.0f)*factor)*(1.0f/max)*radiusfactor;
            pose.translate(0,height,0);
            if(rotate){pose.pushPose();pose.mulPose(Vector3f.YP.rotationDegrees(45));}
            drawCube(vertices,pose,size,pos,size,light);
            if(rotate)pose.popPose();
        }
        pose.popPose();
    }
    private static void drawDrillRod(OreDrillTileEntMaster tile,VertexConsumer vertices,PoseStack pose,int light){
        int length=tile.getRods();
        pose.pushPose();
        float height=0.5f,pos=height,f=-0.4f;
        pose.translate(0,height*f,0);
        for(int i=0;i<length;i++){
            float size=0.2f;
            pose.translate(0,height*2,0);
            pose.pushPose();pose.mulPose(Vector3f.YP.rotationDegrees(45));
            drawCube(vertices,pose,size,pos,size,light);
            pose.popPose();
        }
        pose.popPose();
    }
    /** Original OreDrillCube: six full-texture quads of half-size (x,y,z) around the origin. */
    private static void drawCube(VertexConsumer b,PoseStack pose,float x,float y,float z,int light){
        Matrix4f m=pose.last().pose();Matrix3f n=pose.last().normal();
        quad(b,m,n,light,0,1,0, -x,y,-z,0,1, -x,y,z,0,0, x,y,z,1,0, x,y,-z,1,1);
        quad(b,m,n,light,0,-1,0, x,-y,-z,1,0, x,-y,z,1,1, -x,-y,z,0,1, -x,-y,-z,0,0);
        quad(b,m,n,light,1,0,0, -x,-y,-z,0,1, -x,y,-z,0,0, x,y,-z,1,0, x,-y,-z,1,1);
        quad(b,m,n,light,-1,0,0, x,-y,z,1,0, x,y,z,1,1, -x,y,z,0,1, -x,-y,z,0,0);
        quad(b,m,n,light,0,0,-1, x,-y,-z,0,0, x,y,-z,0,1, x,y,z,1,1, x,-y,z,1,0);
        quad(b,m,n,light,0,0,1, -x,-y,z,1,0, -x,y,z,1,1, -x,y,-z,0,1, -x,-y,-z,0,0);
    }
    private static void quad(VertexConsumer b,Matrix4f m,Matrix3f n,int light,float nx,float ny,float nz,float... v){
        for(int i=0;i<4;i++){int o=i*5;b.vertex(m,v[o],v[o+1],v[o+2]).color(255,255,255,255).uv(v[o+3],v[o+4]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n,nx,ny,nz).endVertex();}
    }
    @Override public boolean shouldRenderOffScreen(OreDrillTileEntMaster tile){return tile.isFormed();}
    @Override public int getViewDistance(){return 128;}
}
