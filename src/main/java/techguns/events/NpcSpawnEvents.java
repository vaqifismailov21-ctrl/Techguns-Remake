package techguns.events;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import techguns.Techguns;
import techguns.entities.spawn.*;
@EventBusSubscriber(modid=Techguns.MODID)
public final class NpcSpawnEvents {
    @SubscribeEvent public static void join(EntityJoinLevelEvent event){if(event.getLevel() instanceof ServerLevel level&&event.getEntity() instanceof TGDummySpawn dummy){event.setCanceled(true);TGSpawnManager.replace(level,dummy);}}
    private NpcSpawnEvents(){}
}
