package techguns.client;

import java.util.*;
import java.lang.reflect.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.TGConfig;
import techguns.TGSounds;
import techguns.client.models.MeshSnapshot;
import techguns.client.particle.WeaponEffects;
import techguns.deatheffects.DeathEffectEvents.Death;
import techguns.deatheffects.EntityDeathUtils.DeathType;

/** Source death timing, texture overlays, sounds, particle systems and flying body geometry. */
@Mod.EventBusSubscriber(modid="techguns",value=Dist.CLIENT)
public final class DeathEffects {
    private record State(LivingEntity entity,DeathType type){}
    private static final Map<UUID,State> active=new HashMap<>();
    public static void receive(Death message){
        var mc=Minecraft.getInstance();if(mc.level==null||!TGConfig.cl_enableDeathFX||message.type()>=DeathType.values().length)return;
        var type=DeathType.values()[message.type()];if(type==DeathType.DEFAULT||type==DeathType.DISMEMBER||type==DeathType.GORE&&!TGConfig.cl_enableDeathFX_Gore)return;
        if(!(mc.level.getEntity(message.entityId()) instanceof LivingEntity entity)||!entity.getUUID().equals(message.uuid())||active.containsKey(entity.getUUID()))return;
        active.put(entity.getUUID(),new State(entity,type));
        var position=entity.position().add(0,entity.getBbHeight()/2,0);var motion=message.motion();
        var sound=type==DeathType.LASER?TGSounds.DEATH_LASER:type==DeathType.BIO?TGSounds.DEATH_BIO:TGSounds.DEATH_GORE;
        mc.level.playLocalSound(position.x,position.y,position.z,sound.get(),net.minecraft.sounds.SoundSource.HOSTILE,1,1,false);
        if(type==DeathType.LASER){WeaponEffects.spawnLegacy("laserdeathFire",mc.level,position.x,position.y,position.z,motion.x,0,motion.z);WeaponEffects.spawnLegacy("laserdeathAsh",mc.level,position.x,position.y,position.z,motion.x,0,motion.z);}
        else if(type==DeathType.BIO)WeaponEffects.spawnLegacy("biodeath",mc.level,position.x,position.y,position.z,motion.x,motion.y,motion.z);
        else gore(entity,motion);
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;var level=Minecraft.getInstance().level;
        active.values().removeIf(s->s.entity.level()!=level||s.entity.isRemoved()||s.entity.deathTime>=20||s.entity.isAlive()&&s.entity.tickCount>0&&s.entity.hurtTime==0);
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    @SubscribeEvent(priority=EventPriority.HIGH) public static void render(RenderLivingEvent.Pre event){
        var entity=event.getEntity();var state=active.get(entity.getUUID());if(state==null||!TGConfig.cl_enableDeathFX)return;
        if(state.type==DeathType.GORE){if(TGConfig.cl_enableDeathFX_Gore)event.setCanceled(true);return;}
        event.setCanceled(true);var renderer=event.getRenderer();EntityModel model=renderer.getModel();float partial=event.getPartialTick();
        float progress=Mth.clamp((entity.deathTime+partial)/20F,0,1),color=1-progress*progress,alpha=(1-progress)*(1-progress);
        var pose=event.getPoseStack();pose.pushPose();
        try{
            pose.mulPose(Axis.YP.rotationDegrees(180-Mth.rotLerp(partial,entity.yBodyRotO,entity.yBodyRot)));pose.scale(-1,-1,1);
            if(entity instanceof net.minecraft.world.entity.monster.Slime slime)pose.scale(slime.getSize(),slime.getSize(),slime.getSize());
            pose.translate(0,-1.501,0);model.young=entity.isBaby();model.riding=false;model.attackTime=0;model.prepareMobModel(entity,0,0,partial);model.setupAnim(entity,0,0,entity.tickCount+partial,entity.getYHeadRot()-entity.yBodyRot,entity.getXRot());
            var texture=renderer.getTextureLocation(entity);var buffers=event.getMultiBufferSource();
            float glow=(float)(.5+Math.sin((Math.sqrt(progress)+.75)*2*Math.PI)/2);
            var overlay=ResourceLocation.fromNamespaceAndPath("techguns",state.type==DeathType.LASER?"textures/fx/laserdeath.png":"textures/fx/bio.png");
            if(state.type==DeathType.BIO){
                var random=new Random(entity.getId());var parts=bodyMeshes(model,new PoseStack());
                if(parts.isEmpty()){
                    model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityTranslucent(texture)),event.getPackedLight(),OverlayTexture.NO_OVERLAY,color,1,color,alpha);
                    model.renderToBuffer(pose,buffers.getBuffer(RenderType.eyes(overlay)),0xf000f0,OverlayTexture.NO_OVERLAY,glow,glow,glow,1);
                }
                for(var part:parts){pose.pushPose();float scale=1+random.nextFloat()*progress;pose.scale(scale,scale,scale);part.render(pose,buffers.getBuffer(RenderType.entityTranslucent(texture)),event.getPackedLight(),OverlayTexture.NO_OVERLAY,color,1,color,alpha);part.render(pose,buffers.getBuffer(RenderType.eyes(overlay)),0xf000f0,OverlayTexture.NO_OVERLAY,glow,glow,glow,1);pose.popPose();}
            }else{
                model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityTranslucent(texture)),event.getPackedLight(),OverlayTexture.NO_OVERLAY,1,color,color,alpha);
                model.renderToBuffer(pose,buffers.getBuffer(RenderType.eyes(overlay)),0xf000f0,OverlayTexture.NO_OVERLAY,glow,glow,glow,1);
            }
        }finally{pose.popPose();}
    }
    /** Field types survive production obfuscation; no mappings-dependent private field names. */
    static List<MeshSnapshot> bodyMeshes(EntityModel<?> model,PoseStack pose){
        if(model instanceof techguns.client.models.LegacyDeathModel legacy)return legacy.deathParts(pose);
        var meshes=new ArrayList<MeshSnapshot>();for(var part:roots(model)){var mesh=new MeshSnapshot();part.render(pose,mesh,0xf000f0,OverlayTexture.NO_OVERLAY);if(!mesh.vertices.isEmpty())meshes.add(mesh);}return meshes;
    }
    static List<ModelPart> roots(EntityModel<?> model){
        Set<ModelPart> parts=Collections.newSetFromMap(new IdentityHashMap<>());
        for(Class<?> type=model.getClass();type!=null&&type!=Object.class;type=type.getSuperclass())for(var field:type.getDeclaredFields()){
            if(Modifier.isStatic(field.getModifiers()))continue;
            if(!ModelPart.class.isAssignableFrom(field.getType())&&!field.getType().isArray())continue;
            try{field.setAccessible(true);var value=field.get(model);if(value instanceof ModelPart part)parts.add(part);else if(value instanceof ModelPart[] array)Collections.addAll(parts,array);}catch(ReflectiveOperationException ignored){}
        }
        var all=new ArrayList<>(parts);parts.removeIf(part->all.stream().anyMatch(parent->parent!=part&&parent.getAllParts().anyMatch(child->child==part)));
        var result=new ArrayList<>(parts);
        if(result.size()==1&&result.get(0).isEmpty())return result.get(0).getAllParts().filter(p->!p.isEmpty()).toList();
        return result;
    }
    @SuppressWarnings({"rawtypes","unchecked"}) private static void gore(LivingEntity entity,Vec3 motion){
        var mc=Minecraft.getInstance();var renderer=mc.getEntityRenderDispatcher().getRenderer(entity);if(!(renderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer living))return;
        EntityModel model=living.getModel();model.young=entity.isBaby();model.prepareMobModel(entity,0,0,0);model.setupAnim(entity,0,0,entity.tickCount,0,entity.getXRot());
        var id=net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).getPath();int color=bloodColor(id);float scale=entity.isBaby()?.5F:1;
        float fxScale=scale*(entity instanceof techguns.entities.npcs.SuperMutantBasic?1.2f:1);
        WeaponEffects.spawnColored("GoreFX_Blood",entity.position(),motion,fxScale,color);
        var texture=living.getTextureLocation(entity);var random=entity.getRandom();
        var capture=new PoseStack();capture.scale(-scale,-scale,scale);
        for(var mesh:bodyMeshes(model,capture)){
            var velocity=motion.scale(.35).add((.5-random.nextDouble())*.35,(entity.onGround()?random.nextDouble():.5-random.nextDouble())*.35,(.5-random.nextDouble())*.35);
            mc.particleEngine.add(new Gib(entity,mesh,texture,entity.position().add(0,entity.getBbHeight()/2,0),velocity,color));
        }
    }
    private static int bloodColor(String id){return switch(id){case "zombie","husk","zombie_villager","zombiesoldier","zombiefarmer","zombieminer"->0x6e1529;case "skeleton","stray","ghast","skeletonsoldier","ghastling"->0xffffff;case "wither_skeleton"->0x323232;case "creeper"->0x32af39;case "spider","cave_spider","cyberdemon"->0x559c11;case "alienbug"->0xebff46;case "supermutantbasic","supermutantelite","supermutantheavy"->0x6d3c19;case "armysoldier","stormtrooper","commando","dictatordave","psychosteve","bandit"->0xa0151f;case "enderman"->0xa024a7;case "slime"->0x28ff28;case "magma_cube"->0x5c1a00;case "zombified_piglin","zombiepigmansoldier"->0x6e330b;default->0xaa1a25;};}
    private static final class Gib extends Particle {
        final MeshSnapshot mesh;final ResourceLocation texture;final int blood;final org.joml.Vector3f axis;boolean landed;
        Gib(LivingEntity entity,MeshSnapshot mesh,ResourceLocation texture,Vec3 position,Vec3 velocity,int blood){
            super((net.minecraft.client.multiplayer.ClientLevel)entity.level(),position.x,position.y,position.z);this.mesh=mesh;this.texture=texture;this.blood=blood;xd=velocity.x;yd=velocity.y;zd=velocity.z;gravity=.03F;lifetime=75+random.nextInt(50);hasPhysics=true;setSize(.1F,.1F);axis=new org.joml.Vector3f(random.nextFloat(),random.nextFloat(),random.nextFloat()).normalize();
        }
        @Override public ParticleRenderType getRenderType(){return ParticleRenderType.CUSTOM;}
        @Override public void tick(){
            xo=x;yo=y;zo=z;if(age++>=lifetime){remove();return;}yd-=.03;double falling=yd;move(xd,yd,zd);if(onGround){landed=true;yd=-falling*.9;xd*=.6;zd*=.6;}xd*=.98;yd*=.98;zd*=.98;
            if(!landed)WeaponEffects.spawnColored("GoreTrailFX_Blood",new Vec3(x,y,z),Vec3.ZERO,.6F,blood);
        }
        @Override public void render(com.mojang.blaze3d.vertex.VertexConsumer unused,net.minecraft.client.Camera camera,float partial){
            var mc=Minecraft.getInstance();if(mc.level!=level)return;var pose=new PoseStack();var eye=camera.getPosition();pose.translate(Mth.lerp(partial,xo,x)-eye.x,Mth.lerp(partial,yo,y)-eye.y,Mth.lerp(partial,zo,z)-eye.z);pose.mulPose(new org.joml.Quaternionf().rotationAxis((age+partial)*.15F,axis));pose.translate(-mesh.centerX(),-mesh.centerY(),-mesh.centerZ());
            float alpha=Mth.clamp((lifetime-age-partial)/10,0,1);var buffers=mc.renderBuffers().bufferSource();var type=RenderType.entityTranslucent(texture);mesh.render(pose,buffers.getBuffer(type),getLightColor(partial),OverlayTexture.NO_OVERLAY,1,1,1,alpha);buffers.endBatch(type);
        }
    }
    private DeathEffects(){}
}
