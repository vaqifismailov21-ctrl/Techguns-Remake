package techguns.entities.projectiles;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import techguns.network.NetworkHooks;
import techguns.TGEntities;
import techguns.TGPackets;
import techguns.damagesystem.DamageSystem;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;

/** Original bullet movement/collision/damage, adapted to the current entity and ray-tracing APIs.
 * NPC faction/weapon offsets and the particle system are integrated with their respective later ports.
 */
public class GenericProjectile extends Entity implements IEntityWithComplexSpawn {
    protected float damage, damageDropStart, damageDropEnd, damageMin, penetration, radius;
    public float speed = 1;
    protected int ticksToLive = 100, lifetime = 100;
    protected LivingEntity shooter;
    private UUID shooterUuid;
    protected boolean silenced, blockdamage, posInitialized;
    protected double startX, startY, startZ, gravity;

    public GenericProjectile(EntityType<? extends GenericProjectile> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }
    public GenericProjectile(Level level, LivingEntity shooter, float damage, float speed, int ttl, float spread,
                             float dropStart, float dropEnd, float minDamage, float penetration, boolean blockDamage, EnumBulletFirePos firePos) {
        this(TGEntities.GENERIC_PROJECTILE.get(), level);
        init(shooter, damage, speed, ttl, spread, dropStart, dropEnd, minDamage, penetration, blockDamage, firePos);
    }
    protected void init(LivingEntity shooter, float damage, float speed, int ttl, float spread,
                        float dropStart, float dropEnd, float minDamage, float penetration, boolean blockDamage, EnumBulletFirePos firePos) {
        this.shooter = shooter; shooterUuid = shooter == null ? null : shooter.getUUID();
        if (shooter == null) throw new IllegalArgumentException("Use the entity-type constructor and configure a position for a shooterless bullet");
        float yaw = shooter.getYHeadRot() + (float) (spread - 2 * random.nextDouble() * spread) * 40;
        float pitch = shooter.getXRot() + (float) (spread - 2 * random.nextDouble() * spread) * 40;
        double x = shooter.getX(), y = shooter.getEyeY() - 0.10000000149011612, z = shooter.getZ();
        float sideOffset=.16f;
        if(shooter instanceof techguns.api.npc.INPCTechgunsShooter npc){sideOffset+=npc.getBulletOffsetSide();y+=npc.getBulletOffsetHeight();}
        double sideX = Mth.cos(yaw / 180 * (float) Math.PI) * sideOffset;
        double sideZ = Mth.sin(yaw / 180 * (float) Math.PI) * sideOffset;
        if (firePos == EnumBulletFirePos.RIGHT) { x -= sideX; z -= sideZ; }
        else if (firePos == EnumBulletFirePos.LEFT) { x += sideX; z += sideZ; }
        moveTo(x, y, z, yaw, pitch);
        float f = 0.4F;
        shoot(-Mth.sin(yaw / 180 * (float) Math.PI) * Mth.cos(pitch / 180 * (float) Math.PI) * f,
                -Mth.sin(pitch / 180 * (float) Math.PI) * f,
                Mth.cos(yaw / 180 * (float) Math.PI) * Mth.cos(pitch / 180 * (float) Math.PI) * f, 1.5F, 1);
        // The original invokes Vec3d.normalize() without assigning its result. Keep its 1.5 velocity factor.
        setDeltaMovement(getDeltaMovement().scale(speed));
        configure(damage, speed, ttl, dropStart, dropEnd, minDamage, penetration, blockDamage);
    }
    public void configure(float damage, float speed, int ttl, float dropStart, float dropEnd, float minDamage, float penetration, boolean blockDamage) {
        this.damage = damage; this.speed = speed; ticksToLive = lifetime = ttl;
        damageDropStart = dropStart; damageDropEnd = dropEnd; damageMin = minDamage;
        this.penetration = penetration; blockdamage = blockDamage;
    }
    public LivingEntity getShooter() {
        if (shooter == null && shooterUuid != null && level() instanceof ServerLevel server && server.getEntity(shooterUuid) instanceof LivingEntity living) shooter = living;
        return shooter;
    }
    protected void setShooter(LivingEntity owner){shooter=owner;shooterUuid=owner==null?null:owner.getUUID();}
    public int getTicksToLive() { return ticksToLive; }
    public GenericProjectile setSilenced() { silenced = true; return this; }
    public GenericProjectile setProjectileGravity(double value) { gravity = value; return this; }
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        float length = Mth.sqrt((float) (x * x + y * y + z * z));
        if (length == 0) throw new IllegalArgumentException("Bullet direction must be nonzero");
        double vx = (x / length + random.nextGaussian() * 0.007499999832361937 * inaccuracy) * velocity;
        double vy = (y / length + random.nextGaussian() * 0.007499999832361937 * inaccuracy) * velocity;
        double vz = (z / length + random.nextGaussian() * 0.007499999832361937 * inaccuracy) * velocity;
        setDeltaMovement(vx, vy, vz);
        setYRot((float) (Mth.atan2(vx, vz) * 180 / Math.PI));
        setXRot((float) (Mth.atan2(vy, Math.sqrt(vx * vx + vz * vz)) * 180 / Math.PI));
        yRotO = getYRot(); xRotO = getXRot();
    }
    protected void initStartPos() { startX = getX(); startY = getY(); startZ = getZ(); posInitialized = true; }
    @Override public void tick() {
        if (!posInitialized && !level().isClientSide) initStartPos();
        xo = getX(); yo = getY(); zo = getZ();
        super.tick();
        getShooter();
        Vec3 motion = getDeltaMovement();
        if (xRotO == 0 && yRotO == 0) {
            setYRot((float) (Mth.atan2(motion.x, motion.z) * 180 / Math.PI));
            setXRot((float) (Mth.atan2(motion.y, motion.horizontalDistance()) * 180 / Math.PI));
            yRotO = getYRot(); xRotO = getXRot();
        }
        ticksToLive--;
        Vec3 start = position(), end = start.add(motion);
        HitResult hit = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) end = hit.getLocation();
        EntityHitResult entityHit = findEntityOnPath(start, end);
        if (entityHit != null) hit = entityHit;
        if (hit instanceof EntityHitResult entity && entity.getEntity() instanceof Player victim
                && shooter instanceof Player attacker && !attacker.canHarmPlayer(victim)) hit = null;
        if (hit != null && hit.getType() != HitResult.Type.MISS) onHit(hit);

        Vec3 next = position().add(motion);
        float yaw = (float) (Mth.atan2(motion.x, motion.z) * 180 / Math.PI);
        float pitch = (float) (Mth.atan2(motion.y, motion.horizontalDistance()) * 180 / Math.PI);
        if (pitch - xRotO < -180) xRotO -= 360;
        if (pitch - xRotO >= 180) xRotO += 360;
        if (yaw - yRotO < -180) yRotO -= 360;
        if (yaw - yRotO >= 180) yRotO += 360;
        setXRot(xRotO + (pitch - xRotO) * 0.2F); setYRot(yRotO + (yaw - yRotO) * 0.2F);
        float drag = inWaterUpdateBehaviour(0.99F);
        setDeltaMovement(motion.scale(drag).add(0, -gravity, 0));
        setPos(next);
        checkInsideBlocks();
        if (ticksToLive <= 0) discard();
    }
    protected float inWaterUpdateBehaviour(float drag) {
        if (isInWater()) {
            var motion = getDeltaMovement();
            for (int i = 0; i < 4; i++) level().addParticle(net.minecraft.core.particles.ParticleTypes.BUBBLE,
                    getX() - motion.x * 0.25, getY() - motion.y * 0.25, getZ() - motion.z * 0.25, motion.x, motion.y, motion.z);
            drag = 0.85F;
        }
        if (isInWaterRainOrBubble()) clearFire();
        return drag;
    }
    @Nullable protected EntityHitResult findEntityOnPath(Vec3 start, Vec3 end) {
        Entity closest = null; Vec3 point = null; double distance = 0;
        var entities = level().getEntities(this, getBoundingBox().expandTowards(getDeltaMovement()).inflate(1),
                entity -> !entity.isSpectator() && entity.isAlive() && entity.isPickable());
        for (Entity entity : entities) if (entity != shooter) {
            var intersection = entity.getBoundingBox().inflate(0.30000001192092896).clip(start, end);
            if (intersection.isPresent()) {
                double candidate = start.distanceToSqr(intersection.get());
                if (candidate < distance || distance == 0) { closest = entity; point = intersection.get(); distance = candidate; }
            }
        }
        return closest == null ? null : new EntityHitResult(closest, point);
    }
    protected void onHit(HitResult hit) {
        if (hit instanceof EntityHitResult entityHit && !level().isClientSide) {
            Entity target = entityHit.getEntity();
            TGDamageSource source = getProjectileDamageSource();
            if (target instanceof LivingEntity living) {
                float amount = DamageSystem.getDamageFactor(shooter, living) * getDamage();
                if (amount > 0) {
                    if (source.knockbackMultiplier == 0) living.hurt(TGDamageSource.getKnockbackDummyDmgSrc(this, shooter), 0.01F);
                    living.hurt(source, amount);
                    if (source.wasSuccessful()) onHitEffect(living, entityHit);
                }
            } else target.hurt(source, getDamage());
            discard();
        }
        if (hit instanceof BlockHitResult blockHit) { hitBlock(blockHit); discard(); }
    }
    protected void onHitEffect(LivingEntity target, HitResult hit) {}
    protected void hitBlock(BlockHitResult hit) {
        if (!level().isClientSide) {
            SoundType sound = level().getBlockState(hit.getBlockPos()).getSoundType(level(), hit.getBlockPos(), this);
            TGPackets.sendBulletImpact(this, hit, sound);
        }
    }
    protected TGDamageSource getProjectileDamageSource() {
        TGDamageSource source = TGDamageSource.causeBulletDamage(this, shooter, DeathType.GORE);
        source.armorPenetration = penetration; source.setNoKnockback(); return source;
    }
    public void shiftForward(float factor) { setPos(position().add(getDeltaMovement().scale(factor))); }
    protected double getDistanceTravelled() { return new Vec3(startX, startY, startZ).distanceTo(position()); }
    public float damageAtDistance(double distance) {
        if (damageDropEnd == 0 || distance <= damageDropStart) return damage;
        if (distance > damageDropEnd) return damageMin;
        float factor = 1 - (float) ((distance - damageDropStart) / (damageDropEnd - damageDropStart));
        return damageMin + (damage - damageMin) * factor;
    }
    protected float getDamage() { return damageAtDistance(getDistanceTravelled()); }
    // 1.21.1: eye height 0.25 is set on the EntityType builders (TGEntities).
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("damage", damage); tag.putFloat("speed", speed);
        tag.putShort("ticksToLive", (short) ticksToLive); tag.putShort("lifetime", (short) lifetime);
        tag.putShort("damageDropStart", (short) damageDropStart); tag.putShort("damageDropEnd", (short) damageDropEnd);
        tag.putFloat("damageMin", damageMin); if (gravity != 0) tag.putFloat("gravity", (float) gravity);
        // Additional state prevents a chunk reload from losing attribution or restarting damage falloff.
        if (shooterUuid != null) tag.putUUID("shooter", shooterUuid);
        tag.putFloat("penetration", penetration); tag.putBoolean("blockdamage", blockdamage); tag.putBoolean("silenced", silenced);
        if (posInitialized) { tag.putDouble("startX", startX); tag.putDouble("startY", startY); tag.putDouble("startZ", startZ); }
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        damage = tag.getFloat("damage"); speed = tag.getFloat("speed");
        ticksToLive = tag.getShort("ticksToLive"); lifetime = tag.getShort("lifetime");
        damageDropStart = tag.getShort("damageDropStart"); damageDropEnd = tag.getShort("damageDropEnd"); damageMin = tag.getFloat("damageMin");
        gravity = tag.getFloat("gravity"); penetration = tag.getFloat("penetration"); blockdamage = tag.getBoolean("blockdamage"); silenced = tag.getBoolean("silenced");
        shooterUuid = tag.hasUUID("shooter") ? tag.getUUID("shooter") : null;
        posInitialized = tag.contains("startX");
        if (posInitialized) { startX = tag.getDouble("startX"); startY = tag.getDouble("startY"); startZ = tag.getDouble("startZ"); }
    }
    @Override public void writeSpawnData(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(speed); var motion = getDeltaMovement(); buffer.writeDouble(motion.x); buffer.writeDouble(motion.y); buffer.writeDouble(motion.z);
        buffer.writeVarInt(shooter == null ? 0 : shooter.getId());
        buffer.writeDouble(gravity);buffer.writeVarInt(ticksToLive);
    }
    @Override public void readSpawnData(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        speed = buffer.readFloat(); setDeltaMovement(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        int owner = buffer.readVarInt(); if (level().getEntity(owner) instanceof LivingEntity living) shooter = living;
        gravity=buffer.readDouble();ticksToLive=lifetime=buffer.readVarInt();
    }
    @Override public void lerpMotion(double x, double y, double z) { /* Original bullet ignores post-spawn velocity updates. */ }
    public static void burnBlocks(Level level, BlockHitResult hit, double chance) {
        if (level.isClientSide || level.random.nextDouble() > chance) return;
        BlockPos target = hit.getBlockPos().relative(hit.getDirection());
        if (level.isEmptyBlock(target)) {
            if (hit.getDirection() == net.minecraft.core.Direction.UP && level.getBlockState(hit.getBlockPos()).is(Blocks.FARMLAND))
                level.setBlockAndUpdate(hit.getBlockPos(), Blocks.DIRT.defaultBlockState());
            level.setBlockAndUpdate(target, Blocks.FIRE.defaultBlockState());
        }
    }
}
