package techguns.events;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;
import techguns.entities.spawn.*;
@Mod.EventBusSubscriber(modid=Techguns.MODID)
public final class NpcSpawnEvents {
    @SubscribeEvent public static void join(EntityJoinLevelEvent event){if(event.getLevel() instanceof ServerLevel level&&event.getEntity() instanceof TGDummySpawn dummy){event.setCanceled(true);TGSpawnManager.replace(level,dummy);}}
    private NpcSpawnEvents(){}
}
