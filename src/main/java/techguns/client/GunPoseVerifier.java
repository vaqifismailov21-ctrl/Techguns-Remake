package techguns.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.TGuns;
import techguns.Techguns;
import techguns.capabilities.TGExtendedPlayer;

/** Synthetic model checks; these players are never added to the user's world. */
@Mod.EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class GunPoseVerifier {
    private static boolean done;
    @SubscribeEvent public static void verify(TickEvent.ClientTickEvent event) {
        var game=Minecraft.getInstance();
        if(done || event.phase!=TickEvent.Phase.END || game.level==null || !Boolean.getBoolean("techguns.verifyClientResources")) return;
        done=true;
        var player=new RemotePlayer(game.level,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"weapon-pose-fixture"));
        for(boolean slim:new boolean[]{false,true}) for(var arm:HumanoidArm.values()) for(var registered:TGuns.guns()) {
            var stack=registered.get().getDefaultInstance(); player.setMainArm(arm);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack); player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            var pose=IClientItemExtensions.of(stack).getArmPose(player,InteractionHand.MAIN_HAND,stack);
            if(!registered.get().hasBowAnim()) {require(pose==null,"Flaming sword must preserve original sword stance");continue;}
            require(pose==HumanoidModel.ArmPose.BOW_AND_ARROW,"Original gun aiming pose must reach Forge's player-renderer hook");
            var model=new PlayerModel<RemotePlayer>(game.getEntityModels().bakeLayer(slim?ModelLayers.PLAYER_SLIM:ModelLayers.PLAYER),slim);
            model.rightArmPose=arm==HumanoidArm.RIGHT?pose:HumanoidModel.ArmPose.EMPTY;
            model.leftArmPose=arm==HumanoidArm.LEFT?pose:HumanoidModel.ArmPose.EMPTY;
            model.setupAnim(player,0,0,0,0,0);
            require(model.rightArm.xRot < -1.3F && model.leftArm.xRot < -1.3F,"Both model arms must aim instead of hanging down");
        }
        var gun=TGuns.goldenrevolver.get(); var stack=gun.getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setItemInHand(InteractionHand.OFF_HAND,stack);
        var state=TGExtendedPlayer.get(player); var extension=IClientItemExtensions.of(stack);
        require(extension.getArmPose(player,InteractionHand.OFF_HAND,stack)==null,"Idle offhand gun must use the original relaxed pose");
        state.getAttackTime(true).setRecoilTime(System.currentTimeMillis()+5000);
        require(extension.getArmPose(player,InteractionHand.OFF_HAND,stack)==HumanoidModel.ArmPose.BOW_AND_ARROW,"Firing offhand must aim");
        state.getAttackTime(true).setRecoilTime(1);
        require(extension.getArmPose(player,InteractionHand.OFF_HAND,stack)==null,"Expired offhand recoil must not leave a stuck aiming pose");
        player.invalidateCaps();
        require(TGExtendedPlayer.find(player).isEmpty(),"Invalidated client player must have no renderable inventory state");
        player.reviveCaps();
        require(TGExtendedPlayer.get(player)==state,"Client capability revival must retain its data");
        player.discard();
        require(TGExtendedPlayer.find(player).isEmpty(),"Removed client player must be skipped by rendering");
        Techguns.LOGGER.info("TECHGUNS_GUN_POSE_CONTRACT_PASSED: {} guns, two skin models, both main arms; offhand activation/expiry and client capability lifecycle",TGuns.guns().size());
    }
    private static void require(boolean condition,String message) { if(!condition) throw new IllegalStateException(message); }
    private GunPoseVerifier() {}
}
