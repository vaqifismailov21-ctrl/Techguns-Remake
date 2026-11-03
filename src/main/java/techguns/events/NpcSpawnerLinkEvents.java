package techguns.events;
import net.neoforged.fml.common.EventBusSubscriber;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.Mod;
import techguns.Techguns;
import techguns.tileentities.NpcSpawnerEntity;

@EventBusSubscriber(modid=Techguns.MODID)
public final class NpcSpawnerLinkEvents {
    private static final Set<Mob> LINKED=Collections.newSetFromMap(new WeakHashMap<>());
    public static NpcSpawnerEntity spawner(Mob mob){
        if(!(mob.level() instanceof ServerLevel world)||!mob.getPersistentData().contains("TGSpawner"))return null;
        var link=mob.getPersistentData().getCompound("TGSpawner");if(!world.dimension().location().toString().equals(link.getString("dimension")))return null;
        var pos=BlockPos.of(link.getLong("pos"));if(!world.hasChunkAt(pos))return null;
        return world.getBlockEntity(pos) instanceof NpcSpawnerEntity spawner?spawner:null;
    }
    @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event){if(event.getEntity() instanceof Mob mob&&!mob.level().isClientSide&&!LINKED.contains(mob)){var spawner=spawner(mob);if(spawner!=null){spawner.relink(mob);LINKED.add(mob);}}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent event){if(event.getEntity() instanceof Mob mob){var spawner=spawner(mob);if(spawner!=null){spawner.relink(mob);spawner.killed(mob);}LINKED.remove(mob);}}
    @SubscribeEvent public static void leave(EntityLeaveLevelEvent event){if(event.getEntity() instanceof Mob mob){
        // Chunk unloading retains the guard's persistent link; only actual removal frees its active slot.
        if(mob.getRemovalReason()!=null&&mob.getRemovalReason().shouldDestroy()){var spawner=spawner(mob);if(spawner!=null)spawner.despawned(mob);}LINKED.remove(mob);
    }}
    private NpcSpawnerLinkEvents(){}
}
