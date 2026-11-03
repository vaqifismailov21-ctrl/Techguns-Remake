package techguns.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import techguns.TGProjectilePackets;
import techguns.TGSounds;

/** PacketGunImpactFX normal-bullet sounds and original material particle systems. */
public final class ClientProjectileEffects {
    public static void impact(TGProjectilePackets.Impact message) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        SoundEvent sound = switch (message.material()) {
            case 0 -> TGSounds.BULLET_IMPACT_STONE.get(); case 1 -> TGSounds.BULLET_IMPACT_WOOD.get();
            case 2 -> TGSounds.BULLET_IMPACT_GLASS.get(); case 3 -> TGSounds.BULLET_IMPACT_METAL.get();
            default -> TGSounds.BULLET_IMPACT_DIRT.get();
        };
        level.playLocalSound(message.x(), message.y(), message.z(), sound, SoundSource.AMBIENT, 1, 1, true);
        techguns.client.particle.BulletImpactEffects.spawn(message.material(),new net.minecraft.world.phys.Vec3(message.x(),message.y(),message.z()),message.pitch(),message.yaw());
        if(message.incendiary()) techguns.client.particle.WeaponEffects.spawn("Impact_IncendiaryBullet",new net.minecraft.world.phys.Vec3(message.x(),message.y(),message.z()),message.pitch(),message.yaw());
    }
    private ClientProjectileEffects() {}
}
