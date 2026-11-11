package techguns.power.wire;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.registries.RegistryObject;
import techguns.TGItems;
import techguns.TGWeaponBlocks;

/** Registry entries of the power wires: coils, connectors, the transformer and the pylon. */
public final class WireBlocks {
    private WireBlocks(){}
    public static final RegistryObject<Block> CONNECTOR_COPPER=TGWeaponBlocks.BLOCKS.register("wire_connector_copper",()->new ConnectorBlock(WireTier.COPPER));
    public static final RegistryObject<Block> CONNECTOR_STEEL=TGWeaponBlocks.BLOCKS.register("wire_connector_steel",()->new ConnectorBlock(WireTier.STEEL));
    public static final RegistryObject<Block> CONNECTOR_HV=TGWeaponBlocks.BLOCKS.register("wire_connector_high_voltage",()->new ConnectorBlock(WireTier.HIGH_VOLTAGE));
    public static final RegistryObject<Block> TRANSFORMER=TGWeaponBlocks.BLOCKS.register("transformer",TransformerBlock::new);
    public static final RegistryObject<Block> PYLON=TGWeaponBlocks.BLOCKS.register("pylon",PylonBlock::new);

    public static final RegistryObject<Item> COIL_COPPER=TGItems.ITEMS.register("wire_coil_copper",()->new WireCoilItem(WireTier.COPPER));
    public static final RegistryObject<Item> COIL_STEEL=TGItems.ITEMS.register("wire_coil_steel",()->new WireCoilItem(WireTier.STEEL));
    public static final RegistryObject<Item> COIL_COPPER_INSULATED=TGItems.ITEMS.register("wire_coil_copper_insulated",()->new WireCoilItem(WireTier.COPPER,true));
    public static final RegistryObject<Item> COIL_STEEL_INSULATED=TGItems.ITEMS.register("wire_coil_steel_insulated",()->new WireCoilItem(WireTier.STEEL,true));
    public static final RegistryObject<Item> COIL_HV=TGItems.ITEMS.register("wire_coil_high_voltage",()->new WireCoilItem(WireTier.HIGH_VOLTAGE));
    public static final RegistryObject<Item> CONNECTOR_COPPER_ITEM=item(CONNECTOR_COPPER),CONNECTOR_STEEL_ITEM=item(CONNECTOR_STEEL),CONNECTOR_HV_ITEM=item(CONNECTOR_HV),
        TRANSFORMER_ITEM=item(TRANSFORMER),PYLON_ITEM=item(PYLON);
    private static RegistryObject<Item> item(RegistryObject<Block> block){return TGItems.ITEMS.register(block.getId().getPath(),()->new BlockItem(block.get(),new Item.Properties()));}

    public static final RegistryObject<BlockEntityType<ConnectorEntity>> CONNECTOR_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("wire_connector",
        ()->BlockEntityType.Builder.of(ConnectorEntity::new,CONNECTOR_COPPER.get(),CONNECTOR_STEEL.get(),CONNECTOR_HV.get()).build(null));
    public static final RegistryObject<BlockEntityType<TransformerEntity>> TRANSFORMER_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("transformer",
        ()->BlockEntityType.Builder.of(TransformerEntity::new,TRANSFORMER.get()).build(null));
    public static final RegistryObject<BlockEntityType<PylonEntity>> PYLON_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("pylon",
        ()->BlockEntityType.Builder.of(PylonEntity::new,PYLON.get()).build(null));

    public static Item coil(WireTier tier,boolean insulated){
        if(insulated&&tier==WireTier.COPPER)return COIL_COPPER_INSULATED.get();
        if(insulated&&tier==WireTier.STEEL)return COIL_STEEL_INSULATED.get();
        return coil(tier);
    }
    public static Item coil(WireTier tier){return switch(tier){case COPPER->COIL_COPPER.get();case STEEL->COIL_STEEL.get();case HIGH_VOLTAGE->COIL_HV.get();};}
    /** Main block (the one wires are stored on) of a wire host block at pos, or null. */
    public static BlockPos hostAt(Level level,BlockPos pos){
        var state=level.getBlockState(pos);
        if(state.getBlock() instanceof PylonBlock)return pos.below(state.getValue(PylonBlock.PART));
        if(state.getBlock() instanceof TransformerBlock)return state.getValue(TransformerBlock.HALF)==DoubleBlockHalf.UPPER?pos.below():pos;
        return state.getBlock() instanceof WireHost?pos:null;
    }
    public static boolean isWireBlock(BlockState state){return state.getBlock() instanceof WireHost;}
    public static List<RegistryObject<Item>> tabItems(){return List.of(COIL_COPPER,COIL_COPPER_INSULATED,COIL_STEEL,COIL_STEEL_INSULATED,COIL_HV,CONNECTOR_COPPER_ITEM,CONNECTOR_STEEL_ITEM,CONNECTOR_HV_ITEM,TRANSFORMER_ITEM,PYLON_ITEM);}
    public static void init(){}
}
