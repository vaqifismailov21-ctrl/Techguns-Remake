package techguns.entities.npcs;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import techguns.*;
import techguns.damagesystem.TGDamageSource;
import techguns.entities.projectiles.*;

public final class AttackHelicopter extends GenericFlyingMob {
    private static final EntityDataAccessor<Boolean> ATTACKING=SynchedEntityData.defineId(AttackHelicopter.class,EntityDataSerializers.BOOLEAN);
    public static final int MAX_DEATH_TIME=100;
    private int ambientSoundTick;
    public AttackHelicopter(EntityType<? extends AttackHelicopter> type,Level level){super(type,level);xpReward=5;}
    public static AttributeSupplier.Builder attributes(){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,100).add(Attributes.FOLLOW_RANGE,128).add(Attributes.ARMOR,16).add(Attributes.ARMOR_TOUGHNESS,5);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(ATTACKING,false);}
    public boolean isAttacking(){return entityData.get(ATTACKING);}public void setAttacking(boolean value){entityData.set(ATTACKING,value);}
    @Override protected void registerGoals(){goalSelector.addGoal(5,new RandomFly(this));goalSelector.addGoal(7,new LookAround(this));goalSelector.addGoal(7,new HelicopterAttack(this));targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,Player.class,true));}
    public static final class HelicopterAttack extends Goal {
        private final AttackHelicopter host;private int timer;
        public HelicopterAttack(AttackHelicopter host){this.host=host;}
        @Override public boolean canUse(){return host.getTarget()!=null;}
        @Override public void start(){timer=0;}@Override public void stop(){host.setAttacking(false);}@Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){
            var target=host.getTarget();if(target==null)return;
            if(host.distanceToSqr(target)<4096&&host.getSensing().hasLineOfSight(target)){
                ++timer;
                if(timer>=14&&timer<24&&timer%2==0){if(timer==14)host.playSound(TGSounds.HELICOPTER_BURST.get(),8,1);host.level().addFreshEntity(new GenericProjectile(host.level(),host,12,1,100,.05f,30,40,8,.25f,false,EnumBulletFirePos.CENTER));}
                else if(timer==35){host.playSound(TGSounds.ROCKET_FIRE.get(),8,1);host.level().addFreshEntity(new RocketProjectile(host.level(),host,12,1,100,.05f,30,40,8,.25f,false,host.random.nextBoolean()?EnumBulletFirePos.LEFT:EnumBulletFirePos.RIGHT,"default"));}
                else if(timer>35)timer=-30;
            }else if(timer>0)--timer;
            host.setAttacking(timer>10);
        }
    }
    @Override public void baseTick(){super.baseTick();if(--ambientSoundTick<=0&&deathTime==0){playSound(TGSounds.HELICOPTER_ROTOR.get(),6,1);ambientSoundTick=61;}}
    @Override protected void tickDeath(){
        if(deathTime==0)playSound(TGSounds.HELICOPTER_DEATH.get(),6,1);++deathTime;
        if(deathTime==MAX_DEATH_TIME){
            if(!level().isClientSide){dropExperience();TGProjectilePackets.effect(this,"HelicopterDeathExplosion",position().add(0,-8,0),0,0);}
            playSound(TGSounds.HELICOPTER_EXPLODE.get(),6,1);remove(RemovalReason.KILLED);
        }
    }
    @Override protected float getStandingEyeHeight(Pose pose,EntityDimensions dimensions){return .5f;}
    @Override protected ResourceLocation getDefaultLootTable(){return ResourceLocation.parse("techguns:entities/attackhelicopter");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return TGSounds.HELICOPTER_HIT.get();}
    @Override protected SoundEvent getDeathSound(){return TGSounds.HELICOPTER_HIT.get();}
    @Override protected float getSoundVolume(){return 10;}
    @Override protected int getTargetFlyHeight(){return 24;}
    @Override public int getMaxSpawnClusterSize(){return 1;}
    @Override public float getTotalArmorAgainstType(TGDamageSource source){return switch(source.damageType){case LIGHTNING,ENERGY->10;case FIRE,ICE,PHYSICAL,PROJECTILE,POISON,RADIATION->20;default->0;};}
    @Override public float getPenetrationResistance(TGDamageSource source){return source.damageType==techguns.api.damagesystem.DamageType.PROJECTILE?.5f:0;}
    @Override public float getBulletOffsetSide(){return 2;}
    @Override public float getWeaponPosX(){return 0;}@Override public float getWeaponPosY(){return 0;}@Override public float getWeaponPosZ(){return 0;}
}
