package techguns.client;

import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.joml.Matrix4f;
import techguns.Techguns;
import techguns.world.ScannedStructure;
import techguns.world.structures.*;
import techguns.world.structures.camp.*;

/** Actual block/BE renderers on source plans; nest is cut away to expose its original underground rooms. */
public final class StructureVisualVerifier {
    public static void verify(Minecraft mc){
        var plans=new LinkedHashMap<String,LegacyStructurePlan>();
        var carrier=new LegacyStructurePlan(RandomSource.create(8128),pos->Blocks.AIR.defaultBlockState());ScannedStructure.load("aircraft_carrier").plan(carrier,BlockPos.ZERO.above(40),0,carrier.random,RandomSource.create(1729),0);plans.put("aircraft-carrier",carrier);
        var nest=new LegacyStructurePlan(RandomSource.create(8128),pos->Blocks.STONE.defaultBlockState());new AlienBugNest(0,94,0,24,0,24,RandomSource.create(1729)).setBlocks(nest);plans.put("alien-nest-cutaway",nest);
        var camp=new CampWorld(RandomSource.create(8128),pos->pos.getY()<=95?Blocks.DIRT.defaultBlockState():Blocks.AIR.defaultBlockState(),(x,z)->96,320,0);var campRandom=RandomSource.create(1729);CampTerrain.removeJunkInArea(camp,-1,-1,50,50);var base=new MilitaryCamp(4,campRandom);base.init(0,96,0,48,48);base.setBlocks(camp,campRandom);plans.put("military-camp",camp);
        var castle=new techguns.world.dungeon.DungeonWorld(RandomSource.create(1729),pos->pos.getY()<=95?Blocks.DIRT.defaultBlockState():Blocks.AIR.defaultBlockState());new techguns.world.dungeon.Dungeon(techguns.world.dungeon.IDungeonPreset.PRESET_CASTLE).generate(castle,0,91,0,40,35,45);plans.put("castle",castle);plans.put("castle-cutaway",castle);
        var target=new TextureTarget(960,720,true,Minecraft.ON_OSX);var view=RenderSystem.getModelViewStack();RenderSystem.backupProjectionMatrix();view.pushPose();view.setIdentity();RenderSystem.applyModelViewMatrix();float fogStart=RenderSystem.getShaderFogStart(),fogEnd=RenderSystem.getShaderFogEnd();var report=new ArrayList<Map<String,Object>>();
        try{
            var output=mc.gameDirectory.toPath().resolve("verification");Files.createDirectories(output);RenderSystem.setShaderFogStart(1000);RenderSystem.setShaderFogEnd(2000);
            for(var fixture:plans.entrySet()){
                String name=fixture.getKey();var plan=fixture.getValue();var bounds=BoundingBox.encapsulatingPositions(plan.blocks().keySet()).orElseThrow();double cx=(bounds.minX()+bounds.maxX()+1)/2.0,cy=(bounds.minY()+bounds.maxY()+1)/2.0,cz=(bounds.minZ()+bounds.maxZ()+1)/2.0;float half=Math.max(bounds.getXSpan(),bounds.getZSpan())*.65f;
                target.setClearColor(.08f,.09f,.11f,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.disableScissor();RenderSystem.enableDepthTest();RenderSystem.setShaderColor(1,1,1,1);com.mojang.blaze3d.platform.Lighting.setupFor3DItems();RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(-half*4/3,half*4/3,-half,half,.1f,600),VertexSorting.DISTANCE_TO_ORIGIN);
                var pose=new PoseStack();pose.translate(0,0,-200);pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(28));pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-45));pose.translate(-cx,-cy,-cz);var buffers=mc.renderBuffers().bufferSource();int rendered=0,entities=0;
                for(var entry:plan.blocks().entrySet()){
                    var pos=entry.getKey();var state=entry.getValue();if(state.isAir()||name.contains("cutaway")&&pos.getZ()>cz)continue;
                    pose.pushPose();pose.translate(pos.getX(),pos.getY(),pos.getZ());
                    if(state.getRenderShape()==RenderShape.ENTITYBLOCK_ANIMATED&&state.getBlock() instanceof EntityBlock entityBlock){var entity=entityBlock.newBlockEntity(pos,state);if(entity!=null){entity.setLevel(mc.level);if(mc.getBlockEntityRenderDispatcher().getRenderer(entity)==null)throw new IllegalStateException("Missing structure block entity renderer: "+state);mc.getBlockEntityRenderDispatcher().renderItem(entity,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);entities++;}}
                    else mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
                    pose.popPose();rendered++;
                }
                buffers.endBatch();if(name.equals("aircraft-carrier")&&entities<1)throw new IllegalStateException("Carrier chest renderer coverage is empty");try(var pixels=Screenshot.takeScreenshot(target)){int bg=pixels.getPixelRGBA(0,0),drawn=0;for(int y=0;y<pixels.getHeight();y++)for(int x=0;x<pixels.getWidth();x++)if(pixels.getPixelRGBA(x,y)!=bg)drawn++;if(drawn<1000)throw new IllegalStateException("Empty source structure render "+name);pixels.writeToFile(output.resolve("structure-"+name+".png"));report.add(Map.of("structure",name,"sourceCells",plan.blocks().size(),"renderedBlocks",rendered,"blockEntityRenderers",entities,"drawnPixels",drawn));}
            }
            if(org.lwjgl.opengl.GL11.glGetError()!=0)throw new IllegalStateException("OpenGL error in source structure rendering");Files.writeString(output.resolve("structure-render-report.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));Techguns.LOGGER.info("TECHGUNS_STRUCTURE_RENDER_PASSED: five source structure views, castle whole/cutaway, military camp, carrier block/chest renderers and underground nest cutaway; full world-side comparison pending");
        }catch(Exception error){throw new IllegalStateException("Structure GPU verification failed",error);}
        finally{RenderSystem.setShaderFogStart(fogStart);RenderSystem.setShaderFogEnd(fogEnd);target.destroyBuffers();mc.getMainRenderTarget().bindWrite(true);view.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.restoreProjectionMatrix();RenderSystem.disableDepthTest();}
    }
    private StructureVisualVerifier(){}
}
