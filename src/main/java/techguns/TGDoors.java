package techguns;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import techguns.blocks.*;
import techguns.items.LargeDoorItem;
import techguns.tileentities.LargeDoorEntity;
public final class TGDoors {
    public static final RegistryObject<BunkerDoorBlock> BUNKER=TGWeaponBlocks.BLOCKS.register("bunker_door",BunkerDoorBlock::new);
    public static final RegistryObject<Item> BUNKER_ITEM=TGItems.ITEMS.register("bunker_door",()->new DoubleHighBlockItem(BUNKER.get(),new Item.Properties()));
    public static final List<RegistryObject<LargeDoorBlock>> BLOCKS=new ArrayList<>();
    public static final List<RegistryObject<Item>> ITEMS=new ArrayList<>();
    static{for(int i=0;i<4;i++){int variant=i;String id=new String[]{"metal_door_3x3","hangar_door_up","hangar_door_down","nether_door_3x3"}[i];var block=TGWeaponBlocks.BLOCKS.register(id,()->new LargeDoorBlock(variant));BLOCKS.add(block);ITEMS.add(TGItems.ITEMS.register(id,()->new LargeDoorItem(block.get())));}}
    public static final RegistryObject<BlockEntityType<LargeDoorEntity>> ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("large_door",()->BlockEntityType.Builder.of(LargeDoorEntity::new,BLOCKS.stream().map(RegistryObject::get).toArray(net.minecraft.world.level.block.Block[]::new)).build(null));
    public static void init(){}private TGDoors(){}
}
