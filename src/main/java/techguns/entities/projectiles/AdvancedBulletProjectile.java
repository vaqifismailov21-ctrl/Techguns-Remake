package techguns.entities.projectiles;

import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import techguns.*;

/** Original advanced-magazine bullet: physical damage, blue tracer and material-specific impacts. */
public final class AdvancedBulletProjectile extends GenericProjectile {
    public AdvancedBulletProjectile(EntityType<? extends AdvancedBulletProjectile> type,Level level){super(type,level);}
    public AdvancedBulletProjectile(Level level,LivingEntity owner,float damage,float speed,int ttl,float spread,float start,float end,float minimum,float penetration,boolean blocks,EnumBulletFirePos side){
        this(TGEntities.ADVANCED_PROJECTILE.get(),level);init(owner,damage,speed,ttl,spread,start,end,minimum,penetration,blocks,side);
    }
    public static String impactGroup(SoundType sound){
        return "Impact_Bullet"+(sound==SoundType.STONE?"Rock":sound==SoundType.WOOD||sound==SoundType.LADDER?"Wood":sound==SoundType.GLASS?"Glass":sound==SoundType.METAL||sound==SoundType.ANVIL?"Metal":sound==SoundType.GRAVEL||sound==SoundType.SAND?"Dirt":"Default")+"_Blue";
    }
    public static void impact(GenericProjectile projectile,BlockHitResult hit){
        var level=projectile.level;if(level.isClientSide)return;var location=hit.getLocation();var state=level.getBlockState(hit.getBlockPos());var sound=state.getSoundType(level,hit.getBlockPos(),projectile);
        SoundEvent effect=sound==SoundType.STONE?TGSounds.BULLET_IMPACT_STONE.get():sound==SoundType.WOOD||sound==SoundType.LADDER?TGSounds.BULLET_IMPACT_WOOD.get():sound==SoundType.GLASS?TGSounds.BULLET_IMPACT_GLASS.get():sound==SoundType.METAL||sound==SoundType.ANVIL?TGSounds.BULLET_IMPACT_METAL.get():TGSounds.BULLET_IMPACT_DIRT.get();
        level.playSound(null,location.x,location.y,location.z,effect,SoundSource.AMBIENT,1,1);
        TGProjectilePackets.effect(projectile,impactGroup(sound),location,TGProjectilePackets.impactPitch(hit.getDirection()),TGProjectilePackets.impactYaw(hit.getDirection()));
    }
    @Override protected void hitBlock(BlockHitResult hit){impact(this,hit);}
}
