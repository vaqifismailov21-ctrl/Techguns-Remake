package techguns.entities.special;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.PushReaction;
import techguns.TGEntities;
import techguns.TGRadiationSystem;

public final class EntityRadiation extends Entity {
    public static final int RAD_INTERVAL = 20;
    private int age, duration, strength, outerStrength, originalStrength, originalOuterStrength;
    private float radius, innerRadius;
    public EntityRadiation(EntityType<? extends EntityRadiation> type, Level level) { super(type, level); noPhysics = true; }
    public EntityRadiation(Level level, double x, double y, double z, int duration, float innerRadius, int strength, float radius, int outerStrength) {
        this(TGEntities.RADIATION.get(), level); setPos(x, y, z); setRadiationValues(duration, innerRadius, strength, radius, outerStrength);
    }
    public EntityRadiation(Entity entity, int duration, float innerRadius, int strength, float radius, int outerStrength) {
        this(entity.level(), entity.getX(), entity.getY(), entity.getZ(), duration, innerRadius, strength, radius, outerStrength);
    }
    public EntityRadiation(BlockEntity block, int duration, float innerRadius, int strength, float radius, int outerStrength) {
        this(block.getLevel(), block.getBlockPos().getX() + 0.5, block.getBlockPos().getY() + 0.5,
                block.getBlockPos().getZ() + 0.5, duration, innerRadius, strength, radius, outerStrength);
    }
    public EntityRadiation setRadiationValues(int duration, float innerRadius, int strength, float radius, int outerStrength) {
        this.duration = duration; this.innerRadius = innerRadius; this.radius = radius;
        this.strength = originalStrength = strength; this.outerStrength = originalOuterStrength = outerStrength;
        return this;
    }
    @Override protected void defineSynchedData() {}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getInt("Age"); duration = tag.getInt("Duration"); radius = tag.getFloat("radius"); innerRadius = tag.getFloat("inner_radius");
        strength = tag.getShort("strength"); outerStrength = tag.getShort("strength_outer");
        originalStrength = tag.getShort("strength_original"); originalOuterStrength = tag.getShort("strength_outer_original");
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", age); tag.putInt("Duration", duration); tag.putFloat("radius", radius); tag.putFloat("inner_radius", innerRadius);
        tag.putShort("strength", (short) strength); tag.putShort("strength_outer", (short) outerStrength);
        tag.putShort("strength_original", (short) originalStrength); tag.putShort("strength_outer_original", (short) originalOuterStrength);
    }
    @Override public void tick() {
        super.tick(); age++;
        if (age > duration) { discard(); return; }
        if (!level().isClientSide && age % RAD_INTERVAL == 0) {
            if (age * 2 >= duration) {
                double factor = 1.0 - ((age - duration * 0.5) / (duration * 0.5));
                strength = Math.max((int) Math.round(originalStrength * factor), 0);
                outerStrength = Math.max((int) Math.round(originalOuterStrength * factor), 0);
            }
            TGRadiationSystem.applyRadToEntities(level(), getX(), getY(), getZ(), radius, RAD_INTERVAL + 2, strength, innerRadius, outerStrength);
        }
    }
    @Override public boolean isPickable() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public PushReaction getPistonPushReaction() { return PushReaction.IGNORE; }
}
