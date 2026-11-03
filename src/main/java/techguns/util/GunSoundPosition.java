package techguns.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Original TGSound gunPosition offset (including its cos/sin convention). */
public final class GunSoundPosition {
    public static Vec3 of(LivingEntity entity) {
        double angle = Math.toRadians(entity.getYHeadRot());
        return new Vec3(entity.getX() + Math.cos(angle), entity.getY() + entity.getBbHeight() * 0.5,
                entity.getZ() + Math.sin(angle));
    }
    private GunSoundPosition() {}
}
