package techguns.items.guns;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import techguns.TGWeaponPackets;
import techguns.TGProjectilePackets;
import techguns.capabilities.TGExtendedPlayer;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GenericProjectile;
import techguns.entities.projectiles.GenericProjectileIncendiary;
import techguns.items.guns.ammo.AmmoType;
import techguns.items.guns.ammo.AmmoTypes;
import techguns.util.InventoryUtil;

/** First functional port of the original magazine-gun path. Charge/tools/shotguns are separate later stages. */
public class GenericGun extends Item implements techguns.items.armors.ICamoChangeable {
    public final int minFiretime, clipsize, reloadtime, maxRange, recoiltime;
    public final float damage, speed, accuracy, damageDropStart, damageDropEnd, damageMin;
    private final Supplier<AmmoType> ammoType;
    private final Supplier<SoundEvent> fireSound, reloadSound;
    private Supplier<SoundEvent> rechamberSound;
    private Supplier<SoundEvent> fireStartSound;
    private int maxLoopDelay;
    public float aiAttackRange=15;
    public int aiAttackTime=60,aiBurstCount,aiBurstAttackTime;
    public GenericGun setAI(float range,int time,int count,int delay){aiAttackRange=range;aiAttackTime=time;aiBurstCount=count;aiBurstAttackTime=delay;return this;}
    public GenericGun setFiresoundStart(Supplier<SoundEvent> sound) {fireStartSound=sound;return this;}
    public GenericGun setMaxLoopDelay(int value) {maxLoopDelay=value;return this;}
    private int ammoPerReload=1, extraPellets;
    private float pelletSpread=0.015F;
    private boolean semiAuto,burst,checkRecoil,checkMuzzleFlash,silenced;
    private boolean shootWithLeftClick=true;
    public GenericGun setShootWithLeftClick(boolean value) {shootWithLeftClick=value;return this;}
    public boolean shootsWithLeftClick() {return shootWithLeftClick;}
    private double projectileGravity;
    private float forwardOffset;
    public enum ProjectileKind {BULLET,ROCKET,GRENADE,FLAME,BIO}
    protected ProjectileKind projectileKind=ProjectileKind.BULLET;
    protected techguns.entities.projectiles.EnergyProjectile.Mode energyMode;
    public GenericGun setEnergyMode(techguns.entities.projectiles.EnergyProjectile.Mode mode){energyMode=mode;return this;}
    public techguns.entities.projectiles.EnergyProjectile.Mode getEnergyMode(){return energyMode;}
    protected float getForwardOffset(){return forwardOffset;}
    public GenericGun setProjectileKind(ProjectileKind value) {projectileKind=value;return this;}
    private boolean canZoom,toggleZoom,fireCenteredZoomed;
    private float zoomMult=1,zoomBonus=1;
    private EnumCrosshairStyle crossHairStyle=EnumCrosshairStyle.GUN_DYNAMIC;
    private boolean aimedBowAnimation=true;
    protected int lockOnTicks;
    public int getLockOnTicks() { return lockOnTicks; }
    private techguns.api.guns.GunHandType handType = techguns.api.guns.GunHandType.TWO_HANDED;
    private float penetration;
    private int muzzleFlashTicks = 5, camoCount = 1;

