package techguns.deatheffects;

import java.util.UUID;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import techguns.TGPackets;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;

@Mod.EventBusSubscriber(modid="techguns")
public final class DeathEffectEvents {
    public record Death(int entityId,UUID uuid,int type,Vec3 motion){}
    public static boolean eligible(LivingEntity entity,TGDamageSource damage,float roll){
        return damage.deathType!=null&&damage.deathType!=DeathType.DEFAULT&&roll<damage.goreChance&&EntityDeathUtils.hasSpecialDeathAnim(entity,damage.deathType);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void died(LivingDeathEvent event){
        var entity=event.getEntity();
        if(entity.level().isClientSide||!(event.getSource() instanceof TGDamageSource damage)||!eligible(entity,damage,entity.getRandom().nextFloat()))return;
        TGPackets.NETWORK.send(PacketDistributor.NEAR.with(()->new PacketDistributor.TargetPoint(entity.getX(),entity.getY(),entity.getZ(),100,entity.level().dimension())),new Death(entity.getId(),entity.getUUID(),damage.deathType.ordinal(),entity.getDeltaMovement()));
    }
    public static void register(){
        TGPackets.NETWORK.messageBuilder(Death.class,9,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeVarInt(m.entityId);b.writeUUID(m.uuid);b.writeByte(m.type);b.writeDouble(m.motion.x);b.writeDouble(m.motion.y);b.writeDouble(m.motion.z);})
            .decoder(b->new Death(b.readVarInt(),b.readUUID(),b.readUnsignedByte(),new Vec3(b.readDouble(),b.readDouble(),b.readDouble())))
            .consumerMainThread((m,c)->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->techguns.client.DeathEffects.receive(m))).add();
    }
    private DeathEffectEvents(){}
}
