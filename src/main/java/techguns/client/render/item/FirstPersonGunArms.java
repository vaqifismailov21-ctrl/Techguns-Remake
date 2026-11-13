package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;
import techguns.TGConfig;
import techguns.api.guns.GunHandType;
import techguns.items.guns.GenericGun;

/**
 * The player's own arms on a gun in first person: the right hand on the grip, and on two-handed guns the left hand under
 * the barrel. Drawn in the same hand space as vanilla's empty-hand arm, so they move with view bobbing and equipping.
 */
@Mod.EventBusSubscriber(modid = Techguns.MODID, value = Dist.CLIENT)
public final class FirstPersonGunArms {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void hand(RenderHandEvent event) {
        if (event.isCanceled() || !TGConfig.cl_firstPersonArms || event.getHand() != InteractionHand.MAIN_HAND) return;
        var game = Minecraft.getInstance();
        var player = game.player;
        if (player == null || player.isInvisible() || !(event.getItemStack().getItem() instanceof GenericGun gun)) return;
        if (techguns.client.ClientZoom.isZoomed(player, gun) && GunItemRenderer.get().hasScope(gun)) return;
        if (!(game.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer)) return;
        boolean rightHanded = player.getMainArm() == HumanoidArm.RIGHT;
        HumanoidArm main = rightHanded ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        var pose = event.getPoseStack();
        float equip = event.getEquipProgress();
        arm(pose, event.getMultiBufferSource(), event.getPackedLight(), equip, main, renderer, player, TGConfig.cl_armRightX, TGConfig.cl_armRightY, TGConfig.cl_armRightZ, 0);
        // the other hand only when it is free: a gun or anything else in the off hand keeps its own place
        if (gun.getGunHandType() != GunHandType.ONE_HANDED && player.getOffhandItem().isEmpty())
            arm(pose, event.getMultiBufferSource(), event.getPackedLight(), equip, main.getOpposite(), renderer, player, TGConfig.cl_armLeftX, TGConfig.cl_armLeftY, TGConfig.cl_armLeftZ, TGConfig.cl_armLeftYaw);
    }

    /** Vanilla ItemInHandRenderer.renderPlayerArm without the swing, moved by the configured offset (mirrored for the left side). */
    private static void arm(PoseStack pose, MultiBufferSource buffers, int light, float equip, HumanoidArm side, PlayerRenderer renderer, AbstractClientPlayer player,
                            float dx, float dy, float dz, float yaw) {
        boolean right = side == HumanoidArm.RIGHT;
        float f = right ? 1 : -1;
        pose.pushPose();
        // offsets are given for the hand on the player's main side (right) and the other one (left); mirrored for left-handed players
        float mainSign = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        pose.translate(f * 0.64000005F + dx * mainSign, -0.6F + equip * -0.6F + dy, -0.71999997F + dz);
        pose.mulPose(Axis.YP.rotationDegrees(f * (45.0F - yaw)));
        pose.translate(f * -1.0F, 3.6F, 3.5F);
        pose.mulPose(Axis.ZP.rotationDegrees(f * 120.0F));
        pose.mulPose(Axis.XP.rotationDegrees(200.0F));
        pose.mulPose(Axis.YP.rotationDegrees(f * -135.0F));
        pose.translate(f * 5.6F, 0.0F, 0.0F);
        if (right) renderer.renderRightHand(pose, buffers, light, player);
        else renderer.renderLeftHand(pose, buffers, light, player);
        pose.popPose();
    }
    private FirstPersonGunArms() {}
}