    public GenericGun(int minFiretime, int clipsize, int reloadtime, int maxRange, int recoiltime,
                      float damage, float speed, float accuracy, float dropStart, float dropEnd, float minDamage,
                      Supplier<AmmoType> ammoType, Supplier<SoundEvent> fire, Supplier<SoundEvent> reload) {
        super(new Properties().stacksTo(1));
        this.minFiretime = minFiretime; this.clipsize = clipsize; this.reloadtime = reloadtime; this.maxRange = maxRange;
        this.recoiltime = recoiltime; this.damage = damage; this.speed = speed; this.accuracy = accuracy;
        damageDropStart = dropStart; damageDropEnd = dropEnd; damageMin = minDamage;
        this.ammoType = ammoType; fireSound = fire; reloadSound = reload;
    }
    public AmmoType getAmmoType() { return ammoType.get(); }
    public GenericGun setAmmoCount(int value) { if(value<1) throw new IllegalArgumentException("Ammo per reload must be positive"); ammoPerReload=value; return this; }
    public int getAmmoCount() { return ammoPerReload; }
    public GenericGun setShotgunSpread(int extraPellets,float spread) { if(extraPellets<0 || spread<0) throw new IllegalArgumentException("Invalid shotgun spread"); this.extraPellets=extraPellets;pelletSpread=spread;return this; }
    public GenericGun setShotgunSpread(int count,float spread,boolean burst) { this.burst=burst;return setShotgunSpread(count,spread); }
    public GenericGun setZoom(float multiplier,boolean toggle,float accuracyBonus,boolean centered) {
        canZoom=true;zoomMult=multiplier;toggleZoom=toggle;zoomBonus=accuracyBonus;fireCenteredZoomed=centered;return this;
    }
    public boolean canZoom() { return canZoom; }
    public boolean isHoldZoom() { return canZoom&&!toggleZoom; }
    public float getZoomMult() { return zoomMult; }
    public float getZoomBonus() { return zoomBonus; }
    public GenericGun setCrossHair(EnumCrosshairStyle style) { crossHairStyle=style;return this; }
    public EnumCrosshairStyle getCrossHairStyle() { return crossHairStyle; }
    public GenericGun setCheckRecoil() { checkRecoil=true;return this; }
    public GenericGun setCheckMuzzleFlash() { checkMuzzleFlash=true;return this; }
    public boolean checksRecoil() { return checkRecoil; }
    public boolean checksMuzzleFlash() { return checkMuzzleFlash; }
    public GenericGun setSilenced(boolean value) { silenced=value;return this; }
    public GenericGun setGravity(double value) { projectileGravity=value;return this; }
    public GenericGun setForwardOffset(float value) { forwardOffset=value;return this; }
    protected double projectileGravity() {return projectileGravity;}
    protected float projectileOffset() {return forwardOffset;}
    protected void chargedEffects(Player player,InteractionHand hand,float charge) {
        if(player.level().isClientSide) {predictAnimation(player,hand,false,recoiltime,muzzleFlashTicks);TGExtendedPlayer.find(player).ifPresent(state->state.getAttackTime(hand==InteractionHand.OFF_HAND).setRecoilChargeProgress(charge));playLocalWeaponSound(player,fireSound.get(),false);}
        else if(player instanceof ServerPlayer server) {TGWeaponPackets.sound(server,fireSound.get(),false);TGWeaponPackets.animate(server,hand,false,recoiltime,muzzleFlashTicks,charge);}
    }
    public GenericGun setRechamberSound(Supplier<SoundEvent> sound) { rechamberSound=sound; return this; }
    public boolean isShotgun() { return extraPellets>0; }
    public int getPelletCount() { return 1+extraPellets; }
    public int getAmmoDisplayCount(ItemStack stack){return getCurrentAmmo(stack)*(burst?getPelletCount():1);}
    public int getClipDisplayCount(){return clipsize*(burst?getPelletCount():1);}
    public float getSpread() { return pelletSpread; }
    protected void initializeAmmo(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (!tag.contains("ammo")) {
            tag.putShort("ammo", (short) clipsize);
            if(!tag.contains("ammovariant")) tag.putString("ammovariant", AmmoTypes.TYPE_DEFAULT);
            if (!tag.contains("camo")) tag.putByte("camo", (byte) 0);
        }
    }
    public int getCurrentAmmo(ItemStack stack) { initializeAmmo(stack); return Math.max(0, Math.min(clipsize, stack.getTag().getShort("ammo"))); }
    public String getCurrentAmmoVariantKey(ItemStack stack) {
        initializeAmmo(stack); String key = stack.getTag().getString("ammovariant");
        return key.isEmpty() ? AmmoTypes.TYPE_DEFAULT : key;
    }
    public int getCurrentAmmoVariant(ItemStack stack) { return getAmmoType().getIDforVariantKey(getCurrentAmmoVariantKey(stack)); }
    public int useAmmo(ItemStack stack, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Ammo consumption must be nonnegative");
        int used = Math.min(getCurrentAmmo(stack), amount); stack.getTag().putShort("ammo", (short) (getCurrentAmmo(stack) - used)); return used;
    }
    public void setAmmo(ItemStack stack, int amount) { initializeAmmo(stack); stack.getTag().putShort("ammo", (short) Math.max(0, Math.min(clipsize, amount))); }
    public boolean isFullyLoaded(ItemStack stack) { return getCurrentAmmo(stack) == clipsize; }
    public boolean isSemiAuto() { return semiAuto; }
    public boolean hasBowAnim() { return aimedBowAnimation; }
    public GenericGun setNoBowAnim() { aimedBowAnimation=false; return this; }
    public GenericGun setSemiAuto(boolean value) { semiAuto=value; return this; }
    public GenericGun setHandType(techguns.api.guns.GunHandType value) { handType=value; return this; }
    public techguns.api.guns.GunHandType getGunHandType() { return handType; }
    public GenericGun setPenetration(float value) { penetration=value; return this; }
    public float getPenetration() { return penetration; }
    public GenericGun setMuzzleFlashTime(int value) { muzzleFlashTicks=value; return this; }
    public int getMuzzleFlashTicks() { return muzzleFlashTicks; }
    public GenericGun setCamoCount(int value) { camoCount=value; return this; }
    @Override public int getCamoCount() { return camoCount; }
    @Override public Component getCurrentCamoName(ItemStack stack) {
        int camo=getCurrentCamoIndex(stack);
        return Component.translatable(camo>0 ? getDescriptionId()+".camoname."+camo : "techguns.item.defaultcamo");
    }
    public boolean canFireInHand(Player player, InteractionHand hand) {
        return hand==InteractionHand.MAIN_HAND || techguns.api.guns.GunManager.canUseOffhand(player);
    }

