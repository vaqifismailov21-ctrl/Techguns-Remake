package techguns.events;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;
import techguns.api.guns.GunManager;

/** The original offhand item restrictions also apply to shields, food and block interaction. */
@Mod.EventBusSubscriber(modid=Techguns.MODID)
public final class GunUsageEvents {
    @SubscribeEvent public static void item(PlayerInteractEvent.RightClickItem event) {
        if(event.getHand()==InteractionHand.OFF_HAND && !GunManager.canUseOffhand(event.getEntity())) {
            event.setCanceled(true); event.setCancellationResult(InteractionResult.PASS);
        }
    }
    @SubscribeEvent public static void block(PlayerInteractEvent.RightClickBlock event) {
        if(event.getHand()==InteractionHand.OFF_HAND && !GunManager.canUseOffhand(event.getEntity())) {
            event.setUseItem(Event.Result.DENY); event.setUseBlock(Event.Result.ALLOW);
        }
    }
    private GunUsageEvents() {}
}
