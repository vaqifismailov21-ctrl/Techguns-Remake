package techguns.client.particle;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import techguns.items.guns.MiningGun;
/** Original ScreenTestFX emitter retained in weapon-local coordinates for the fuel sword. */
@EventBusSubscriber(modid="techguns",value=Dist.CLIENT)
public final class ItemWeaponEffects {
    private static final Map<LivingEntity,EnumMap<InteractionHand,State>> states=new WeakHashMap<>();
    private static final class State {
        final List<BulletImpactParticle> particles=new ArrayList<>();final Random random=new Random();int initial,delay;
        final BulletImpactDefinition definition;
        State(){definition=WeaponEffects.definitions().get("ScreenTestFXFlames");initial=definition.range("initialdelay").sampleInt(random);}
        void tick() {
            particles.forEach(BulletImpactParticle::tick);particles.removeIf(p->!p.isAlive());if(initial-->0)return;
            if(delay--<=0) {
                int count=definition.range("particlecount").sampleInt(random);
                for(int i=0;i<count;i++){var sample=definition.sample(random,0,0);particles.add(new BulletImpactParticle(Minecraft.getInstance().level,definition.offset.add(sample.position()),sample.motion(),definition,random));}
                delay=definition.range("spawndelay").sampleInt(random);
            }
        }
    }
    public static void render(LivingEntity owner,InteractionHand hand,PoseStack pose,MultiBufferSource buffers,float partial,boolean third) {
        if(owner==null||Minecraft.getInstance().level==null)return;
        var stack=owner.getItemInHand(hand);
        if(!(stack.getItem() instanceof MiningGun gun)||!gun.hasAmbient()||gun.getCurrentAmmo(stack)==0)return;
        var state=states.computeIfAbsent(owner,e->new EnumMap<>(InteractionHand.class)).computeIfAbsent(hand,h->new State());
        for(var particle:state.particles)particle.renderAttached(pose,buffers,partial,third);
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var game=Minecraft.getInstance();if(false||game.isPaused())return;
        if(game.level==null){states.clear();return;}
        states.entrySet().removeIf(e->e.getKey().isRemoved()||e.getKey().level()!=game.level);
        for(var entry:states.entrySet()) {
            var owner=entry.getKey();entry.getValue().entrySet().removeIf(e->{var stack=owner.getItemInHand(e.getKey());return !(stack.getItem() instanceof MiningGun gun)||!gun.hasAmbient()||gun.getCurrentAmmo(stack)==0;});
            entry.getValue().values().forEach(State::tick);
        }
    }
    private ItemWeaponEffects(){}
}
