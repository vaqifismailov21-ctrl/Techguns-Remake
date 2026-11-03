package techguns.api.guns;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import techguns.items.guns.GenericGun;

/** Original two-handed/offhand rules, applied to both server input and client rendering. */
public final class GunManager {
    public static boolean canUseOffhand(LivingEntity entity) { return canUseOffhand(entity.getMainHandItem(), entity.getOffhandItem(), entity); }
    public static boolean canUseOffhand(ItemStack main, ItemStack off, LivingEntity entity) {
        if (off.getItem() instanceof GenericGun gun && gun.getGunHandType() == GunHandType.TWO_HANDED) return false;
        return !(main.getItem() instanceof GenericGun gun) || gun.getGunHandType() != GunHandType.TWO_HANDED;
    }
    private GunManager() {}
}
