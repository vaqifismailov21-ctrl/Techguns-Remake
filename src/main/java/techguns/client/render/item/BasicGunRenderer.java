package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;
import techguns.TGuns;
import techguns.capabilities.TGExtendedPlayer;
import techguns.client.models.ModelMultipart;
import techguns.client.models.guns.*;
import techguns.client.render.fx.PistolMuzzleFlash;
import techguns.items.guns.GenericGun;
import java.util.Map;

/** Original RenderGunBase/RenderGunBase90 settings for ballistic weapons. */
public final class BasicGunRenderer extends RenderItemBase {
    private final boolean rotation90;
    private final PistolMuzzleFlash.Specification flash;
    private final float recoilTranslation,recoilAngle;
    private ItemStack stack;
    private float fire,reload,muzzle;
    private boolean preview;
    private boolean scopeVerification;
    private int parts=1;
    private GunAnimation reloadAnimation=GunAnimation.genericReload,reloadThirdAnimation=GunAnimation.genericReload;
    private GunAnimation recoilAnimation=GunAnimation.genericRecoil,scopeAnimation=GunAnimation.scopeRecoil;
    private float reloadThirdTranslation=0,reloadThirdAngle=40,scopeScale=3,scopeTranslation=0,scopeAngle=0;
    private ResourceLocation scopeTexture;
    private boolean scopeFlipY;
    private boolean obj;
    private float objRotation=90;
    private float charge,chargeTranslation=0.25F;
    BasicGunRenderer firstScale(float value) {firstPersonScale=value;return this;}
    BasicGunRenderer groundAndFrame(float value) {groundScale=frameScale=value;return this;}
    BasicGunRenderer chargeTranslation(float value) {chargeTranslation=value;return this;}
    BasicGunRenderer objRotation() {obj=true;return this;}
    BasicGunRenderer objRotation(float angle) {obj=true;objRotation=angle;return this;}
    private float reloadTranslation=1,reloadAngle=40;
    BasicGunRenderer(ModelMultipart model,String texture,boolean rotation90,float scale,float gui,float[] base,float[][] translations,
                             float recoilTranslation,float recoilAngle,PistolMuzzleFlash.Specification flash) {
        super(model,new ResourceLocation("techguns","textures/guns/"+texture+".png"));
        this.rotation90=rotation90; baseScale=scale;guiScale=gui;groundScale=0.35F; baseTranslation=base;this.translations=translations;
        this.recoilTranslation=recoilTranslation;this.recoilAngle=recoilAngle;this.flash=flash;
    }
    private static PistolMuzzleFlash.Specification flash(boolean rifle,float y,float z,float scale,float jx,float jy,float js,float y3,float z3) {
        return new PistolMuzzleFlash.Specification(rifle?PistolMuzzleFlash.RIFLE_TEXTURE:PistolMuzzleFlash.TEXTURE,rifle?12:11,0,0,y,z,scale,jx,jy,js,y3,z3);
    }
    BasicGunRenderer parts(int value) { parts=value;return this; }
    BasicGunRenderer recoil(GunAnimation animation) {recoilAnimation=animation;return this;}
    BasicGunRenderer reload(GunAnimation animation,float translation,float angle) { reloadAnimation=animation;reloadTranslation=translation;reloadAngle=angle;return this; }
    BasicGunRenderer reloadThird(GunAnimation animation,float translation,float angle) {reloadThirdAnimation=animation;reloadThirdTranslation=translation;reloadThirdAngle=angle;return this;}
    BasicGunRenderer scope(String texture,float scale,boolean flipY) {scopeTexture=new ResourceLocation("techguns","textures/fx/"+texture+".png");scopeScale=scale;scopeFlipY=flipY;return this;}
    BasicGunRenderer scopeRecoil(GunAnimation animation,float translation,float angle) {scopeAnimation=animation;scopeTranslation=translation;scopeAngle=angle;return this;}
    public boolean hasScope() {return scopeTexture!=null;}
    public void verifyResources(net.minecraft.server.packs.resources.ResourceManager resources,GenericGun gun) {
        var paths=new java.util.ArrayList<ResourceLocation>();paths.add(texture);
        for(int i=1;i<gun.getCamoCount();i++)paths.add(new ResourceLocation(texture.getNamespace(),texture.getPath().replace(".png","_"+i+".png")));
        if(flash!=null)paths.add(flash.texture());if(scopeTexture!=null)paths.add(scopeTexture);
        for(var path:paths)if(resources.getResource(path).isEmpty())throw new IllegalStateException("Missing original weapon texture "+path);
    }
    public static Map<GenericGun,BasicGunRenderer> create() {
        return GeneratedBallisticRenderers.create();
    }
    public void renderWeapon(ItemStack stack,ItemTransforms.TransformType context,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
        this.stack=stack;
        if(context.firstPerson() && hasScope() && (scopeVerification||techguns.client.ClientZoom.isZoomed(Minecraft.getInstance().player,(GenericGun)stack.getItem()))) {
            pose.pushPose();pose.translate(0.5,0.5,0.5);applyAnimation(pose,context);pose.popPose();
            pose.pushPose();pose.translate(0.5,0.5,0.5);
            boolean left=context==ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
            if(fire>0) scopeAnimation.play(pose,fire,left,scopeTranslation,scopeAngle);
            float x=left?0.56F:-0.56F,y=0.52F,h=scopeScale/2;
            var vertices=buffers.getBuffer(techguns.client.render.fx.LegacyFxRenderTypes.sprite(scopeTexture,false,true));
            var matrix=pose.last().pose();float top=scopeFlipY?0:1,bottom=1-top;
            vertices.vertex(matrix,x-h,y+h,0).color(1F,1F,1F,1F).uv(0,top).endVertex();
            vertices.vertex(matrix,x-h,y-h,0).color(1F,1F,1F,1F).uv(0,bottom).endVertex();
            vertices.vertex(matrix,x+h,y-h,0).color(1F,1F,1F,1F).uv(1,bottom).endVertex();
            vertices.vertex(matrix,x+h,y+h,0).color(1F,1F,1F,1F).uv(1,top).endVertex();
            pose.popPose();return;
        }
        render(context,pose,buffers,light,overlay);
        boolean third=context==ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND || context==ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
        if((context.firstPerson()||third)&&stack.getItem() instanceof techguns.items.guns.MiningGun tool&&tool.hasAmbient()) {
            var owner=context.firstPerson()?Minecraft.getInstance().player:GunRenderContext.holder();
            if(owner!=null) {
                boolean left=context==ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND||context==ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
                boolean offhand=left==(owner.getMainArm()==net.minecraft.world.entity.HumanoidArm.RIGHT);
                pose.pushPose();pose.translate(0.5,0.5,0.5);
                if(reload>0)(context.firstPerson()?reloadAnimation:reloadThirdAnimation).play(pose,reload,left,context.firstPerson()?reloadTranslation:reloadThirdTranslation,context.firstPerson()?reloadAngle:reloadThirdAngle);
                techguns.client.particle.ItemWeaponEffects.render(owner,offhand?net.minecraft.world.InteractionHand.OFF_HAND:net.minecraft.world.InteractionHand.MAIN_HAND,pose,buffers,Minecraft.getInstance().getFrameTime(),third);
                pose.popPose();
            }
        }
        if((context.firstPerson()||third)&&muzzle>0) {
            pose.pushPose();pose.translate(0.5,0.5,0.5);
            PistolMuzzleFlash.render(flash,pose,buffers,muzzle,context==ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND||context==ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND,third);
            pose.popPose();
        }
        else if((context.firstPerson()||third)&&reload<=0&&stack.is(TGuns.flamethrower.get())&&Minecraft.getInstance().player!=null&&Minecraft.getInstance().player.isFree(0,1.5,0)) {
            pose.pushPose();pose.translate(0.5,0.5,0.5);
            var pilot=new PistolMuzzleFlash.Specification(new ResourceLocation("techguns","textures/fx/flamethrower2.png"),16,flash.rightX(),flash.leftX(),flash.y(),flash.z(),flash.scale()*0.5F,0,0,0,flash.thirdY(),flash.thirdZ(),4,4,false,true);
            PistolMuzzleFlash.render(pilot,pose,buffers,(Minecraft.getInstance().level.getGameTime()%30)/30F,context==ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND||context==ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND,third);pose.popPose();
        }
    }
    public void renderVerification(ItemStack stack,PoseStack pose,MultiBufferSource buffers,float fire,float reload,float muzzle) {
        renderVerification(stack,ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND,pose,buffers,fire,reload,muzzle);
    }
    public void renderVerification(ItemStack stack,ItemTransforms.TransformType context,PoseStack pose,MultiBufferSource buffers,float fire,float reload,float muzzle) {
        preview=true;this.fire=fire;this.reload=reload;this.muzzle=muzzle;this.charge=0;
        try { renderWeapon(stack,context,pose,buffers,15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY); }
        finally { preview=false; }
    }
    public void renderScopeVerification(ItemStack stack,PoseStack pose,MultiBufferSource buffers) {
        scopeVerification=true;try{renderVerification(stack,pose,buffers,0.35F,0,0);}finally{scopeVerification=false;}
    }
    public void renderChargeVerification(ItemStack stack,PoseStack pose,MultiBufferSource buffers) {
        preview=true;fire=reload=muzzle=0;charge=1;
        try{renderWeapon(stack,ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND,pose,buffers,15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);}
        finally{preview=false;charge=0;}
    }
    @Override protected void setBaseRotation(PoseStack pose,ItemTransforms.TransformType context) {
        if(obj) {pose.mulPose(Vector3f.YP.rotationDegrees(objRotation));return;}
        pose.mulPose(Vector3f.XP.rotationDegrees(-180));pose.mulPose(Vector3f.YP.rotationDegrees(rotation90?-90:180));
    }
    @Override protected void applyAnimation(PoseStack pose,ItemTransforms.TransformType context) {
        boolean first=context.firstPerson(),third=context==ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND||context==ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
        boolean left=context==ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND||context==ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
        var entity=first?Minecraft.getInstance().player:third?GunRenderContext.holder():null;
        // Original RenderGunBase turns the weapon into the hand for non-aiming NPC models (CyberDemon).
        if(third&&entity instanceof techguns.api.npc.INPCTechgunsShooter shooter&&!shooter.hasWeaponArmPose())pose.mulPose(Vector3f.XP.rotationDegrees(90));
        if(!preview) {
            fire=reload=muzzle=charge=0;
            if(entity instanceof techguns.entities.npcs.GenericNPC npc){
                var gun=(GenericGun)stack.getItem();float partial=Minecraft.getInstance().getFrameTime();
                if(npc.weaponRecoilTicks>0&&gun.recoiltime>0)fire=Math.max(.001f,Math.min(1,1-(npc.weaponRecoilTicks-partial)/gun.recoiltime));
                if(npc.weaponMuzzleTicks>0&&gun.getMuzzleFlashTicks()>0)muzzle=Math.max(.001f,Math.min(1,1-(npc.weaponMuzzleTicks-partial)/gun.getMuzzleFlashTicks()));
            }else if(entity instanceof Player player){
            boolean offhand=left==(player.getMainArm()==net.minecraft.world.entity.HumanoidArm.RIGHT);
            var state=TGExtendedPlayer.find(player).orElse(null);
            if(state==null) return;
            var attack=state.getAttackTime(offhand); long now=System.currentTimeMillis();
            if(player.getUseItem()==stack&&stack.getItem() instanceof techguns.items.guns.GenericGunCharge charged)charge=Math.min(1,player.getTicksUsingItem()/charged.fullChargeTime);
            if(attack.getReloadTime()>now&&attack.getReloadTimeTotal()>0) reload=1-(attack.getReloadTime()-now)/(float)attack.getReloadTimeTotal();
            else if(attack.getRecoilTime()>now&&attack.getRecoilTimeTotal()>0) fire=1-(attack.getRecoilTime()-now)/(float)attack.getRecoilTimeTotal();
            if(charge==0&&fire>0&&stack.getItem() instanceof techguns.items.guns.GenericGunCharge)charge=(1-fire)*attack.getRecoilChargeProgress();
            long remaining=attack.getMuzzleFlashTime()-now;
            if(remaining>0&&remaining<=attack.getMuzzleFlashTimeTotal()) muzzle=1-remaining/(float)attack.getMuzzleFlashTimeTotal();
            if(first&&fire==0&&reload==0&&offhand&&player.isShiftKeyDown()) pose.mulPose(Vector3f.XP.rotationDegrees(-35));
            }else return;
        }
        if(first&&charge>0)pose.translate(0,0,chargeTranslation*charge);
        if(reload>0) (first?reloadAnimation:reloadThirdAnimation).play(pose,reload,left,first?reloadTranslation:reloadThirdTranslation,first?reloadAngle:reloadThirdAngle);
        else if(fire>0) recoilAnimation.play(pose,fire,left,first?recoilTranslation:0,first?recoilAngle:5);
    }
    @Override protected void renderContents(ItemTransforms.TransformType context,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
        var gun=(GenericGun)stack.getItem();var activeTexture=texture;
        int camo=gun.getCurrentCamoIndex(stack);
        if(camo>0 && camo<gun.getCamoCount()) activeTexture=new ResourceLocation(texture.getNamespace(),texture.getPath().replace(".png","_"+camo+".png"));
        // Legacy model rendering culled back faces; zero-thickness boxes otherwise fight themselves.
        for(int part=0;part<parts;part++) {
            var partTexture=activeTexture;
            if(gun==TGuns.rocketlauncher.get()&&part==1) {
                String variant=gun.getCurrentAmmoVariantKey(stack);
                partTexture=new ResourceLocation("techguns","textures/guns/"+(variant.equals("nuke")?"rocket_nuke":variant.equals("high_velocity")?"rocket_hv":"rocket")+".png");
            }
            if(part==1&&gun instanceof techguns.items.guns.MiningGun tool) {
                int head=tool.getMiningHeadLevel(stack);
                if(head>0&&tool.tool()==techguns.items.guns.MiningGun.Tool.POWERHAMMER)partTexture=new ResourceLocation("techguns","textures/guns/powerhammer_"+(head==1?"obsidian":"carbon")+".png");
                if(head>0&&tool.tool()==techguns.items.guns.MiningGun.Tool.MININGDRILL)partTexture=new ResourceLocation("techguns","textures/guns/miningdrill_carbon.png");
            }
            com.mojang.blaze3d.vertex.VertexConsumer vertices=buffers.getBuffer(RenderType.entityCutout(partTexture));
            var layerTexture=partTexture;
            model.setLayers((alpha,twoSided)->buffers.getBuffer(alpha?RenderType.entityTranslucent(layerTexture):twoSided?RenderType.entityCutoutNoCull(layerTexture):RenderType.entityCutout(layerTexture)));
            if(part==1&&gun instanceof techguns.items.guns.MiningGun tool&&tool.tool()==techguns.items.guns.MiningGun.Tool.CHAINSAW) {
                int head=tool.getMiningHeadLevel(stack);
                if(head==1)vertices=new techguns.client.models.TintedVertexConsumer(vertices,0.9F,0.55F,1);
                else if(head==2)vertices=new techguns.client.models.TintedVertexConsumer(vertices,0.4F,0.4F,0.4F);
            }
            model.renderModel(pose,vertices,light,overlay,context,context.firstPerson()?Minecraft.getInstance().player:GunRenderContext.holder(),gun.getCurrentAmmo(stack),reload,part,fire,charge);
        }
    }
}
