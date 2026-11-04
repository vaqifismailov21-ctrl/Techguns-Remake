package techguns.client;

import java.nio.file.Files;
import java.util.*;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import techguns.Techguns;
import techguns.entities.npcs.NPCTurret;
import techguns.oredrill.OreDrillBlock;
import techguns.oredrill.OreDrillTileEntMaster;
import techguns.tileentities.TurretTileEnt;

/** GPU frames of the ported turret (base renderer + entity renderer with its gun) and formed ore drills from the QA world. */
public final class MachineVisualVerifier {
    private record Scene(String name,BlockPos min,BlockPos max,List<BlockPos> blockEntities,List<net.minecraft.world.entity.Entity> entities){}
    public static void verify(Minecraft mc){
        var level=mc.level;var scenes=new ArrayList<Scene>();
        for(var be:blockEntitiesNear(mc,new BlockPos(0,60,0),new BlockPos(40,110,30))){
            if(be instanceof TurretTileEnt turret){
                var turrets=level.getEntitiesOfClass(NPCTurret.class,new AABB(be.getBlockPos()).inflate(2));
                if(turrets.isEmpty())throw new IllegalStateException("Turret entity was not synchronised to the client");
                var entity=turrets.get(0);entity.setYHeadRot(35);entity.yHeadRotO=35;entity.setXRot(-12);entity.xRotO=-12;
                if(turret.getWeapon().isEmpty())throw new IllegalStateException("Turret gun was not synchronised to the client");
                scenes.add(new Scene("turret",be.getBlockPos().offset(-1,-1,-1),be.getBlockPos().offset(1,2,1),List.of(be.getBlockPos()),List.of(entity)));
            }else if(be instanceof OreDrillTileEntMaster drill&&drill.isFormed()){
                int r=drill.getRadius()+2,len=drill.getTotalDrillLength()+1;var dir=drill.getDrill_direction();
                var a=be.getBlockPos().offset(-r,-r,-r);var b=be.getBlockPos().relative(dir,len).offset(r,r,r);
                var min=new BlockPos(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()));var max=new BlockPos(Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ()));
                scenes.add(new Scene("oredrill-r"+drill.getDrillRadius()+"-l"+drill.getTotalDrillLength(),min,max,List.of(be.getBlockPos()),List.of()));
            }
        }
        if(scenes.stream().noneMatch(s->s.name.equals("turret"))||scenes.stream().filter(s->s.name.startsWith("oredrill")).count()<2)throw new IllegalStateException("Missing turret/ore drill QA scenes: "+scenes.stream().map(Scene::name).toList());
        var target=new TextureTarget(960,720,true,Minecraft.ON_OSX);var view=RenderSystem.getModelViewStack();RenderSystem.backupProjectionMatrix();view.pushPose();view.setIdentity();RenderSystem.applyModelViewMatrix();var report=new ArrayList<Map<String,Object>>();
        try{
            var output=mc.gameDirectory.toPath().resolve("verification");Files.createDirectories(output);
            for(var scene:scenes){
                double cx=(scene.min.getX()+scene.max.getX()+1)/2.0,cy=(scene.min.getY()+scene.max.getY()+1)/2.0,cz=(scene.min.getZ()+scene.max.getZ()+1)/2.0;
                float half=Math.max(Math.max(scene.max.getX()-scene.min.getX(),scene.max.getZ()-scene.min.getZ()),scene.max.getY()-scene.min.getY())*.62f+1;
                target.setClearColor(.08f,.09f,.11f,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.enableDepthTest();RenderSystem.setShaderColor(1,1,1,1);com.mojang.blaze3d.platform.Lighting.setupFor3DItems();
                RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(-half*4/3,half*4/3,-half,half,.1f,600),VertexSorting.DISTANCE_TO_ORIGIN);
                var pose=new PoseStack();pose.translate(0,0,-200);pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(28));pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-45));pose.translate(-cx,-cy,-cz);
                var buffers=mc.renderBuffers().bufferSource();int blocks=0,renderers=0,entities=0;
                for(var pos:BlockPos.betweenClosed(scene.min,scene.max)){
                    var state=level.getBlockState(pos);if(state.isAir()||state.getRenderShape()!=RenderShape.MODEL)continue;
                    if(!(state.getBlock() instanceof OreDrillBlock)&&!state.is(techguns.TGWorld.CLUSTERS.get(techguns.world.ClusterKind.COAL).get())&&!state.is(techguns.TGWorld.CLUSTERS.get(techguns.world.ClusterKind.COMMON_METAL).get()))continue;
                    pose.pushPose();pose.translate(pos.getX(),pos.getY(),pos.getZ());mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();blocks++;
                }
                for(var pos:scene.blockEntities){var be=level.getBlockEntity(pos);var renderer=mc.getBlockEntityRenderDispatcher().getRenderer(be);if(renderer==null)throw new IllegalStateException("Missing renderer for "+be);pose.pushPose();pose.translate(pos.getX(),pos.getY(),pos.getZ());renderer.render(be,0,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();renderers++;}
                for(var entity:scene.entities){mc.getEntityRenderDispatcher().render(entity,entity.getX(),entity.getY(),entity.getZ(),entity.getYRot(),0,pose,buffers,LightTexture.FULL_BRIGHT);entities++;}
                buffers.endBatch();
                try(var pixels=Screenshot.takeScreenshot(target)){
                    int bg=pixels.getPixelRGBA(0,0),drawn=0;for(int y=0;y<pixels.getHeight();y++)for(int x=0;x<pixels.getWidth();x++)if(pixels.getPixelRGBA(x,y)!=bg)drawn++;
                    if(drawn<500)throw new IllegalStateException("Blank machine frame "+scene.name);
                    pixels.writeToFile(output.resolve("machine-"+scene.name+".png"));
                    var row=new LinkedHashMap<String,Object>();row.put("scene",scene.name);row.put("modelBlocks",blocks);row.put("blockEntityRenderers",renderers);row.put("entities",entities);row.put("drawnPixels",drawn);report.add(row);
                }
            }
            if(org.lwjgl.opengl.GL11.glGetError()!=0)throw new IllegalStateException("OpenGL error in machine rendering");
            Files.writeString(output.resolve("machine-render-report.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));
            Techguns.LOGGER.info("TECHGUNS_MACHINE_RENDER_PASSED: {} scenes; turret base/entity/gun and formed ore drills with block models and drill renderer",scenes.size());
        }catch(Exception error){throw new IllegalStateException("Machine GPU verification failed",error);}
        finally{target.destroyBuffers();mc.getMainRenderTarget().bindWrite(true);view.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.restoreProjectionMatrix();RenderSystem.disableDepthTest();}
    }
    private static List<net.minecraft.world.level.block.entity.BlockEntity> blockEntitiesNear(Minecraft mc,BlockPos min,BlockPos max){
        var list=new ArrayList<net.minecraft.world.level.block.entity.BlockEntity>();
        for(int cx=min.getX()>>4;cx<=max.getX()>>4;cx++)for(int cz=min.getZ()>>4;cz<=max.getZ()>>4;cz++){var chunk=mc.level.getChunk(cx,cz);for(var be:chunk.getBlockEntities().values())if(be.getBlockPos().getY()>=min.getY()&&be.getBlockPos().getY()<=max.getY())list.add(be);}
        return list;
    }
    private MachineVisualVerifier(){}
}
