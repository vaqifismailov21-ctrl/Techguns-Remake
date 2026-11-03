package techguns.entities.projectiles;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import techguns.TGEntities;

/** Original hand-cannon projectile; gravity is supplied by its weapon factory. */
public final class StoneBulletProjectile extends GenericProjectile {
    public StoneBulletProjectile(EntityType<? extends StoneBulletProjectile> type,Level level) { super(type,level); }
    public StoneBulletProjectile(Level level,LivingEntity owner,float damage,float speed,int ttl,float spread,
                                 float dropStart,float dropEnd,float minimum,float penetration,boolean blockDamage,EnumBulletFirePos firePos) {
        this(TGEntities.STONE_BULLET.get(),level);
        init(owner,damage,speed,ttl,spread,dropStart,dropEnd,minimum,penetration,blockDamage,firePos);
    }
}
