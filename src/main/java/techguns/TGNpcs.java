package techguns;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.entities.npcs.*;
public final class TGNpcs {
 public static final Map<String,DeferredHolder<EntityType<?>, ? extends EntityType<? extends GenericNPC>>> TYPES=new LinkedHashMap<>();
 public static final Map<String,DeferredHolder<net.minecraft.world.item.Item, Item>> EGGS=new LinkedHashMap<>();
 public static final DeferredHolder<EntityType<?>, EntityType<ZombieSoldier>> ZOMBIESOLDIER=TGEntities.ENTITIES.register("zombiesoldier",()->EntityType.Builder.<ZombieSoldier>of(ZombieSoldier::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:zombiesoldier"));
 public static final DeferredHolder<EntityType<?>, EntityType<ZombieFarmer>> ZOMBIEFARMER=TGEntities.ENTITIES.register("zombiefarmer",()->EntityType.Builder.<ZombieFarmer>of(ZombieFarmer::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:zombiefarmer"));
 public static final DeferredHolder<EntityType<?>, EntityType<ZombieMiner>> ZOMBIEMINER=TGEntities.ENTITIES.register("zombieminer",()->EntityType.Builder.<ZombieMiner>of(ZombieMiner::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:zombieminer"));
 public static final DeferredHolder<EntityType<?>, EntityType<ArmySoldier>> ARMYSOLDIER=TGEntities.ENTITIES.register("armysoldier",()->EntityType.Builder.<ArmySoldier>of(ArmySoldier::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:armysoldier"));
 public static final DeferredHolder<EntityType<?>, EntityType<Bandit>> BANDIT=TGEntities.ENTITIES.register("bandit",()->EntityType.Builder.<Bandit>of(Bandit::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:bandit"));
 public static final DeferredHolder<EntityType<?>, EntityType<Commando>> COMMANDO=TGEntities.ENTITIES.register("commando",()->EntityType.Builder.<Commando>of(Commando::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:commando"));
 public static final DeferredHolder<EntityType<?>, EntityType<DictatorDave>> DICTATORDAVE=TGEntities.ENTITIES.register("dictatordave",()->EntityType.Builder.<DictatorDave>of(DictatorDave::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:dictatordave"));
 public static final DeferredHolder<EntityType<?>, EntityType<CyberDemon>> CYBERDEMON=TGEntities.ENTITIES.register("cyberdemon",()->EntityType.Builder.<CyberDemon>of(CyberDemon::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).fireImmune().build("techguns:cyberdemon"));
 public static final DeferredHolder<EntityType<?>, EntityType<SkeletonSoldier>> SKELETONSOLDIER=TGEntities.ENTITIES.register("skeletonsoldier",()->EntityType.Builder.<SkeletonSoldier>of(SkeletonSoldier::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:skeletonsoldier"));
 public static final DeferredHolder<EntityType<?>, EntityType<PsychoSteve>> PSYCHOSTEVE=TGEntities.ENTITIES.register("psychosteve",()->EntityType.Builder.<PsychoSteve>of(PsychoSteve::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:psychosteve"));
 public static final DeferredHolder<EntityType<?>, EntityType<StormTrooper>> STORMTROOPER=TGEntities.ENTITIES.register("stormtrooper",()->EntityType.Builder.<StormTrooper>of(StormTrooper::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:stormtrooper"));
 public static final DeferredHolder<EntityType<?>, EntityType<Outcast>> OUTCAST=TGEntities.ENTITIES.register("outcast",()->EntityType.Builder.<Outcast>of(Outcast::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:outcast"));
 public static final DeferredHolder<EntityType<?>, EntityType<ZombiePigmanSoldier>> ZOMBIEPIGMANSOLDIER=TGEntities.ENTITIES.register("zombiepigmansoldier",()->EntityType.Builder.<ZombiePigmanSoldier>of(ZombiePigmanSoldier::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:zombiepigmansoldier"));
 public static final DeferredHolder<EntityType<?>, EntityType<SuperMutantBasic>> SUPERMUTANTBASIC=TGEntities.ENTITIES.register("supermutantbasic",()->EntityType.Builder.<SuperMutantBasic>of(SuperMutantBasic::new,MobCategory.MONSTER).sized(1f,2.7f).clientTrackingRange(5).updateInterval(3).build("techguns:supermutantbasic"));
 public static final DeferredHolder<EntityType<?>, EntityType<SuperMutantElite>> SUPERMUTANTELITE=TGEntities.ENTITIES.register("supermutantelite",()->EntityType.Builder.<SuperMutantElite>of(SuperMutantElite::new,MobCategory.MONSTER).sized(1.4f,3.3f).clientTrackingRange(5).updateInterval(3).build("techguns:supermutantelite"));
 public static final DeferredHolder<EntityType<?>, EntityType<SuperMutantHeavy>> SUPERMUTANTHEAVY=TGEntities.ENTITIES.register("supermutantheavy",()->EntityType.Builder.<SuperMutantHeavy>of(SuperMutantHeavy::new,MobCategory.MONSTER).sized(1.2f,3f).clientTrackingRange(5).updateInterval(3).build("techguns:supermutantheavy"));
 public static final DeferredHolder<EntityType<?>, EntityType<ZombiePoliceman>> ZOMBIEPOLICEMAN=TGEntities.ENTITIES.register("zombiepoliceman",()->EntityType.Builder.<ZombiePoliceman>of(ZombiePoliceman::new,MobCategory.MONSTER).sized(0.6f,1.8f).clientTrackingRange(5).updateInterval(3).build("techguns:zombiepoliceman"));
 static {
 TYPES.put("zombiesoldier",ZOMBIESOLDIER);EGGS.put("zombiesoldier",TGItems.ITEMS.register("zombiesoldier_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ZOMBIESOLDIER,7697512,3715128,new Item.Properties())));
 TYPES.put("zombiefarmer",ZOMBIEFARMER);EGGS.put("zombiefarmer",TGItems.ITEMS.register("zombiefarmer_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ZOMBIEFARMER,7697512,3715128,new Item.Properties())));
 TYPES.put("zombieminer",ZOMBIEMINER);EGGS.put("zombieminer",TGItems.ITEMS.register("zombieminer_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ZOMBIEMINER,7697512,3715128,new Item.Properties())));
 TYPES.put("armysoldier",ARMYSOLDIER);EGGS.put("armysoldier",TGItems.ITEMS.register("armysoldier_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ARMYSOLDIER,7635054,1643794,new Item.Properties())));
 TYPES.put("bandit",BANDIT);EGGS.put("bandit",TGItems.ITEMS.register("bandit_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(BANDIT,9411929,2896151,new Item.Properties())));
 TYPES.put("commando",COMMANDO);EGGS.put("commando",TGItems.ITEMS.register("commando_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(COMMANDO,1643794,7635054,new Item.Properties())));
 TYPES.put("dictatordave",DICTATORDAVE);EGGS.put("dictatordave",TGItems.ITEMS.register("dictatordave_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(DICTATORDAVE,9411929,10526720,new Item.Properties())));
 TYPES.put("cyberdemon",CYBERDEMON);EGGS.put("cyberdemon",TGItems.ITEMS.register("cyberdemon_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(CYBERDEMON,16716049,7829367,new Item.Properties())));
 TYPES.put("skeletonsoldier",SKELETONSOLDIER);EGGS.put("skeletonsoldier",TGItems.ITEMS.register("skeletonsoldier_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(SKELETONSOLDIER,4210752,15790320,new Item.Properties())));
 TYPES.put("psychosteve",PSYCHOSTEVE);EGGS.put("psychosteve",TGItems.ITEMS.register("psychosteve_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(PSYCHOSTEVE,7697512,15790320,new Item.Properties())));
 TYPES.put("stormtrooper",STORMTROOPER);EGGS.put("stormtrooper",TGItems.ITEMS.register("stormtrooper_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(STORMTROOPER,15790320,4210752,new Item.Properties())));
 TYPES.put("outcast",OUTCAST);EGGS.put("outcast",TGItems.ITEMS.register("outcast_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(OUTCAST,1052688,16711680,new Item.Properties())));
 TYPES.put("zombiepigmansoldier",ZOMBIEPIGMANSOLDIER);EGGS.put("zombiepigmansoldier",TGItems.ITEMS.register("zombiepigmansoldier_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ZOMBIEPIGMANSOLDIER,16716049,7798784,new Item.Properties())));
 TYPES.put("supermutantbasic",SUPERMUTANTBASIC);EGGS.put("supermutantbasic",TGItems.ITEMS.register("supermutantbasic_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(SUPERMUTANTBASIC,13019499,7427374,new Item.Properties())));
 TYPES.put("supermutantelite",SUPERMUTANTELITE);EGGS.put("supermutantelite",TGItems.ITEMS.register("supermutantelite_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(SUPERMUTANTELITE,13019499,6374686,new Item.Properties())));
 TYPES.put("supermutantheavy",SUPERMUTANTHEAVY);EGGS.put("supermutantheavy",TGItems.ITEMS.register("supermutantheavy_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(SUPERMUTANTHEAVY,13019499,5321998,new Item.Properties())));
 TYPES.put("zombiepoliceman",ZOMBIEPOLICEMAN);EGGS.put("zombiepoliceman",TGItems.ITEMS.register("zombiepoliceman_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ZOMBIEPOLICEMAN,3158064,255,new Item.Properties())));
 }
 public static void attributes(EntityAttributeCreationEvent event){
 event.put(ZOMBIESOLDIER.get(),ZombieSoldier.attributes().build());
 event.put(ZOMBIEFARMER.get(),ZombieFarmer.attributes().build());
 event.put(ZOMBIEMINER.get(),ZombieMiner.attributes().build());
 event.put(ARMYSOLDIER.get(),ArmySoldier.attributes().build());
 event.put(BANDIT.get(),Bandit.attributes().build());
 event.put(COMMANDO.get(),Commando.attributes().build());
 event.put(DICTATORDAVE.get(),DictatorDave.attributes().build());
 event.put(CYBERDEMON.get(),CyberDemon.attributes().build());
 event.put(SKELETONSOLDIER.get(),SkeletonSoldier.attributes().build());
 event.put(PSYCHOSTEVE.get(),PsychoSteve.attributes().build());
 event.put(STORMTROOPER.get(),StormTrooper.attributes().build());
 event.put(OUTCAST.get(),Outcast.attributes().build());
 event.put(ZOMBIEPIGMANSOLDIER.get(),ZombiePigmanSoldier.attributes().build());
 event.put(SUPERMUTANTBASIC.get(),SuperMutantBasic.attributes().build());
 event.put(SUPERMUTANTELITE.get(),SuperMutantElite.attributes().build());
 event.put(SUPERMUTANTHEAVY.get(),SuperMutantHeavy.attributes().build());
 event.put(ZOMBIEPOLICEMAN.get(),ZombiePoliceman.attributes().build());
 }
 public static void init(){}
 private TGNpcs(){}
}
