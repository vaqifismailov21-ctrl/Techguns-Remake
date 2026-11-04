package techguns.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.TGSounds;
import techguns.Techguns;

/** Opt-in sound-coordinate checks; creates no audible sounds and never moves the real player. */
@Mod.EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class GunAudioVerifier {
    private static boolean done;
    @SubscribeEvent public static void verify(TickEvent.ClientTickEvent event) {
        var level=Minecraft.getInstance().level;
        if(done || event.phase!=TickEvent.Phase.END || level==null || !Boolean.getBoolean("techguns.verifyClientResources")) return;
        done=true;
        var owner=new ArmorStand(EntityType.ARMOR_STAND,level);
        owner.setPos(5,70,10);
        var ownShot=new GunSound(TGSounds.PISTOL_FIRE.get(),owner,4,1,false,true);
        var ownReload=new GunSound(TGSounds.PISTOL_RELOAD.get(),owner,1,1,true,true);
        var remoteShot=new GunSound(TGSounds.PISTOL_FIRE.get(),owner,4,1,false,false);
        var remoteReload=new GunSound(TGSounds.PISTOL_RELOAD.get(),owner,1,1,true,false);
        double shotX=remoteShot.getX(),shotY=remoteShot.getY(),shotZ=remoteShot.getZ();
        for(int i=0;i<16;i++) {
            owner.setPos(5+i,70+i*0.1,10-i); owner.setYHeadRot(i*22.5F);
            ownShot.tick(); ownReload.tick(); remoteShot.tick(); remoteReload.tick();
            for(var sound:java.util.List.of(ownShot,ownReload))
                require(sound.isRelative() && sound.getAttenuation()==SoundInstance.Attenuation.NONE
                        && sound.getX()==0 && sound.getY()==0 && sound.getZ()==-1,
                        "Own weapon audio must remain centred through movement and turns");
            var expected=techguns.util.GunSoundPosition.of(owner);
            require(!remoteReload.isRelative() && remoteReload.getAttenuation()==SoundInstance.Attenuation.LINEAR
                    && Math.abs(remoteReload.getX()-expected.x)<0.00001 && Math.abs(remoteReload.getZ()-expected.z)<0.00001
                    && Math.abs(remoteReload.getY()-expected.y)<0.00001,"Observer reload must follow the original entity offset");
            require(remoteShot.getX()==shotX && remoteShot.getY()==shotY && remoteShot.getZ()==shotZ,
                    "Observer gunfire must stay at its firing position");
        }
        owner.discard(); remoteReload.tick(); ownReload.tick();
        require(remoteReload.isStopped() && ownReload.isStopped(),"Removed owner must stop reload audio");
        Techguns.LOGGER.info("TECHGUNS_WEAPON_AUDIO_CONTRACT_PASSED: 16 movement/rotation steps; own centred, observer positional; headphone listening pending");
    }
    private static void require(boolean condition,String message) { if(!condition) throw new IllegalStateException(message); }
    private GunAudioVerifier() {}
}
