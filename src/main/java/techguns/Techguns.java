package techguns;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slf4j.Logger;
import techguns.items.guns.ammo.AmmoTypes;
import techguns.items.guns.ammo.MagazineUnloading;
import techguns.capabilities.TGExtendedPlayer;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import techguns.api.radiation.TGRadiation;

/** 1.21.1 NeoForge entry point. Only implemented systems are registered here. */
@Mod(Techguns.MODID)
public final class Techguns {
    public static final String MODID = "techguns";
    public static final String NAME = "Techguns";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("techguns", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.techguns"))
                    .icon(() -> TGItems.PISTOL_ROUNDS.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        TGItems.SHARED_ITEMS.forEach((id, item) -> { if (TGItems.enabled(id)) output.accept(item.get()); });
                        output.accept(TGItems.RAD_AWAY.get()); output.accept(TGItems.RAD_PILLS.get());
                        TGArmors.SHIELDS.values().forEach(item -> output.accept(item.get()));
                        TGuns.guns().forEach(gun -> output.accept(gun.get().getDefaultInstance()));
                        output.accept(TGuns.stielgranate.get());output.accept(TGuns.fraggrenade.get());
                        output.accept(TGSteelTools.COMBAT_KNIFE.get());output.accept(TGSteelTools.CROWBAR.get());
                        TGEquipment.ARMOR.values().forEach(item->output.accept(item.get()));
                        TGAccessories.ITEMS.values().forEach(item->output.accept(item.get()));
                        TGMachines.ITEMS.values().forEach(item->output.accept(item.get()));
                        output.accept(TGTurrets.ITEM.get());
                        TGOreDrill.ITEMS.values().forEach(item->output.accept(item.get()));
                        TGBenches.ITEMS.values().forEach(item->output.accept(item.get()));
                        output.accept(TGFabricator.HOUSING_ITEM.get());output.accept(TGFabricator.GLASS_ITEM.get());
                        output.accept(TGFabricator.REACTION_HOUSING_ITEM.get());output.accept(TGFabricator.REACTION_GLASS_ITEM.get());
                        output.accept(TGFluids.ACID_BUCKET.get());output.accept(TGFluids.OIL_BUCKET.get());output.accept(techguns.power.PowerBlocks.FUEL_GENERATOR_ITEM.get());output.accept(techguns.power.PowerBlocks.LIQUID_GENERATOR_ITEM.get());output.accept(techguns.power.PowerBlocks.SOLAR_PANEL_ITEM.get());output.accept(techguns.power.PowerBlocks.ENERGY_CELL_ITEM.get());
                        output.accept(TGDoors.BUNKER_ITEM.get());TGDoors.ITEMS.forEach(item->output.accept(item.get()));
                        TGDecorations.ITEMS.values().forEach(item->output.accept(item.get()));
                        output.accept(TGCharges.TNT_ITEM.get());output.accept(TGCharges.ADVANCED_ITEM.get());
                        TGWorld.ITEMS.values().forEach(item->output.accept(item.get()));
                        TGNpcs.EGGS.values().forEach(item->output.accept(item.get()));
                        TGSpecialNpcs.EGGS.values().forEach(item->output.accept(item.get()));
                    })
                    .build());

    public Techguns(IEventBus bus, net.neoforged.fml.ModContainer container) {
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
        bus.addListener(TGNpcs::attributes);
        techguns.items.armors.TGArmorMaterial.REGISTER.register(bus);
        techguns.capabilities.TGExtendedPlayerCapProvider.ATTACHMENTS.register(bus);
        TGDataComponents.COMPONENTS.register(bus);
        TGItems.ITEMS.register(bus);
        TGSounds.SOUNDS.register(bus);
        TABS.register(bus);
        TGMenuTypes.MENUS.register(bus);
        TGRecipeSerializers.SERIALIZERS.register(bus);
        techguns.recipes.TGIngredients.TYPES.register(bus);
        TGRadiation.ATTRIBUTES.register(bus);
        TGRadiationSystem.EFFECTS.register(bus);
        TGRadiationSystem.POTIONS.register(bus);
        TGEntities.ENTITIES.register(bus);
        TGWeaponBlocks.BLOCKS.register(bus);TGWeaponBlocks.BLOCK_ENTITIES.register(bus);
        TGPackets.register();
        TGPackets.NETWORK.registerOn(bus);
        container.registerConfig(ModConfig.Type.COMMON, TGConfig.COMMON);
        container.registerConfig(ModConfig.Type.CLIENT, TGConfig.CLIENT);
        bus.addListener(TGConfig::onConfigEvent);
        bus.addListener(this::commonSetup);
        bus.addListener(this::registerCapabilities);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(TGRadiationSystem::brewing);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(techguns.items.guns.MiningGun::onBreak);
        LOGGER.info("Techguns Remake for Minecraft 1.21.1 (NeoForge)");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AmmoTypes.init();
            TGNpcGunSettings.setup();
            MagazineUnloading.init();
            TGRadiationSystem.setup();
            TGArmors.setup();
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) { techguns.capabilities.TGCapabilityRegistration.register(event); }
}
