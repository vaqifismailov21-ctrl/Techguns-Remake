package techguns;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import techguns.network.DistExecutor;
import techguns.network.NetworkDirection;
import techguns.network.PacketDistributor;

/** Server-authored bullet impacts; clients cannot select a world position or trigger server effects. */
public final class TGProjectilePackets {
    public record Impact(short material, double x, double y, double z, float pitch, float yaw, boolean incendiary) {}
    public record Effect(String name,Vec3 position,float pitch,float yaw,float scale) {public Effect(String name,Vec3 position,float pitch,float yaw){this(name,position,pitch,yaw,1);}}
    public record AttachedEffect(int entityId,java.util.UUID uuid,String name,Vec3 offset,boolean charging) {}
    public static void register() {
        TGPackets.NETWORK.messageBuilder(AttachedEffect.class,8,NetworkDirection.PLAY_TO_CLIENT)
                .encoder((m,b)->{b.writeVarInt(m.entityId);b.writeUUID(m.uuid);b.writeUtf(m.name,80);b.writeDouble(m.offset.x);b.writeDouble(m.offset.y);b.writeDouble(m.offset.z);b.writeBoolean(m.charging);})
                .decoder(b->new AttachedEffect(b.readVarInt(),b.readUUID(),b.readUtf(80),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readBoolean()))
                .consumerMainThread((m,c)->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->techguns.client.particle.WeaponEffects.receive(m))).add();
        TGPackets.NETWORK.messageBuilder(Effect.class,7,NetworkDirection.PLAY_TO_CLIENT)
                .encoder((m,b)->{b.writeUtf(m.name,80);b.writeDouble(m.position.x);b.writeDouble(m.position.y);b.writeDouble(m.position.z);b.writeFloat(m.pitch);b.writeFloat(m.yaw);b.writeFloat(m.scale);})
                .decoder(b->new Effect(b.readUtf(80),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readFloat(),b.readFloat(),b.readFloat()))
                .consumerMainThread((m,c)->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->techguns.client.particle.WeaponEffects.spawn(m.name,m.position,m.pitch,m.yaw,m.scale))).add();
        TGPackets.NETWORK.messageBuilder(Impact.class, 3, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TGProjectilePackets::encode)
                .decoder(TGProjectilePackets::decode)
                .consumerMainThread((message, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> techguns.client.ClientProjectileEffects.impact(message))).add();
    }
    public static void attach(net.minecraft.world.entity.LivingEntity entity,String name,Vec3 offset,boolean charging) {
        if(entity.level().isClientSide)return;
        TGPackets.NETWORK.send(PacketDistributor.TRACKING_ENTITY.with(()->entity),new AttachedEffect(entity.getId(),entity.getUUID(),name,offset,charging));
    }
    public static void effect(Entity source,String name,Vec3 position,float pitch,float yaw) {
        effect(source.level(),name,position,pitch,yaw);
    }
    public static void scaledEffect(Entity source,String name,Vec3 position,float scale){
        if(source.level().isClientSide)return;
        TGPackets.NETWORK.send(PacketDistributor.NEAR.with(()->new PacketDistributor.TargetPoint(position.x,position.y,position.z,160,source.level().dimension())),new Effect(name,position,0,0,scale));
    }
    public static void effect(net.minecraft.world.level.Level level,String name,Vec3 position,float pitch,float yaw) {
        if(level.isClientSide)return;
        TGPackets.NETWORK.send(PacketDistributor.NEAR.with(()->new PacketDistributor.TargetPoint(position.x,position.y,position.z,160,level.dimension())),new Effect(name,position,pitch,yaw));
    }
    public static void encode(Impact message, net.minecraft.network.FriendlyByteBuf buffer) {
        buffer.writeShort(message.material); buffer.writeDouble(message.x); buffer.writeDouble(message.y); buffer.writeDouble(message.z);
        buffer.writeFloat(message.pitch); buffer.writeFloat(message.yaw); buffer.writeBoolean(message.incendiary);
    }
    public static Impact decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new Impact(buffer.readShort(),buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),buffer.readFloat(),buffer.readFloat(),buffer.readBoolean());
    }
    public static float impactPitch(net.minecraft.core.Direction face) { return face==net.minecraft.core.Direction.UP ? -90 : face==net.minecraft.core.Direction.DOWN ? 90 : 0; }
    public static float impactYaw(net.minecraft.core.Direction face) { return face.getAxis().isHorizontal() ? face.toYRot() : 0; }
    public static void send(Entity projectile, net.minecraft.world.phys.BlockHitResult hit, SoundType sound) {
        Vec3 location=hit.getLocation();
        short material = (short) (sound == SoundType.STONE ? 0 : sound == SoundType.WOOD || sound == SoundType.LADDER ? 1
                : sound == SoundType.GLASS ? 2 : sound == SoundType.METAL || sound == SoundType.ANVIL ? 3
                : sound == SoundType.GRAVEL || sound == SoundType.SAND ? 4 : -1);
        TGPackets.NETWORK.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(location.x, location.y, location.z,
                128, projectile.level().dimension())), new Impact(material, location.x, location.y, location.z,
                        impactPitch(hit.getDirection()),impactYaw(hit.getDirection()),projectile instanceof techguns.entities.projectiles.GenericProjectileIncendiary));
    }
    private TGProjectilePackets() {}
}
