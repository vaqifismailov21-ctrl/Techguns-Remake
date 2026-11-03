package techguns.entities.projectiles;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import techguns.TGEntities;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;

/** Original AS50 explosive round: 1.15 direct modifier and a 1.5-radius vanilla explosion. */
public final class GenericProjectileExplosive extends GenericProjectile {
    private boolean detonated;
    public GenericProjectileExplosive(EntityType<? extends GenericProjectileExplosive> type,Level level) { super(type,level); }
    public GenericProjectileExplosive(Level level,LivingEntity owner,float damage,float speed,int ttl,float spread,
                                     float dropStart,float dropEnd,float minimum,float penetration,boolean blockDamage,EnumBulletFirePos firePos) {
        this(TGEntities.EXPLOSIVE_BULLET.get(),level);
        init(owner,damage*1.15F,speed,ttl,spread,dropStart,dropEnd,minimum*1.15F,penetration,blockDamage,firePos);
    }
    @Override protected TGDamageSource getProjectileDamageSource() {
        var source=TGDamageSource.causeExplosionDamage(this,shooter,DeathType.GORE);
        source.armorPenetration=penetration;source.goreChance=1;source.knockbackMultiplier=3;return source;
    }
    @Override protected void onHitEffect(LivingEntity entity,HitResult hit) { explode(hit); }
    @Override protected void hitBlock(BlockHitResult hit) { explode(hit); }
    private void explode(HitResult hit) {
        if(detonated || level.isClientSide) return;
        detonated=true;var at=hit.getLocation();
        var explosion=new net.minecraft.world.level.Explosion(level,this,at.x,at.y,at.z,1.5F,false,blockdamage?net.minecraft.world.level.Explosion.BlockInteraction.DESTROY:net.minecraft.world.level.Explosion.BlockInteraction.NONE);
        explosion.explode();explosion.finalizeExplosion(false);
        level.playSound(null,at.x,at.y,at.z,net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,net.minecraft.sounds.SoundSource.BLOCKS,4,(1+(random.nextFloat()-random.nextFloat())*0.2F)*0.7F);
        techguns.TGProjectilePackets.effect(this,"MiningChargeBlockExplosion",at,0,0);
        discard();
    }
}
