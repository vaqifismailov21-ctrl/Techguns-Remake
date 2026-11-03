package techguns.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.TGWeaponPackets;

public final class GunSounds {
    public static void play(LivingEntity entity, SoundEvent sound, boolean reload) {
        Minecraft.getInstance().getSoundManager().play(new GunSound(sound, entity, reload ? 1 : 4, 1,
                reload, entity == Minecraft.getInstance().player));
    }
    public static void playRechamber(LivingEntity entity,SoundEvent sound) {
        Minecraft.getInstance().getSoundManager().play(new GunSound(sound,entity,1,1,false,entity==Minecraft.getInstance().player));
    }
    public static void playCharge(LivingEntity entity,SoundEvent sound) {
        Minecraft.getInstance().getSoundManager().play(new GunSound(sound,entity,4,1,true,entity==Minecraft.getInstance().player).charging());
    }
    public static void receive(TGWeaponPackets.WeaponSound message) {
        var game = Minecraft.getInstance();
        if (game.level == null) return;
        var entity = game.level.getEntity(message.entityId());
        // The firing player already predicted this sound. UUID guards entity-ID reuse.
        if (!(entity instanceof LivingEntity living) || living == game.player || !living.getUUID().equals(message.uuid())) return;
        var sound = ForgeRegistries.SOUND_EVENTS.getValue(message.sound());
        if(sound!=null) {
            if(sound==techguns.TGSounds.BIOGUN_CHARGE.get()||sound==techguns.TGSounds.POWERHAMMER_CHARGE.get())playCharge(living,sound);
            else play(living,sound,message.reload());
        }
    }
    private GunSounds() {}
}
