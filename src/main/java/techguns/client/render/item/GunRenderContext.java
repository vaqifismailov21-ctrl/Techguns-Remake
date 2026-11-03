package techguns.client.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;
import techguns.api.guns.GunManager;

/** Supplies the living holder that modern BEWLR does not receive in its render method. */
@Mod.EventBusSubscriber(modid=Techguns.MODID,value=Dist.CLIENT)
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
