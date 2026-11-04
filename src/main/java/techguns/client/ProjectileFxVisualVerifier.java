package techguns.client;

import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import techguns.*;
import techguns.client.particle.WeaponEffects;

/** Isolated client only: runs actual particle emitters and the particle engine into a private framebuffer. */
public final class ProjectileFxVisualVerifier {
    private record Fixture(String group,double distance,float scale){}
    public static void verify(Minecraft mc){
        var fixtures=List.of(new Fixture("TFGExplosion",25,3.75F),new Fixture("TFGChargeStart",2,1),new Fixture("GuidedMissileExplosion",8,1),new Fixture("RocketExplosion",10,1),new Fixture("NukeExplosion",35,1),new Fixture("Impact_BulletRock_Blue",2,1),new Fixture("Impact_BulletWood_Blue",2,1),new Fixture("Impact_BulletGlass_Blue",2,1));
        var camera=mc.gameRenderer.getMainCamera();var look=new Vector3f(0,0,-1).rotate(camera.rotation());
        var view=RenderSystem.getModelViewStack();var target=new TextureTarget(960,540,true,Minecraft.ON_OSX);RenderSystem.backupProjectionMatrix();view.pushPose();view.setIdentity();view.mulPose(new Quaternionf(camera.rotation()).conjugate());RenderSystem.applyModelViewMatrix();int frames=0;var results=new ArrayList<Map<String,Object>>();
        try{
            var output=mc.gameDirectory.toPath().resolve("verification");Files.createDirectories(output);
            for(var fixture:fixtures){
                mc.particleEngine.setLevel(mc.level);WeaponEffects.reload();
                var position=camera.getPosition().add(new Vec3(look.x,look.y,look.z).scale(fixture.distance));WeaponEffects.spawn(fixture.group,position,0,0,fixture.scale);
                int drawn=0;
                for(int tick=0;tick<=20;tick++){
                    WeaponEffects.tick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));mc.particleEngine.tick();
                    if(tick!=2&&tick!=8&&tick!=16)continue;
                    target.setClearColor(.02F,.025F,.03F,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.disableScissor();RenderSystem.setShaderColor(1,1,1,1);RenderSystem.setProjectionMatrix(new Matrix4f().perspective((float)Math.toRadians(70),960F/540,.05F,200),VertexSorting.DISTANCE_TO_ORIGIN);
                    mc.particleEngine.render(new PoseStack(),mc.renderBuffers().bufferSource(),mc.gameRenderer.lightTexture(),camera,.5F);mc.renderBuffers().bufferSource().endBatch();
                    try(var image=Screenshot.takeScreenshot(target)){
                        int background=image.getPixelRGBA(0,0),changed=0;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if(image.getPixelRGBA(x,y)!=background)changed++;drawn+=changed;
                        image.writeToFile(output.resolve("projectile-fx-"+fixture.group.toLowerCase(Locale.ROOT)+"-"+tick+".png"));results.add(Map.of("group",fixture.group,"tick",tick,"drawnPixels",changed));frames++;
                    }
                }
                if(drawn<5)throw new IllegalStateException("Particle group produced no visible GPU output: "+fixture.group);
            }
            view.setIdentity();RenderSystem.applyModelViewMatrix();
            var blue=TGEntities.ADVANCED_PROJECTILE.get().create(mc.level);var gauss=TGEntities.ENERGY_PROJECTILE.get().create(mc.level);gauss.mode=techguns.entities.projectiles.EnergyProjectile.Mode.GAUSS;var stone=TGEntities.STONE_BULLET.get().create(mc.level);
            for(var entity:new net.minecraft.world.entity.Entity[]{blue,gauss,stone}){
                entity.tickCount=10;entity.setYRot(90);entity.setDeltaMovement(1,0,0);target.setClearColor(.02F,.025F,.03F,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.setProjectionMatrix(new Matrix4f().perspective((float)Math.toRadians(70),960F/540,.05F,200),VertexSorting.DISTANCE_TO_ORIGIN);
                var pose=new PoseStack();pose.translate(0,0,-4);pose.scale(6,6,6);var buffers=mc.renderBuffers().bufferSource();mc.getEntityRenderDispatcher().getRenderer(entity).render(entity,90,.5F,pose,buffers,net.minecraft.client.renderer.LightTexture.FULL_BRIGHT);buffers.endBatch();
                String name=net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).getPath()+(entity==gauss?"-gauss":"");
                try(var image=Screenshot.takeScreenshot(target)){int background=image.getPixelRGBA(0,0),changed=0;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if(image.getPixelRGBA(x,y)!=background)changed++;if(changed<5)throw new IllegalStateException("Projectile renderer produced no GPU output: "+name);image.writeToFile(output.resolve("projectile-model-"+name+".png"));results.add(Map.of("projectile",name,"drawnPixels",changed));frames++;}
            }
            if(org.lwjgl.opengl.GL11.glGetError()!=0)throw new IllegalStateException("OpenGL error during projectile effect verification");
            Files.writeString(output.resolve("projectile-fx-report.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(results));
            Techguns.LOGGER.info("TECHGUNS_PROJECTILE_FX_RENDER_PASSED: {} actual particle-engine/projectile-renderer frames; BFG charge/explosion, rockets/nuke, blue impacts and advanced/Gauss/stone projectiles",frames);
        }catch(Exception error){throw new IllegalStateException("Projectile FX GPU verification failed",error);}
        finally{mc.particleEngine.setLevel(mc.level);WeaponEffects.reload();target.destroyBuffers();mc.getMainRenderTarget().bindWrite(true);view.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.restoreProjectionMatrix();RenderSystem.disableDepthTest();}
    }
    private ProjectileFxVisualVerifier(){}
}
