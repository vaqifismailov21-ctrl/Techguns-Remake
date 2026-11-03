package techguns.client.render.item;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import techguns.Techguns;
import techguns.api.guns.GunManager;

/** Supplies the living holder that modern BEWLR does not receive in its render method. */
@EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
public final class GunRenderContext {
    private static LivingEntity holder;
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void before(RenderLivingEvent.Pre<?,?> event) { holder=event.getEntity(); }
    @SubscribeEvent public static void after(RenderLivingEvent.Post<?,?> event) { holder=null; }
    public static LivingEntity holder() { return holder; }
    @SubscribeEvent public static void hand(RenderHandEvent event) {
        var player=Minecraft.getInstance().player;
        if(player!=null && event.getHand()==InteractionHand.OFF_HAND && !GunManager.canUseOffhand(player)) event.setCanceled(true);
    }
    private GunRenderContext() {}
}
