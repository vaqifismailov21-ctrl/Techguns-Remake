package techguns.power.plant;

import java.util.List;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import techguns.TGItems;
import techguns.TGMenuTypes;
import techguns.TGWeaponBlocks;

/** Blocks, block entities and menu of the multiblock power plants. */
public final class PlantBlocks {
    private PlantBlocks(){}
    private static RegistryObject<Block> block(String id,java.util.function.Supplier<Block> block){return TGWeaponBlocks.BLOCKS.register(id,block);}
    /** Block item with the how-to-build lines of the controllers (techguns.plant.<id>.tip1..3). */
    private static RegistryObject<Item> item(RegistryObject<Block> block){return TGItems.ITEMS.register(block.getId().getPath(),()->new BlockItem(block.get(),new Item.Properties()){
        @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,net.minecraft.world.level.Level level,java.util.List<net.minecraft.network.chat.Component> tooltip,net.minecraft.world.item.TooltipFlag flag){
            String key="techguns.plant."+block.getId().getPath()+".tip";
            for(int i=1;i<=4;i++)if(net.minecraft.locale.Language.getInstance().has(key+i))tooltip.add(net.minecraft.network.chat.Component.translatable(key+i).withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    });}

    public static final RegistryObject<Block> CASING=block("plant_casing",()->new PlantPartBlock(PlantPartBlock.Type.CASING));
    public static final RegistryObject<Block> GLASS=block("plant_glass",()->new PlantPartBlock(PlantPartBlock.Type.GLASS));
    public static final RegistryObject<Block> TOWER=block("turbine_tower",()->new PlantPartBlock(PlantPartBlock.Type.TOWER));
    public static final RegistryObject<Block> COIL=block("fusion_coil",()->new PlantPartBlock(PlantPartBlock.Type.COIL));
    public static final RegistryObject<Block> HUB=block("rotor_hub",RotorHubBlock::new);
    public static final RegistryObject<Block> WIND=block("wind_plant",()->new PlantControllerBlock(PlantKind.WIND));
    public static final RegistryObject<Block> GEOTHERMAL=block("geothermal_plant",()->new PlantControllerBlock(PlantKind.GEOTHERMAL));
    public static final RegistryObject<Block> FISSION=block("fission_reactor",()->new PlantControllerBlock(PlantKind.FISSION));
    public static final RegistryObject<Block> FUSION=block("fusion_reactor",()->new PlantControllerBlock(PlantKind.FUSION));

    public static final RegistryObject<Item> CASING_ITEM=item(CASING),GLASS_ITEM=item(GLASS),TOWER_ITEM=item(TOWER),COIL_ITEM=item(COIL),
            HUB_ITEM=item(HUB),WIND_ITEM=item(WIND),GEOTHERMAL_ITEM=item(GEOTHERMAL),FISSION_ITEM=item(FISSION),FUSION_ITEM=item(FUSION);

    public static final RegistryObject<Item> BLADE_ITEM=TGItems.ITEMS.register("turbine_blade",()->new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<BlockEntityType<RotorHubEntity>> HUB_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("rotor_hub",()->BlockEntityType.Builder.of(RotorHubEntity::new,HUB.get()).build(null));
    public static final RegistryObject<BlockEntityType<PlantPartEntity>> PART_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("plant_part",()->BlockEntityType.Builder.of(PlantPartEntity::new,CASING.get(),GLASS.get(),TOWER.get(),COIL.get()).build(null));
    public static final RegistryObject<BlockEntityType<WindPlant>> WIND_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("wind_plant",()->BlockEntityType.Builder.of(WindPlant::new,WIND.get()).build(null));
    public static final RegistryObject<BlockEntityType<GeothermalPlant>> GEOTHERMAL_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("geothermal_plant",()->BlockEntityType.Builder.of(GeothermalPlant::new,GEOTHERMAL.get()).build(null));
    public static final RegistryObject<BlockEntityType<FissionReactor>> FISSION_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("fission_reactor",()->BlockEntityType.Builder.of(FissionReactor::new,FISSION.get()).build(null));
    public static final RegistryObject<BlockEntityType<FusionReactor>> FUSION_ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("fusion_reactor",()->BlockEntityType.Builder.of(FusionReactor::new,FUSION.get()).build(null));
    public static final RegistryObject<MenuType<PlantMenu>> MENU=TGMenuTypes.MENUS.register("power_plant",()->IForgeMenuType.create(PlantMenu::new));

    public static List<RegistryObject<Item>> tabItems(){return List.of(WIND_ITEM,GEOTHERMAL_ITEM,FISSION_ITEM,FUSION_ITEM,CASING_ITEM,GLASS_ITEM,TOWER_ITEM,HUB_ITEM,BLADE_ITEM,COIL_ITEM);}
    public static void init(){}
}
