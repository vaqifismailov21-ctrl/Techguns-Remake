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
    public AttackHelicopter(EntityType<? extends AttackHelicopter> type,Level level){super(type,level);xpReward=35;}
    public static AttributeSupplier.Builder attributes(){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,250).add(Attributes.FOLLOW_RANGE,192).add(Attributes.ARMOR,16).add(Attributes.ARMOR_TOUGHNESS,5);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(ATTACKING,false);}
    public boolean isAttacking(){return entityData.get(ATTACKING);}public void setAttacking(boolean value){entityData.set(ATTACKING,value);}
    @Override protected void registerGoals(){goalSelector.addGoal(5,new RandomFly(this));goalSelector.addGoal(7,new LookAround(this));goalSelector.addGoal(7,new HelicopterAttack(this));targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,Player.class,true){
        // vanilla searches only 4 blocks up/down; flying 24 blocks above the ground it never found anyone
        @Override protected net.minecraft.world.phys.AABB getTargetSearchArea(double range){return mob.getBoundingBox().inflate(range,getTargetFlyHeight()*2,range);}
    });}
    public static final class HelicopterAttack extends Goal {
        private final AttackHelicopter host;private int timer;
        public HelicopterAttack(AttackHelicopter host){this.host=host;}
        @Override public boolean canUse(){return host.getTarget()!=null;}
        @Override public void start(){timer=0;}@Override public void stop(){host.setAttacking(false);}@Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){
            var target=host.getTarget();if(target==null)return;
            if(host.distanceToSqr(target)<4096&&host.getSensing().hasLineOfSight(target)){
                ++timer;
                // a short machine gun burst, then a salvo of five rockets, every second one a fast one
                if(timer>=8&&timer<18&&timer%2==0){if(timer==8)host.playSound(TGSounds.HELICOPTER_BURST.get(),8,1);var bullet=new GenericProjectile(host.level(),host,12,1,100,.05f,30,40,8,.25f,false,EnumBulletFirePos.CENTER);aim(bullet,target,8);host.level().addFreshEntity(bullet);}
                else if(timer>=40&&timer<=65&&timer%5==0){
                    boolean fast=timer==45||timer==60;
                    host.playSound(TGSounds.ROCKET_FIRE.get(),8,1);
                    var rocket=new RocketProjectile(host.level(),host,12,fast?1.2f:1,100,.05f,30,40,8,.25f,false,host.random.nextBoolean()?EnumBulletFirePos.LEFT:EnumBulletFirePos.RIGHT,fast?"high_velocity":"default");
                    aim(rocket,target,4);host.level().addFreshEntity(rocket);
                }
                else if(timer>65)timer=-60;
            }else if(timer>0)--timer;
            host.setAttacking(timer>10);
        }
        /** The body pitch follows the target only loosely, so shots went along it and missed: aim at the target's middle instead. */
        private static void aim(GenericProjectile shot,net.minecraft.world.entity.LivingEntity target,float spread){
            shot.shoot(target.getX()-shot.getX(),target.getY()+target.getBbHeight()*.5-shot.getY(),target.getZ()-shot.getZ(),1.5F,spread);
        }
    }
    /* Death loot is held back until the wreck explodes and then thrown out of the blast with the debris. */
    private DamageSource heldLoot;private Player heldKiller;private int heldKillerTime;private boolean releasingLoot;
    @Override protected void dropAllDeathLoot(DamageSource source){
        if(releasingLoot){super.dropAllDeathLoot(source);return;}
        heldLoot=source;heldKiller=lastHurtByPlayer;heldKillerTime=lastHurtByPlayerTime;
    }
    private void releaseLoot(){
        if(heldLoot==null)return;
        lastHurtByPlayer=heldKiller;lastHurtByPlayerTime=Math.max(heldKillerTime,1);releasingLoot=true;
        try{super.dropAllDeathLoot(heldLoot);}finally{releasingLoot=false;heldLoot=null;}
    }
    @Override public net.minecraft.world.entity.item.ItemEntity spawnAtLocation(net.minecraft.world.item.ItemStack stack,float y){
        var item=super.spawnAtLocation(stack,y);
        if(item!=null&&releasingLoot){
            item.setPos(getX(),getY()-8,getZ());double a=random.nextDouble()*Math.PI*2,v=.35+random.nextDouble()*.35;
            item.setDeltaMovement(Math.cos(a)*v,.4+random.nextDouble()*.4,Math.sin(a)*v);
        }
        return item;
    }
    @Override public void baseTick(){super.baseTick();if(--ambientSoundTick<=0&&deathTime==0){playSound(TGSounds.HELICOPTER_ROTOR.get(),6,1);ambientSoundTick=61;}}
    @Override protected void tickDeath(){
        if(deathTime==0)playSound(TGSounds.HELICOPTER_DEATH.get(),6,1);++deathTime;
        if(deathTime==MAX_DEATH_TIME){
            if(!level().isClientSide){releaseLoot();dropExperience();TGProjectilePackets.effect(this,"HelicopterDeathExplosion",position().add(0,-8,0),0,0);}
            playSound(TGSounds.HELICOPTER_EXPLODE.get(),6,1);remove(RemovalReason.KILLED);
        }
    }
    @Override protected float getStandingEyeHeight(Pose pose,EntityDimensions dimensions){return .5f;}
    @Override protected ResourceLocation getDefaultLootTable(){return ResourceLocation.parse("techguns:entities/attackhelicopter");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return TGSounds.HELICOPTER_HIT.get();}
    @Override protected SoundEvent getDeathSound(){return TGSounds.HELICOPTER_HIT.get();}
    @Override protected float getSoundVolume(){return 8;}
    @Override protected int getTargetFlyHeight(){return 16;}
    @Override public int getMaxSpawnClusterSize(){return 2;}
    /** Rockets of other hostile NPCs (and its own) do not bring it down. */
    @Override public boolean hurt(DamageSource source,float amount){
        if(hostile(source.getEntity())||hostile(source.getDirectEntity()))return false;
        return super.hurt(source,amount);
    }
    private boolean hostile(net.minecraft.world.entity.Entity e){
        if(e==null)return false;if(e==this)return true;
        if(e instanceof GenericProjectile p&&p.getShooter()!=null&&p.getShooter()!=e)return hostile(p.getShooter());
        return e instanceof techguns.api.npc.factions.ITGNpcTeam t&&t.getTGFaction()==techguns.api.npc.factions.TGNpcFaction.HOSTILE;
    }
    @Override public float getTotalArmorAgainstType(TGDamageSource source){return switch(source.damageType){case LIGHTNING,ENERGY->10;case FIRE,ICE,PHYSICAL,PROJECTILE,POISON,RADIATION->20;default->0;};}
    @Override public float getPenetrationResistance(TGDamageSource source){return source.damageType==techguns.api.damagesystem.DamageType.PROJECTILE?.5f:0;}
    @Override public float getBulletOffsetSide(){return 2;}
    @Override public float getWeaponPosX(){return 0;}@Override public float getWeaponPosY(){return 0;}@Override public float getWeaponPosZ(){return 0;}
}
