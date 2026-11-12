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
        var net=WireNetwork.get(level);if(net.all().isEmpty())return;
        for(var w:net.live(level.getGameTime())){
            if(w.insulated())continue;
            var a=WireNetwork.point(level,w.a());var b=WireNetwork.point(level,w.b());if(a==null||b==null)continue;
            var line=LINES.get(w);
            if(line==null||!line.a.equals(a)||!line.b.equals(b)){line=new Line(a,b,w.tier());LINES.put(w,line);}
            for(var e:level.getEntitiesOfClass(LivingEntity.class,line.box,LivingEntity::isAlive)){
                var bb=e.getBoundingBox().inflate(0.12);Vec3 hit=null;
                for(var p:line.pts)if(bb.contains(p)){hit=p;break;}
                if(hit==null)continue;
                shock(level,e,w.tier(),hit);
            }
        }
    }
    /** Points along a hanging wire and the box around them, worked out once per wire. */
    private static final class Line{
        final Vec3 a,b;final Vec3[] pts;final AABB box;
        Line(Vec3 a,Vec3 b,WireTier tier){
            this.a=a;this.b=b;double length=a.distanceTo(b),sag=WireTier.sag(tier,length);int n=Math.max(2,(int)(length*3));
            pts=new Vec3[n+1];double x0=a.x,y0=a.y,z0=a.z,x1=a.x,y1=a.y,z1=a.z;
            for(int i=0;i<=n;i++){double t=i/(double)n;var p=pts[i]=a.lerp(b,t).add(0,-sag*4*t*(1-t),0);
                x0=Math.min(x0,p.x);y0=Math.min(y0,p.y);z0=Math.min(z0,p.z);x1=Math.max(x1,p.x);y1=Math.max(y1,p.y);z1=Math.max(z1,p.z);}
            box=new AABB(x0,y0,z0,x1,y1,z1).inflate(0.4);
        }
    }
    private static final java.util.Map<WireConnection,Line> LINES=new java.util.WeakHashMap<>();

    /** A full hazmat suit (rubber) keeps the wearer safe. */
    public static boolean protectedSuit(LivingEntity e){
        for(var slot:new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.entity.EquipmentSlot.LEGS,net.minecraft.world.entity.EquipmentSlot.FEET}){
            var id=net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(e.getItemBySlot(slot).getItem());
            if(id==null||!id.getNamespace().equals(Techguns.MODID)||!id.getPath().startsWith("hazmat_"))return false;
        }
        return true;
    }
    private static void shock(ServerLevel level,LivingEntity e,WireTier tier,Vec3 at){
        if(protectedSuit(e)){if(level.getGameTime()%20==0)level.sendParticles(ParticleTypes.ELECTRIC_SPARK,at.x,at.y,at.z,3,0.1,0.1,0.1,0.2);return;}
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
