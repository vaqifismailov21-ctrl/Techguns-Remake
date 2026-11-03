package techguns;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.entities.npcs.*;
public final class TGSpecialNpcs {
    public static final DeferredHolder<EntityType<?>, EntityType<AlienBug>> ALIEN_BUG=TGEntities.ENTITIES.register("alienbug",()->EntityType.Builder.<AlienBug>of(AlienBug::new,MobCategory.MONSTER).sized(1.1f,1.2f).clientTrackingRange(5).updateInterval(3).build("techguns:alienbug"));
    public static final DeferredHolder<EntityType<?>, EntityType<Ghastling>> GHASTLING=TGEntities.ENTITIES.register("ghastling",()->EntityType.Builder.<Ghastling>of(Ghastling::new,MobCategory.MONSTER).sized(1,2.1f).eyeHeight(1.5f).fireImmune().clientTrackingRange(5).updateInterval(3).build("techguns:ghastling"));
    public static final DeferredHolder<EntityType<?>, EntityType<AttackHelicopter>> HELICOPTER=TGEntities.ENTITIES.register("attackhelicopter",()->EntityType.Builder.<AttackHelicopter>of(AttackHelicopter::new,MobCategory.MONSTER).sized(4,4).eyeHeight(.5f).fireImmune().clientTrackingRange(13).updateInterval(3).build("techguns:attackhelicopter"));
    public static final Map<String,DeferredHolder<net.minecraft.world.item.Item, Item>> EGGS=new LinkedHashMap<>();
    static {
        EGGS.put("alienbug",TGItems.ITEMS.register("alienbug_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ALIEN_BUG,0xc6a96b,0x71552e,new Item.Properties())));
        EGGS.put("ghastling",TGItems.ITEMS.register("ghastling_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(GHASTLING,0xaeaeae,0xce81ff,new Item.Properties())));
        EGGS.put("attackhelicopter",TGItems.ITEMS.register("attackhelicopter_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(HELICOPTER,0x373d23,0x8ec0d7,new Item.Properties())));
    }
    public static void init(IEventBus bus){bus.addListener(TGSpecialNpcs::attributes);}
    private static void attributes(EntityAttributeCreationEvent event){event.put(ALIEN_BUG.get(),AlienBug.attributes().build());event.put(GHASTLING.get(),Ghastling.attributes().build());event.put(HELICOPTER.get(),AttackHelicopter.attributes().build());}
    private TGSpecialNpcs(){}
}
