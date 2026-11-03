package techguns;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.blocks.*;
import techguns.items.LargeDoorItem;
import techguns.tileentities.LargeDoorEntity;
public final class TGDoors {
    public static final DeferredHolder<net.minecraft.world.level.block.Block, BunkerDoorBlock> BUNKER=TGWeaponBlocks.BLOCKS.register("bunker_door",BunkerDoorBlock::new);
    public static final DeferredHolder<net.minecraft.world.item.Item, Item> BUNKER_ITEM=TGItems.ITEMS.register("bunker_door",()->new DoubleHighBlockItem(BUNKER.get(),new Item.Properties()));
    public static final List<DeferredHolder<net.minecraft.world.level.block.Block, LargeDoorBlock>> BLOCKS=new ArrayList<>();
    public static final List<DeferredHolder<net.minecraft.world.item.Item, Item>> ITEMS=new ArrayList<>();
    static{for(int i=0;i<4;i++){int variant=i;String id=new String[]{"metal_door_3x3","hangar_door_up","hangar_door_down","nether_door_3x3"}[i];var block=TGWeaponBlocks.BLOCKS.register(id,()->new LargeDoorBlock(variant));BLOCKS.add(block);ITEMS.add(TGItems.ITEMS.register(id,()->new LargeDoorItem(block.get())));}}
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LargeDoorEntity>> ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("large_door",()->BlockEntityType.Builder.of(LargeDoorEntity::new,BLOCKS.stream().map(DeferredHolder::get).toArray(net.minecraft.world.level.block.Block[]::new)).build(null));
    public static void init(){}private TGDoors(){}
}
