package techguns.client;
import java.util.*;
import java.nio.file.*;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.*;
import org.joml.Matrix4f;
import techguns.*;
import techguns.entities.npcs.*;

/** Real registered renderers in the isolated QA client; no user window input or screenshots. */
public final class NpcVisualVerifier {
    public static void verify(Minecraft mc){
        var entities=new LinkedHashMap<String,LivingEntity>();
        TGNpcs.TYPES.forEach((id,type)->entities.put(id,type.get().create(mc.level)));
        entities.put("alienbug",TGSpecialNpcs.ALIEN_BUG.get().create(mc.level));entities.put("ghastling",TGSpecialNpcs.GHASTLING.get().create(mc.level));entities.put("attackhelicopter",TGSpecialNpcs.HELICOPTER.get().create(mc.level));
        var target=new TextureTarget(960,540,true,Minecraft.ON_OSX);var view=RenderSystem.getModelViewStack();RenderSystem.backupProjectionMatrix();view.pushPose();view.setIdentity();RenderSystem.applyModelViewMatrix();var results=new ArrayList<Map<String,Object>>();
        try{
            var output=mc.gameDirectory.toPath().resolve("verification");Files.createDirectories(output);int frames=0;
            for(var entry:entities.entrySet()){
                String id=entry.getKey();var entity=entry.getValue();
                if(entity instanceof GenericNPC npc)npc.onSpawnByManager(3);
                for(int phase=0;phase<3;phase++){
                    entity.tickCount=30;entity.yBodyRot=entity.yBodyRotO=0;entity.setYRot(0);entity.setYHeadRot(phase==1?20:0);entity.yHeadRotO=entity.getYHeadRot();
                    if(entity instanceof GenericNPC npc){npc.weaponRecoilTicks=phase==1?3:0;npc.weaponMuzzleTicks=phase==1?2:0;}
                    if(entity instanceof AlienBug bug)bug.handleEntityEvent((byte)4);
                    if(entity instanceof Ghastling ghast)ghast.setAttacking(phase==1);
                    if(entity instanceof AttackHelicopter heli)heli.deathTime=phase==2?60:0;
                    var renderer=mc.getEntityRenderDispatcher().getRenderer(entity);var texture=renderer.getTextureLocation(entity);
                    if(mc.getResourceManager().getResource(texture).isEmpty())throw new IllegalStateException("Missing original NPC skin "+texture);
                    var loaded=mc.getTextureManager().getTexture(texture);loaded.getId();if(loaded==net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getTexture())throw new IllegalStateException("Missing NPC skin fallback "+texture);
                    target.setClearColor(.08f,.09f,.11f,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.disableScissor();RenderSystem.enableDepthTest();RenderSystem.setShaderColor(1,1,1,1);
                    RenderSystem.setProjectionMatrix(new Matrix4f().perspective((float)Math.toRadians(45),960f/540,.05f,200),VertexSorting.DISTANCE_TO_ORIGIN);
                    var pose=new PoseStack();boolean heli=entity instanceof AttackHelicopter;pose.translate(0,heli?-1:-1.25,heli?-23:-7);pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(phase==2?140:35));
                    var buffers=mc.renderBuffers().bufferSource();renderer.render(entity,0,.5f,pose,buffers,LightTexture.FULL_BRIGHT);buffers.endBatch();
                    try(var image=Screenshot.takeScreenshot(target)){
                        int bg=image.getPixelRGBA(0,0),pixels=0;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if(image.getPixelRGBA(x,y)!=bg)pixels++;
                        if(pixels<20)throw new IllegalStateException("NPC renderer produced no geometry: "+id+" phase "+phase);
                        image.writeToFile(output.resolve("npc-"+id+"-"+phase+".png"));results.add(Map.of("entity",id,"phase",phase,"drawnPixels",pixels,"texture",texture.toString()));frames++;
                    }
                }
            }
            if(org.lwjgl.opengl.GL11.glGetError()!=0)throw new IllegalStateException("OpenGL error in NPC render verification");
            Files.writeString(output.resolve("npc-render-report.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(results));
            Techguns.LOGGER.info("TECHGUNS_NPC_RENDER_PASSED: {} original NPC types, {} registered-renderer frames with original skins and equipment, including charged ghastling and helicopter crash",entities.size(),frames);
        }catch(Exception error){throw new IllegalStateException("NPC GPU verification failed",error);}
        finally{target.destroyBuffers();mc.getMainRenderTarget().bindWrite(true);view.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.restoreProjectionMatrix();RenderSystem.disableDepthTest();}
    }
    private NpcVisualVerifier(){}
}
