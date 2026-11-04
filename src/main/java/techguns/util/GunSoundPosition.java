package techguns.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Original TGSound gunPosition offset (including its cos/sin convention). */
public final class GunSoundPosition {
    public static Vec3 of(LivingEntity entity) {
        double angle = Math.toRadians(entity.getYHeadRot());
        // 1.12 used (cos, sin) of the yaw: one block to the shooter's left, so shots came from one side. Use one block in front.
        return new Vec3(entity.getX() - Math.sin(angle), entity.getY() + entity.getBbHeight() * 0.5,
                entity.getZ() + Math.cos(angle));
    }
    private GunSoundPosition() {}
}
