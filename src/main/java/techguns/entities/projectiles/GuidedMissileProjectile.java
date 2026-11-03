package techguns.entities.projectiles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import techguns.TGEntities;
/** Source steering limit: nine degrees per tick, retaining current speed and the target's centre. */
public final class GuidedMissileProjectile extends RocketProjectile {
    public static final double MAX_TURN_ANGLE=Math.toRadians(9);
    public Entity target;
    private java.util.UUID targetUuid;
    private boolean highVelocity;
    public GuidedMissileProjectile(EntityType<? extends GuidedMissileProjectile> type,Level level) {super(type,level);}
    public GuidedMissileProjectile(Level level,LivingEntity owner,float damage,float speed,int ttl,float spread,float inner,float outer,float minimum,float penetration,boolean blocks,EnumBulletFirePos side,String variant,Entity target) {
        this(TGEntities.GUIDED_MISSILE.get(),level);highVelocity=variant.equals("high_velocity");float factor=highVelocity?0.75F:1;
        init(owner,damage,speed*(highVelocity?2:1),highVelocity?Math.round(ttl*0.75F):ttl,spread,inner*factor,outer*factor,minimum,penetration,blocks,side);
        this.target=target;targetUuid=target==null?null:target.getUUID();gravity=target==null?0.01:0;
    }
    @Override public boolean isHighVelocity() {return highVelocity;}
    @Override protected double blockFactor() {return 0.25;}
    @Override protected String explosionEffect() {return "GuidedMissileExplosion";}
    @Override protected String trailEffect() {return "GuidedMissileExhaust";}
    @Override public void tick() {
        if(target==null&&targetUuid!=null&&level instanceof net.minecraft.server.level.ServerLevel server)target=server.getEntity(targetUuid);
        if(target!=null) {
            var motion=getDeltaMovement();double speed=motion.length();var from=motion.normalize();
            var to=target.position().add(0,target.getBbHeight()*0.5,0).subtract(position()).normalize();
            double angle=Math.acos(net.minecraft.util.Mth.clamp(from.dot(to),-1,1));
            if(angle<MAX_TURN_ANGLE)setDeltaMovement(to.scale(speed));
            else {
                var axis=from.cross(to).normalize();if(axis.lengthSqr()<1E-10)axis=from.cross(new Vec3(0,1,0)).normalize();
                double c=Math.cos(MAX_TURN_ANGLE),s=Math.sin(MAX_TURN_ANGLE);
                setDeltaMovement(from.scale(c).add(axis.cross(from).scale(s)).add(axis.scale(axis.dot(from)*(1-c))).scale(speed));
            }
        }
        super.tick();
    }
    @Override public void writeSpawnData(FriendlyByteBuf b) {super.writeSpawnData(b);b.writeBoolean(highVelocity);b.writeVarInt(target==null?-1:target.getId());}
    @Override public void readSpawnData(FriendlyByteBuf b) {super.readSpawnData(b);highVelocity=b.readBoolean();target=level.getEntity(b.readVarInt());}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {super.addAdditionalSaveData(tag);tag.putBoolean("highVelocity",highVelocity);if(targetUuid!=null)tag.putUUID("target",targetUuid);}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {super.readAdditionalSaveData(tag);highVelocity=tag.getBoolean("highVelocity");targetUuid=tag.hasUUID("target")?tag.getUUID("target"):null;}
}
