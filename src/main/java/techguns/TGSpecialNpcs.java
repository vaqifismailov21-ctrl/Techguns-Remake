package techguns;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import techguns.entities.npcs.*;
public final class TGSpecialNpcs {
    public static final RegistryObject<EntityType<AlienBug>> ALIEN_BUG=TGEntities.ENTITIES.register("alienbug",()->EntityType.Builder.<AlienBug>of(AlienBug::new,MobCategory.MONSTER).sized(1.1f,1.2f).clientTrackingRange(5).updateInterval(3).build("techguns:alienbug"));
    public static final RegistryObject<EntityType<Ghastling>> GHASTLING=TGEntities.ENTITIES.register("ghastling",()->EntityType.Builder.<Ghastling>of(Ghastling::new,MobCategory.MONSTER).sized(1,2.1f).fireImmune().clientTrackingRange(5).updateInterval(3).build("techguns:ghastling"));
    public static final RegistryObject<EntityType<AttackHelicopter>> HELICOPTER=TGEntities.ENTITIES.register("attackhelicopter",()->EntityType.Builder.<AttackHelicopter>of(AttackHelicopter::new,MobCategory.MONSTER).sized(4,4).fireImmune().clientTrackingRange(13).updateInterval(3).build("techguns:attackhelicopter"));
    public static final Map<String,RegistryObject<Item>> EGGS=new LinkedHashMap<>();
    static {
        EGGS.put("alienbug",TGItems.ITEMS.register("alienbug_spawn_egg",()->new net.minecraftforge.common.ForgeSpawnEggItem(ALIEN_BUG,0xc6a96b,0x71552e,new Item.Properties())));
        EGGS.put("ghastling",TGItems.ITEMS.register("ghastling_spawn_egg",()->new net.minecraftforge.common.ForgeSpawnEggItem(GHASTLING,0xaeaeae,0xce81ff,new Item.Properties())));
        EGGS.put("attackhelicopter",TGItems.ITEMS.register("attackhelicopter_spawn_egg",()->new net.minecraftforge.common.ForgeSpawnEggItem(HELICOPTER,0x373d23,0x8ec0d7,new Item.Properties())));
    }
    public static void init(IEventBus bus){bus.addListener(TGSpecialNpcs::attributes);}
    private static void attributes(EntityAttributeCreationEvent event){event.put(ALIEN_BUG.get(),AlienBug.attributes().build());event.put(GHASTLING.get(),Ghastling.attributes().build());event.put(HELICOPTER.get(),AttackHelicopter.attributes().build());}
    private TGSpecialNpcs(){}
}
