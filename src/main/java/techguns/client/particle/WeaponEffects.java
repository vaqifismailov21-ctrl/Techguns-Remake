package techguns.client.particle;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
/** Original non-energy FX groups and inherited definitions, evaluated at their source cadence. */
@EventBusSubscriber(modid="techguns",value=Dist.CLIENT)
public final class WeaponEffects {
    private static Map<String,BulletImpactDefinition> definitions;
    private static Map<String,List<String>> groups;
    private static final List<Emitter> active=new ArrayList<>();
    private static final List<Emitter> pending=new ArrayList<>();
    private static final Random random=new Random();
    private static Reader read(String path) throws IOException {return new InputStreamReader(Minecraft.getInstance().getResourceManager().open(ResourceLocation.fromNamespaceAndPath("techguns","particles/"+path)),StandardCharsets.UTF_8);}
    public static void load() {
        if(definitions!=null)return;
        try(var data=read("nonenergyfx.txt");var lists=read("nonenergyfx-groups.json")) {
            definitions=BulletImpactDefinition.read(data,true);groups=new HashMap<>();
            for(var e:JsonParser.parseReader(lists).getAsJsonObject().entrySet()) {var names=new ArrayList<String>();for(var n:e.getValue().getAsJsonArray())names.add(n.getAsString());groups.put(e.getKey(),names);}
        } catch(IOException error) {throw new IllegalStateException("Cannot load original weapon effects",error);}
    }
    public static Map<String,BulletImpactDefinition> definitions(){load();return java.util.Collections.unmodifiableMap(definitions);}
    public static void spawn(String name,Vec3 position,float pitch,float yaw) {spawn(name,position,pitch,yaw,null);}
    public static void spawn(String name,Vec3 position,float pitch,float yaw,float scale){spawn(name,position,pitch,yaw,null,scale);}
    public static void attach(String name,Entity entity) {spawn(name,entity.position(),-entity.getXRot(),-entity.getYRot(),entity);}
    public static void attach(String name,Entity entity,float scale){spawn(name,entity.position(),-entity.getXRot(),-entity.getYRot(),entity,scale);}
    public static void spawnLegacy(String name,net.minecraft.world.level.Level level,double x,double y,double z,double vx,double vy,double vz){
        spawnMoving(name,new Vec3(x,y,z),new Vec3(vx,vy,vz));
    }
    public static void spawnColored(String name,Vec3 position,Vec3 velocity,float scale,int color){
        load();var level=Minecraft.getInstance().level;if(level==null)return;
        var type=definitions.get(name);if(type==null)throw new IllegalArgumentException("Missing original death effect "+name);
        var emitter=new Emitter(level,type,position,0,0,null);emitter.ownVelocity=velocity;emitter.scale=scale;emitter.startSizeRate*=scale;emitter.color=color;pending.add(emitter);
    }
    private static void spawnMoving(String name,Vec3 position,Vec3 velocity){
        load();var level=Minecraft.getInstance().level;if(level==null)return;
        if(groups.containsKey(name)){for(var child:groups.get(name))spawnMoving(child,position,velocity);return;}
        var type=definitions.get(name);if(type==null)throw new IllegalArgumentException("Missing original moving effect "+name);
        var emitter=new Emitter(level,type,position,0,0,null);emitter.ownVelocity=velocity;pending.add(emitter);
    }
    public static void receive(techguns.TGProjectilePackets.AttachedEffect message) {
        var game=Minecraft.getInstance();if(game.level==null)return;
        var entity=game.level.getEntity(message.entityId());
        if(entity instanceof net.minecraft.world.entity.LivingEntity living&&living!=game.player&&living.getUUID().equals(message.uuid()))attachHead(message.name(),living,message.offset(),message.charging());
    }
    public static void attachHead(String name,net.minecraft.world.entity.LivingEntity entity,Vec3 offset,boolean charging) {
        load();if(Minecraft.getInstance().level==null)return;
        if(groups.containsKey(name)){for(var child:groups.get(name))attachHead(child,entity,offset,charging);return;}
        var type=definitions.get(name);if(type==null)throw new IllegalArgumentException("Missing original attached effect "+name);
        var emitter=new Emitter(Minecraft.getInstance().level,type,entity.position(),entity.getXRot(),entity.getYHeadRot(),entity);emitter.headOffset=offset;emitter.charging=charging;pending.add(emitter);
    }
    private static void spawn(String name,Vec3 position,float pitch,float yaw,Entity entity) {
        spawn(name,position,pitch,yaw,entity,1);
    }
    private static void spawn(String name,Vec3 position,float pitch,float yaw,Entity entity,float scale) {
        load();if(Minecraft.getInstance().level==null)return;
        if(groups.containsKey(name)) {for(var child:groups.get(name))spawn(child,position,pitch,yaw,entity,scale);return;}
        var definition=definitions.get(name);if(definition==null)throw new IllegalArgumentException("Unknown original weapon FX "+name);
        var emitter=new Emitter(Minecraft.getInstance().level,definition,position,pitch,yaw,entity);emitter.scale=scale;emitter.startSizeRate*=scale;pending.add(emitter);
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var game=Minecraft.getInstance();if(false||game.isPaused())return;
        active.addAll(pending);pending.clear();active.removeIf(e->{boolean done=e.level!=game.level||e.tick();e.alive=!done;return done;});
    }
    public static void reload() {active.clear();pending.clear();definitions=null;groups=null;}
    private static final class Emitter {
        final ClientLevel level;final BulletImpactDefinition type;final Entity entity;final int lifetime;
        Vec3 position;float pitch,yaw,startSize,startSizeRate;final float startSizeDamping;int initial,age,delay;
        BulletImpactParticle previous,parent;
        Vec3 headOffset,ownVelocity=Vec3.ZERO;boolean charging,alive=true;float scale=1;int color=0xffffff;
        Emitter(ClientLevel level,BulletImpactDefinition type,Vec3 position,float pitch,float yaw,Entity entity) {
            this.level=level;this.type=type;this.position=position;this.pitch=pitch;this.yaw=yaw;this.entity=entity;
            lifetime=type.range("systemlifetime").sampleInt(random);initial=type.range("initialdelay").sampleInt(random);startSizeRate=type.range("startsizerate").sample(random);
            startSizeDamping=type.range("startsizeratedamping").sample(random);
        }
        boolean tick() {
            position=position.add(ownVelocity);
            if(entity!=null) {if(entity.isRemoved())return true;position=entity.position();pitch=-entity.getXRot();yaw=-entity.getYRot();}
            if(headOffset!=null&&entity instanceof net.minecraft.world.entity.LivingEntity living) {
                if(charging&&!living.isUsingItem())return true;
                pitch=living.getXRot();yaw=living.getYHeadRot();
                position=living.getEyePosition().add(headOffset.xRot((float)Math.toRadians(-pitch)).yRot((float)Math.toRadians(-yaw)));
            }
            if(parent!=null) {if(!parent.isAlive())return true;position=parent.position();}
            if(initial-->0)return false;
            startSize+=startSizeRate;startSizeRate*=startSizeDamping;
            if(delay--<=0) {
                int count=type.range("particlecount").sampleInt(random);
                for(int i=0;i<count;i++) {
                    var sample=type.sample(random,pitch,yaw);var motion=sample.motion().scale(scale).add(ownVelocity.scale(type.range("systemvelocityfactor").sample(random)));
                    if(entity!=null)motion=motion.add(entity.getDeltaMovement().scale(type.range("systemvelocityfactor").sample(random)));
                    level.getProfiler().push("techgunsWeaponParticle");
                    var offset=sample.position().scale(scale);
                    if(type.volumeType.equals("TRAIL")&&entity!=null)offset=entity.getDeltaMovement().normalize().scale(-type.volumeRadius*i/Math.max(1F,count)*scale);
                    var particle=new BulletImpactParticle(level,position.add(type.offset).add(offset),motion,type,random);particle.scale(scale);particle.setColor((color>>16&255)/255F,(color>>8&255)/255F,(color&255)/255F);
                    if(type.streak) {particle.linkPrevious(previous);previous=particle;}
                    if(type.attachedSystem!=null) {
                        var childType=definitions.get(type.attachedSystem);
                        if(childType==null)throw new IllegalStateException("Missing particle child "+type.attachedSystem);
                        var child=new Emitter(level,childType,particle.position(),0,0,null);child.parent=particle;child.scale=scale;child.startSizeRate*=scale;pending.add(child);
                    }
                    particle.addStartSize(startSize);
                    if(type.particlesStickToSystem)particle.stickTo(()->position,()->alive&&(entity==null||!entity.isRemoved()));
                    else if(type.particlesMoveWithSystem&&entity!=null)particle.follow(entity);
                    Minecraft.getInstance().particleEngine.add(particle);level.getProfiler().pop();
                }
                delay=type.range("spawndelay").sampleInt(random);
            }
            boolean expired=age>=lifetime;age++;
            return parent!=null?false:entity==null?expired:lifetime>0&&expired;
        }
    }
    private WeaponEffects() {}
}
