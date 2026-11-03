package techguns.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import techguns.TGWeaponPackets;
import techguns.Techguns;
import techguns.capabilities.TGExtendedPlayer;
import techguns.items.guns.GenericGun;

@Mod.EventBusSubscriber(modid = Techguns.MODID, value = Dist.CLIENT)
public final class ClientWeaponEvents {
    private static boolean fireMain, fireOff;
    public static float muzzleJitterX, muzzleJitterY, muzzleJitterScale;
    @SubscribeEvent public static void mouse(InputEvent.MouseButton.Pre event) {
        var game = Minecraft.getInstance();
        if (game.player == null || game.screen != null || !game.mouseHandler.isMouseGrabbed() || game.player.isSpectator()) return;
        InteractionHand hand = event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT ? InteractionHand.MAIN_HAND
                : event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? InteractionHand.OFF_HAND : null;
        if(event.getButton()==GLFW.GLFW_MOUSE_BUTTON_RIGHT
                && game.player.getMainHandItem().getItem() instanceof GenericGun aimed && aimed.canZoom()
                && (game.player.isShiftKeyDown() || !(game.player.getOffhandItem().getItem() instanceof GenericGun)
                    || !techguns.api.guns.GunManager.canUseOffhand(game.player))) {
            event.setCanceled(true);
            if(event.getAction()==GLFW.GLFW_PRESS && !aimed.isHoldZoom()) ClientZoom.toggle(aimed);
            fireOff=false;return;
        }
        if (event.getAction() == GLFW.GLFW_RELEASE) {
            if (hand == InteractionHand.MAIN_HAND) fireMain=false;
            else if (hand == InteractionHand.OFF_HAND) fireOff=false;
        }
        if (hand != null && (hand == InteractionHand.MAIN_HAND || !game.player.isShiftKeyDown())
                && game.player.getItemInHand(hand).getItem() instanceof GenericGun gun && gun.canFireInHand(game.player,hand) && (hand!=InteractionHand.MAIN_HAND||gun.shootsWithLeftClick())) {
            event.setCanceled(true);
            if (hand == InteractionHand.MAIN_HAND) fireMain = event.getAction() == GLFW.GLFW_PRESS;
            else fireOff = event.getAction() == GLFW.GLFW_PRESS;
        }
    }
    /** Original TGTickHandler: consume a semi-auto press once, even if pressed during cooldown. */
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOW) public static void shooting(TickEvent.PlayerTickEvent event) {
        var game = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.START || event.player != game.player) return;
        if (game.screen != null || !game.mouseHandler.isMouseGrabbed() || game.player.isSpectator() || !game.player.isAlive()) {
            fireMain = fireOff = false; return;
        }
        fireMain = shoot(game.player, InteractionHand.MAIN_HAND, fireMain);
        fireOff = shoot(game.player, InteractionHand.OFF_HAND, fireOff);
    }
    private static boolean shoot(Player player, InteractionHand hand, boolean pressed) {
        var stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof GenericGun gun) || !gun.canFireInHand(player,hand) || hand==InteractionHand.MAIN_HAND&&!gun.shootsWithLeftClick()) return false;
        var state=TGExtendedPlayer.find(player).orElse(null);
        if(state==null) return false;
        if (pressed && state.getFireDelay(hand) <= 0) {
            boolean aiming=hand==InteractionHand.MAIN_HAND&&ClientZoom.isZoomed(player,gun);
            TGWeaponPackets.request(hand, false,aiming);
            gun.shootGunPrimary(stack, player.level(), player, hand,aiming);
        }
        return pressed && !gun.isSemiAuto();
    }
    @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered event) {
        var player = Minecraft.getInstance().player;
        if (player != null && event.isAttack() && player.getMainHandItem().getItem() instanceof GenericGun gun && gun.shootsWithLeftClick()) {
            event.setCanceled(true); event.setSwingHand(false);
        }
    }
    @SubscribeEvent public static void keys(TickEvent.ClientTickEvent event) {
        var game = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || game.player == null || game.screen != null) return;
        while (ClientModEvents.RELOAD.consumeClick()) {
            for (var hand : InteractionHand.values()) if (game.player.getItemInHand(hand).getItem() instanceof GenericGun gun) {
                TGWeaponPackets.request(hand, true);
                gun.tryForcedReload(game.player.getItemInHand(hand), game.level, game.player, hand);
            }
        }
    }
    public static void animate(TGWeaponPackets.Animation message) {
        var level = Minecraft.getInstance().level;
        if(level!=null&&level.getEntity(message.entityId()) instanceof techguns.entities.npcs.GenericNPC npc&&npc.getUUID().equals(message.uuid())){npc.weaponRecoilTicks=message.ticks();npc.weaponMuzzleTicks=message.muzzleTicks();return;}
        if (level == null || !(level.getEntity(message.entityId()) instanceof Player player) || !player.getUUID().equals(message.uuid())) return;
        // Preserve an already predicted local animation; server packets animate observers.
        var state=TGExtendedPlayer.find(player).orElse(null);
        if(state==null) return;
        var attack = state.getAttackTime(message.hand() == InteractionHand.OFF_HAND);
        if (player == Minecraft.getInstance().player && (message.reload() ? attack.getReloadTime() : attack.getRecoilTime()) > System.currentTimeMillis()) return;
        animateLocal(player, message.hand(), message.reload(), message.ticks(), message.muzzleTicks());
        attack.setRecoilChargeProgress(message.charge());
    }
    public static void animateLocal(Player player, InteractionHand hand, boolean reload, int ticks, int muzzleTicks) {
        var state=TGExtendedPlayer.find(player).orElse(null);
        if(state==null) return;
        var attack = state.getAttackTime(hand == InteractionHand.OFF_HAND);
        int milliseconds = Math.max(0, Math.min(60000, ticks * 50));
        long end = System.currentTimeMillis() + milliseconds;
        var held=player.getItemInHand(hand).getItem();
        var gun=held instanceof GenericGun weapon?weapon:null;
        if (reload) { attack.setReloadTime(end); attack.setReloadTimeTotal(milliseconds);if(player==Minecraft.getInstance().player)ClientZoom.reset(); }
        else {
            if(gun==null || !gun.checksRecoil() || attack.getRecoilTime()<=System.currentTimeMillis()) {
                attack.setRecoilTime(end); attack.setRecoilTimeTotal(milliseconds); attack.setReloadTime(0);
                attack.setRecoilChargeProgress(0);
            }
            if(gun==null || !gun.checksMuzzleFlash() || attack.getMuzzleFlashTime()<=System.currentTimeMillis()) {
                attack.setMuzzleFlashTime(System.currentTimeMillis() + muzzleTicks * 50L);
                attack.setMuzzleFlashTimeTotal(muzzleTicks * 50);
            }
            muzzleJitterX = 1 - player.getRandom().nextFloat() * 2;
            muzzleJitterY = 1 - player.getRandom().nextFloat() * 2;
            muzzleJitterScale = 1 - player.getRandom().nextFloat() * 2;
        }
    }
    private ClientWeaponEvents() {}
}
