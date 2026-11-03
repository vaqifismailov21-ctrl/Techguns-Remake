package techguns.entities.npcs;
import java.util.EnumSet;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import techguns.api.npc.*;
import techguns.damagesystem.TGDamageSource;
import techguns.entities.projectiles.*;

/** Original active AIFireballAttack (the unused AIGhastlingAttack is deliberately not substituted). */
public final class Ghastling extends Monster implements INpcTGDamageSystem,INPCTechgunsShooter {
    private static final EntityDataAccessor<Boolean> ATTACKING=SynchedEntityData.defineId(Ghastling.class,EntityDataSerializers.BOOLEAN);
    public Ghastling(EntityType<? extends Ghastling> type,Level level){super(type,level);xpReward=10;setPathfindingMalus(BlockPathTypes.WATER,-1);setPathfindingMalus(BlockPathTypes.LAVA,8);setPathfindingMalus(BlockPathTypes.DANGER_FIRE,0);setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE,0);}
    public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.FOLLOW_RANGE,64).add(Attributes.ARMOR,10).add(Attributes.ARMOR_TOUGHNESS,0);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(ATTACKING,false);}
    public boolean isAttacking(){return entityData.get(ATTACKING);}
    public void setAttacking(boolean value){entityData.set(ATTACKING,value);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(4,new FireballAttack(this));goalSelector.addGoal(5,new MoveTowardsRestrictionGoal(this,1));goalSelector.addGoal(7,new WaterAvoidingRandomStrollGoal(this,1,0));goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(8,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this).setAlertOthers());targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    public static final class FireballAttack extends Goal {
        private final Ghastling host;private int step,time;
        public FireballAttack(Ghastling host){this.host=host;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){return host.getTarget()!=null&&host.getTarget().isAlive();}
        @Override public void start(){step=0;}
        @Override public void stop(){host.setAttacking(false);}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){
            --time;var target=host.getTarget();if(target==null)return;double distance=host.distanceToSqr(target);
            if(distance<4){if(time<=0){time=20;host.doHurtTarget(target);}host.getMoveControl().setWantedPosition(target.getX(),target.getY(),target.getZ(),1);}
            else if(distance<host.getAttributeValue(Attributes.FOLLOW_RANGE)*host.getAttributeValue(Attributes.FOLLOW_RANGE)){
                if(time<=0){
                    ++step;if(step==1){time=30;host.setAttacking(true);}else if(step<=4)time=6;else{time=50;step=0;host.setAttacking(false);}
                    if(step>1){host.level().levelEvent(null,1018,host.blockPosition(),0);host.level().addFreshEntity(new EnergyProjectile(host.level(),host,EnergyProjectile.Mode.ALIEN,6,1.5f,200,.05f,200,200,6,0,false,EnumBulletFirePos.CENTER));}
                }
                host.getLookControl().setLookAt(target,10,10);
            }else{host.getNavigation().stop();host.getMoveControl().setWantedPosition(target.getX(),target.getY(),target.getZ(),1);}
        }
    }
    @Override protected ResourceLocation getDefaultLootTable(){return ResourceLocation.parse("techguns:entities/ghastling");}
    @Override protected float getStandingEyeHeight(Pose pose,EntityDimensions dimensions){return 1.5f;}
    @Override protected SoundEvent getAmbientSound(){return SoundEvents.GHAST_AMBIENT;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.GHAST_HURT;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.GHAST_DEATH;}
    @Override protected float getSoundVolume(){return 10;}
    @Override public float getTotalArmorAgainstType(TGDamageSource source){return 0;}
    @Override public float getPenetrationResistance(TGDamageSource source){return 0;}
    @Override public float getWeaponPosX(){return 0;}@Override public float getWeaponPosY(){return 0;}@Override public float getWeaponPosZ(){return 0;}
}