    public boolean shootGunPrimary(ItemStack stack, Level level, Player player, InteractionHand hand) {
        return shootGunPrimary(stack,level,player,hand,false);
    }
    public boolean shootGunPrimary(ItemStack stack, Level level, Player player, InteractionHand hand,boolean aiming) {
        if (player.isSpectator() || !player.isAlive() || player.getItemInHand(hand) != stack || stack.getItem() != this || !canFireInHand(player,hand)) return false;
        var state = level.isClientSide ? TGExtendedPlayer.find(player).orElse(null) : TGExtendedPlayer.get(player);
        if(state==null) return false;
        if (state.getFireDelay(hand) > 0) return false;
        if (getCurrentAmmo(stack) <= 0) return tryForcedReload(stack, level, player, hand);
        // Original minigun has a zero interval, but its input runs once per tick.
        // Enforce that cadence against duplicate network packets as well.
        state.setFireDelay(hand, Math.max(1,minFiretime));
        if (!player.getAbilities().instabuild) useAmmo(stack, 1);
        SoundEvent shotSound=fireSound.get();
        if(maxLoopDelay>0) {
            if(state.getLoopSoundDelay(hand)<=0 && fireStartSound!=null) shotSound=fireStartSound.get();
            state.setLoopSoundDelay(hand,maxLoopDelay);
        }
        if (level.isClientSide) {
            if(energyMode==techguns.entities.projectiles.EnergyProjectile.Mode.GAUSS){
                boolean right=hand==InteractionHand.MAIN_HAND==(player.getMainArm()==HumanoidArm.RIGHT);
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->techguns.client.particle.WeaponEffects.attachHead("GaussFireFX",player,new net.minecraft.world.phys.Vec3(right?-0.14:0.14,-0.09,0.5),false));
            }
            if(projectileKind==ProjectileKind.FLAME) {
                boolean right=hand==InteractionHand.MAIN_HAND==(player.getMainArm()==HumanoidArm.RIGHT);
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->techguns.client.particle.WeaponEffects.attachHead("FlamethrowerFireFX",player,new net.minecraft.world.phys.Vec3(right?-0.15:0.15,-0.05,0.5),false));
            }
            predictAnimation(player, hand, false, recoiltime, muzzleFlashTicks);
            playLocalWeaponSound(player, shotSound, false);
            playRechamberSound(player,level);
            return true;
        }
        float accuracyBonus = Math.max(0, Math.min(1, 1 - techguns.items.armors.TGEquipmentBonuses.getBonus(player, techguns.items.armors.TGArmorBonus.GUN_ACCURACY, false)));
        if (hand == InteractionHand.MAIN_HAND && player.isBlocking()) accuracyBonus *= handType==techguns.api.guns.GunHandType.ONE_HANDED ? 4 : 8;
        boolean right = hand == InteractionHand.MAIN_HAND == (player.getMainArm() == HumanoidArm.RIGHT);
        var firePos = right ? EnumBulletFirePos.RIGHT : EnumBulletFirePos.LEFT;
        if(aiming&&canZoom&&hand==InteractionHand.MAIN_HAND) {
            accuracyBonus*=zoomBonus;
            if(fireCenteredZoomed) firePos=EnumBulletFirePos.CENTER;
        }
        boolean blockDamage = !state.enableSafemode;
        if(projectileKind==ProjectileKind.FLAME)TGProjectilePackets.attach(player,"FlamethrowerFireFX",new net.minecraft.world.phys.Vec3(right?-0.15:0.15,-0.05,0.5),false);
        spawnBullet(stack,level,player,firePos,accuracy*accuracyBonus,blockDamage,forwardOffset);
        for(int pellet=0;pellet<extraPellets;pellet++) spawnBullet(stack,level,player,firePos,pelletSpread*accuracyBonus,blockDamage,
                forwardOffset+(burst?speed/extraPellets*(pellet+1):0));
        // The firing client plays immediately, as in the original SoundUtil path.
        if (player instanceof ServerPlayer server) TGWeaponPackets.sound(server, shotSound, false);
        playRechamberSound(player,level);
        if (player instanceof ServerPlayer server) TGWeaponPackets.animate(server, hand, false, recoiltime, muzzleFlashTicks);
        return true;
    }
    protected void spawnBullet(ItemStack stack,Level level,Player player,EnumBulletFirePos firePos,float spread,boolean blockDamage,float offset) {
        if(energyMode==techguns.entities.projectiles.EnergyProjectile.Mode.SONIC){spawnSonic(level,player,firePos,blockDamage,offset);return;}
        GenericProjectile projectile=createProjectile(stack,level,player,firePos,spread,blockDamage);
        if(!(projectile instanceof techguns.entities.projectiles.GuidedMissileProjectile))projectile.setProjectileGravity(projectileGravity);
        if(silenced) projectile.setSilenced();
        if(offset>0) projectile.shiftForward(offset/speed);
        level.addFreshEntity(projectile);
    }
    protected GenericProjectile createProjectile(ItemStack stack,Level level,Player player,EnumBulletFirePos firePos,float spread,boolean blockDamage) {
        return createProjectileForLiving(stack,level,player,firePos,spread,blockDamage,1);
    }
    protected GenericProjectile createProjectileForLiving(ItemStack stack,Level level,LivingEntity player,EnumBulletFirePos firePos,float spread,boolean blockDamage,float damageScale) {
        float damage=this.damage*damageScale,damageMin=this.damageMin*damageScale;
        String key = getCurrentAmmoVariantKey(stack);
        int ttl=(int)Math.ceil(maxRange/speed);
        if(energyMode!=null){
            if(energyMode==techguns.entities.projectiles.EnergyProjectile.Mode.GAUSS)TGProjectilePackets.attach(player,"GaussFireFX",new net.minecraft.world.phys.Vec3(firePos==EnumBulletFirePos.RIGHT?-0.14:firePos==EnumBulletFirePos.LEFT?0.14:0,-0.09,0.5),false);
            return new techguns.entities.projectiles.EnergyProjectile(level,player,energyMode,damage,speed,ttl,spread,damageDropStart,damageDropEnd,damageMin,penetration,blockDamage,firePos);
        }
        if(projectileKind==ProjectileKind.BIO)return new techguns.entities.projectiles.BioGunProjectile(level,player,damage,speed,ttl,spread,damageDropStart,damageDropEnd,damageMin,penetration,blockDamage,firePos,1);
        if(projectileKind==ProjectileKind.ROCKET) return new techguns.entities.projectiles.RocketProjectile(level,player,damage,speed,ttl,spread,damageDropStart,damageDropEnd,damageMin,penetration,blockDamage,firePos,key);
        if(projectileKind==ProjectileKind.GRENADE) return new techguns.entities.projectiles.GrenadeProjectile(level,player,damage,speed,ttl,spread,damageDropStart,damageDropEnd,damageMin,penetration,blockDamage,firePos,2,false,true);
        if(projectileKind==ProjectileKind.FLAME) return new techguns.entities.projectiles.FlamethrowerProjectile(level,player,damage,speed,ttl,spread,damageDropStart,damageDropEnd,damageMin,penetration,blockDamage,firePos);
        if(getAmmoType()==AmmoTypes.ADVANCED_MAGAZINE)return new techguns.entities.projectiles.AdvancedBulletProjectile(level,player,damage,speed,ttl,spread,damageDropStart,damageDropEnd,damageMin,penetration,blockDamage,firePos);
        if(getAmmoType()==AmmoTypes.STONE_BULLETS)return new techguns.entities.projectiles.StoneBulletProjectile(level,player,damage,speed,ttl,spread,damageDropStart,damageDropEnd,damageMin,penetration,blockDamage,firePos);
        if(key.equals(AmmoTypes.TYPE_EXPLOSIVE)) return new techguns.entities.projectiles.GenericProjectileExplosive(level,player,damage,speed,(int)Math.ceil(maxRange/speed),spread,
                damageDropStart,damageDropEnd,damageMin,penetration,blockDamage,firePos);
        return key.equals(AmmoTypes.TYPE_INCENDIARY)
                ? new GenericProjectileIncendiary(level, player, damage, speed, (int) Math.ceil(maxRange / speed), spread,
                    damageDropStart, damageDropEnd, damageMin, penetration, blockDamage, firePos, isShotgun())
                : new GenericProjectile(level, player, damage, speed, (int) Math.ceil(maxRange / speed), spread,
                    damageDropStart, damageDropEnd, damageMin, penetration, blockDamage, firePos);
    }
    private void spawnSonic(Level level,LivingEntity player,EnumBulletFirePos side,boolean blocks,float offset){
        spawnSonic(level,player,side,blocks,offset,1);
    }
    private void spawnSonic(Level level,LivingEntity player,EnumBulletFirePos side,boolean blocks,float offset,float damageScale){
        var hits=new java.util.HashSet<java.util.UUID>();
        for(int ring=0;ring<=2;ring++)for(int i=0;i<(ring==0?1:5*ring);i++){
            var projectile=new techguns.entities.projectiles.EnergyProjectile(level,player,energyMode,damage*damageScale,speed,(int)Math.ceil(maxRange/speed),0,damageDropStart,damageDropEnd,damageMin*damageScale,penetration,blocks,side);
            projectile.sonicGroup(hits,ring==0);
            if(ring>0){
                var dir=new net.minecraft.world.phys.Vec3(1,0,0).yRot(0.075F*ring).xRot((float)(Math.PI*2/(5*ring)*i));
                double p=Math.toRadians(player.getXRot());dir=new net.minecraft.world.phys.Vec3(dir.x*Math.cos(p)-dir.y*Math.sin(p),dir.x*Math.sin(p)+dir.y*Math.cos(p),dir.z).yRot((float)Math.toRadians(-player.getYHeadRot()-90));
                // the centre shot flies at 1.5x speed (GenericProjectile.shoot); rings at 1x lagged behind the visible wave
                projectile.setDeltaMovement(dir.scale(speed*1.5));
            }
            if(offset>0)projectile.shiftForward(offset/speed);level.addFreshEntity(projectile);
        }
    }
    /** NPCs use the original separate AI firing path: infinite ammunition, difficulty and zoom accuracy multipliers. */
    public void fireWeaponFromNPC(LivingEntity shooter,float damageScale,float accuracyScale){
        if(shooter.level().isClientSide)return;
        var stack=shooter.getMainHandItem();
        // Original: turrets fire from the centre, other NPCs from the right hand.
        var firePos=shooter instanceof techguns.entities.npcs.NPCTurret?EnumBulletFirePos.CENTER:EnumBulletFirePos.RIGHT;
        TGWeaponPackets.sound(shooter,fireSound.get(),false);
        TGWeaponPackets.animate(shooter,InteractionHand.MAIN_HAND,false,recoiltime,muzzleFlashTicks,0);
        for(int pellet=0;pellet<=extraPellets;pellet++){
            float offset=forwardOffset+(pellet>0&&burst?speed/extraPellets*pellet:0);
            if(energyMode==techguns.entities.projectiles.EnergyProjectile.Mode.SONIC){spawnSonic(shooter.level(),shooter,firePos,false,offset,damageScale);continue;}
            float spread=(pellet==0?accuracy:pelletSpread)*zoomBonus*accuracyScale;
            var projectile=createProjectileForLiving(stack,shooter.level(),shooter,firePos,spread,false,damageScale);
            if(!(projectile instanceof techguns.entities.projectiles.GuidedMissileProjectile))projectile.setProjectileGravity(projectileGravity);
            if(silenced)projectile.setSilenced();
            if(offset>0)projectile.shiftForward(offset/speed);
            shooter.level().addFreshEntity(projectile);
        }
    }
    private void playRechamberSound(Player player,Level level) {
        if(rechamberSound==null) return;
        if(level.isClientSide) net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                () -> () -> techguns.client.audio.GunSounds.playRechamber(player,rechamberSound.get()));
        else {
            var position=techguns.util.GunSoundPosition.of(player);
            level.playSound(player,position.x,position.y,position.z,rechamberSound.get(),net.minecraft.sounds.SoundSource.PLAYERS,1,1);
        }
    }
    /** Name of the ammunition switch key, filled in by the client. */
    public static java.util.function.Supplier<Component> ammoSwitchKey = () -> Component.literal("G");
    /**
     * Unloads the gun and loads the next ammunition type the player carries (community edition key). Ammunition in the
     * gun goes back to the inventory: a full magazine as a magazine, a part-used one as an empty magazine and rounds.
     */
    public boolean toggleAmmoType(ItemStack stack, Player player, InteractionHand hand) {
        var type = getAmmoType();
        if (!type.hasMultipleVariants() || player.isSpectator() || !player.isAlive()) return false;
        var state = TGExtendedPlayer.get(player);
        if (state.getFireDelay(hand) > 0) return false;
        var variants = type.getVariants();
        int current = getCurrentAmmoVariant(stack), next = -1;
        for (int step = 1; step < variants.size() && next < 0; step++) {
            int candidate = (current + step) % variants.size();
            boolean carried = true;
            for (var ammo : type.getAmmo(candidate)) if (!ammo.isEmpty() && !InventoryUtil.canConsumeAmmoPlayer(player, ammo)) carried = false;
            if (carried) next = candidate;
        }
        if (next < 0) {
            player.displayClientMessage(Component.translatable("techguns.msg.noammotype").withStyle(net.minecraft.ChatFormatting.RED), true);
            return false;
        }
        int old = getCurrentAmmo(stack);
        if (old > 0) {
            if (ammoPerReload > 1) {
                for (var ammo : type.getAmmo(current)) if (!ammo.isEmpty()) giveOrDrop(player, ammo.copyWithCount(old));
            } else if (old >= clipsize) {
                for (var ammo : type.getAmmo(current)) if (!ammo.isEmpty()) giveOrDrop(player, ammo.copyWithCount(1));
            } else {
                ItemStack[] empties = type.getEmptyMag();
                for (int i = 0; i < empties.length; i++) if (!empties[i].isEmpty()) {
                    giveOrDrop(player, empties[i].copyWithCount(1));
                    int bullets = (int) Math.floor(old / type.getShotsPerBullet(clipsize, old));
                    if (bullets > 0) giveOrDrop(player, type.getBullet(current)[i].copyWithCount(bullets));
                }
            }
        }
        setAmmo(stack, 0);
        stack.getOrCreateTag().putString("ammovariant", variants.get(next).getKey());
        tryForcedReload(stack, player.level(), player, hand);
        player.displayClientMessage(Component.translatable("techguns.msg.ammotype").append(": ").append(type.getAmmo(next)[0].getHoverName()), true);
        return true;
    }
    public boolean tryForcedReload(ItemStack stack, Level level, Player player, InteractionHand hand) {
        if (player.isSpectator() || !player.isAlive() || player.getItemInHand(hand) != stack || stack.getItem() != this) return false;
        var state = level.isClientSide ? TGExtendedPlayer.find(player).orElse(null) : TGExtendedPlayer.get(player);
        if(state==null) return false;
        if (state.getFireDelay(hand) > 0 || isFullyLoaded(stack)) return false;
        int variant = getCurrentAmmoVariant(stack), oldAmmo = getCurrentAmmo(stack);
        if (!InventoryUtil.consumeAmmoPlayer(player, getAmmoType().getAmmo(variant))) return false;
        ItemStack[] empties = getAmmoType().getEmptyMag();
        for (int i = 0; i < empties.length; i++) if (!empties[i].isEmpty()) {
            giveOrDrop(player, empties[i].copyWithCount(1));
            // Preserve the original conversion from shots remaining into bullet items on forced reload.
            int bullets = (int) Math.floor(oldAmmo / getAmmoType().getShotsPerBullet(clipsize, oldAmmo));
            if (bullets > 0) giveOrDrop(player, getAmmoType().getBullet(variant)[i].copyWithCount(bullets));
        }
        if(ammoPerReload>1) {
            int loaded=1;
            int missingItems=ammoPerReload-oldAmmo;
            while(loaded<missingItems && InventoryUtil.consumeAmmoPlayer(player,getAmmoType().getAmmo(variant))) loaded++;
            setAmmo(stack,oldAmmo+loaded);
        } else setAmmo(stack, clipsize);
        state.setFireDelay(hand, reloadtime - minFiretime);
        if (level.isClientSide) {
            predictAnimation(player, hand, true, reloadtime, 0);
            playLocalWeaponSound(player, reloadSound.get(), true);
        } else if (player instanceof ServerPlayer server) TGWeaponPackets.sound(server, reloadSound.get(), true);
        if (player instanceof ServerPlayer server) TGWeaponPackets.animate(server, hand, true, reloadtime, 0);
        return true;
    }
    private static void giveOrDrop(Player player, ItemStack stack) {
        int remainder = InventoryUtil.addAmmoToPlayerInventory(player, stack);
        if (remainder > 0 && !player.level().isClientSide) player.drop(stack.copyWithCount(remainder), false);
    }
    protected static void predictAnimation(Player player, InteractionHand hand, boolean reload, int ticks, int muzzleTicks) {
        net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                () -> () -> techguns.client.ClientWeaponEvents.animateLocal(player, hand, reload, ticks, muzzleTicks));
    }
    protected static void playLocalWeaponSound(Player player, SoundEvent sound, boolean reload) {
        net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                () -> () -> techguns.client.audio.GunSounds.play(player, sound, reload));
    }
    @Override public void onCraftedBy(ItemStack stack, Level level, Player player) { initializeAmmo(stack); }
    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13F * getCurrentAmmo(stack) / clipsize); }
    @Override public int getBarColor(ItemStack stack) { return net.minecraft.util.Mth.hsvToRgb(getCurrentAmmo(stack) / (float) clipsize / 3, 1, 1); }
    @Override public boolean onEntitySwing(ItemStack stack, LivingEntity entity) { return true; }
    @Override public boolean onLeftClickEntity(ItemStack stack, Player player, net.minecraft.world.entity.Entity target) { return true; }
    @Override public boolean shouldCauseReequipAnimation(ItemStack before, ItemStack after, boolean slotChanged) { return slotChanged || before.getItem() != after.getItem(); }
    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flags) {
        int variant = getCurrentAmmoVariant(stack);
        boolean incendiary = getCurrentAmmoVariantKey(stack).equals(AmmoTypes.TYPE_INCENDIARY);
        float modifier = incendiary ? 1.1F : getCurrentAmmoVariantKey(stack).equals(AmmoTypes.TYPE_EXPLOSIVE)?1.15F:projectileKind==ProjectileKind.ROCKET&&getCurrentAmmoVariantKey(stack).equals(AmmoTypes.TYPE_NUKE)?5:1;
        for (var ammo : getAmmoType().getAmmo(variant)) tooltip.add(Component.translatable("techguns.gun.tooltip.ammo").append(": "+(ammoPerReload>1?ammoPerReload+"x ":"")).append(ammo.getHoverName()));
        tooltip.add(Component.translatable("techguns.gun.tooltip.damage").append((isShotgun()?"(x"+getPelletCount()+")":"")+": " + damage * modifier));
        if(getAmmoType().hasMultipleVariants()) tooltip.add(Component.translatable("techguns.gun.tooltip.ammoswitch.key",ammoSwitchKey.get()).withStyle(net.minecraft.ChatFormatting.GRAY));
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("techguns.gun.tooltip.handtype").append(": ").append(handType.displayName()));
            var type=incendiary||projectileKind==ProjectileKind.FLAME?techguns.api.damagesystem.DamageType.FIRE:
                    projectileKind==ProjectileKind.ROCKET||projectileKind==ProjectileKind.GRENADE||getCurrentAmmoVariantKey(stack).equals(AmmoTypes.TYPE_EXPLOSIVE)?techguns.api.damagesystem.DamageType.EXPLOSION:
                    projectileKind==ProjectileKind.BIO?techguns.api.damagesystem.DamageType.POISON:this instanceof MiningGun tool?tool.tool()==MiningGun.Tool.SHISHKEBAP?techguns.api.damagesystem.DamageType.FIRE:techguns.api.damagesystem.DamageType.PHYSICAL:techguns.api.damagesystem.DamageType.PROJECTILE;
            if(energyMode!=null)type=switch(energyMode){case TESLA->techguns.api.damagesystem.DamageType.LIGHTNING;case NDR->techguns.api.damagesystem.DamageType.RADIATION;case ALIEN,NETHER->techguns.api.damagesystem.DamageType.FIRE;case GAUSS->techguns.api.damagesystem.DamageType.PROJECTILE;case SONIC->techguns.api.damagesystem.DamageType.PHYSICAL;default->techguns.api.damagesystem.DamageType.ENERGY;};
            tooltip.add(Component.translatable("techguns.gun.tooltip.damageType").append(": ").append(type.displayName()));
            tooltip.add(Component.translatable("techguns.gun.tooltip.spread").append(": " + accuracy));
            if(canZoom){
                tooltip.add(Component.translatable("techguns.gun.tooltip.zoom").append(": x"+String.format(java.util.Locale.ROOT,"%.2f",1f/Math.max(zoomMult,0.0001f))+" (").append(Component.translatable(toggleZoom?"techguns.gun.tooltip.zoom.toogle":"techguns.gun.tooltip.zoom.hold")).append(")").withStyle(net.minecraft.ChatFormatting.YELLOW));
                if(zoomBonus!=1f)tooltip.add(Component.translatable("techguns.gun.tooltip.spread.scoped").append(": "+String.format(java.util.Locale.ROOT,"%.4f",zoomBonus*accuracy)));
            }
            tooltip.add(Component.translatable("techguns.gun.tooltip.clipsize").append(": " + getClipDisplayCount()));
            tooltip.add(Component.translatable("techguns.gun.tooltip.reloadTime").append(": " + reloadtime * 0.05F + "s"));
        } else tooltip.add(Component.translatable("techguns.gun.tooltip.shift1").append(" ")
                .append(Component.translatable("techguns.gun.tooltip.shift2").withStyle(net.minecraft.ChatFormatting.GREEN)).append(" ")
                .append(Component.translatable("techguns.gun.tooltip.shift3").withStyle(net.minecraft.ChatFormatting.GRAY)));
    }
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() { return techguns.client.render.item.GunItemRenderer.get(); }
            @Override public net.minecraft.client.model.HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return techguns.client.render.item.GunArmPose.get(entity,hand,GenericGun.this);
            }
        });
    }
}
