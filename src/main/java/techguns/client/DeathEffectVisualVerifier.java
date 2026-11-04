package techguns.client;

import java.nio.file.Files;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import techguns.deatheffects.DeathEffectEvents.Death;
import techguns.deatheffects.EntityDeathUtils.DeathType;

/** Real living-renderer event path; entities exist only in the isolated QA client. */
public final class DeathEffectVisualVerifier {
    public static void verify(Minecraft mc){
        var target=new TextureTarget(480,420,true,Minecraft.ON_OSX);var view=RenderSystem.getModelViewStack();RenderSystem.backupProjectionMatrix();view.pushPose();view.setIdentity();RenderSystem.applyModelViewMatrix();int id=-3500,frames=0;
        try{
            var output=mc.gameDirectory.toPath().resolve("verification");Files.createDirectories(output);
            var types=new EntityType<?>[]{EntityType.COW,EntityType.ZOMBIE,EntityType.SLIME,techguns.TGNpcs.ZOMBIESOLDIER.get(),techguns.TGNpcs.SUPERMUTANTHEAVY.get(),techguns.TGNpcs.CYBERDEMON.get(),techguns.TGSpecialNpcs.ALIEN_BUG.get(),techguns.TGSpecialNpcs.GHASTLING.get()};
            var report=new java.util.ArrayList<java.util.Map<String,Object>>();
            for(var type:types)for(var death:new DeathType[]{DeathType.LASER,DeathType.BIO,DeathType.GORE}){
                var entity=(LivingEntity)type.create(mc.level);entity.setPos(mc.player.position());entity.setHealth(0);entity.setYRot(0);entity.yBodyRot=0;entity.setYHeadRot(0);mc.level.putNonPlayerEntity(id--,entity);
                var renderer=mc.getEntityRenderDispatcher().getRenderer(entity);
                if(renderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer living){
                    @SuppressWarnings("rawtypes") var model=living.getModel();model.setupAnim(entity,0,0,30,0,0);
                    var meshes=DeathEffects.bodyMeshes(model,new com.mojang.blaze3d.vertex.PoseStack());
                    if(meshes.isEmpty())throw new IllegalStateException("Missing death geometry for "+type);
                    int vertices=meshes.stream().mapToInt(mesh->mesh.vertices.size()).sum();
                    if(vertices<24)throw new IllegalStateException("Empty death geometry for "+type);
                    report.add(java.util.Map.of("entity",type.toShortString(),"death",death.name(),"parts",meshes.size(),"vertices",vertices));
                }
                DeathEffects.receive(new Death(entity.getId(),entity.getUUID(),death.ordinal(),Vec3.ZERO));
                for(int time:new int[]{1,6,12,18}){
                    entity.deathTime=time;target.setClearColor(.12f,.15f,.18f,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,480,420,0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());g.pose().translate(0,0,-11000);
                    InventoryScreen.renderEntityInInventoryFollowsMouse(g,240,350,105,-35,-15,entity);g.flush();try(var pixels=Screenshot.takeScreenshot(target)){pixels.writeToFile(output.resolve("death-"+type.toShortString()+"-"+death.name().toLowerCase(java.util.Locale.ROOT)+"-"+time+".png"));}frames++;
                }
                mc.level.removeEntity(entity.getId(),net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            }
            if(org.lwjgl.opengl.GL11.glGetError()!=0)throw new IllegalStateException("OpenGL error during death effect rendering");
            Files.writeString(output.resolve("death-model-report.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));
            techguns.Techguns.LOGGER.info("TECHGUNS_DEATH_EFFECT_RENDER_PASSED: {} real living-renderer frames; laser/bio/gore on three vanilla and five original NPC model families",frames);
        }catch(Exception error){throw new IllegalStateException("Death-effect GPU verification failed",error);}finally{target.destroyBuffers();mc.getMainRenderTarget().bindWrite(true);view.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.restoreProjectionMatrix();RenderSystem.disableDepthTest();}
    }
    private DeathEffectVisualVerifier(){}
}
