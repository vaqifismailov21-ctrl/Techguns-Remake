package techguns.client;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import techguns.Techguns;
import techguns.capabilities.TGExtendedPlayer;
import techguns.items.guns.GenericGun;

/** Original sneak + right click toggle, hold-zoom mode, and reset on item change/reload. */
@EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class ClientZoom {
    private static final techguns.api.guns.WeaponAimState state=new techguns.api.guns.WeaponAimState();
    public static void toggle(GenericGun gun) {
        var player=Minecraft.getInstance().player;
        if(player==null || !gun.canZoom()) return;
        boolean reloading=TGExtendedPlayer.find(player).map(p->p.getAttackTime(false).getReloadTime()>System.currentTimeMillis()).orElse(true);
        state.press(gun,player.getInventory().selected,reloading);
    }
    public static boolean isZoomed(Player player,GenericGun gun) {
        return player!=null&&player==Minecraft.getInstance().player&&state.active(gun,player.getInventory().selected);
    }
    public static void reset() { state.reset(); }
    @SubscribeEvent public static void tick(ClientTickEvent.Pre event) {
        if(false) return;
        var game=Minecraft.getInstance();var player=game.player;
        if(player==null || !player.isAlive() || player.isRemoved() || !(player.getMainHandItem().getItem() instanceof GenericGun gun) || !gun.canZoom()) { reset();return; }
        boolean reloading=TGExtendedPlayer.find(player).map(p->p.getAttackTime(false).getReloadTime()>System.currentTimeMillis()).orElse(true);
        state.tick(gun,player.getInventory().selected,true,reloading,player.isShiftKeyDown());
    }
    @SubscribeEvent public static void fov(ComputeFovModifierEvent event) {
        var player=event.getPlayer();
        if(techguns.TGConfig.cl_lockSpeedFov) {
            double walk=player.getAbilities().getWalkingSpeed();
            if(walk>0)event.setNewFovModifier(event.getNewFovModifier()/(float)((player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)/walk+1)/2));
            if(player.isSprinting())event.setNewFovModifier(event.getNewFovModifier()*techguns.TGConfig.cl_fixedSprintFov);
        }
        if(player.getMainHandItem().getItem() instanceof GenericGun gun && isZoomed(player,gun))
            event.setNewFovModifier(event.getNewFovModifier()*gun.getZoomMult());
    }
    private ClientZoom() {}
}
