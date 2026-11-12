package techguns.events;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;
import techguns.tileentities.NpcSpawnerEntity;

@Mod.EventBusSubscriber(modid=Techguns.MODID)
public final class NpcSpawnerLinkEvents {
    private static final Set<Mob> LINKED=Collections.newSetFromMap(new WeakHashMap<>());
    public static NpcSpawnerEntity spawner(Mob mob){
        if(!(mob.level() instanceof ServerLevel world)||!mob.getPersistentData().contains("TGSpawner"))return null;
        var link=mob.getPersistentData().getCompound("TGSpawner");if(!world.dimension().location().toString().equals(link.getString("dimension")))return null;
        var pos=BlockPos.of(link.getLong("pos"));if(!world.hasChunkAt(pos))return null;
        return world.getBlockEntity(pos) instanceof NpcSpawnerEntity spawner?spawner:null;
    }
    /** Guards that carry a spawner link which is not resolved yet (the spawner's chunk may load after them). */
    private static final Set<Mob> PENDING=Collections.newSetFromMap(new WeakHashMap<>());
    @SubscribeEvent public static void join(net.minecraftforge.event.entity.EntityJoinLevelEvent event){
        if(event.getEntity() instanceof Mob mob&&!event.getLevel().isClientSide()&&!LINKED.contains(mob)&&mob.getPersistentData().contains("TGSpawner"))PENDING.add(mob);
    }
    @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.LevelTickEvent event){
        if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END||PENDING.isEmpty()||!(event.level instanceof ServerLevel world))return;
        for(var mob:new ArrayList<>(PENDING)){
            if(mob==null||mob.isRemoved()){PENDING.remove(mob);continue;}
            if(mob.level()!=world)continue;
            var spawner=spawner(mob);if(spawner!=null){spawner.relink(mob);LINKED.add(mob);PENDING.remove(mob);}
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent event){if(event.getEntity() instanceof Mob mob){var spawner=spawner(mob);if(spawner!=null){spawner.relink(mob);spawner.killed(mob);}LINKED.remove(mob);PENDING.remove(mob);}}
    @SubscribeEvent public static void leave(EntityLeaveLevelEvent event){if(event.getEntity() instanceof Mob mob){
        // Chunk unloading retains the guard's persistent link; only actual removal frees its active slot.
        if(mob.getRemovalReason()!=null&&mob.getRemovalReason().shouldDestroy()){var spawner=spawner(mob);if(spawner!=null)spawner.despawned(mob);}LINKED.remove(mob);
    }}
    private NpcSpawnerLinkEvents(){}
}
