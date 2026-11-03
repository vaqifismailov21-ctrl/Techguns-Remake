package techguns.entities.projectiles;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import techguns.*;
import techguns.blocks.BioBlobBlock;
import techguns.tileentities.BioBlobBlockEntity;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;
public final class BioGunProjectile extends GenericProjectile {
    public int power=1;private boolean trail;
    public BioGunProjectile(EntityType<? extends BioGunProjectile> type,Level level) {super(type,level);}
    public BioGunProjectile(Level level,LivingEntity owner,float damage,float speed,int ttl,float spread,float start,float end,float minimum,float penetration,boolean blocks,EnumBulletFirePos side,int power) {
        this(TGEntities.BIO.get(),level);init(owner,damage,speed,ttl,spread,start,end,minimum,penetration,blocks,side);this.power=power;gravity=0.01;
    }
    @Override public void tick() {if(level().isClientSide&&!trail){trail=true;techguns.client.particle.WeaponEffects.attach("BioGunTrail",this);}super.tick();if(isInWater())discard();}
    @Override protected TGDamageSource getProjectileDamageSource() {var source=TGDamageSource.causePoisonDamage(this,shooter,DeathType.BIO);source.goreChance=1;source.armorPenetration=penetration;source.setNoKnockback();return source;}
    @Override protected void onHitEffect(LivingEntity target,HitResult hit) {target.addEffect(new MobEffectInstance(MobEffects.POISON,100,3,false,true));impact(hit);}
    private void impact(HitResult hit) {
        if(level().isClientSide)return;var p=hit.getLocation();
        level().playSound(null,p.x,p.y,p.z,TGSounds.BIOGUN_IMPACT.get(),net.minecraft.sounds.SoundSource.AMBIENT,1,1);
        TGProjectilePackets.effect(this,"biogunImpact",p,hit instanceof BlockHitResult block?TGProjectilePackets.impactPitch(block.getDirection()):-getXRot(),hit instanceof BlockHitResult block?TGProjectilePackets.impactYaw(block.getDirection()):-getYRot());
    }
    @Override protected void hitBlock(BlockHitResult hit) {
        if(!level().isClientSide) {
            if(level().getBlockEntity(hit.getBlockPos()) instanceof BioBlobBlockEntity blob)blob.hitBlob(power,shooter);
            else {
                var pos=hit.getBlockPos().relative(hit.getDirection());
                if(level().getBlockEntity(pos) instanceof BioBlobBlockEntity blob)blob.hitBlob(power,shooter);
                else if(blockdamage&&level().isEmptyBlock(pos)) {
                    var snapshot=net.neoforged.neoforge.common.util.BlockSnapshot.create(level().dimension(),level(),pos);
                    if(!net.neoforged.neoforge.event.EventHooks.onBlockPlace(shooter,snapshot,hit.getDirection())) {
                        level().setBlockAndUpdate(pos,TGWeaponBlocks.BIOBLOB.get().defaultBlockState().setValue(BioBlobBlock.FACING,hit.getDirection().getOpposite()).setValue(BioBlobBlock.SIZE,net.minecraft.util.Mth.clamp(power-1,0,2)));
                    }
                }
            }
            impact(hit);
        }
    }
    @Override public void writeSpawnData(net.minecraft.network.RegistryFriendlyByteBuf b) {super.writeSpawnData(b);b.writeVarInt(power);}
    @Override public void readSpawnData(net.minecraft.network.RegistryFriendlyByteBuf b) {super.readSpawnData(b);power=b.readVarInt();}
    @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {super.addAdditionalSaveData(tag);tag.putInt("power",power);}
    @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {super.readAdditionalSaveData(tag);power=tag.getInt("power");}
}
