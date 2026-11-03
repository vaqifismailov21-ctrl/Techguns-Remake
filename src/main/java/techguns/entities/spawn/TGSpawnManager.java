package techguns.entities.spawn;
import java.util.*;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import techguns.*;
import techguns.entities.npcs.GenericNPC;

public final class TGSpawnManager {
    public record Spawn(Supplier<? extends EntityType<? extends GenericNPC>> type,int danger,IntSupplier weight){}
    private static final List<Spawn> OVERWORLD=List.of(
        new Spawn(TGNpcs.ZOMBIESOLDIER,1,()->TGConfig.spawnWeightZombieSoldier),
        new Spawn(TGNpcs.ZOMBIEFARMER,0,()->TGConfig.spawnWeightZombieFarmer),
        new Spawn(TGNpcs.ZOMBIEMINER,0,()->TGConfig.spawnWeightZombieMiner),
        new Spawn(TGNpcs.SKELETONSOLDIER,1,()->TGConfig.spawnWeightSkeletonSoldier),
        new Spawn(TGNpcs.PSYCHOSTEVE,1,()->TGConfig.spawnWeightPsychoSteve),
        new Spawn(TGNpcs.BANDIT,2,()->TGConfig.spawnWeightBandit));
    private static final List<Spawn> NETHER=List.of(new Spawn(TGNpcs.ZOMBIEPIGMANSOLDIER,0,()->TGConfig.spawnWeightZombiePigmanSoldier),new Spawn(TGNpcs.CYBERDEMON,0,()->TGConfig.spawnWeightCyberDemon));
    public static int distanceDanger(double distance,boolean nether){double factor=nether?.5:1;return distance<TGConfig.distanceSpawnLevel0*factor?0:distance<TGConfig.distanceSpawnLevel1*factor?1:distance<TGConfig.distanceSpawnLevel2*factor?2:3;}
    private static boolean tagged(Holder<Biome> biome,String id){return biome.is(TagKey.create(Registries.BIOME,ResourceLocation.parse(id)));}
    public static int biomeDanger(Holder<Biome> biome){
        if(tagged(biome,"minecraft:is_nether")||tagged(biome,"minecraft:is_end"))return 0;
        for(String tag:new String[]{"c:is_desert","c:is_sandy","c:is_snowy","c:is_swamp","c:is_spooky","c:is_dead","c:is_wasteland"})if(tagged(biome,tag))return 2;
        for(String tag:new String[]{"c:is_cold","c:is_sparse_vegetation","minecraft:is_savanna","c:is_dry"})if(tagged(biome,tag))return 1;
        return 0;
    }
    public static boolean blacklisted(Holder<Biome> biome){String id=biome.unwrapKey().map(k->k.location().toString()).orElse("");return Arrays.asList(TGConfig.biomeBlacklist).contains(id);}
    public static EntityType<? extends GenericNPC> choose(boolean nether,int danger,RandomSource random){
        var table=nether?NETHER:OVERWORLD;var candidates=new ArrayList<Spawn>();
        for(int d=0;d<=Math.min(5,danger);d++)for(var spawn:table)if(spawn.danger==d&&spawn.weight.getAsInt()>0)candidates.add(spawn);
        int total=candidates.stream().mapToInt(s->s.weight.getAsInt()).sum();if(total==0)return null;
        int roll=random.nextInt(total),accumulated=0;
        // Retain the original inclusive comparison, including its endpoint bias.
        for(var spawn:candidates){accumulated+=spawn.weight.getAsInt();if(accumulated>=roll)return spawn.type.get();}return null;
    }
    public static GenericNPC replace(ServerLevel level,TGDummySpawn dummy){
        var biome=level.getBiome(BlockPos.containing(Math.round(dummy.getX()),64,Math.round(dummy.getZ())));
        if(blacklisted(biome)){dummy.discard();return null;}
        boolean nether=level.dimension().equals(Level.NETHER);var spawn=level.getSharedSpawnPos();
        double distance=Math.hypot(dummy.getX()-spawn.getX(),dummy.getZ()-spawn.getZ());int danger=distanceDanger(distance,nether)+biomeDanger(biome);
        var type=choose(nether,danger,level.random);GenericNPC npc=null;
        if(type!=null){npc=type.create(level);npc.moveTo(dummy.getX(),dummy.getY(),dummy.getZ(),dummy.getYRot(),0);npc.setYHeadRot(dummy.getYRot());npc.onSpawnByManager(danger);level.addFreshEntity(npc);}
        dummy.discard();return npc;
    }
    private TGSpawnManager(){}
}
