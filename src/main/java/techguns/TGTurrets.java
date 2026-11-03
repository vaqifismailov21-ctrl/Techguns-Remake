package techguns;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import techguns.entities.npcs.NPCTurret;
import techguns.machines.TurretBlock;
import techguns.machines.TurretBlockItem;
import techguns.tileentities.TurretTileEnt;

/** Original turret: BasicMachine meta 3 (TurretTileEnt) and entity techguns:turret (tracking range 80, update interval 3). */
public final class TGTurrets {
    public static final RegistryObject<TurretBlock> BLOCK=TGWeaponBlocks.BLOCKS.register("turret",TurretBlock::new);
    public static final RegistryObject<Item> ITEM=TGItems.ITEMS.register("turret",()->new TurretBlockItem(BLOCK.get()));
    public static final RegistryObject<BlockEntityType<TurretTileEnt>> ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("turret",()->BlockEntityType.Builder.of(TurretTileEnt::new,BLOCK.get()).build(null));
    public static final RegistryObject<EntityType<NPCTurret>> TURRET=TGEntities.ENTITIES.register("turret",()->EntityType.Builder.<NPCTurret>of(NPCTurret::new,MobCategory.MISC).sized(0.6f,0.9f).clientTrackingRange(5).updateInterval(3).build("techguns:turret"));
    public static void init(IEventBus bus){bus.addListener(TGTurrets::attributes);}
    private static void attributes(EntityAttributeCreationEvent event){event.put(TURRET.get(),NPCTurret.attributes().build());}
    private TGTurrets(){}
}
