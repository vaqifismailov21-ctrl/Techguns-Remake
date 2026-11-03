package techguns.events;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import techguns.Techguns;
import techguns.api.guns.GunManager;

/** The original offhand item restrictions also apply to shields, food and block interaction. */
@EventBusSubscriber(modid=Techguns.MODID)
public final class GunUsageEvents {
    @SubscribeEvent public static void item(PlayerInteractEvent.RightClickItem event) {
        if(event.getHand()==InteractionHand.OFF_HAND && !GunManager.canUseOffhand(event.getEntity())) {
            event.setCanceled(true); event.setCancellationResult(InteractionResult.PASS);
        }
    }
    @SubscribeEvent public static void block(PlayerInteractEvent.RightClickBlock event) {
        if(event.getHand()==InteractionHand.OFF_HAND && !GunManager.canUseOffhand(event.getEntity())) {
            event.setUseItem(net.neoforged.neoforge.common.util.TriState.FALSE); event.setUseBlock(net.neoforged.neoforge.common.util.TriState.TRUE);
        }
    }
    private GunUsageEvents() {}
}
