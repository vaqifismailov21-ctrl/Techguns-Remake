package techguns.entities.npcs;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import techguns.TGSounds;
import techguns.api.npc.INpcTGDamageSystem;
import techguns.api.npc.factions.*;
import techguns.damagesystem.TGDamageSource;

/** Original always-hostile spider AI, bespoke resistances and bite animation. */
public final class AlienBug extends Spider implements ITGNpcTeam,INpcTGDamageSystem {
    private int attackTimer;
    public AlienBug(EntityType<? extends AlienBug> type,Level level){super(type,level);}
    public static AttributeSupplier.Builder attributes(){return Spider.createAttributes().add(Attributes.ATTACK_DAMAGE,4).add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,1);}
    @Override protected ResourceLocation getDefaultLootTable(){return BuiltInLootTables.EMPTY;}
    @Override protected void registerGoals(){
        goalSelector.addGoal(1,new FloatGoal(this));goalSelector.addGoal(3,new LeapAtTargetGoal(this,.4f));
        goalSelector.addGoal(4,new MeleeAttackGoal(this,1,true){
            @Override public boolean canContinueToUse(){if(mob.getLightLevelDependentMagicValue()>=.5f&&mob.getRandom().nextInt(100)==0){mob.setTarget(null);return false;}return super.canContinueToUse();}
            @Override protected double getAttackReachSqr(LivingEntity target){return 4+target.getBbWidth();}
        });
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.8));goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(6,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new BugTarget<>(this,Player.class));targetSelector.addGoal(3,new BugTarget<>(this,IronGolem.class));
    }
    private static final class BugTarget<T extends LivingEntity> extends NearestAttackableTargetGoal<T>{
        BugTarget(AlienBug bug,Class<T> target){super(bug,target,true);}
        @Override public void start(){super.start();mob.playSound(TGSounds.ALIENBUG_AGGRO.get(),1,1);}
    }
    @Override public boolean doHurtTarget(Entity target){boolean hit=super.doHurtTarget(target);attackTimer=10;if(!level.isClientSide){playSound(TGSounds.ALIENBUG_BITE.get(),2,1);level.broadcastEntityEvent(this,(byte)4);}return hit;}
    public int getAttackTimer(){return attackTimer;}
    @Override public void aiStep(){super.aiStep();if(attackTimer>0)attackTimer--;}
    @Override public void handleEntityEvent(byte event){if(event==4)attackTimer=10;else super.handleEntityEvent(event);}
    @Override protected SoundEvent getAmbientSound(){return TGSounds.ALIENBUG_IDLE.get();}
    @Override protected SoundEvent getHurtSound(DamageSource source){return TGSounds.ALIENBUG_HURT.get();}
    @Override protected SoundEvent getDeathSound(){return TGSounds.ALIENBUG_DEATH.get();}
    @Override protected void playStepSound(BlockPos pos,BlockState state){playSound(TGSounds.ALIENBUG_STEP.get(),.15f,1);}
    @Override public TGNpcFaction getTGFaction(){return TGNpcFaction.HOSTILE;}
    @Override public float getTotalArmorAgainstType(TGDamageSource source){return switch(source.damageType){case ENERGY,EXPLOSION,ICE,LIGHTNING->5;case PHYSICAL,PROJECTILE->10;case POISON,RADIATION->20;default->0;};}
    @Override public float getPenetrationResistance(TGDamageSource source){return 0;}
}
