package techguns;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.*;
import techguns.machines.*;
public final class TGFabricator {
    public static final RegistryObject<MachinePartBlock> HOUSING=TGWeaponBlocks.BLOCKS.register("fabricator_housing",()->new MachinePartBlock(false));
    public static final RegistryObject<MachinePartBlock> GLASS=TGWeaponBlocks.BLOCKS.register("fabricator_glass",()->new MachinePartBlock(true));
    public static final RegistryObject<MachinePartBlock> REACTION_HOUSING=TGWeaponBlocks.BLOCKS.register("reactionchamber_housing",()->new MachinePartBlock(false,true));
    public static final RegistryObject<MachinePartBlock> REACTION_GLASS=TGWeaponBlocks.BLOCKS.register("reactionchamber_glass",()->new MachinePartBlock(true,true));
    public static final RegistryObject<Item> HOUSING_ITEM=TGItems.ITEMS.register("fabricator_housing",()->new BlockItem(HOUSING.get(),new Item.Properties()));
    public static final RegistryObject<Item> GLASS_ITEM=TGItems.ITEMS.register("fabricator_glass",()->new BlockItem(GLASS.get(),new Item.Properties()));
    public static final RegistryObject<Item> REACTION_HOUSING_ITEM=TGItems.ITEMS.register("reactionchamber_housing",()->new BlockItem(REACTION_HOUSING.get(),new Item.Properties()));
    public static final RegistryObject<Item> REACTION_GLASS_ITEM=TGItems.ITEMS.register("reactionchamber_glass",()->new BlockItem(REACTION_GLASS.get(),new Item.Properties()));
    public static final RegistryObject<BlockEntityType<MachinePartEntity>> ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("machine_part",()->BlockEntityType.Builder.of(MachinePartEntity::new,HOUSING.get(),GLASS.get(),REACTION_HOUSING.get(),REACTION_GLASS.get()).build(null));
    public static void init(){}private TGFabricator(){}
}
