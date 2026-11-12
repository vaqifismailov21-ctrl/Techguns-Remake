package techguns;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import techguns.items.guns.ammo.AmmoTypes;
import techguns.items.guns.ammo.MagazineUnloading;
import techguns.capabilities.TGExtendedPlayer;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import techguns.api.radiation.TGRadiation;

/** Fresh 1.20.1 entry point. Only implemented systems are registered here. */
@Mod(Techguns.MODID)
public final class Techguns {
    public static final String MODID = "techguns";
    public static final String NAME = "Techguns";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("techguns", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.techguns"))
                    .withSearchBar()
                    .icon(() -> TGItems.PISTOL_ROUNDS.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        TGItems.SHARED_ITEMS.forEach((id, item) -> { if (TGItems.enabled(id)) output.accept(item.get()); });
                        output.accept(TGItems.RAD_AWAY.get()); output.accept(TGItems.RAD_PILLS.get()); output.accept(TGItems.BANDAGES.get()); output.accept(TGItems.MEDKIT.get());
                        TGArmors.SHIELDS.values().forEach(item -> output.accept(item.get()));
                        TGuns.guns().forEach(gun -> output.accept(gun.get().getDefaultInstance()));
                        output.accept(TGuns.stielgranate.get());output.accept(TGuns.fraggrenade.get());
                        output.accept(TGSteelTools.COMBAT_KNIFE.get());output.accept(TGSteelTools.CROWBAR.get());
                        TGEquipment.ARMOR.values().forEach(item->output.accept(item.get()));
                        TGAccessories.ITEMS.values().forEach(item->output.accept(item.get()));
                        TGMachines.ITEMS.forEach((kind,item)->{if(kind!=techguns.machines.MachineKind.FABRICATOR&&kind!=techguns.machines.MachineKind.REACTION)output.accept(item.get());});
                        output.accept(TGTurrets.ITEM.get());
                        TGBenches.ITEMS.values().forEach(item->output.accept(item.get()));
                        output.accept(TGFluids.ACID_BUCKET.get());output.accept(TGFluids.OIL_BUCKET.get());techguns.power.PowerBlocks.singleBlockTabItems().forEach(item->output.accept(item.get()));
                        output.accept(TGDoors.BUNKER_ITEM.get());TGDoors.ITEMS.forEach(item->output.accept(item.get()));
                        TGDecorations.ITEMS.values().forEach(item->output.accept(item.get()));
                        output.accept(TGCharges.TNT_ITEM.get());output.accept(TGCharges.ADVANCED_ITEM.get());
                        TGWorld.ITEMS.values().forEach(item->output.accept(item.get()));
                        TGNpcs.EGGS.values().forEach(item->output.accept(item.get()));
                        TGSpecialNpcs.EGGS.values().forEach(item->output.accept(item.get()));
                    })
                    .build());
    /** Second tab: every block of the multiblock machines and power plants, so they are easy to find. */
    public static final RegistryObject<CreativeModeTab> MULTIBLOCK_TAB = TABS.register("techguns_multiblocks", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.techguns_multiblocks"))
                    .withTabsBefore(TAB.getId())
                    .icon(() -> techguns.power.plant.PlantBlocks.FUSION_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(TGMachines.ITEMS.get(techguns.machines.MachineKind.FABRICATOR).get());
                        output.accept(TGFabricator.HOUSING_ITEM.get());output.accept(TGFabricator.GLASS_ITEM.get());
                        output.accept(TGMachines.ITEMS.get(techguns.machines.MachineKind.REACTION).get());
                        output.accept(TGFabricator.REACTION_HOUSING_ITEM.get());output.accept(TGFabricator.REACTION_GLASS_ITEM.get());
                        TGOreDrill.ITEMS.values().forEach(item->output.accept(item.get()));
                        techguns.power.plant.PlantBlocks.tabItems().forEach(item->output.accept(item.get()));
                        output.accept(techguns.power.wire.WireBlocks.TRANSFORMER_ITEM.get());output.accept(techguns.power.wire.WireBlocks.PYLON_ITEM.get());
                    })
                    .build());

    public Techguns() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        TGFluids.init(bus);
        TGArmors.init();
        TGuns.init();
        TGSteelTools.init();
        TGEquipment.init();
        TGAccessories.init();
        TGMachines.init();
        TGBenches.init();
        TGFabricator.init();
        TGDoors.init();
        TGDecorations.init();
        TGCharges.init();
        TGWorld.init(bus);
        TGNpcs.init();
        TGSpecialNpcs.init(bus);
        TGTurrets.init(bus);
        TGOreDrill.init();
        techguns.power.PowerBlocks.init();
        techguns.fluids.FluidPipes.init();
        bus.addListener(TGNpcs::attributes);
        TGItems.ITEMS.register(bus);
        TGSounds.SOUNDS.register(bus);
        TABS.register(bus);
        TGMenuTypes.MENUS.register(bus);
        TGRecipeSerializers.SERIALIZERS.register(bus);
        TGRadiation.ATTRIBUTES.register(bus);
        TGRadiationSystem.EFFECTS.register(bus);
        TGRadiationSystem.POTIONS.register(bus);
        TGEntities.ENTITIES.register(bus);
        TGWeaponBlocks.BLOCKS.register(bus);TGWeaponBlocks.BLOCK_ENTITIES.register(bus);
        TGPackets.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TGConfig.COMMON);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, TGConfig.CLIENT);
        bus.addListener(TGConfig::onConfigEvent);
        bus.addListener(this::commonSetup);
        bus.addListener(this::registerCapabilities);
        LOGGER.info("Techguns fresh port: incomplete development build for Minecraft 1.20.1");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AmmoTypes.init();
            TGNpcGunSettings.setup();
            net.minecraftforge.common.crafting.CraftingHelper.register(techguns.recipes.HardenedGlassIngredient.ID,techguns.recipes.HardenedGlassIngredient.SERIALIZER);
            net.minecraftforge.common.crafting.CraftingHelper.register(techguns.recipes.ElectrumIngredient.ID,techguns.recipes.ElectrumIngredient.SERIALIZER);
            MagazineUnloading.init();
            TGRadiationSystem.setup();
            TGArmors.setup();
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) { event.register(TGExtendedPlayer.class); }
}
