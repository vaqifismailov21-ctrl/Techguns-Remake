package techguns.power;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.TGItems;
import techguns.TGMenuTypes;
import techguns.TGWeaponBlocks;

/**
 * Power blocks added at the user's request; the original Techguns has none and relies on other mods. Modelled on the
 * common tech-mod set: a solid fuel generator (like a Thermal Stirling dynamo), a liquid fuel generator for oil, fuels and
 * lava (like a compression/magmatic dynamo or a combustion generator), a solar panel and an energy cell. Generators give
 * Forge Energy (FE) to every adjacent block that accepts it and can be drained by cables of other mods.
 */
public final class PowerBlocks {
    /** FE per tick while burning, buffer and the most given away per tick. */
    public static final int GENERATOR_RATE=40,GENERATOR_CAPACITY=100_000,GENERATOR_OUTPUT=400;
    /** Liquid generator: FE per tick, buffer, output, tank size (mB) and mB taken per refill. */
    public static final int LIQUID_RATE=80,LIQUID_CAPACITY=200_000,LIQUID_OUTPUT=800,LIQUID_TANK=8000,LIQUID_DRAW=10;
    /** FE per tick in full daylight (less in rain), buffer and output. */
    public static final int SOLAR_RATE=20,SOLAR_CAPACITY=20_000,SOLAR_OUTPUT=200;
    /** Energy cell: buffer and transfer per tick (in on every side, out through the front). */
    public static final int CELL_CAPACITY=1_000_000,CELL_TRANSFER=1000;

    public static final DeferredHolder<net.minecraft.world.level.block.Block, Block> FUEL_GENERATOR=TGWeaponBlocks.BLOCKS.register("fuel_generator",()->new GeneratorBlock(false));
    public static final DeferredHolder<net.minecraft.world.level.block.Block, Block> LIQUID_GENERATOR=TGWeaponBlocks.BLOCKS.register("liquid_generator",()->new GeneratorBlock(true));
    public static final DeferredHolder<net.minecraft.world.level.block.Block, Block> SOLAR_PANEL=TGWeaponBlocks.BLOCKS.register("solar_panel",SolarPanelBlock::new);
    public static final DeferredHolder<net.minecraft.world.level.block.Block, Block> ENERGY_CELL=TGWeaponBlocks.BLOCKS.register("energy_cell",EnergyCellBlock::new);
    public static final DeferredHolder<net.minecraft.world.item.Item, Item> FUEL_GENERATOR_ITEM=TGItems.ITEMS.register("fuel_generator",()->new BlockItem(FUEL_GENERATOR.get(),new Item.Properties()));
    public static final DeferredHolder<net.minecraft.world.item.Item, Item> LIQUID_GENERATOR_ITEM=TGItems.ITEMS.register("liquid_generator",()->new BlockItem(LIQUID_GENERATOR.get(),new Item.Properties()));
    public static final DeferredHolder<net.minecraft.world.item.Item, Item> SOLAR_PANEL_ITEM=TGItems.ITEMS.register("solar_panel",()->new BlockItem(SOLAR_PANEL.get(),new Item.Properties()));
    public static final DeferredHolder<net.minecraft.world.item.Item, Item> ENERGY_CELL_ITEM=TGItems.ITEMS.register("energy_cell",()->new EnergyCellItem(ENERGY_CELL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FuelGeneratorEntity>> FUEL_GENERATOR_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("fuel_generator",()->BlockEntityType.Builder.of(FuelGeneratorEntity::new,FUEL_GENERATOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LiquidGeneratorEntity>> LIQUID_GENERATOR_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("liquid_generator",()->BlockEntityType.Builder.of(LiquidGeneratorEntity::new,LIQUID_GENERATOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarPanelEntity>> SOLAR_PANEL_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("solar_panel",()->BlockEntityType.Builder.of(SolarPanelEntity::new,SOLAR_PANEL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyCellEntity>> ENERGY_CELL_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("energy_cell",()->BlockEntityType.Builder.of(EnergyCellEntity::new,ENERGY_CELL.get()).build(null));
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<GeneratorMenu>> GENERATOR_MENU=TGMenuTypes.MENUS.register("generator",()->IMenuTypeExtension.create(GeneratorMenu::new));

    /** Class loading registers the entries above. */
    public static void init(){}
    private PowerBlocks(){}
}
