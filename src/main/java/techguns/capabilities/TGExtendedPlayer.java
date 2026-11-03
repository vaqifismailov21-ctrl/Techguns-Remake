package techguns.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.TGConfig;
import techguns.TGPermissions;
import techguns.api.capabilities.AttackTime;
import techguns.api.capabilities.ITGExtendedPlayer;
import techguns.gui.player.TGPlayerInventory;
import techguns.util.DataUtil;

/** Original persistent values and per-hand weapon state. Every instance retains its own player. */
public final class TGExtendedPlayer implements ITGExtendedPlayer {
    public static final int MAX_RADIATION = 1000;
    public final Player entity;
    public final TGPlayerInventory tg_inventory;
    private final AttackTime[] attackTimes = {new AttackTime(), new AttackTime()};
    public int fireDelayMainhand, fireDelayOffhand, loopSoundDelayMainhand, loopSoundDelayOffhand, swingSoundDelay;
    public boolean gotCreativeFlightLastTick, wasFlying, isForwardKeyPressed, isGliding;
    public ItemStack gunMainHand = ItemStack.EMPTY, gunOffHand = ItemStack.EMPTY;
    public Entity lockOnEntity;
    public int lockOnTicks;
    private boolean jumpKeyPressed, chargingWeapon;
    public boolean enableJetpack = true, enableStepAssist = true;
    public boolean enableNightVision, enableSafemode, enableHovermode;
    public boolean showTGHudElements = true;
    public int radlevel;
    public short foodleft;
    public float lastSaturation = 1.0F;
    private boolean dirty = true;
    private final ItemStack[] lastEquipment = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    private boolean lastGliding;

    public TGExtendedPlayer(Player entity) {
        this.entity = entity;
        tg_inventory = new TGPlayerInventory(entity);
    }
    public static TGExtendedPlayer get(Player player) {
        return player.getCapability(TGExtendedPlayerCapProvider.CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Techguns player capability is missing"));
    }
    /** Rendering can run while an old player is being removed or replaced. Never fabricate inventory data. */
    public static java.util.Optional<TGExtendedPlayer> find(Player player) {
        return player==null || player.isRemoved() ? java.util.Optional.empty()
                : player.getCapability(TGExtendedPlayerCapProvider.CAPABILITY).resolve();
    }
    public AttackTime getAttackTime(boolean offhand) { return attackTimes[offhand ? 1 : 0]; }
    public Player getEntity() { return entity; }
    public TGPlayerInventory getTGInventory() { return tg_inventory; }
    public boolean isRecoiling(boolean offhand) { return getAttackTime(offhand).isRecoiling(); }
    public boolean isReloading(boolean offhand) { return getAttackTime(offhand).isReloading(); }
    public int getFireDelay(InteractionHand hand) { return hand == InteractionHand.MAIN_HAND ? fireDelayMainhand : fireDelayOffhand; }
    public void setFireDelay(InteractionHand hand, int delay) {
        if (hand == InteractionHand.MAIN_HAND) fireDelayMainhand = delay; else fireDelayOffhand = delay;
    }
    public int getLoopSoundDelay(InteractionHand hand) { return hand == InteractionHand.MAIN_HAND ? loopSoundDelayMainhand : loopSoundDelayOffhand; }
    public void setLoopSoundDelay(InteractionHand hand, int delay) {
        if (hand == InteractionHand.MAIN_HAND) loopSoundDelayMainhand = delay; else loopSoundDelayOffhand = delay;
    }
    public boolean isJumpkeyPressed() { return jumpKeyPressed; }
    public void setJumpkeyPressed(boolean value) { if (value != jumpKeyPressed) { jumpKeyPressed = value; markDirty(); } }
    public boolean isChargingWeapon() { return chargingWeapon; }
    public void setChargingWeapon(boolean value) { if (value != chargingWeapon) { chargingWeapon = value; markDirty(); } }
    public void addRadiation(int amount) {
        if (TGConfig.WIP_disableRadiationSystem) return;
        radlevel = (int) Math.max(0L, Math.min(MAX_RADIATION, (long) radlevel + amount));
        markDirty();
    }
    public void markDirty() { dirty = true; }
    public boolean needsSync() { return dirty || tg_inventory.dirty; }
    public void clearDirty() { dirty = false; tg_inventory.dirty = false; }

    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tg_inventory.saveNBTData(tag);
        tag.putByte("states", DataUtil.compress(enableJetpack, enableNightVision, enableSafemode,
                enableStepAssist, showTGHudElements, enableHovermode));
        tag.putShort("foodLeft", foodleft);
        tag.putFloat("lastSaturation", lastSaturation);
        tag.putInt("radlevel", radlevel);
        return tag;
    }
    public void loadFromNBT(CompoundTag tag) {
        tg_inventory.loadNBTData(tag);
        if (tag.contains("states", Tag.TAG_BYTE)) {
            var states = DataUtil.uncompress(tag.getByte("states"));
            enableJetpack = states.get(0); enableNightVision = states.get(1); enableSafemode = states.get(2);
            enableStepAssist = states.get(3); showTGHudElements = states.get(4); enableHovermode = states.get(5);
        }
        foodleft = tag.getShort("foodLeft");
        lastSaturation = tag.contains("lastSaturation") ? tag.getFloat("lastSaturation") : 1.0F;
        radlevel = Math.max(0, Math.min(MAX_RADIATION, tag.getInt("radlevel")));
        markDirty();
    }
    public void enforcePermissions() {
        if (!entity.level.isClientSide && !enableSafemode && !TGPermissions.canUseUnsafeMode(entity)) { enableSafemode = true; markDirty(); }
    }

