package techguns.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import java.nio.file.Files;
import techguns.TGuns;
import techguns.Techguns;
import techguns.client.render.item.GunItemRenderer;

/** Opt-in GPU rendering test. Renders only synthetic test frames, never captures the user's desktop or world. */
@Mod.EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class WeaponVisualVerifier {
    private static boolean done;
    @SubscribeEvent public static void verify(ScreenEvent.Render.Post event) {
        if(done || !Boolean.getBoolean("techguns.verifyWeaponVisuals") || !(event.getScreen() instanceof net.minecraft.client.gui.screens.TitleScreen)) return;
        done=true;
        var game=Minecraft.getInstance();
        var target=new TextureTarget(960,540,true,Minecraft.ON_OSX);
        var modelView=RenderSystem.getModelViewStack();
        RenderSystem.backupProjectionMatrix(); modelView.pushPose(); modelView.setIdentity(); RenderSystem.applyModelViewMatrix();
        try {
            Files.createDirectories(game.gameDirectory.toPath().resolve("verification"));
            int weaponCount=0;
            for(var registered : TGuns.guns()) {
            var gun=registered.get();String name=net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(gun).getPath();
            for(int i=0;i<(gun==TGuns.combatshotgun.get()?9:6);i++) {
                boolean third=i>=3&&i<6||i==7; int animation=i<6?i%3:i==8?2:1;
                target.setClearColor(0.12F,0.15F,0.18F,1); target.clear(Minecraft.ON_OSX); target.bindWrite(true);
                RenderSystem.setProjectionMatrix(new Matrix4f().perspective((float)Math.toRadians(70),960F/540,0.05F,100),VertexSorting.DISTANCE_TO_ORIGIN);
                RenderSystem.enableDepthTest(); RenderSystem.setShaderColor(1,1,1,1);
                com.mojang.blaze3d.platform.Lighting.setupFor3DItems();
                var pose=new PoseStack();
                // Vanilla hand origin and ItemRenderer's lower-corner offset before the custom renderer.
                pose.translate(0.56,-0.52,-0.72); pose.translate(-0.5,-0.5,-0.5);
                var buffers=game.renderBuffers().bufferSource();
                GunItemRenderer.get().renderVerificationFrame(gun.getDefaultInstance(),third?net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND:net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                        pose,buffers,animation==1 ? (i>=6?0.8F:0.35F) : 0,animation==2 ? (i==8?0.95F:0.35F) : 0,animation==1&&i<6 ? 0.15F : 0);
                buffers.endBatch();
                try(var pixels=Screenshot.takeScreenshot(target)) {
                    pixels.writeToFile(game.gameDirectory.toPath().resolve("verification/"+name+"-"+i+".png"));
                }
            }
            weaponCount++;
            }
            // Include every camo and upgraded tool head in the actual GUI item pipeline.
            target.setClearColor(0.12F,0.15F,0.18F,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,960,540,0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);
            var itemGraphics=new net.minecraft.client.gui.GuiGraphics(game,game.renderBuffers().bufferSource());itemGraphics.pose().translate(0,0,-11000);
            var itemStacks=new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
            for(var registered:TGuns.guns()) {
                var gun=registered.get();
                for(int camo=0;camo<gun.getCamoCount();camo++){var stack=gun.getDefaultInstance();gun.getCurrentAmmo(stack);stack.getOrCreateTag().putByte("camo",(byte)camo);itemStacks.add(stack);}
                if(gun instanceof techguns.items.guns.MiningGun tool&&tool.tool()!=techguns.items.guns.MiningGun.Tool.SHISHKEBAP)for(int head=1;head<=(tool.tool()==techguns.items.guns.MiningGun.Tool.MININGDRILL?1:2);head++) {var stack=gun.getDefaultInstance();stack.getOrCreateTag().putInt("miningHead",head);itemStacks.add(stack);}
            }
            itemStacks.add(TGuns.stielgranate.get().getDefaultInstance());itemStacks.add(TGuns.fraggrenade.get().getDefaultInstance());
            itemStacks.add(techguns.TGSteelTools.COMBAT_KNIFE.get().getDefaultInstance());itemStacks.add(techguns.TGSteelTools.CROWBAR.get().getDefaultInstance());
            for(int i=0;i<itemStacks.size();i++) {
                var stack=itemStacks.get(i);int x=8+(i%18)*52,y=8+(i/18)*54;
                itemGraphics.pose().pushPose();itemGraphics.pose().translate(x,y,0);itemGraphics.pose().scale(2,2,2);itemGraphics.renderItem(stack,0,0);itemGraphics.pose().popPose();
            }
            itemGraphics.flush();
            try(var pixels=Screenshot.takeScreenshot(target)){pixels.writeToFile(game.gameDirectory.toPath().resolve("verification/nonenergy-inventory.png"));}
            Techguns.LOGGER.info("TECHGUNS_WEAPON_INVENTORY_RENDER_PASSED: {} weapon/camo/head stacks, including nested loaded rocket model",itemStacks.size());
            for(var registered:TGuns.guns()) {
                var gun=registered.get();boolean scope=GunItemRenderer.get().hasScope(gun),charge=gun instanceof techguns.items.guns.GenericGunCharge;
                if(!scope&&!charge)continue;
                target.setClearColor(0.12F,0.15F,0.18F,1);target.clear(Minecraft.ON_OSX);target.bindWrite(true);
                RenderSystem.setProjectionMatrix(new Matrix4f().perspective((float)Math.toRadians(70),960F/540,0.05F,100),VertexSorting.DISTANCE_TO_ORIGIN);
                RenderSystem.enableDepthTest();var pose=new PoseStack();pose.translate(0.56,-0.52,-0.72);pose.translate(-0.5,-0.5,-0.5);
                var buffers=game.renderBuffers().bufferSource();GunItemRenderer.get().renderSpecialVerification(gun.getDefaultInstance(),pose,buffers,scope);buffers.endBatch();
                try(var pixels=Screenshot.takeScreenshot(target)){pixels.writeToFile(game.gameDirectory.toPath().resolve("verification/"+registered.getId().getPath()+(scope?"-scope":"-charge")+".png"));}
            }
            target.setClearColor(0.12F,0.15F,0.18F,1); target.clear(Minecraft.ON_OSX); target.bindWrite(true);
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,960,540,0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);
            var graphics=new net.minecraft.client.gui.GuiGraphics(game,game.renderBuffers().bufferSource());
            graphics.pose().translate(0,0,-11000);
            graphics.blit(techguns.client.gui.TGPlayerInventoryScreen.TEXTURE,20,66,0,0,176,166);
            new techguns.client.gui.TGGuiTabButton(25,40,false,net.minecraft.world.item.Items.CRAFTING_TABLE.getDefaultInstance(),()->{}).render(graphics,0,0,0);
            new techguns.client.gui.TGGuiTabButton(53,40,true,techguns.TGItems.PISTOL_ROUNDS.get().getDefaultInstance(),()->{}).render(graphics,0,0,0);
            graphics.flush();
            try(var pixels=Screenshot.takeScreenshot(target)) {
                pixels.writeToFile(game.gameDirectory.toPath().resolve("verification/inventory-tabs.png"));
            }
            // Small adjacent view changes expose thin surfaces; exercise both hand contexts.
            for(boolean left : new boolean[]{false,true}) for(int frame=0;frame<12;frame++) {
                target.setClearColor(0.12F,0.15F,0.18F,1); target.clear(Minecraft.ON_OSX); target.bindWrite(true);
                RenderSystem.setProjectionMatrix(new Matrix4f().perspective((float)Math.toRadians(70),960F/540,0.05F,100),VertexSorting.DISTANCE_TO_ORIGIN);
                RenderSystem.enableDepthTest(); RenderSystem.setShaderColor(1,1,1,1);
                com.mojang.blaze3d.platform.Lighting.setupFor3DItems();
                var pose=new PoseStack();
                pose.translate(left?-0.36:0.36,-0.28,-0.85);
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees((frame-5.5F)*0.35F));
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees((frame-5.5F)*0.12F));
                pose.translate(-0.5,-0.5,-0.5);
                var buffers=game.renderBuffers().bufferSource();
                GunItemRenderer.get().renderMotionVerificationFrame(TGuns.goldenrevolver.get().getDefaultInstance(),
                        left?net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND:net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,pose,buffers);
                buffers.endBatch();
                try(var pixels=Screenshot.takeScreenshot(target)) {
                    pixels.writeToFile(game.gameDirectory.toPath().resolve("verification/goldenrevolver-motion-"+(left?"left-":"right-")+frame+".png"));
                }
            }
            int error=org.lwjgl.opengl.GL11.glGetError();
            if(error!=0) throw new IllegalStateException("OpenGL error while rendering pistol test frames: "+error);
            Techguns.LOGGER.info("TECHGUNS_WEAPON_VISUAL_RENDER_PASSED: {} weapons, first/third person idle/fire/reload frames saved; visual parity still pending",weaponCount);
            Techguns.LOGGER.info("TECHGUNS_WEAPON_MOTION_RENDER_PASSED: 24 golden revolver frames, two hands; in-world flicker comparison pending");
        } catch(Exception error) { throw new IllegalStateException("Techguns weapon GPU test failed",error); }
        finally {
            target.destroyBuffers(); game.getMainRenderTarget().bindWrite(true);
            modelView.popPose(); RenderSystem.applyModelViewMatrix(); RenderSystem.restoreProjectionMatrix();
            RenderSystem.disableDepthTest();
        }
    }
    private WeaponVisualVerifier() {}
}
