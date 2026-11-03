package techguns.tileentities;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.TGWorld;

/** Finite, linked guards from TGSpawnerTileEnt: a despawn does not count as a kill. */
public final class NpcSpawnerEntity extends BlockEntity {
    public record MobEntry(CompoundTag entity,int weight){}
    private int delay=200,spawnDelay=200,mobsLeft=5,maxActive=3,spawnHeightOffset;
    private double spawnRange=2;
    private final List<MobEntry> types=new ArrayList<>();
    private final Map<UUID,Mob> active=new HashMap<>();
    private ItemStack weaponOverride=ItemStack.EMPTY;
    public NpcSpawnerEntity(BlockPos pos,BlockState state){super(TGWorld.NPC_SPAWNER_ENTITY.get(),pos,state);}
    public void setParams(int remaining,int activeLimit,int interval,int range){mobsLeft=remaining;maxActive=activeLimit;spawnDelay=Math.max(1,interval);delay=spawnDelay;spawnRange=range;setChanged();}
    public void setSpawnHeightOffset(int height){spawnHeightOffset=height;setChanged();}
    public void setWeaponOverride(ItemStack weapon){weaponOverride=weapon.copy();setChanged();}
    public void addMobType(EntityType<?> type,int weight){if(weight<=0)return;var tag=new CompoundTag();tag.putString("id",ForgeRegistries.ENTITY_TYPES.getKey(type).toString());types.add(new MobEntry(tag,weight));setChanged();}
    public void addMobType(CompoundTag entity,int weight){if(weight>0){types.add(new MobEntry(entity.copy(),weight));setChanged();}}
    public void relink(Mob mob){active.putIfAbsent(mob.getUUID(),mob);}
    public void killed(Mob mob){if(active.remove(mob.getUUID())!=null){--mobsLeft;setChanged();}}
    public void despawned(Mob mob){active.remove(mob.getUUID());}
    public int remaining(){return mobsLeft;}public int activeCount(){return active.size();}public int delay(){return delay;}
    public Collection<Mob> activeMobs(){return List.copyOf(active.values());}
    public void tick(){
        if(!(level instanceof ServerLevel world))return;
        --delay;
        if(delay<=0&&active.size()<Math.min(maxActive,mobsLeft)&&!types.isEmpty()){
            if(world.getDifficulty()!=Difficulty.PEACEFUL){
                int roll=world.random.nextInt(types.stream().mapToInt(MobEntry::weight).sum());MobEntry chosen=null;for(var type:types){roll-=type.weight;if(roll<0){chosen=type;break;}}
                var entity=EntityType.loadEntityRecursive(chosen.entity.copy(),world,e->{e.moveTo(worldPosition.getX()+(world.random.nextDouble()-world.random.nextDouble())*spawnRange+.5,worldPosition.getY()+1+spawnHeightOffset,worldPosition.getZ()+(world.random.nextDouble()-world.random.nextDouble())*spawnRange+.5,0,0);return e;});
                if(entity instanceof Mob mob){
                    // Preserve Forge spawn hooks and source equipment initialization.
                    mob.finalizeSpawn(world,world.getCurrentDifficultyAt(mob.blockPosition()),MobSpawnType.SPAWNER,null,null);
                    if(mob instanceof PathfinderMob pathfinder)pathfinder.restrictTo(worldPosition,10);
                    var link=new CompoundTag();link.putLong("pos",worldPosition.asLong());link.putString("dimension",world.dimension().location().toString());mob.getPersistentData().put("TGSpawner",link);
                    if(!weaponOverride.isEmpty())mob.setItemSlot(EquipmentSlot.MAINHAND,weaponOverride.copy());
                    if(world.tryAddFreshEntityWithPassengers(mob)){active.put(mob.getUUID(),mob);world.levelEvent(2004,worldPosition,0);mob.spawnAnim();delay=spawnDelay;setChanged();}
                    else delay=spawnDelay;
                }else delay=spawnDelay;
            }else delay=spawnDelay;
        }else if(delay<=0){
            delay=spawnDelay;active.values().removeIf(mob->!mob.isAlive()||mob.level!=world);
        }
        if(mobsLeft<=0)world.removeBlock(worldPosition,false);
    }
    @Override protected void saveAdditional(CompoundTag tag){
        super.saveAdditional(tag);tag.putInt("mobsLeft",mobsLeft);tag.putInt("delay",delay);tag.putInt("maxActive",maxActive);tag.putInt("spawnDelay",spawnDelay);tag.putDouble("spawnRange",spawnRange);tag.putInt("spawnHeightOffset",spawnHeightOffset);
        var entries=new ListTag();for(var type:types){var entry=new CompoundTag();entry.put("Entity",type.entity.copy());entry.putInt("Weight",type.weight);entries.add(entry);}tag.put("mobtypes",entries);if(!weaponOverride.isEmpty())tag.put("weapon",weaponOverride.save(new CompoundTag()));
    }
    @Override public void load(CompoundTag tag){
        super.load(tag);mobsLeft=tag.getInt("mobsLeft");delay=tag.getInt("delay");spawnDelay=Math.max(1,tag.getInt("spawnDelay"));maxActive=Math.max(1,tag.getInt("maxActive"));spawnRange=tag.getDouble("spawnRange");spawnHeightOffset=tag.getInt("spawnHeightOffset");types.clear();active.clear();
        for(var raw:tag.getList("mobtypes",Tag.TAG_COMPOUND)){var entry=(CompoundTag)raw;int weight=entry.getInt("Weight");if(weight>0)types.add(new MobEntry(entry.getCompound("Entity").copy(),weight));}weaponOverride=tag.contains("weapon",Tag.TAG_COMPOUND)?ItemStack.of(tag.getCompound("weapon")):ItemStack.EMPTY;
    }
}
