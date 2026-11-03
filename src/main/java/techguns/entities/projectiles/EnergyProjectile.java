package techguns.entities.projectiles;

import java.util.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import techguns.*;
import techguns.damagesystem.*;
import techguns.deatheffects.EntityDeathUtils.DeathType;

/** Shared modern entity plumbing for the original energy projectile specializations. */
public final class EnergyProjectile extends GenericProjectile {
    public enum Mode {LASER,TESLA,BLASTER,ALIEN,NETHER,DEATOMIZER,GAUSS,NDR,SONIC,TFG}
    public Mode mode=Mode.BLASTER;
    public float size=1;
    public double beamDistance;
    public int beamLifetime;
    public long seed;
    public boolean mainProjectile;
    private int chainTargets=4;
    private UUID previousTeslaTarget;
    private boolean traced,trailStarted,exploded;
    private Set<UUID> sonicHits=new HashSet<>();
    public EnergyProjectile(EntityType<? extends EnergyProjectile> type,Level level){super(type,level);seed=level.random.nextLong();}
    public EnergyProjectile(Level level,LivingEntity owner,Mode mode,float damage,float speed,int ttl,float spread,float start,float end,float min,float pen,boolean blocks,EnumBulletFirePos side){
        this(TGEntities.ENERGY_PROJECTILE.get(),level);this.mode=mode;
        int lifetime=mode==Mode.LASER?7:mode==Mode.TESLA||mode==Mode.NDR?10:ttl;
        init(owner,damage,speed,lifetime,spread,start,end,min,pen,blocks,side);beamLifetime=lifetime;
        if(isBeam()){noCulling=true;if(mode!=Mode.NDR&&!level.isClientSide)traceBeam();}
    }
    public boolean isBeam(){return mode==Mode.LASER||mode==Mode.TESLA||mode==Mode.NDR;}
    public EnergyProjectile charge(float charge){size=1+charge*3+(charge>=1?1:0);return this;}
    public void sonicGroup(Set<UUID> shared,boolean center){sonicHits=shared;mainProjectile=center;}
    @Override public void tick(){
        if(isBeam()){
            xo=getX();yo=getY();zo=getZ();tickCount++;getShooter();
            if(mode==Mode.NDR&&shooter!=null){
                double yaw=Math.toRadians(shooter.getYHeadRot());
                setPos(shooter.getX()-Math.cos(yaw)*0.16,shooter.getEyeY()-0.10000000149011612,shooter.getZ()-Math.sin(yaw)*0.16);
                setDeltaMovement(shooter.getLookAngle().scale(speed));setYRot(shooter.getYHeadRot());setXRot(shooter.getXRot());
                traceBeam();
            }
            if(--ticksToLive<=0)discard();
        }else super.tick();
        if(level().isClientSide&&!trailStarted){
            trailStarted=true;String name=switch(mode){case ALIEN->"AlienBlasterTrail";case NETHER->"CyberDemonBlasterTrail";case DEATOMIZER->"BlueBlasterTrail";case GAUSS->"GaussProjectileTrail";case TFG->"TFGTrail";case NDR->"BeamGunMuzzleFX";case SONIC->mainProjectile?"SonicShotgunTrail":null;default->null;};
            if(name!=null)net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->techguns.client.particle.WeaponEffects.attach(name,this,mode==Mode.TFG?size*0.75F:1));
        }
    }
    public void traceBeam(){
        if(!posInitialized)initStartPos();
        Vec3 start=position(),end=start.add(getDeltaMovement());
        var block=level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
        if(block.getType()!=HitResult.Type.MISS)end=block.getLocation();
        HitResult hit=findEntityOnPath(start,end);if(hit==null&&block.getType()!=HitResult.Type.MISS)hit=block;
        if(hit instanceof EntityHitResult e&&e.getEntity() instanceof Player victim&&shooter instanceof Player owner&&!owner.canHarmPlayer(victim))hit=null;
        beamDistance=hit==null?speed:start.distanceTo(hit.getLocation());
        if(hit!=null)onHit(hit);traced=true;
    }
    @Override protected EntityHitResult findEntityOnPath(Vec3 start,Vec3 end){
        if(mode!=Mode.SONIC)return super.findEntityOnPath(start,end);
        Entity closest=null;Vec3 point=null;double best=Double.MAX_VALUE;
        for(var e:level().getEntities(this,getBoundingBox().expandTowards(getDeltaMovement()).inflate(1),e->e!=shooter&&!e.isSpectator()&&e.isAlive()&&e.isPickable()&&!sonicHits.contains(e.getUUID()))){
            var intersection=e.getBoundingBox().inflate(0.30000001192092896).clip(start,end);
            if(intersection.isPresent()&&start.distanceToSqr(intersection.get())<best){closest=e;point=intersection.get();best=start.distanceToSqr(point);}
        }
        return closest==null?null:new EntityHitResult(closest,point);
    }
    @Override protected void onHit(HitResult hit){
        if(!isBeam()&&mode!=Mode.SONIC){super.onHit(hit);return;}
        if(hit instanceof BlockHitResult block){hitBlock(block);if(mode==Mode.SONIC)discard();return;}
        if(!(hit instanceof EntityHitResult entity)||level().isClientSide)return;
        var target=entity.getEntity();if(mode==Mode.SONIC&&!sonicHits.add(target.getUUID()))return;
        var source=getProjectileDamageSource();
        if(target instanceof LivingEntity living){
            float amount=DamageSystem.getDamageFactor(shooter,living)*getDamage();
            if(amount>0){
                if(source.knockbackMultiplier==0||mode==Mode.SONIC)living.hurt(TGDamageSource.getKnockbackDummyDmgSrc(this,shooter),0.01F);
                living.hurt(source,amount);if(source.wasSuccessful())onHitEffect(living,hit);
            }
        }else target.hurt(source,getDamage());
        if(mode==Mode.TESLA&&chainTargets>0&&target instanceof LivingEntity living)chain(living);
    }
    private void chain(LivingEntity source){
        Vec3 from=new Vec3(source.getX(),source.getY()+source.getEyeHeight()*0.5,source.getZ());
        for(var next:level().getEntitiesOfClass(LivingEntity.class,new AABB(source.position(),source.position()).inflate(8),e->e!=source&&e!=shooter&&e.isAlive()&&!e.getUUID().equals(previousTeslaTarget))){
            Vec3 to=new Vec3(next.getX(),next.getY()+next.getEyeHeight()*0.5,next.getZ());
            if(next.position().distanceToSqr(from)>=64||level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()!=HitResult.Type.MISS)continue;
            var arc=new EnergyProjectile(TGEntities.ENERGY_PROJECTILE.get(),level());arc.mode=Mode.TESLA;arc.chainTargets=chainTargets-1;arc.setShooter(shooter);
            arc.previousTeslaTarget=source.getUUID();
            arc.configure(damage*0.75F,speed,10,damageDropStart,damageDropEnd,damageMin,penetration,blockdamage);
            arc.beamLifetime=10;arc.setPos(from);arc.initStartPos();arc.setDeltaMovement(to.subtract(from).normalize().scale(speed));arc.beamDistance=from.distanceTo(to);arc.traced=true;arc.noCulling=true;
            arc.onHit(new EntityHitResult(next,to));level().addFreshEntity(arc);break;
        }
    }
    @Override protected void onHitEffect(LivingEntity target,HitResult hit){
        if(mode==Mode.ALIEN)target.setSecondsOnFire(3);
        if(mode==Mode.NDR&&TGRadiationSystem.isEnabled())target.addEffect(new net.minecraft.world.effect.MobEffectInstance(TGRadiationSystem.RADIATION.get(),40,4,false,true));
        if(mode==Mode.TFG)explode();
        if(mode==Mode.GAUSS)effect("GaussRifleImpact_Block",hit);
    }
    private void effect(String name,HitResult hit){if(!level().isClientSide)TGProjectilePackets.effect(this,name,hit.getLocation(),0,0);}
    @Override protected void hitBlock(BlockHitResult hit){
        if(mode==Mode.TFG){explode();return;}
        if(mode==Mode.GAUSS)AdvancedBulletProjectile.impact(this,hit);
        String effect=switch(mode){case LASER,BLASTER->"LaserGunImpact";case ALIEN->"AlienExplosion";case NETHER->"CyberdemonBlasterImpact";case DEATOMIZER->"BlueBlasterExplosion";case GAUSS->"GaussRifleImpact_Block";case NDR->"BeamGunImpactFX";case SONIC->mainProjectile?"SonicShotgunImpact":null;default->null;};
        if(effect!=null)effect(effect,hit);
        if(mode==Mode.ALIEN&&blockdamage)burnBlocks(level(),hit,0.35);
    }
    private void explode(){
        if(exploded)return;exploded=true;
        if(!level().isClientSide){
            var explosion=new TGExplosion(level(),shooter,this,getX(),getY(),getZ(),damage*0.66F*size,damage*0.33F*size,size,size*2,blockdamage?0.5:0);
            explosion.blockDropChance=0.1F;explosion.doExplosion(false);
            TGProjectilePackets.scaledEffect(this,"TFGExplosion",position(),size*0.75F);
            level().playSound(null,getX(),getY(),getZ(),TGSounds.TFG_EXPLOSION.get(),net.minecraft.sounds.SoundSource.PLAYERS,5,1);
            if(size>3)level().playSound(null,getX(),getY(),getZ(),TGSounds.TFG_EXPLOSION_ECHO.get(),net.minecraft.sounds.SoundSource.PLAYERS,10,1);
        }discard();
    }
    @Override protected float inWaterUpdateBehaviour(float drag){return mode==Mode.BLASTER?drag:super.inWaterUpdateBehaviour(drag);}
    @Override protected TGDamageSource getProjectileDamageSource(){
        TGDamageSource source=switch(mode){
            case ALIEN,NETHER->TGDamageSource.causeFireDamage(this,shooter,mode==Mode.ALIEN?DeathType.LASER:DeathType.DEFAULT);
            case TESLA->TGDamageSource.causeLightningDamage(this,shooter,DeathType.LASER);
            case NDR->TGDamageSource.causeRadiationDamage(this,shooter,DeathType.LASER);
            case GAUSS->TGDamageSource.causeBulletDamage(this,shooter,DeathType.GORE);
            case SONIC->new TGDamageSource("tg_sonic",this,shooter,techguns.api.damagesystem.DamageType.PHYSICAL,DeathType.GORE);
            default->TGDamageSource.causeEnergyDamage(this,shooter,mode==Mode.DEATOMIZER?DeathType.GORE:DeathType.LASER);
        };
        source.armorPenetration=penetration;
        if(mode!=Mode.ALIEN&&mode!=Mode.NETHER&&mode!=Mode.DEATOMIZER&&mode!=Mode.SONIC)source.setNoKnockback();
        source.goreChance=switch(mode){case LASER,DEATOMIZER,SONIC->1;case ALIEN->0.25F;default->0.5F;};
        if(mode==Mode.SONIC){source.ignoreHurtresistTime=true;source.setKnockback(2);}
        return source;
    }
    public Vec3 beamEnd(){return position().add(getDeltaMovement().normalize().scale(beamDistance));}
    @Override public AABB getBoundingBoxForCulling(){return isBeam()?new AABB(position(),beamEnd()).inflate(1):super.getBoundingBoxForCulling();}
    @Override public void writeSpawnData(FriendlyByteBuf b){super.writeSpawnData(b);b.writeEnum(mode);b.writeFloat(size);b.writeDouble(beamDistance);b.writeVarInt(beamLifetime);b.writeLong(seed);b.writeBoolean(mainProjectile);}
    @Override public void readSpawnData(FriendlyByteBuf b){super.readSpawnData(b);mode=b.readEnum(Mode.class);size=b.readFloat();beamDistance=b.readDouble();beamLifetime=b.readVarInt();seed=b.readLong();mainProjectile=b.readBoolean();noCulling=isBeam();traced=true;}
    @Override protected void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putString("mode",mode.name());n.putFloat("size",size);n.putDouble("beamDistance",beamDistance);n.putInt("beamLifetime",beamLifetime);n.putBoolean("mainProjectile",mainProjectile);n.putInt("chainTargets",chainTargets);if(previousTeslaTarget!=null)n.putUUID("previousTeslaTarget",previousTeslaTarget);}
    @Override protected void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);mode=Mode.valueOf(n.getString("mode"));size=n.getFloat("size");beamDistance=n.getDouble("beamDistance");beamLifetime=n.getInt("beamLifetime");mainProjectile=n.getBoolean("mainProjectile");chainTargets=n.getInt("chainTargets");previousTeslaTarget=n.hasUUID("previousTeslaTarget")?n.getUUID("previousTeslaTarget"):null;noCulling=isBeam();}
}
