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
        goalSelector.addGoal(3,new OpenDoorGoal(this,true));
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
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,DifficultyInstance difficulty,MobSpawnType reason,@Nullable SpawnGroupData data,@Nullable CompoundTag tag){
        data=super.finalizeSpawn(level,difficulty,reason,data,tag);equip(Math.round(difficulty.getSpecialMultiplier()*3));setCombatTask();setCanPickUpLoot(false);return data;
    }
    public void onSpawnByManager(int difficulty){setCanPickUpLoot(false);equip(difficulty);setCombatTask();}
    /** Random gear, unless armour was already given (an NPC set up through NBT keeps its own); then the drop settings. */
    private void equip(int difficulty){
        boolean dressed=false;
        for(var slot:EquipmentSlot.values())if(slot.getType()==EquipmentSlot.Type.ARMOR&&!getItemBySlot(slot).isEmpty())dressed=true;
        if(!dressed)addRandomArmor(difficulty);
        if(TGConfig.disableGunDrops)setDropChance(EquipmentSlot.MAINHAND,0);
        if(TGConfig.disableArmorDrops)for(var slot:EquipmentSlot.values())if(slot.getType()==EquipmentSlot.Type.ARMOR)setDropChance(slot,0);
    }
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
    /** Opens doors on its way; never walks off a ledge higher than one block. */
    @Override protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level){
        var nav=new net.minecraft.world.entity.ai.navigation.GroundPathNavigation(this,level);nav.setCanOpenDoors(true);nav.setCanPassDoors(true);nav.setCanFloat(true);return nav;
    }
    @Override public int getMaxFallDistance(){return 1;}
    /** NPCs of one faction do not hurt each other (stray bullets, rocket splash) or themselves. */
    @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source,float amount){
        if(friendly(source.getEntity())||friendly(source.getDirectEntity()))return false;
        return super.hurt(source,amount);
    }
    protected boolean friendly(@Nullable Entity entity){
        if(entity==null)return false;if(entity==this)return true;
        if(entity instanceof net.minecraft.world.entity.projectile.Projectile p&&p.getOwner()!=null&&p.getOwner()!=entity)return friendly(p.getOwner());
        if(entity instanceof techguns.entities.projectiles.GenericProjectile p&&p.getShooter()!=null&&p.getShooter()!=entity)return friendly(p.getShooter());
        return entity instanceof ITGNpcTeam team&&team.getTGFaction()==getTGFaction();
    }
    /** Living soldiers hear players: running, falling and shooting carry far, sneaking hardly at all. */
    protected boolean hears(){return false;}
    private int hearingCooldown;
    private void listen(){
        if(--hearingCooldown>0)return;
        var target=getTarget();
        if(target!=null){if(!validTarget(target))setTarget(null);else return;}
        Player loudest=null;float best=0;
        for(var player:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(24,10,24),this::validTarget)){
            float loud=loudness(player);if(loud<=0)continue;double range=4+loud*16;
            if(distanceToSqr(player)<=range*range&&loud>best){best=loud;loudest=player;}
        }
        if(loudest!=null){setTarget(loudest);hearingCooldown=40;}
    }
    private boolean validTarget(LivingEntity target){return target.isAlive()&&!(target instanceof Player p&&(p.isCreative()||p.isSpectator()));}
    private static float loudness(Player player){
        if(player.isSilent())return 0;
        float loud=0;double moved=Math.abs(player.walkDist-player.walkDistO);
        if(moved>0.02)loud+=player.isSprinting()?1.2f:player.isShiftKeyDown()?0.1f:0.6f;
        if(!player.onGround()&&player.fallDistance>1)loud+=0.8f;
        var data=techguns.capabilities.TGExtendedPlayer.find(player).orElse(null);
        if(data!=null){
            if(player.getMainHandItem().getItem() instanceof GenericGun)loud+=gunNoise(data.fireDelayMainhand);
            if(player.getOffhandItem().getItem() instanceof GenericGun)loud+=gunNoise(data.fireDelayOffhand)*0.8f;
        }
        return loud;
    }
    private static float gunNoise(int delay){return delay<=0?0:net.minecraft.util.Mth.clamp(0.4f+delay/12f,0.4f,1.6f);}
    @Override public TGNpcFaction getTGFaction(){return TGNpcFaction.HOSTILE;}
    @Override public float getWeaponPosX(){return 0;}
    @Override public float getWeaponPosY(){return 0;}
    @Override public float getWeaponPosZ(){return 0;}
    public boolean getHasAimedBowAnim(){return hasAimedBowAnim;}
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound(){return net.minecraft.sounds.SoundEvents.VILLAGER_AMBIENT;}
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source){return net.minecraft.sounds.SoundEvents.VILLAGER_HURT;}
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound(){return net.minecraft.sounds.SoundEvents.VILLAGER_DEATH;}
    @Override public void aiStep(){super.aiStep();if(!level().isClientSide&&hears()&&(tickCount&3)==0)listen();if(weaponRecoilTicks>0)weaponRecoilTicks--;if(weaponMuzzleTicks>0)weaponMuzzleTicks--;}
    public boolean isSwingingArms(){return weaponRecoilTicks>0;}
    protected boolean shouldBurnInDay(){return false;}
    public static ItemStack camo(Item item,int index){var stack=new ItemStack(item);stack.getOrCreateTag().putByte("camo",(byte)index);return stack;}
    public int randomCamo(Item item){return item instanceof techguns.items.armors.ICamoChangeable c?random.nextInt(c.getCamoCount()):0;}
}
