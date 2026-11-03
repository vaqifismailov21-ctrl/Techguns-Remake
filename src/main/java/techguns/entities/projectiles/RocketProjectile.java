package techguns.entities.projectiles;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import techguns.*;
import techguns.damagesystem.*;
import techguns.deatheffects.EntityDeathUtils.DeathType;
/** Original rocket hit damage followed by TGExplosion. HV and nuclear variants retain their source modifiers. */
public class RocketProjectile extends GenericProjectile {
    private boolean trail;
    protected String trailEffect() {return "RocketLauncherExhaust";}
    @Override public void tick() {
        if(level.isClientSide&&!trail) {trail=true;techguns.client.particle.WeaponEffects.attach(trailEffect(),this);}
        super.tick();
    }
    public RocketProjectile(EntityType<? extends RocketProjectile> type,Level level) {super(type,level);}
    public RocketProjectile(Level level,LivingEntity owner,float damage,float speed,int ttl,float spread,float inner,float outer,float minimum,float penetration,boolean blocks,EnumBulletFirePos side,String variant) {
        this(variant.equals("nuke")?TGEntities.ROCKET_NUKE.get():variant.equals("high_velocity")?TGEntities.ROCKET_HV.get():TGEntities.ROCKET.get(),level);
        float factor=isNuclear()?5:isHighVelocity()?0.75F:1;
        init(owner,damage*(isNuclear()?5:1),speed*(isHighVelocity()?2:1),isHighVelocity()?Math.round(ttl*0.75F):ttl,spread,inner*factor,outer*factor,minimum*(isNuclear()?5:1),penetration,blocks,side);
    }
    public boolean isNuclear() {return getType()==TGEntities.ROCKET_NUKE.get();}
    public boolean isHighVelocity() {return getType()==TGEntities.ROCKET_HV.get();}
    protected double blockFactor() {return 0.5;}
    protected String explosionEffect() {return isNuclear()?"NukeExplosion":"RocketExplosion";}
    protected void explodeRocket() {
        if(!level.isClientSide) {
            var explosion=new TGExplosion(level,getShooter(),this,getX(),getY(),getZ(),damage,damageMin,damageDropStart,damageDropEnd,blockdamage?blockFactor():0);
            if(isNuclear()) {explosion.blockDropChance=0.05F;explosion.setDmgSrc(getProjectileDamageSource());}
            explosion.doExplosion(!isNuclear());
            TGProjectilePackets.effect(this,explosionEffect(),position(),0,0);
            if(isNuclear()) {
                level.playSound(null,getX(),getY(),getZ(),TGSounds.NUKE_EXPLOSION.get(),net.minecraft.sounds.SoundSource.BLOCKS,4,1);
                if(TGRadiationSystem.isEnabled()) level.addFreshEntity(new techguns.entities.special.EntityRadiation(this,1200,damageDropStart,9,damageDropEnd,2));
            }
        }
        discard();
    }
    @Override protected void onHitEffect(LivingEntity target,HitResult hit) {explodeRocket();}
    @Override protected void hitBlock(BlockHitResult hit) {setPos(hit.getLocation());explodeRocket();}
    @Override protected TGDamageSource getProjectileDamageSource() {
        var source=TGDamageSource.causeExplosionDamage(this,shooter,isNuclear()?DeathType.LASER:DeathType.GORE);
        source.goreChance=1;source.knockbackMultiplier=3;return source;
    }
}
