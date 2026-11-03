package techguns.client.audio;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import techguns.util.GunSoundPosition;

/** One-shot weapon audio. Reload follows its owner, as in the original TGSound. */
public final class GunSound extends AbstractTickableSoundInstance {
    private final LivingEntity entity;
    private final boolean moving;
    private boolean charging;
    public GunSound charging(){charging=true;return this;}

    public GunSound(SoundEvent sound, LivingEntity entity, float volume, float pitch, boolean moving, boolean ownPlayer) {
        super(sound, SoundSource.PLAYERS, RandomSource.create());
        this.entity = entity;
        this.moving = moving;
        this.volume = volume;
        this.pitch = pitch;
        // A world source at the listener crosses ears as the camera moves between sound updates.
        // Keep own weapon audio centred in listener space; other shooters remain positional.
        relative = ownPlayer;
        attenuation = ownPlayer ? Attenuation.NONE : Attenuation.LINEAR;
        updatePosition();
    }

    private void updatePosition() {
        if (relative) {
            x = y = 0;
            z = -1;
        } else {
            var position = GunSoundPosition.of(entity);
            x = position.x; y = position.y; z = position.z;
        }
    }

    @Override public void tick() {
        if (entity.isRemoved()) { stop(); return; }
        if(charging&&!entity.isUsingItem()){stop();return;}
        if (moving) updatePosition();
    }
}
