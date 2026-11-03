package techguns.client.render.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import techguns.capabilities.TGExtendedPlayer;
import techguns.items.guns.GenericGun;

/** Original TGEventHandler aimed-bow pose, exposed through Forge's modern item hook. */
public final class GunArmPose {
    public static HumanoidModel.ArmPose get(LivingEntity entity, InteractionHand hand, GenericGun gun) {
        if(!gun.hasBowAnim() || !(entity instanceof Player player) || entity.isRemoved()) return null;
        if(hand==InteractionHand.MAIN_HAND) return HumanoidModel.ArmPose.BOW_AND_ARROW;
        if(player.getMainHandItem().getItem() instanceof GenericGun main && main.hasBowAnim()) return null;
        if(!gun.canFireInHand(player,hand)) return null;
        var state=TGExtendedPlayer.find(player).orElse(null);
        long now=System.currentTimeMillis();
        return state!=null && (state.getAttackTime(true).getReloadTime()>now || state.getAttackTime(true).getRecoilTime()>now)
                ? HumanoidModel.ArmPose.BOW_AND_ARROW : null;
    }
    private GunArmPose() {}
}
