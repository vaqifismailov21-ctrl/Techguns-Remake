package techguns;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import techguns.oredrill.*;

/** Original BlockOreDrill (meta 0-4, 5 = hidden scaffold state) with its slave and master tile entities. */
public final class TGOreDrill {
    public static final Map<OreDrillType,RegistryObject<OreDrillBlock>> BLOCKS=new EnumMap<>(OreDrillType.class);
    public static final Map<OreDrillType,RegistryObject<Item>> ITEMS=new EnumMap<>(OreDrillType.class);
    public static final RegistryObject<OreDrillBlock> SCAFFOLD;
    public static final RegistryObject<OreDrillBlock> CONTROLLER;
    static{
        for(var type:OreDrillType.values()){
            RegistryObject<OreDrillBlock> block=TGWeaponBlocks.BLOCKS.register(type.id(),()->type==OreDrillType.SCAFFOLD?new OreDrillScaffoldBlock():new OreDrillBlock(type));
            BLOCKS.put(type,block);
            ITEMS.put(type,TGItems.ITEMS.register(type.id(),()->new OreDrillBlockItem(block.get())));
        }
        SCAFFOLD=BLOCKS.get(OreDrillType.SCAFFOLD);CONTROLLER=BLOCKS.get(OreDrillType.CONTROLLER);
    }
    public static final RegistryObject<BlockEntityType<OreDrillSlaveEntity>> SLAVE=TGWeaponBlocks.BLOCK_ENTITIES.register("oredrill_slave",()->BlockEntityType.Builder.of(OreDrillSlaveEntity::new,BLOCKS.get(OreDrillType.FRAME).get(),BLOCKS.get(OreDrillType.SCAFFOLD).get(),BLOCKS.get(OreDrillType.ROD).get(),BLOCKS.get(OreDrillType.ENGINE).get()).build(null));
    public static final RegistryObject<BlockEntityType<OreDrillTileEntMaster>> MASTER=TGWeaponBlocks.BLOCK_ENTITIES.register("oredrill_controller",()->BlockEntityType.Builder.of(OreDrillTileEntMaster::new,CONTROLLER.get()).build(null));
    public static void init(){}
    private TGOreDrill(){}
}