    public void saveToNBT(CompoundTag tag) { tag.merge(saveToNBT()); }
    public void copyFrom(TGExtendedPlayer other) {
        loadFromNBT(other.saveToNBT());
        fireDelayMainhand = other.fireDelayMainhand; fireDelayOffhand = other.fireDelayOffhand;
        loopSoundDelayMainhand = other.loopSoundDelayMainhand; loopSoundDelayOffhand = other.loopSoundDelayOffhand;
        swingSoundDelay = other.swingSoundDelay;
        attackTimes[0].copyFrom(other.attackTimes[0]); attackTimes[1].copyFrom(other.attackTimes[1]);
        gotCreativeFlightLastTick = other.gotCreativeFlightLastTick; wasFlying = other.wasFlying;
        gunMainHand = other.gunMainHand.copy(); gunOffHand = other.gunOffHand.copy();
        jumpKeyPressed = other.jumpKeyPressed; isForwardKeyPressed = other.isForwardKeyPressed; isGliding = other.isGliding;
    }
    public void swapAttackTimes() {
        AttackTime first = attackTimes[0]; attackTimes[0] = attackTimes[1]; attackTimes[1] = first;
        int delay = fireDelayMainhand; fireDelayMainhand = fireDelayOffhand; fireDelayOffhand = delay;
    }
    public void detectEquipmentChanges() {
        if(lastGliding!=isGliding){lastGliding=isGliding;markDirty();}
        for (int slot = 0; slot < 3; slot++) {
            ItemStack current = tg_inventory.getItem(slot);
            if (!ItemStack.matches(current, lastEquipment[slot])) {
                lastEquipment[slot] = current.copy();
                markDirty();
            }
        }
    }

    public CompoundTag synchronizationData(boolean full) {
        CompoundTag tag = saveToNBT();
        if (!full) {
            tag.remove("Items");
            tag.remove("foodLeft");
            tag.remove("lastSaturation");
            tag.remove("radlevel");
            for (int i = 0; i < 3; i++) tag.put("equipment" + i, tg_inventory.getItem(i).save(new CompoundTag()));
        }
        tag.putBoolean("chargingWeapon", chargingWeapon);
        tag.putBoolean("jumpPressed", jumpKeyPressed);
        tag.putBoolean("gliding",isGliding);
        return tag;
    }
    public void applySynchronization(CompoundTag tag, boolean full) {
        if (full) loadFromNBT(tag);
        else {
            var states = DataUtil.uncompress(tag.getByte("states"));
            enableJetpack = states.get(0); enableNightVision = states.get(1); enableSafemode = states.get(2);
            enableStepAssist = states.get(3); showTGHudElements = states.get(4); enableHovermode = states.get(5);
            for (int i = 0; i < 3; i++) tg_inventory.setItem(i, ItemStack.of(tag.getCompound("equipment" + i)));
        }
        chargingWeapon = tag.getBoolean("chargingWeapon");
        jumpKeyPressed = tag.getBoolean("jumpPressed");
        isGliding=tag.getBoolean("gliding");
        clearDirty();
    }
}
