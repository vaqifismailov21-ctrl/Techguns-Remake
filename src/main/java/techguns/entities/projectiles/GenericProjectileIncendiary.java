package techguns.entities.projectiles;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import techguns.TGEntities;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;

public class GenericProjectileIncendiary extends GenericProjectile {
    protected boolean showFireTrail;
    private boolean trailStarted;
    @Override public void tick() {
        if(level().isClientSide&&showFireTrail&&!trailStarted){trailStarted=true;techguns.client.particle.WeaponEffects.attach("IncendiaryShotgunTrail",this);}
        super.tick();
    }
    public GenericProjectileIncendiary(EntityType<? extends GenericProjectileIncendiary> type, Level level) { super(type, level); }
    public GenericProjectileIncendiary(Level level, LivingEntity shooter, float damage, float speed, int ttl, float spread,
                                      float dropStart, float dropEnd, float minDamage, float penetration, boolean blockDamage, EnumBulletFirePos firePos, boolean fireTrail) {
        this(TGEntities.INCENDIARY_PROJECTILE.get(), level);
        init(shooter, damage * 1.1F, speed, ttl, spread, dropStart, dropEnd, minDamage * 1.1F, penetration, blockDamage, firePos);
        showFireTrail = fireTrail;
    }
    @Override protected TGDamageSource getProjectileDamageSource() {
        var source = TGDamageSource.causeFireDamage(this, shooter, DeathType.DEFAULT);
        source.armorPenetration = penetration; source.setNoKnockback(); return source;
    }
    @Override protected void onHitEffect(LivingEntity target, HitResult hit) { if (!target.fireImmune()) target.igniteForSeconds(3); }
    @Override protected void hitBlock(BlockHitResult hit) {
        if (blockdamage) burnBlocks(level(), hit, damage / 40F);
        super.hitBlock(hit);
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putBoolean("fireTrail", showFireTrail); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); showFireTrail = tag.getBoolean("fireTrail"); }
    @Override public void writeSpawnData(net.minecraft.network.RegistryFriendlyByteBuf buffer) { super.writeSpawnData(buffer); buffer.writeBoolean(showFireTrail); }
    @Override public void readSpawnData(net.minecraft.network.RegistryFriendlyByteBuf buffer) { super.readSpawnData(buffer); showFireTrail = buffer.readBoolean(); }
}
