package techguns;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.*;
import techguns.machines.*;
public final class TGFabricator {
    public static final DeferredHolder<net.minecraft.world.level.block.Block, MachinePartBlock> HOUSING=TGWeaponBlocks.BLOCKS.register("fabricator_housing",()->new MachinePartBlock(false));
    public static final DeferredHolder<net.minecraft.world.level.block.Block, MachinePartBlock> GLASS=TGWeaponBlocks.BLOCKS.register("fabricator_glass",()->new MachinePartBlock(true));
    public static final DeferredHolder<net.minecraft.world.level.block.Block, MachinePartBlock> REACTION_HOUSING=TGWeaponBlocks.BLOCKS.register("reactionchamber_housing",()->new MachinePartBlock(false,true));
    public static final DeferredHolder<net.minecraft.world.level.block.Block, MachinePartBlock> REACTION_GLASS=TGWeaponBlocks.BLOCKS.register("reactionchamber_glass",()->new MachinePartBlock(true,true));
    public static final DeferredHolder<net.minecraft.world.item.Item, Item> HOUSING_ITEM=TGItems.ITEMS.register("fabricator_housing",()->new BlockItem(HOUSING.get(),new Item.Properties()));
    public static final DeferredHolder<net.minecraft.world.item.Item, Item> GLASS_ITEM=TGItems.ITEMS.register("fabricator_glass",()->new BlockItem(GLASS.get(),new Item.Properties()));
    public static final DeferredHolder<net.minecraft.world.item.Item, Item> REACTION_HOUSING_ITEM=TGItems.ITEMS.register("reactionchamber_housing",()->new BlockItem(REACTION_HOUSING.get(),new Item.Properties()));
    public static final DeferredHolder<net.minecraft.world.item.Item, Item> REACTION_GLASS_ITEM=TGItems.ITEMS.register("reactionchamber_glass",()->new BlockItem(REACTION_GLASS.get(),new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachinePartEntity>> ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("machine_part",()->BlockEntityType.Builder.of(MachinePartEntity::new,HOUSING.get(),GLASS.get(),REACTION_HOUSING.get(),REACTION_GLASS.get()).build(null));
    public static void init(){}private TGFabricator(){}
}
