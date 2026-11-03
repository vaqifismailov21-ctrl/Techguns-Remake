package techguns.entities.projectiles;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import techguns.*;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;
public final class FlamethrowerProjectile extends GenericProjectile {
    private boolean trail;
    public FlamethrowerProjectile(EntityType<? extends FlamethrowerProjectile> type,Level level) {super(type,level);}
    public FlamethrowerProjectile(Level level,LivingEntity owner,float damage,float speed,int ttl,float spread,float start,float end,float minimum,float penetration,boolean blocks,EnumBulletFirePos side) {
        this(TGEntities.FLAME.get(),level);init(owner,damage,speed,ttl,spread,start,end,minimum,penetration,blocks,side);
    }
    @Override public void tick() {
        if(level.isClientSide&&!trail) {trail=true;techguns.client.particle.WeaponEffects.attach("FlamethrowerTrail",this);}
        super.tick();
    }
    @Override protected float inWaterUpdateBehaviour(float drag) {drag=super.inWaterUpdateBehaviour(drag);if(isInWaterRainOrBubble())discard();return drag;}
    @Override protected TGDamageSource getProjectileDamageSource() {var source=TGDamageSource.causeFireDamage(this,shooter,DeathType.DEFAULT);source.armorPenetration=penetration;source.setNoKnockback();return source;}
    @Override protected void onHitEffect(LivingEntity target,HitResult hit) {target.setSecondsOnFire(3);}
    @Override protected void hitBlock(BlockHitResult hit) {
        if(blockdamage)burnBlocks(level,hit,0.5);
        TGProjectilePackets.effect(this,"FlamethrowerImpact",hit.getLocation(),0,0);
    }
}
