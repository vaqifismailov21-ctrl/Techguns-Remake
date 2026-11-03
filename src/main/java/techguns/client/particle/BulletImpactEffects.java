package techguns.client.particle;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import techguns.Techguns;

@EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class BulletImpactEffects {
    private static Map<String,BulletImpactDefinition> definitions;
    private static final List<Emitter> emitters=new ArrayList<>();
    private static final Random RANDOM=new Random();
    public static Map<String,BulletImpactDefinition> definitions() {
        if (definitions==null) {
            var resource=ResourceLocation.fromNamespaceAndPath("techguns","particles/impactfx.txt");
            try (var reader=new InputStreamReader(Minecraft.getInstance().getResourceManager().open(resource),StandardCharsets.UTF_8)) {
                definitions=BulletImpactDefinition.read(reader);
            } catch(IOException error) { throw new IllegalStateException("Cannot load original Techguns bullet effects",error); }
        }
        return definitions;
    }
    public static void spawn(short material, Vec3 position,float pitch,float yaw) {
        if(material==5) return;
        var level=Minecraft.getInstance().level; if(level==null) return;
        var names=new ArrayList<>(List.of("BulletImpactFlare","BulletImpactSparks","BulletImpactDust"));
        String debris=switch(material) { case 0 -> "BulletImpactDebris"; case 1 -> "BulletImpactDebrisWood"; case 2 -> "BulletImpactDebrisGlass";
            case 3 -> "BulletImpactSparksExtra"; case 4 -> "BulletImpactDirt"; default -> null; };
        if(debris!=null) names.add(debris);
        for(var name:names) emitters.add(new Emitter(level,definitions().get(name),position,pitch,yaw));
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var game=Minecraft.getInstance();
        if(false || game.isPaused()) return;
        emitters.removeIf(emitter -> emitter.level!=game.level || emitter.tick());
    }
    public static void reload() { definitions=null; emitters.clear(); }
    private static final class Emitter {
        final ClientLevel level; final BulletImpactDefinition type; final Vec3 position; final float pitch,yaw;
        final int lifetime; int initial,age,delay;
        Emitter(ClientLevel level,BulletImpactDefinition type,Vec3 position,float pitch,float yaw) {
            this.level=level;this.type=type;this.position=position;this.pitch=pitch;this.yaw=yaw;
            lifetime=type.range("systemlifetime").sampleInt(RANDOM);initial=type.range("initialdelay").sampleInt(RANDOM);
        }
        boolean tick() {
            if(initial-->0) return false;
            if(delay--<=0) {
                int count=type.range("particlecount").sampleInt(RANDOM);
                for(int i=0;i<count;i++) {
                    Vec3 offset=type.position(RANDOM).xRot((float)Math.toRadians(-pitch)).yRot((float)Math.toRadians(-yaw));
                    Minecraft.getInstance().particleEngine.add(new BulletImpactParticle(level,position.add(offset),type.velocity(RANDOM,pitch,yaw),type,RANDOM));
                }
                delay=type.range("spawndelay").sampleInt(RANDOM);
            }
            return age++>=lifetime;
        }
    }
    private BulletImpactEffects() {}
}
