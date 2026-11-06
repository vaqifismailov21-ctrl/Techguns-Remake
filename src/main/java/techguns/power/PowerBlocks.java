package techguns.power;

import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import techguns.TGItems;
import techguns.TGMenuTypes;
import techguns.TGWeaponBlocks;

/**
 * Power blocks. The original Techguns has none and relies on other mods; these give and take Forge Energy (FE), so
 * they work with the Techguns machines and with cables and machines of other mods. All of them are drawn by
 * {@code PowerRenderer} with box models in the style of the original machines.
 */
public final class PowerBlocks {
    /** Fuel generator: FE per tick while burning, buffer and the most given away per tick. */
    public static final int GENERATOR_RATE=40,GENERATOR_CAPACITY=100_000,GENERATOR_OUTPUT=400;
    /** Liquid generator: FE per tick, buffer, output, tank size (mB) and mB taken per refill. */
    public static final int LIQUID_RATE=80,LIQUID_CAPACITY=200_000,LIQUID_OUTPUT=800,LIQUID_TANK=8000,LIQUID_DRAW=10;
    /** Solar panels by tier: FE per tick in full daylight (less in rain), buffer and output. */
    public static final int[] SOLAR_RATES={20,60,160},SOLAR_CAPACITIES={20_000,60_000,160_000},SOLAR_OUTPUTS={200,600,1600};
    public static final int SOLAR_RATE=20,SOLAR_CAPACITY=20_000,SOLAR_OUTPUT=200;
    /** Energy cell and energy bank: buffer and transfer per tick (in on every side, out through the front). */
    public static final int CELL_CAPACITY=1_000_000,CELL_TRANSFER=1000,BANK_CAPACITY=10_000_000,BANK_TRANSFER=10_000;
    /** Geothermal generator: FE per tick for every lava source below or beside it, buffer, output. */
    public static final int GEOTHERMAL_PER_LAVA=15,GEOTHERMAL_CAPACITY=50_000,GEOTHERMAL_OUTPUT=400;
    /** Nuclear generator: FE per tick, FE from one nuclear micro reactor, buffer, output and the heat that stops it. */
    public static final int NUCLEAR_RATE=400,NUCLEAR_CELL_ENERGY=4_800_000,NUCLEAR_CAPACITY=500_000,NUCLEAR_OUTPUT=2000,NUCLEAR_HEAT_MAX=1000;
    /** Wind turbine: best FE per tick (at Y 128 and above with free space), buffer, output, masts allowed. */
    public static final int WIND_RATE=100,WIND_CAPACITY=50_000,WIND_OUTPUT=500,WIND_MAX_MASTS=6;
    /** Energy cable: FE moved per tick and kept in each piece. */
    public static final int CABLE_TRANSFER=5000;
    /** Floodlight: FE per tick, buffer and beam length. */
    public static final int FLOODLIGHT_USE=2,FLOODLIGHT_CAPACITY=2000,FLOODLIGHT_RANGE=24;

    private static <T extends Block> RegistryObject<T> block(String id,Supplier<T> block){return TGWeaponBlocks.BLOCKS.register(id,block);}
    private static RegistryObject<Item> item(String id,RegistryObject<? extends Block> block){return TGItems.ITEMS.register(id,()->new PowerBlockItem(block.get()));}

    public static final RegistryObject<Block> FUEL_GENERATOR=block("fuel_generator",()->new GeneratorBlock(false));
    public static final RegistryObject<Block> LIQUID_GENERATOR=block("liquid_generator",()->new GeneratorBlock(true));
    public static final RegistryObject<Block> SOLAR_PANEL=block("solar_panel",()->new SolarPanelBlock(0));
    public static final RegistryObject<Block> SOLAR_PANEL_ADVANCED=block("solar_panel_advanced",()->new SolarPanelBlock(1));
    public static final RegistryObject<Block> SOLAR_PANEL_INDUSTRIAL=block("solar_panel_industrial",()->new SolarPanelBlock(2));
    public static final RegistryObject<Block> ENERGY_CELL=block("energy_cell",()->new EnergyCellBlock(false));
    public static final RegistryObject<Block> ENERGY_BANK=block("energy_bank",()->new EnergyCellBlock(true));
    public static final RegistryObject<Block> GEOTHERMAL_GENERATOR=block("geothermal_generator",GeothermalGeneratorBlock::new);
    public static final RegistryObject<Block> NUCLEAR_GENERATOR=block("nuclear_generator",NuclearGeneratorBlock::new);
    public static final RegistryObject<Block> WIND_TURBINE=block("wind_turbine",WindTurbineBlock::new);
    public static final RegistryObject<Block> ENERGY_CABLE=block("energy_cable",EnergyCableBlock::new);
    public static final RegistryObject<Block> FLOODLIGHT=block("floodlight",FloodlightBlock::new);
    public static final RegistryObject<Block> FLOODLIGHT_LIGHT=block("floodlight_light",FloodlightLightBlock::new);

