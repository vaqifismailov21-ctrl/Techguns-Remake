package techguns.items.guns;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import techguns.*;
import techguns.capabilities.TGExtendedPlayer;
import techguns.entities.projectiles.*;
import techguns.items.guns.ammo.AmmoType;
public final class GuidedMissileLauncher extends GenericGunCharge {
    private int persistTicks;
    public GuidedMissileLauncher(int interval,int clip,int reload,int range,int recoil,float damage,float speed,float accuracy,float start,float end,float minimum,Supplier<AmmoType> ammo,Supplier<SoundEvent> fire,Supplier<SoundEvent> reloadSound,float fullCharge,int consumption) {
        super(interval,clip,reload,range,recoil,damage,speed,accuracy,start,end,minimum,ammo,fire,reloadSound,fullCharge,consumption);
    }
    /** The lock only decays while aiming; with the launcher put away it stayed frozen and came back with the gun. */
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){
        super.inventoryTick(stack,level,entity,slot,selected);
        if(entity instanceof Player player&&!(player.getMainHandItem().getItem() instanceof GuidedMissileLauncher)){
            var state=TGExtendedPlayer.get(player);if(state!=null&&(state.lockOnEntity!=null||state.lockOnTicks!=0)){state.lockOnEntity=null;state.lockOnTicks=0;}
        }
    }
    public GuidedMissileLauncher setLockOn(int ticks,int persist) {lockOnTicks=ticks;persistTicks=persist;return this;}
    public Entity traceTarget(Player player) {
        var state=TGExtendedPlayer.find(player).orElse(null);if(state==null)return null;
        Vec3 start=player.getEyePosition(),end=start.add(player.getLookAngle().scale(150));
        var block=player.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));if(block.getType()!=HitResult.Type.MISS)end=block.getLocation();
        Entity target=null;double nearest=Double.MAX_VALUE;
        for(var candidate:player.level().getEntities(player,player.getBoundingBox().expandTowards(player.getLookAngle().scale(150)).inflate(1),e->e.isAlive()&&!e.isSpectator()&&e.isPickable())) {
            var hit=candidate.getBoundingBox().inflate(0.5).clip(start,end);if(hit.isEmpty())continue;
            if(candidate instanceof Player victim&&!player.canHarmPlayer(victim))continue;
            double distance=start.distanceToSqr(hit.get());
            if(candidate==state.lockOnEntity) {target=candidate;break;}
            if(distance<nearest) {nearest=distance;target=candidate;}
        }
        if(target!=null) {
            if(state.lockOnEntity!=null&&state.lockOnEntity.isAlive()) {
                if(state.lockOnEntity==target) {if(++state.lockOnTicks>=lockOnTicks)state.lockOnTicks=lockOnTicks+persistTicks;}
                else if(state.lockOnTicks>0)state.lockOnTicks--;else {state.lockOnEntity=null;state.lockOnTicks=0;}
            } else {state.lockOnEntity=target;state.lockOnTicks=1;}
        } else if(state.lockOnEntity!=null&&!state.lockOnEntity.isAlive()) {state.lockOnEntity=null;state.lockOnTicks=0;}
        else if(state.lockOnTicks>0)state.lockOnTicks--;else state.lockOnEntity=null;
        return state.lockOnEntity;
    }
    @Override public void onUseTick(Level level,LivingEntity entity,ItemStack stack,int remaining) {
        if(!(entity instanceof Player player))return;traceTarget(player);
        if(level.isClientSide) {
            var state=TGExtendedPlayer.find(player).orElse(null);if(state==null)return;
            if(remaining%(state.lockOnTicks>0?4:12)==0)playLocalWeaponSound(player,(state.lockOnTicks>=lockOnTicks?TGSounds.LOCKED_BEEP:TGSounds.LOCKON_BEEP).get(),false);
        }
    }
    @Override protected GenericProjectile createProjectile(ItemStack stack,Level level,Player player,EnumBulletFirePos side,float spread,boolean blocks) {
        var state=TGExtendedPlayer.get(player);var target=state.lockOnTicks>=lockOnTicks?state.lockOnEntity:null;
        return new GuidedMissileProjectile(level,player,damage,speed,(int)Math.ceil(maxRange/speed),spread,damageDropStart,damageDropEnd,damageMin,getPenetration(),blocks,side,getCurrentAmmoVariantKey(stack),target);
    }
    @Override public int consumeAmmoCharge(ItemStack stack,float charge,boolean creative) {return 0;}
    @Override protected GenericProjectile createProjectileForLiving(ItemStack stack,Level level,LivingEntity shooter,EnumBulletFirePos side,float spread,boolean blocks,float damageScale){
        return new GuidedMissileProjectile(level,shooter,damage*damageScale,speed,(int)Math.ceil(maxRange/speed),spread,damageDropStart,damageDropEnd,damageMin*damageScale,getPenetration(),blocks,side,getCurrentAmmoVariantKey(stack),null);
    }
    @Override protected GenericProjectile createChargedProjectile(ItemStack stack,Level level,Player player,EnumBulletFirePos side,float charge,int consumed) {return null;}
}
