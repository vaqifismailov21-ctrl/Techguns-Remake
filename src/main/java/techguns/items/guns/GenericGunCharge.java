package techguns.items.guns;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import techguns.capabilities.TGExtendedPlayer;
import techguns.entities.projectiles.*;
import techguns.items.guns.ammo.AmmoType;
/** Original right-button charge/release path, with charge based on the server's use duration. */
public class GenericGunCharge extends GenericGun {
    public final float fullChargeTime;
    public final int ammoConsumedOnFullCharge;
    protected boolean fireWhileCharging,chargeFireAnims=true;
    protected Supplier<SoundEvent> chargeSound;
    protected String chargeFX;
    protected float chargeX,chargeY,chargeZ;
    public GenericGunCharge(int interval,int clip,int reload,int range,int recoil,float damage,float speed,float accuracy,float start,float end,float minimum,Supplier<AmmoType> ammo,Supplier<SoundEvent> fire,Supplier<SoundEvent> reloadSound,float fullCharge,int consumption) {
        super(interval,clip,reload,range,recoil,damage,speed,accuracy,start,end,minimum,ammo,fire,reloadSound);fullChargeTime=fullCharge;ammoConsumedOnFullCharge=consumption;
    }
    public GenericGunCharge setFireWhileCharging(boolean value) {fireWhileCharging=value;return this;}
    public GenericGunCharge setChargeFireAnims(boolean value) {chargeFireAnims=value;return this;}
    public GenericGunCharge setChargeSound(Supplier<SoundEvent> sound) {chargeSound=sound;return this;}
    public GenericGunCharge setChargeFX(String name,float x,float y,float z) {chargeFX=name;chargeX=x;chargeY=y;chargeZ=z;return this;}
    @Override public int getUseDuration(ItemStack stack) {return 288000;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        var stack=player.getItemInHand(hand);
        if(hand!=InteractionHand.MAIN_HAND || !player.isAlive()||player.isSpectator())return InteractionResultHolder.pass(stack);
        var state=TGExtendedPlayer.find(player).orElse(null);
        if(state==null||state.getFireDelay(hand)>0)return InteractionResultHolder.fail(stack);
        if(getCurrentAmmo(stack)==0) {tryForcedReload(stack,level,player,hand);return InteractionResultHolder.consume(stack);}
        state.setFireDelay(hand,minFiretime);player.startUsingItem(hand);state.setChargingWeapon(true);
        if(chargeFX!=null) {
            var offset=new net.minecraft.world.phys.Vec3(player.getMainArm()==HumanoidArm.RIGHT?chargeX:-chargeX,chargeY,chargeZ);
            if(level.isClientSide)net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->techguns.client.particle.WeaponEffects.attachHead(chargeFX,player,offset,true));
            else techguns.TGProjectilePackets.attach(player,chargeFX,offset,true);
        }
        if(chargeSound!=null) {
            if(level.isClientSide)net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->techguns.client.audio.GunSounds.playCharge(player,chargeSound.get()));
            else if(player instanceof net.minecraft.server.level.ServerPlayer server)techguns.TGWeaponPackets.sound(server,chargeSound.get(),false);
        }
        return InteractionResultHolder.consume(stack);
    }
    @Override public boolean shootGunPrimary(ItemStack stack,Level level,Player player,InteractionHand hand,boolean aiming) {
        if(player.getUseItem()==stack&&!fireWhileCharging)return false;
        return super.shootGunPrimary(stack,level,player,hand,aiming);
    }
    public int consumeAmmoCharge(ItemStack stack,float charge,boolean creative) {
        int amount=(int)Math.ceil(charge*ammoConsumedOnFullCharge);return creative?amount:useAmmo(stack,amount);
    }
    @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity entity,int remaining) {
        if(!(entity instanceof Player player))return;
        TGExtendedPlayer.find(player).ifPresent(state->state.setChargingWeapon(false));
        float charge=net.minecraft.util.Mth.clamp((getUseDuration(stack)-remaining)/fullChargeTime,0,1);
        int consumed=consumeAmmoCharge(stack,charge,player.getAbilities().instabuild);
        if(consumed<(int)Math.ceil(charge*ammoConsumedOnFullCharge))charge=consumed/(float)ammoConsumedOnFullCharge;
        if(!level.isClientSide) {
            var side=player.getMainArm()==HumanoidArm.RIGHT?EnumBulletFirePos.RIGHT:EnumBulletFirePos.LEFT;
            var projectile=createChargedProjectile(stack,level,player,side,charge,consumed);
            // Original spawnChargedProjectile does not apply the primary-fire forward offset.
            if(projectile!=null)level.addFreshEntity(projectile);
        }
        if(chargeFireAnims)chargedEffects(player,player.getUsedItemHand(),charge);
    }
    protected GenericProjectile createChargedProjectile(ItemStack stack,Level level,Player player,EnumBulletFirePos side,float charge,int consumed) {
        if(energyMode==EnergyProjectile.Mode.TFG)return new EnergyProjectile(level,player,energyMode,damage,speed,(int)Math.ceil(maxRange/speed),accuracy,damageDropStart,damageDropEnd,damageMin,getPenetration(),!TGExtendedPlayer.get(player).enableSafemode,side).charge(charge);
        if(projectileKind==ProjectileKind.BIO)return new BioGunProjectile(level,player,damage,speed,(int)Math.ceil(maxRange/speed),accuracy,damageDropStart,damageDropEnd,damageMin,getPenetration(),!TGExtendedPlayer.get(player).enableSafemode,side,consumed);
        return createProjectile(stack,level,player,side,accuracy,!TGExtendedPlayer.get(player).enableSafemode);
    }
}
