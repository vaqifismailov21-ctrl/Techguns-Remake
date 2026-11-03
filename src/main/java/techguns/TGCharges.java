package techguns;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.blocks.MiningChargeBlock;
import techguns.tileentities.MiningChargeEntity;
public final class TGCharges {
    public static final DeferredHolder<net.minecraft.world.level.block.Block, MiningChargeBlock> TNT=TGWeaponBlocks.BLOCKS.register("tnt_charge",()->new MiningChargeBlock(false));
    public static final DeferredHolder<net.minecraft.world.level.block.Block, MiningChargeBlock> ADVANCED=TGWeaponBlocks.BLOCKS.register("advanced_charge",()->new MiningChargeBlock(true));
    public static final DeferredHolder<net.minecraft.world.item.Item, Item> TNT_ITEM=TGItems.ITEMS.register("tnt_charge",()->new BlockItem(TNT.get(),new Item.Properties()));
    public static final DeferredHolder<net.minecraft.world.item.Item, Item> ADVANCED_ITEM=TGItems.ITEMS.register("advanced_charge",()->new BlockItem(ADVANCED.get(),new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MiningChargeEntity>> ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("mining_charge",()->BlockEntityType.Builder.of(MiningChargeEntity::new,TNT.get(),ADVANCED.get()).build(null));
    public static void init(){}private TGCharges(){}
}
