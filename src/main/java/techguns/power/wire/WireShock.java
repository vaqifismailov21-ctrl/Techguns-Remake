package techguns.power.wire;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;

/**
 * Wires under load are bare: anything touching one gets a shock. Copper stings, steel hurts, high voltage hits hard,
 * throws back and sets on fire. Wires that carried nothing in the last second are safe.
 */
@Mod.EventBusSubscriber(modid=Techguns.MODID)
public final class WireShock {
    public static final ResourceKey<DamageType> SHOCK=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"wire_shock"));
    private WireShock(){}

    public static float damage(WireTier tier){return tier==WireTier.HIGH_VOLTAGE?12F:tier==WireTier.STEEL?5F:2F;}

    @SubscribeEvent public static void tick(TickEvent.LevelTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!(event.level instanceof ServerLevel level)||level.getGameTime()%5!=0)return;
        var net=WireNetwork.get(level);
        for(var w:net.live(level.getGameTime())){
            var a=WireNetwork.point(level,w.a());var b=WireNetwork.point(level,w.b());if(a==null||b==null)continue;
            double length=a.distanceTo(b),sag=WireTier.sag(w.tier(),length);
            int n=Math.max(2,(int)(length*3));
            var pts=new Vec3[n+1];AABB box=null;
            for(int i=0;i<=n;i++){double t=i/(double)n;pts[i]=a.lerp(b,t).add(0,-sag*4*t*(1-t),0);var p=new AABB(pts[i],pts[i]);box=box==null?p:box.minmax(p);}
            for(var e:level.getEntitiesOfClass(LivingEntity.class,box.inflate(0.4),LivingEntity::isAlive)){
                var bb=e.getBoundingBox().inflate(0.12);Vec3 hit=null;
                for(var p:pts)if(bb.contains(p)){hit=p;break;}
                if(hit==null)continue;
                shock(level,e,w.tier(),hit);
            }
        }
    }
    private static void shock(ServerLevel level,LivingEntity e,WireTier tier,Vec3 at){
        var type=level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SHOCK);
        if(!e.hurt(new DamageSource(type),damage(tier)))return;
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,at.x,at.y,at.z,tier==WireTier.HIGH_VOLTAGE?30:12,0.2,0.2,0.2,0.5);
        level.playSound(null,e.blockPosition(),tier==WireTier.HIGH_VOLTAGE?SoundEvents.LIGHTNING_BOLT_IMPACT:SoundEvents.FIRECHARGE_USE,SoundSource.BLOCKS,tier==WireTier.HIGH_VOLTAGE?0.7F:0.4F,1.9F);
        if(tier==WireTier.HIGH_VOLTAGE){
            e.setSecondsOnFire(3);
            var away=e.position().subtract(at);away=new Vec3(away.x,0,away.z);
            if(away.lengthSqr()>1e-4){away=away.normalize();e.push(away.x*0.8,0.35,away.z*0.8);e.hurtMarked=true;}
        }
    }
}
