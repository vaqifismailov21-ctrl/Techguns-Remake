package techguns.entities.projectiles;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import techguns.*;
import techguns.damagesystem.TGExplosion;
/** Original reflection at half speed; detonation after the last bounce or a successful entity hit. */
public class GrenadeProjectile extends GenericProjectile {
    private int bounces=3;
    public GrenadeProjectile(EntityType<? extends GrenadeProjectile> type,Level level) {super(type,level);}
    public GrenadeProjectile(Level level,LivingEntity owner,float damage,float speed,int ttl,float spread,float inner,float outer,float minimum,float penetration,boolean blocks,EnumBulletFirePos side,int bounces,boolean frag,boolean launched) {
        this(launched?TGEntities.GRENADE_40MM.get():frag?TGEntities.FRAG_GRENADE.get():TGEntities.GRENADE.get(),level);
        init(owner,damage,speed,ttl,spread,inner,outer,minimum,penetration,blocks,side);this.bounces=bounces;
    }
    public int getBounces() {return bounces;}
    @Override protected void onHit(HitResult hit) {
        if(hit instanceof BlockHitResult block && bounces>0) {
            if(!level.isClientSide) {
                var motion=getDeltaMovement();Direction.Axis axis=block.getDirection().getAxis();
                var reflected=new Vec3(axis==Direction.Axis.X?-motion.x:motion.x,axis==Direction.Axis.Y?-motion.y:motion.y,axis==Direction.Axis.Z?-motion.z:motion.z).scale(0.5);
                // 1.12 replaced the entity on a bounce. Reset its lifetime as the original copy constructor did.
                var next=new GrenadeProjectile((EntityType<? extends GrenadeProjectile>)getType(),level);
                next.configure(damage,speed,ticksToLive,damageDropStart,damageDropEnd,damageMin,penetration,blockdamage);
                next.setShooter(getShooter());next.gravity=gravity;next.bounces=bounces-1;
                next.moveTo(block.getLocation().add(reflected).x,block.getLocation().add(reflected).y,block.getLocation().add(reflected).z,getYRot(),getXRot());
                next.setDeltaMovement(reflected);level.addFreshEntity(next);
                level.playSound(null,getX(),getY(),getZ(),net.minecraft.sounds.SoundEvents.IRON_GOLEM_HURT,net.minecraft.sounds.SoundSource.PLAYERS,1,1);
            }
            discard();return;
        }
        super.onHit(hit);
    }
    @Override protected void hitBlock(BlockHitResult hit) {setPos(hit.getLocation());explode();}
    @Override protected void onHitEffect(LivingEntity target,HitResult hit) {explode();}
    protected void explode() {
        if(!level.isClientSide) {
            new TGExplosion(level,getShooter(),this,getX(),getY(),getZ(),damage,damageMin,damageDropStart,damageDropEnd,0).doExplosion(true);
            TGProjectilePackets.effect(this,"RocketExplosion",position(),0,0);
        }
        discard();
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {super.addAdditionalSaveData(tag);tag.putInt("bounces",bounces);}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {super.readAdditionalSaveData(tag);bounces=tag.getInt("bounces");}
}
