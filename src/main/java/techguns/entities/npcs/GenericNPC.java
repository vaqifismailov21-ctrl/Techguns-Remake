package techguns.entities.npcs;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import techguns.*;
import techguns.api.npc.*;
import techguns.api.npc.factions.*;
import techguns.damagesystem.*;
import techguns.entities.ai.NpcRangedAttackGoal;
import techguns.items.guns.GenericGun;

public class GenericNPC extends Monster implements RangedAttackMob,INPCTechgunsShooter,INpcTGDamageSystem,ITGNpcTeam {
    protected float armor,penetrationResistance;
    protected boolean hasAimedBowAnim=true;
    private Goal combatGoal;
    public int weaponRecoilTicks,weaponMuzzleTicks;
    public GenericNPC(EntityType<? extends GenericNPC> type,Level level){super(type,level);}
    public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.ATTACK_DAMAGE,2).add(Attributes.FOLLOW_RANGE,16);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(1,new FloatGoal(this));
        goalSelector.addGoal(5,new RandomStrollGoal(this,1));
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,8));
        goalSelector.addGoal(6,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this){@Override public boolean canUse(){var attacker=mob.getLastHurtByMob();return attacker!=null&&techguns.factions.TGNpcFactions.canRetaliate(mob,attacker)&&super.canUse();}});
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    public void setCombatTask(){
        if(level().isClientSide)return;
        if(combatGoal!=null)goalSelector.removeGoal(combatGoal);
        combatGoal=getMainHandItem().getItem() instanceof GenericGun gun?new NpcRangedAttackGoal<>(this,gun):new MeleeAttackGoal(this,1.2,false);
        goalSelector.addGoal(4,combatGoal);
    }
    @Override public void setItemSlot(EquipmentSlot slot,ItemStack stack){super.setItemSlot(slot,stack);if(slot==EquipmentSlot.MAINHAND)setCombatTask();}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,DifficultyInstance difficulty,MobSpawnType reason,@Nullable SpawnGroupData data){
        data=super.finalizeSpawn(level,difficulty,reason,data);addRandomArmor(Math.round(difficulty.getSpecialMultiplier()*3));setCombatTask();setCanPickUpLoot(false);return data;
    }
    public void onSpawnByManager(int difficulty){setCanPickUpLoot(false);addRandomArmor(difficulty);setCombatTask();}
    protected void addRandomArmor(int difficulty){setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(TGuns.revolver.get()));}
    protected void setTGArmorStats(float armor,float penetration){this.armor=armor;this.penetrationResistance=penetration;}
    @Override public float getTotalArmorAgainstType(TGDamageSource source){return DamageSystem.getArmorAgainstDamageTypeDefault(this,armor,source.damageType);}
    @Override public float getPenetrationResistance(TGDamageSource source){return penetrationResistance;}
    @Override public void performRangedAttack(LivingEntity target,float distance){
        if(getMainHandItem().getItem() instanceof GenericGun gun){
            float damage=1,accuracy=1;
            switch(level().getDifficulty()){case EASY-> {damage=.6f;accuracy=1.3f;}case NORMAL->{damage=.8f;accuracy=1.15f;}default->{}}
            gun.fireWeaponFromNPC(this,damage,accuracy);
        }
    }
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);setCombatTask();}
    @Override public TGNpcFaction getTGFaction(){return TGNpcFaction.HOSTILE;}
    @Override public float getWeaponPosX(){return 0;}
    @Override public float getWeaponPosY(){return 0;}
    @Override public float getWeaponPosZ(){return 0;}
    public boolean getHasAimedBowAnim(){return hasAimedBowAnim;}
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound(){return net.minecraft.sounds.SoundEvents.VILLAGER_AMBIENT;}
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source){return net.minecraft.sounds.SoundEvents.VILLAGER_HURT;}
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound(){return net.minecraft.sounds.SoundEvents.VILLAGER_DEATH;}
    @Override public void aiStep(){super.aiStep();if(weaponRecoilTicks>0)weaponRecoilTicks--;if(weaponMuzzleTicks>0)weaponMuzzleTicks--;}
    public boolean isSwingingArms(){return weaponRecoilTicks>0;}
    protected boolean shouldBurnInDay(){return false;}
    public static ItemStack camo(Item item,int index){var stack=new ItemStack(item);techguns.util.TGItemNbt.putByte(stack, "camo",(byte)index);return stack;}
    public int randomCamo(Item item){return item instanceof techguns.items.armors.ICamoChangeable c?random.nextInt(c.getCamoCount()):0;}
}
