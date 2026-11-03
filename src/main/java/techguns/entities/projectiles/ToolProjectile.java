package techguns.entities.projectiles;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import techguns.*;
import techguns.api.damagesystem.DamageType;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;
/** Invisible, short-range original pneumatic/chainsaw attack projectile. */
public final class ToolProjectile extends GenericProjectile {
    public ToolProjectile(EntityType<? extends ToolProjectile> type,Level level) {super(type,level);}
    public ToolProjectile(Level level,LivingEntity owner,float damage,float speed,int ttl,float spread,float start,float end,float minimum,float penetration,boolean blocks,EnumBulletFirePos side,boolean hammer) {
        this(hammer?TGEntities.HAMMER.get():TGEntities.CHAINSAW.get(),level);init(owner,damage,speed,ttl,spread,start,end,minimum,penetration,blocks,side);
    }
    @Override protected TGDamageSource getProjectileDamageSource() {var source=new TGDamageSource(getType()==TGEntities.HAMMER.get()?"powerhammer":"chainsaw",this,shooter,DamageType.PHYSICAL,DeathType.GORE);source.goreChance=1;source.armorPenetration=penetration;return source;}
    private void impact(HitResult hit) {
        if(level.isClientSide)return;var p=hit.getLocation();
        level.playSound(null,p.x,p.y,p.z,TGSounds.POWERHAMMER_IMPACT.get(),net.minecraft.sounds.SoundSource.PLAYERS,1,1);TGProjectilePackets.effect(this,"PowerhammerImpact",p,0,0);
    }
    @Override protected void onHitEffect(LivingEntity target,HitResult hit){impact(hit);}
    @Override protected void hitBlock(BlockHitResult hit){impact(hit);}
    @Override protected float inWaterUpdateBehaviour(float drag){return getType()==TGEntities.HAMMER.get()?super.inWaterUpdateBehaviour(drag):drag;}
}