    public static final RegistryObject<Item> FUEL_GENERATOR_ITEM=item("fuel_generator",FUEL_GENERATOR);
    public static final RegistryObject<Item> LIQUID_GENERATOR_ITEM=item("liquid_generator",LIQUID_GENERATOR);
    public static final RegistryObject<Item> GEOTHERMAL_GENERATOR_ITEM=item("geothermal_generator",GEOTHERMAL_GENERATOR);
    public static final RegistryObject<Item> NUCLEAR_GENERATOR_ITEM=item("nuclear_generator",NUCLEAR_GENERATOR);
    public static final RegistryObject<Item> WIND_TURBINE_ITEM=item("wind_turbine",WIND_TURBINE);
    public static final RegistryObject<Item> SOLAR_PANEL_ITEM=item("solar_panel",SOLAR_PANEL);
    public static final RegistryObject<Item> SOLAR_PANEL_ADVANCED_ITEM=item("solar_panel_advanced",SOLAR_PANEL_ADVANCED);
    public static final RegistryObject<Item> SOLAR_PANEL_INDUSTRIAL_ITEM=item("solar_panel_industrial",SOLAR_PANEL_INDUSTRIAL);
    public static final RegistryObject<Item> ENERGY_CELL_ITEM=TGItems.ITEMS.register("energy_cell",()->new EnergyCellItem(ENERGY_CELL.get(),CELL_CAPACITY));
    public static final RegistryObject<Item> ENERGY_BANK_ITEM=TGItems.ITEMS.register("energy_bank",()->new EnergyCellItem(ENERGY_BANK.get(),BANK_CAPACITY));
    public static final RegistryObject<Item> ENERGY_CABLE_ITEM=TGItems.ITEMS.register("energy_cable",()->new PowerBlockItem(ENERGY_CABLE.get(),new Item.Properties(),false));
    public static final RegistryObject<Item> FLOODLIGHT_ITEM=item("floodlight",FLOODLIGHT);

    public static final RegistryObject<BlockEntityType<FuelGeneratorEntity>> FUEL_GENERATOR_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("fuel_generator",()->BlockEntityType.Builder.of(FuelGeneratorEntity::new,FUEL_GENERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<LiquidGeneratorEntity>> LIQUID_GENERATOR_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("liquid_generator",()->BlockEntityType.Builder.of(LiquidGeneratorEntity::new,LIQUID_GENERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<SolarPanelEntity>> SOLAR_PANEL_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("solar_panel",()->BlockEntityType.Builder.of(SolarPanelEntity::new,SOLAR_PANEL.get(),SOLAR_PANEL_ADVANCED.get(),SOLAR_PANEL_INDUSTRIAL.get()).build(null));
    public static final RegistryObject<BlockEntityType<EnergyCellEntity>> ENERGY_CELL_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("energy_cell",()->BlockEntityType.Builder.of(EnergyCellEntity::new,ENERGY_CELL.get(),ENERGY_BANK.get()).build(null));
    public static final RegistryObject<BlockEntityType<GeothermalGeneratorEntity>> GEOTHERMAL_GENERATOR_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("geothermal_generator",()->BlockEntityType.Builder.of(GeothermalGeneratorEntity::new,GEOTHERMAL_GENERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<NuclearGeneratorEntity>> NUCLEAR_GENERATOR_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("nuclear_generator",()->BlockEntityType.Builder.of(NuclearGeneratorEntity::new,NUCLEAR_GENERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<WindTurbineEntity>> WIND_TURBINE_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("wind_turbine",()->BlockEntityType.Builder.of(WindTurbineEntity::new,WIND_TURBINE.get()).build(null));
    public static final RegistryObject<BlockEntityType<EnergyCableEntity>> ENERGY_CABLE_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("energy_cable",()->BlockEntityType.Builder.of(EnergyCableEntity::new,ENERGY_CABLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<FloodlightEntity>> FLOODLIGHT_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("floodlight",()->BlockEntityType.Builder.of(FloodlightEntity::new,FLOODLIGHT.get()).build(null));
    public static final RegistryObject<net.minecraft.world.inventory.MenuType<GeneratorMenu>> GENERATOR_MENU=TGMenuTypes.MENUS.register("generator",()->IForgeMenuType.create(GeneratorMenu::new));

    /** Items in the creative tab, in this order. */
    public static java.util.List<RegistryObject<Item>> tabItems(){
        // the single wind turbine, geothermal and nuclear generators are replaced by the multiblock plants; the old blocks stay for existing worlds
        var list=new java.util.ArrayList<RegistryObject<Item>>(java.util.List.of(FUEL_GENERATOR_ITEM,LIQUID_GENERATOR_ITEM,SOLAR_PANEL_ITEM,
                SOLAR_PANEL_ADVANCED_ITEM,SOLAR_PANEL_INDUSTRIAL_ITEM,ENERGY_CELL_ITEM,ENERGY_BANK_ITEM,ENERGY_CABLE_ITEM,FLOODLIGHT_ITEM));
        list.addAll(2,techguns.power.plant.PlantBlocks.tabItems());return list;
    }

    /** Client tick of the power block entities (sounds); set by the client setup, does nothing on a server. */
    public static java.util.function.Consumer<net.minecraft.world.level.block.entity.BlockEntity> clientTick=entity->{};
    /** Ticker of a power block: the server logic, or the client hook on the client. */
    public static <T extends net.minecraft.world.level.block.entity.BlockEntity,E extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> ticker(
            net.minecraft.world.level.Level level,BlockEntityType<T> type,BlockEntityType<E> expected,java.util.function.Consumer<E> server){
        if(type!=expected)return null;
        @SuppressWarnings("unchecked") net.minecraft.world.level.block.entity.BlockEntityTicker<T> ticker=level.isClientSide?(world,pos,state,entity)->clientTick.accept(entity):(world,pos,state,entity)->server.accept((E)entity);
        return ticker;
    }

    /** Class loading registers the entries above. */
    public static void init(){techguns.power.plant.PlantBlocks.init();}
    private PowerBlocks(){}
}
