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
 * The player's arms on a gun in first person, posed the way gun mods usually do it (MrCrayfish's Gun Mod grip poses):
 * from the vanilla held-item spot, turned to face the camera, the back hand on the grip and on two-handed guns the
 * front hand under the barrel. They share the item's bobbing and equip motion.
 */
@Mod.EventBusSubscriber(modid = Techguns.MODID, value = Dist.CLIENT)
public final class FirstPersonGunArms {
    private static final float PX = 0.0625F;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void hand(RenderHandEvent event) {
        if (event.isCanceled() || !TGConfig.cl_firstPersonArms || event.getHand() != InteractionHand.MAIN_HAND) return;
        var game = Minecraft.getInstance();
        var player = game.player;
        if (player == null || player.isInvisible() || !(event.getItemStack().getItem() instanceof GenericGun gun)) return;
        if (techguns.client.ClientZoom.isZoomed(player, gun) && GunItemRenderer.get().hasScope(gun)) return;
        if (!(game.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer)) return;
        HumanoidArm main = player.getMainArm();
        float o = main == HumanoidArm.RIGHT ? 1 : -1;
        int side = main.getOpposite() == HumanoidArm.RIGHT ? 1 : -1;
        float armWidth = "slim".equals(player.getModelName()) ? 3F : 4F;
        var pose = event.getPoseStack();
        var buffers = event.getMultiBufferSource();
        int light = event.getPackedLight();
        pose.pushPose();
        // vanilla first-person item spot, so the arms move with the gun's equip and bob
        pose.translate(0.56F * o, -0.52F + event.getEquipProgress() * -0.6F, -0.72F);
        pose.translate(TGConfig.cl_armRightX * o, TGConfig.cl_armRightY, TGConfig.cl_armRightZ);
        pose.mulPose(Axis.YP.rotationDegrees(180F));
        boolean twoHanded = gun.getGunHandType() != GunHandType.ONE_HANDED && player.getOffhandItem().isEmpty();
        if (twoHanded) {
            // front hand holding the barrel
            pose.pushPose();
            pose.translate(TGConfig.cl_armLeftX * side, TGConfig.cl_armLeftY, TGConfig.cl_armLeftZ);
            pose.scale(0.5F, 0.5F, 0.5F);
            pose.translate(4F * PX * side, 0, 0);
            pose.translate(armWidth / 2F * PX * side, 0, 0);
            pose.translate(-0.3125F * side, -0.1F, -0.4375F);
            pose.mulPose(Axis.XP.rotationDegrees(80F));
            pose.mulPose(Axis.YP.rotationDegrees(15F * -side));
            pose.mulPose(Axis.ZP.rotationDegrees(15F * -side));
            pose.mulPose(Axis.XP.rotationDegrees(-35F));
            arm(renderer, main.getOpposite(), pose, buffers, light, player);
            pose.popPose();
            // back hand on the grip
            pose.pushPose();
            pose.translate(0, 0.1F, -0.675F);
            pose.scale(0.5F, 0.5F, 0.5F);
            pose.translate(-4F * PX * side, 0, 0);
            pose.translate(-armWidth / 2F * PX * side, 0, 0);
            pose.mulPose(Axis.XP.rotationDegrees(80F));
            arm(renderer, main, pose, buffers, light, player);
            pose.popPose();
        } else {
            pose.pushPose();
            pose.scale(0.5F, 0.5F, 0.5F);
            pose.translate(-4F * PX * side, 0, 0);
            pose.translate(-armWidth / 2F * PX * side, 0, 0);
            pose.translate(0, 0.15F, -1.3125F);
            pose.mulPose(Axis.XP.rotationDegrees(75F));
            arm(renderer, main, pose, buffers, light, player);
            pose.popPose();
        }
        pose.popPose();
    }

    private static void arm(PlayerRenderer renderer, HumanoidArm side, PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player) {
        if (side == HumanoidArm.RIGHT) renderer.renderRightHand(pose, buffers, light, player);
        else renderer.renderLeftHand(pose, buffers, light, player);
    }
    private FirstPersonGunArms() {}
}
