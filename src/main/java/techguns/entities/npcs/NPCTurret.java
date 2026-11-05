package techguns.entities.npcs;

import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import techguns.TGTurrets;
import techguns.api.npc.INPCTechgunsShooter;
import techguns.api.npc.ITGTurretDamage;
import techguns.api.npc.factions.ITGNpcTeam;
import techguns.api.npc.factions.TGNpcFaction;
import techguns.entities.ai.NpcRangedAttackGoal;
import techguns.factions.TGNpcFactions;
import techguns.items.guns.GenericGun;
import techguns.tileentities.TurretTileEnt;

/**
 * Port of the original NPCTurret: a stationary creature mounted on a turret base. It holds no items itself; its
 * weapon, armour and texture tier come from the base. Original AI: watch closest (80 blocks, ±20 vertical),
 * look idle, retaliate by faction/owner, attack monsters and, by base settings, animals and PvP targets.
 */
public class NPCTurret extends PathfinderMob implements RangedAttackMob, INPCTechgunsShooter, ITGNpcTeam, ITGTurretDamage {
    private static final EntityDataAccessor<Byte> TURRET_FACING=SynchedEntityData.defineId(NPCTurret.class,EntityDataSerializers.BYTE);
    private static final ResourceLocation[] TEXTURES=new ResourceLocation[5];
    static{for(int i=0;i<5;i++)TEXTURES[i]=ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/turret_base"+(i==0?"":String.valueOf(i+1))+".png");}

    protected TurretWatchClosestGoal aiWatch;
    protected RandomLookAroundGoal aiIdle;
    public TurretHurtByTargetGoal aiHurt;
    protected TurretNearestAttackableTargetGoal aiTarget;
    private Goal aiRangedAttack;

    public TurretTileEnt mountedTileEnt;
    public boolean active=true;

    public NPCTurret(EntityType<? extends NPCTurret> type,Level level){
        super(type,level);
        setAITasks();
        setNoGravity(true);
        setPersistenceRequired();
    }
    public NPCTurret(Level level,TurretTileEnt tile){
        this(TGTurrets.TURRET.get(),level);
        mountedTileEnt=tile;
        if(!level.isClientSide)setCombatTask();
    }
    public static AttributeSupplier.Builder attributes(){
        return PathfinderMob.createMobAttributes().add(Attributes.MOVEMENT_SPEED,0).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.MAX_HEALTH,30).add(Attributes.FOLLOW_RANGE,20);
    }
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(TURRET_FACING,(byte)Direction.UP.get3DDataValue());}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(TURRET_FACING.equals(key))refreshDimensions();}

    public static ResourceLocation getTexture(int tier){return TEXTURES[tier];}
    public ResourceLocation getTexture(){var tile=getTile();return getTexture(tile==null?0:tile.getTurretTier());}

    @Override protected float getStandingEyeHeight(Pose pose,EntityDimensions dimensions){return getTurretFacing()==Direction.DOWN?0.15f:0.75f;}

    public Direction getTurretFacing(){return Direction.from3DDataValue(entityData.get(TURRET_FACING));}
    public void setTurretFacing(Direction facing){entityData.set(TURRET_FACING,(byte)facing.get3DDataValue());refreshDimensions();}

    /** The base this turret stands on; on the client it is located from the synchronised position and facing. */
    public TurretTileEnt getTile(){
        if(mountedTileEnt!=null&&!mountedTileEnt.isRemoved())return mountedTileEnt;
        if(level().isClientSide&&level().getBlockEntity(basePos()) instanceof TurretTileEnt tile)return tile;
        return null;
    }
    protected BlockPos basePos(){
        int x=Mth.floor(getX()),z=Mth.floor(getZ());
        int y=(int)Math.round(getY())-1;
        if(getTurretFacing()==Direction.DOWN)y=(int)Math.round(getY())+1;
        return new BlockPos(x,y,z);
    }

    @Override public ItemStack getItemBySlot(EquipmentSlot slot){
        if(slot==EquipmentSlot.MAINHAND){var tile=getTile();return tile!=null?tile.getWeapon():ItemStack.EMPTY;}
        return super.getItemBySlot(slot);
    }
    @Override public void setItemSlot(EquipmentSlot slot,ItemStack stack){if(slot!=EquipmentSlot.MAINHAND)super.setItemSlot(slot,stack);}
    @Override public boolean canTakeItem(ItemStack stack){return false;}
    @Override public boolean canPickUpLoot(){return false;}

    @Override public int getArmorValue(){var tile=getTile();return tile!=null?tile.getTurretArmorValue():0;}

    /** Original onDeath only informs the base: no loot, no experience, no equipment drop. */
    @Override public void die(DamageSource source){if(mountedTileEnt!=null)mountedTileEnt.onTurretDeath();}
    @Override protected void dropCustomDeathLoot(DamageSource source,int looting,boolean recentlyHit){}
    @Override protected void dropEquipment(){}
    @Override public int getExperienceReward(){return 0;}
    @Override public boolean removeWhenFarAway(double distance){return false;}

    /**
     * 1.12 turned the body toward the head; the 1.20 control instead pulls a still head back to the body after half a second,
     * which made turrets fire toward their fixed front. The stand simply follows the gun.
     */
    @Override protected net.minecraft.world.entity.ai.control.BodyRotationControl createBodyControl(){
        return new net.minecraft.world.entity.ai.control.BodyRotationControl(this){
            @Override public void clientTick(){NPCTurret.this.yBodyRot=NPCTurret.this.yHeadRot;NPCTurret.this.setYRot(NPCTurret.this.yHeadRot);}
        };
    }
    public void setTileEnt(TurretTileEnt tile){mountedTileEnt=tile;setCombatTask();}

    @Override public void tick(){
        super.tick();
        setPos(xo,yo,zo);
        if(!level().isClientSide){
            if(mountedTileEnt==null){
                if(level().getBlockEntity(basePos()) instanceof TurretTileEnt tile){
                    tile.setMountedTurret(this);mountedTileEnt=tile;tile.needUpdate();setCombatTask();
                }else hurt(level().damageSources().fellOutOfWorld(),1.0f);
            }else if(mountedTileEnt.mountedTurret!=this)discard();
        }
    }

    @Override public void performRangedAttack(LivingEntity target,float distance){
        ItemStack weapon=getMainHandItem();
        if(weapon.isEmpty())return;
        if(weapon.getItem() instanceof GenericGun gun&&mountedTileEnt!=null&&mountedTileEnt.consumeAmmo())gun.fireWeaponFromNPC(this,1.0f,1.0f);
    }

    public void destroy(){setHealth(0);discard();}

    public void setAITasks(){
        if(aiWatch==null)aiWatch=new TurretWatchClosestGoal(this,80.0f,20.0d);
        if(aiIdle==null)aiIdle=new RandomLookAroundGoal(this);
        if(aiHurt==null)aiHurt=new TurretHurtByTargetGoal(this);
        if(aiTarget==null)aiTarget=new TurretNearestAttackableTargetGoal(this,this::isValidTarget,20.0d);
        goalSelector.addGoal(2,aiWatch);
        goalSelector.addGoal(3,aiIdle);
        targetSelector.addGoal(1,aiHurt);
        targetSelector.addGoal(2,aiTarget);
    }
    public void disable(){
        active=false;
        goalSelector.removeGoal(aiWatch);goalSelector.removeGoal(aiIdle);
        targetSelector.removeGoal(aiHurt);targetSelector.removeGoal(aiTarget);
        if(aiRangedAttack!=null)goalSelector.removeGoal(aiRangedAttack);
    }
    /** Sets the combat goal for the gun in the base, as in the original setCombatTask. */
    public void setCombatTask(){
        if(level()==null||level().isClientSide)return;
        if(aiRangedAttack!=null)goalSelector.removeGoal(aiRangedAttack);
        ItemStack stack=getMainHandItem();
        if(stack.getItem() instanceof GenericGun gun){
            aiRangedAttack=new NpcRangedAttackGoal<>(this,gun);
            goalSelector.addGoal(1,aiRangedAttack);
            getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(gun.aiAttackRange);
        }
    }

    /** Original target predicate of TurretEntityAINearestAttackableTarget. */
    public boolean isValidTarget(LivingEntity entity){
        if(entity instanceof Enemy)return true;
        if(mountedTileEnt!=null){
            byte pvp=mountedTileEnt.getPvpSetting();UUID owner=mountedTileEnt.getOwner();
            if(pvp!=0&&owner!=null){
                if(entity instanceof NPCTurret other){
                    if(other.mountedTileEnt!=null)return TGNpcFactions.shouldAttack(owner,other.mountedTileEnt.getOwner(),pvp);
                }else if(entity instanceof Player player){
                    UUID id=player.getGameProfile().getId();
                    if(id!=null){if(owner.equals(id))return false;return TGNpcFactions.shouldAttack(owner,id,pvp);}
                }
            }
            if(mountedTileEnt.attackAnimals&&isLegacyAnimal(entity)&&!(entity instanceof NPCTurret)){
                if(entity instanceof TamableAnimal tameable)return !tameable.isTame();
                if(entity instanceof Horse horse)return !horse.isTamed();
                // 1.12 AbstractHorse did not implement IEntityOwnable; only EntityHorse was checked above.
                if(entity instanceof OwnableEntity ownable&&!(entity instanceof AbstractHorse))return ownable.getOwnerUUID()==null;
                return true;
            }
        }
        return false;
    }
    /** Modern equivalents of the 1.12 IAnimals marker (animals, ambient, water mobs and golems). */
    protected static boolean isLegacyAnimal(LivingEntity entity){return entity instanceof Animal||entity instanceof AmbientCreature||entity instanceof WaterAnimal||entity instanceof AbstractGolem;}

    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putByte("turretFacing",(byte)getTurretFacing().get3DDataValue());}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);setTurretFacing(Direction.from3DDataValue(tag.getByte("turretFacing")));}

    @Override public float getWeaponPosX(){return 0.05f;}
    @Override public float getWeaponPosY(){return 0.7f;}
    @Override public float getWeaponPosZ(){return 0.8f;}
    @Override public TGNpcFaction getTGFaction(){return TGNpcFaction.TURRET;}

    /** Original TurretEntityAIWatchClosest: EntityAIWatchClosest with a separate vertical search range. */
    public static final class TurretWatchClosestGoal extends Goal {
        private final Mob entity;private final float maxDistance;private final double yOffset;private final float chance=0.02f;
        private Entity closest;private int lookTime;
        public TurretWatchClosestGoal(Mob entity,float maxDistance,double yOffset){this.entity=entity;this.maxDistance=maxDistance;this.yOffset=yOffset;setFlags(EnumSet.of(Flag.LOOK));}
        @Override public boolean canUse(){
            if(entity.getRandom().nextFloat()>=chance)return false;
            if(entity.getTarget()!=null)closest=entity.getTarget();
            closest=entity.level().getNearestEntity(LivingEntity.class,net.minecraft.world.entity.ai.targeting.TargetingConditions.forNonCombat().range(Double.MAX_VALUE).ignoreLineOfSight(),entity,entity.getX(),entity.getY(),entity.getZ(),entity.getBoundingBox().inflate(maxDistance,yOffset,maxDistance));
            return closest!=null;
        }
        @Override public boolean canContinueToUse(){return closest.isAlive()&&entity.distanceToSqr(closest)<=(double)(maxDistance*maxDistance)&&lookTime>0;}
        @Override public void start(){lookTime=40+entity.getRandom().nextInt(40);}
        @Override public void stop(){closest=null;}
        @Override public void tick(){entity.getLookControl().setLookAt(closest.getX(),closest.getY()+closest.getEyeHeight(),closest.getZ(),entity.getMaxHeadYRot(),entity.getMaxHeadXRot());--lookTime;}
    }

    /** Original EntityAIHurtByTargetTGFactions for turrets (owner/faction aware, no call for help). */
    public static final class TurretHurtByTargetGoal extends HurtByTargetGoal {
        private final NPCTurret turret;
        public TurretHurtByTargetGoal(NPCTurret turret){super(turret);this.turret=turret;}
        @Override public boolean canUse(){
            LivingEntity attacker=turret.getLastHurtByMob();
            if(attacker instanceof Player player&&turret.mountedTileEnt!=null&&turret.mountedTileEnt.getOwner()!=null){
                // TGNpcFactions.isHostile(owner,player) without a team integration: everyone except the owner.
                if(turret.mountedTileEnt.getOwner().equals(player.getGameProfile().getId()))return false;
            }else if(attacker instanceof ITGNpcTeam team&&!TGNpcFactions.isHostile(turret.getTGFaction(),team.getTGFaction()))return false;
            return super.canUse();
        }
    }

    /** Original TurretEntityAINearestAttackableTarget: check every tick, require sight, ±yOffset vertical search area. */
    public static final class TurretNearestAttackableTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {
        private final double yOffset;
        public TurretNearestAttackableTargetGoal(NPCTurret turret,Predicate<LivingEntity> selector,double yOffset){super(turret,LivingEntity.class,0,true,false,selector);this.yOffset=yOffset;}
        @Override protected AABB getTargetSearchArea(double distance){return mob.getBoundingBox().inflate(distance,yOffset,distance);}
    }
}
