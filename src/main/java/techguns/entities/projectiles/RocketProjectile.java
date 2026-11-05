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
        if(level().isClientSide&&!trail) {trail=true;techguns.client.particle.WeaponEffects.attach(trailEffect(),this);}
        super.tick();
    }
    public RocketProjectile(EntityType<? extends RocketProjectile> type,Level level) {super(type,level);}
    public RocketProjectile(Level level,LivingEntity owner,float damage,float speed,int ttl,float spread,float inner,float outer,float minimum,float penetration,boolean blocks,EnumBulletFirePos side,String variant) {
        this(variant.equals("thermonuke")?TGEntities.ROCKET_THERMONUKE.get():variant.equals("nuke")?TGEntities.ROCKET_NUKE.get():variant.equals("high_velocity")?TGEntities.ROCKET_HV.get():TGEntities.ROCKET.get(),level);
        float factor=isNuclear()?5:isHighVelocity()?0.75F:1;
        if(isThermonuclear()){init(owner,damage*5_000_000F,speed*3,ttl*3,spread,inner*5,outer*5,minimum*5_000_000F,penetration,blocks,side);return;}
        init(owner,damage*(isNuclear()?5:1),speed*(isHighVelocity()?2:1),isHighVelocity()?Math.round(ttl*0.75F):ttl,spread,inner*factor,outer*factor,minimum*(isNuclear()?5:1),penetration,blocks,side);
    }
    public boolean isNuclear() {return getType()==TGEntities.ROCKET_NUKE.get();}
    public boolean isThermonuclear() {return getType()==TGEntities.ROCKET_THERMONUKE.get();}
    public boolean isHighVelocity() {return getType()==TGEntities.ROCKET_HV.get();}
    protected double blockFactor() {return 0.5;}
    /** The thermonuclear rocket keeps its speed in air so it can be fired from outside its own blast. */
    @Override protected float inWaterUpdateBehaviour(float drag) {float d=super.inWaterUpdateBehaviour(drag);return isThermonuclear()&&!isInWater()?1.0F:d;}
    protected String explosionEffect() {return isNuclear()?"NukeExplosion":"RocketExplosion";}
    @Override protected void onStoppedInWater() { explodeRocket(); }
    protected void explodeRocket() {
        if(!level().isClientSide&&isThermonuclear()) {
            techguns.damagesystem.ThermonuclearBlast.detonate(level(),getShooter(),this,getProjectileDamageSource(),position(),damage,blockdamage);
            for(int i=0;i<9;i++){double a=i*Math.PI/4,r=i==0?0:techguns.damagesystem.ThermonuclearBlast.CRATER*0.4;TGProjectilePackets.effect(this,"NukeExplosion",position().add(Math.cos(a)*r,i==0?0:8,Math.sin(a)*r),0,0);}
            level().playSound(null,getX(),getY(),getZ(),TGSounds.NUKE_EXPLOSION.get(),net.minecraft.sounds.SoundSource.BLOCKS,64,0.5F);
        } else if(!level().isClientSide) {
            var explosion=new TGExplosion(level(),getShooter(),this,getX(),getY(),getZ(),damage,damageMin,damageDropStart,damageDropEnd,blockdamage?blockFactor():0);
            if(isNuclear()) {explosion.blockDropChance=0.05F;explosion.setDmgSrc(getProjectileDamageSource());}
            explosion.doExplosion(!isNuclear());
            TGProjectilePackets.effect(this,explosionEffect(),position(),0,0);
            if(isNuclear()) {
                level().playSound(null,getX(),getY(),getZ(),TGSounds.NUKE_EXPLOSION.get(),net.minecraft.sounds.SoundSource.BLOCKS,4,1);
                if(TGRadiationSystem.isEnabled()) level().addFreshEntity(new techguns.entities.special.EntityRadiation(this,1200,damageDropStart,9,damageDropEnd,2));
            }
        }
        discard();
    }
    @Override protected void onHitEffect(LivingEntity target,HitResult hit) {explodeRocket();}
    @Override protected void hitBlock(BlockHitResult hit) {setPos(hit.getLocation());explodeRocket();}
    @Override protected TGDamageSource getProjectileDamageSource() {
        var source=TGDamageSource.causeExplosionDamage(this,shooter,isNuclear()||isThermonuclear()?DeathType.LASER:DeathType.GORE);
        source.goreChance=1;source.knockbackMultiplier=3;return source;
    }
}
