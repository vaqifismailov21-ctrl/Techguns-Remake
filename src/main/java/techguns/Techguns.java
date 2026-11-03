package techguns;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
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
    /** 1.19.2 creative tabs are plain objects; the tab lists the same items in the same order as the 1.20.1 tab. */
    public static final CreativeModeTab TAB = new CreativeModeTab("techguns") {
        @Override public net.minecraft.world.item.ItemStack makeIcon() { return TGItems.PISTOL_ROUNDS.get().getDefaultInstance(); }
        @Override public void fillItemList(net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> items) {
            TabOutput output = items::add;
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
                        // Same as the 1.20.1/1.21.1 tab: Patchouli puts the Techguns manual last (it targets this tab in book.json).
                        var patchouliBook = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation("patchouli", "guide_book"));
                        if (patchouliBook != null && patchouliBook != net.minecraft.world.item.Items.AIR) patchouliBook.fillItemCategory(this, items);
                            }
    };
    @FunctionalInterface private interface TabOutput {
        void add(net.minecraft.world.item.ItemStack stack);
        default void accept(net.minecraft.world.item.ItemStack stack) { add(stack); }
        default void accept(net.minecraft.world.level.ItemLike item) { add(new net.minecraft.world.item.ItemStack(item)); }
    }

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
        bus.addListener(TGNpcs::attributes);
        TGItems.ITEMS.register(bus);
        TGSounds.SOUNDS.register(bus);
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
            // 1.19.2 creative search only lists items that have a category; the 1.20.1 tab is searchable, so give every
            // Techguns item this tab as its category (the tab itself is still filled in the 1.20.1 order above).
            for (var item : net.minecraftforge.registries.ForgeRegistries.ITEMS.getValues())
                if (MODID.equals(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item).getNamespace()) && item.getItemCategory() == null)
                    net.minecraftforge.fml.util.ObfuscationReflectionHelper.setPrivateValue(net.minecraft.world.item.Item.class, item, TAB, "f_41377_");
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
