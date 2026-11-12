package techguns.items.guns;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import techguns.TGSounds;
import techguns.entities.projectiles.*;
import techguns.items.guns.ammo.AmmoTypes;

public final class HandCannon extends GenericGun {
    public HandCannon() { super(12,1,40,25,12,11,1,0.035F,10,25,7,()->AmmoTypes.STONE_BULLETS,TGSounds.HANDGUN_FIRE,TGSounds.HANDGUN_RELOAD);setSemiAuto(true);setGravity(0.015);setCrossHair(EnumCrosshairStyle.QUAD_NO_CORNERS); }
    @Override protected GenericProjectile createProjectile(ItemStack stack,Level level,Player player,EnumBulletFirePos firePos,float spread,boolean blockDamage) {
        return new StoneBulletProjectile(level,player,damage,speed,(int)Math.ceil(maxRange/speed),spread,damageDropStart,damageDropEnd,damageMin,getPenetration(),blockDamage,firePos);
    }
}
