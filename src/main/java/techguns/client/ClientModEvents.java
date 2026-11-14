package techguns.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import techguns.TGMenuTypes;
import techguns.Techguns;
import techguns.TGEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraft.client.renderer.entity.NoopRenderer;
import techguns.client.gui.TGPlayerInventoryScreen;

@Mod.EventBusSubscriber(modid = Techguns.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    public static final KeyMapping NIGHT_VISION = key("toggleNightvision", GLFW.GLFW_KEY_N);
    public static final KeyMapping SAFE_MODE = key("toggleSafemode", GLFW.GLFW_KEY_B);
    public static final KeyMapping STEP_ASSIST = key("toggleStepassist", GLFW.GLFW_KEY_V);
    public static final KeyMapping JETPACK = key("toggleJetpack", GLFW.GLFW_KEY_J);
    public static final KeyMapping RELOAD = key("forceReload", GLFW.GLFW_KEY_R);
    public static final KeyMapping SWITCH_AMMO = key("switchAmmo", GLFW.GLFW_KEY_G);
    /** Active only while a gun is held, so the default mouse buttons do not show as clashing with attack and use. */
    public static final net.minecraftforge.client.settings.IKeyConflictContext GUN_HELD = new net.minecraftforge.client.settings.IKeyConflictContext() {
        @Override public boolean isActive() {
            var player = net.minecraft.client.Minecraft.getInstance().player;
            return net.minecraftforge.client.settings.KeyConflictContext.IN_GAME.isActive() && player != null
                    && (player.getMainHandItem().getItem() instanceof techguns.items.guns.GenericGun || player.getOffhandItem().getItem() instanceof techguns.items.guns.GenericGun);
        }
        @Override public boolean conflicts(net.minecraftforge.client.settings.IKeyConflictContext other) { return false; }
    };
    /** Fire (main hand) and aim / fire off hand; mouse buttons by default, any key or button can be bound. */
    public static final KeyMapping SHOOT = new KeyMapping("techguns.key.shoot", GUN_HELD, InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_LEFT, "techguns.key.categories.techguns");
    public static final KeyMapping AIM = new KeyMapping("techguns.key.shootSecondary", GUN_HELD, InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_RIGHT, "techguns.key.categories.techguns");
    private static KeyMapping key(String name, int code) { return new KeyMapping("techguns.key." + name, InputConstants.Type.KEYSYM, code, "techguns.key.categories.techguns"); }
    @SubscribeEvent public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(NIGHT_VISION); event.register(SAFE_MODE); event.register(STEP_ASSIST); event.register(JETPACK);
        event.register(RELOAD); event.register(SWITCH_AMMO); event.register(SHOOT); event.register(AIM);
    }
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) { event.enqueueWork(() -> {
        if(net.minecraftforge.fml.ModList.get().isLoaded("jeresources"))techguns.plugins.jer.TechgunsJerPlugin.registerDirectly();
        MenuScreens.register(TGMenuTypes.PLAYER_INVENTORY.get(), TGPlayerInventoryScreen::new);
        MenuScreens.register(TGMenuTypes.MACHINE.get(),techguns.client.gui.MachineScreen::new);
        MenuScreens.register(techguns.power.PowerBlocks.GENERATOR_MENU.get(),techguns.client.gui.GeneratorScreen::new);
        MenuScreens.register(techguns.power.plant.PlantBlocks.MENU.get(),techguns.client.gui.PlantScreen::new);
        techguns.power.PowerBlocks.clientTick=techguns.client.audio.PowerSounds::tick;
        techguns.items.guns.GenericGun.ammoSwitchKey=SWITCH_AMMO::getTranslatedKeyMessage;
        MenuScreens.register(TGMenuTypes.TURRET.get(),techguns.client.gui.TurretScreen::new);
        MenuScreens.register(TGMenuTypes.ORE_DRILL.get(),techguns.client.gui.OreDrillScreen::new);
        MenuScreens.register(TGMenuTypes.BENCH.get(),techguns.client.gui.BenchScreen::new);
        MenuScreens.register(TGMenuTypes.DOOR.get(),techguns.client.gui.DoorScreen::new);
        MenuScreens.register(TGMenuTypes.CHARGE.get(),techguns.client.gui.ChargeScreen::new);
        techguns.TGArmors.SHIELDS.values().forEach(item -> net.minecraft.client.renderer.item.ItemProperties.register(item.get(),
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Techguns.MODID, "blocking"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0));
    }); }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(techguns.TGWorld.DUMMY.get(),NoopRenderer::new);
        event.registerEntityRenderer(techguns.TGSpecialNpcs.ALIEN_BUG.get(),context->new techguns.client.render.entities.npcs.SpecialNpcRenderer<>(context,true));
        event.registerEntityRenderer(techguns.TGSpecialNpcs.GHASTLING.get(),context->new techguns.client.render.entities.npcs.SpecialNpcRenderer<>(context,false));
        event.registerEntityRenderer(techguns.TGSpecialNpcs.HELICOPTER.get(),techguns.client.render.entities.npcs.HelicopterRenderer::new);
        techguns.TGNpcs.TYPES.forEach((id,type)->event.registerEntityRenderer(type.get(),context->new techguns.client.render.entities.npcs.NpcRenderer(context,id)));
        event.registerBlockEntityRenderer(techguns.TGDoors.ENTITY.get(),techguns.client.render.tileentities.LargeDoorRenderer::new);
        event.registerBlockEntityRenderer(techguns.fluids.FluidPipes.TANK_ENTITY.get(),techguns.client.render.tileentities.FluidTankRenderer::new);
        event.registerBlockEntityRenderer(techguns.TGMachines.ENTITY.get(),techguns.client.render.tileentities.ProcessingMachineRenderer::new);
        event.registerBlockEntityRenderer(techguns.power.PowerBlocks.FUEL_GENERATOR_ENTITY.get(),techguns.client.render.tileentities.PowerRenderer::new);
        event.registerBlockEntityRenderer(techguns.power.PowerBlocks.LIQUID_GENERATOR_ENTITY.get(),techguns.client.render.tileentities.PowerRenderer::new);
        event.registerBlockEntityRenderer(techguns.power.PowerBlocks.GEOTHERMAL_GENERATOR_ENTITY.get(),techguns.client.render.tileentities.PowerRenderer::new);
        event.registerBlockEntityRenderer(techguns.power.PowerBlocks.NUCLEAR_GENERATOR_ENTITY.get(),techguns.client.render.tileentities.PowerRenderer::new);
        event.registerBlockEntityRenderer(techguns.power.PowerBlocks.SOLAR_PANEL_ENTITY.get(),techguns.client.render.tileentities.PowerRenderer::new);
        event.registerBlockEntityRenderer(techguns.power.PowerBlocks.ENERGY_CELL_ENTITY.get(),techguns.client.render.tileentities.PowerRenderer::new);
        event.registerBlockEntityRenderer(techguns.power.plant.PlantBlocks.WIND_ENTITY.get(),techguns.client.render.tileentities.PlantRenderer::new);
        event.registerBlockEntityRenderer(techguns.power.wire.WireBlocks.PYLON_ENTITY.get(),techguns.client.render.tileentities.PylonRenderer::new);
        event.registerBlockEntityRenderer(techguns.power.plant.PlantBlocks.HUB_ENTITY.get(),techguns.client.render.tileentities.RotorHubRenderer::new);
        event.registerBlockEntityRenderer(techguns.power.plant.PlantBlocks.GEOTHERMAL_ENTITY.get(),techguns.client.render.tileentities.PlantRenderer::new);
        event.registerBlockEntityRenderer(techguns.power.plant.PlantBlocks.FISSION_ENTITY.get(),techguns.client.render.tileentities.PlantRenderer::new);
        event.registerBlockEntityRenderer(techguns.power.plant.PlantBlocks.FUSION_ENTITY.get(),techguns.client.render.tileentities.PlantRenderer::new);
        event.registerBlockEntityRenderer(techguns.power.PowerBlocks.FLOODLIGHT_ENTITY.get(),techguns.client.render.tileentities.PowerRenderer::new);
        event.registerBlockEntityRenderer(techguns.TGTurrets.ENTITY.get(),techguns.client.render.tileentities.TurretBaseRenderer::new);
        event.registerEntityRenderer(techguns.TGTurrets.TURRET.get(),techguns.client.render.entities.npcs.NPCTurretRenderer::new);
        event.registerBlockEntityRenderer(techguns.TGOreDrill.MASTER.get(),techguns.client.render.tileentities.OreDrillRenderer::new);
        event.registerEntityRenderer(TGEntities.ENERGY_PROJECTILE.get(),techguns.client.render.entities.projectiles.RenderEnergyProjectile::new);
        event.registerEntityRenderer(TGEntities.RADIATION.get(), NoopRenderer::new);
        event.registerEntityRenderer(TGEntities.GENERIC_PROJECTILE.get(), techguns.client.render.entities.projectiles.RenderGenericProjectile::new);
        event.registerEntityRenderer(TGEntities.ADVANCED_PROJECTILE.get(), techguns.client.render.entities.projectiles.RenderAdvancedBulletProjectile::new);
        event.registerEntityRenderer(TGEntities.INCENDIARY_PROJECTILE.get(), techguns.client.render.entities.projectiles.RenderGenericProjectile::new);
        event.registerEntityRenderer(TGEntities.EXPLOSIVE_BULLET.get(), techguns.client.render.entities.projectiles.RenderGenericProjectile::new);
        event.registerEntityRenderer(TGEntities.STONE_BULLET.get(), techguns.client.render.entities.projectiles.RenderStoneBullet::new);
        event.registerEntityRenderer(TGEntities.ROCKET.get(),techguns.client.render.entities.projectiles.RenderExplosive::new);
        event.registerEntityRenderer(TGEntities.GUIDED_MISSILE.get(),techguns.client.render.entities.projectiles.RenderExplosive::new);
        event.registerEntityRenderer(TGEntities.ROCKET_HV.get(),techguns.client.render.entities.projectiles.RenderExplosive::new);
        event.registerEntityRenderer(TGEntities.ROCKET_NUKE.get(),techguns.client.render.entities.projectiles.RenderExplosive::new);
        event.registerEntityRenderer(TGEntities.GRENADE.get(),techguns.client.render.entities.projectiles.RenderExplosive::new);
        event.registerEntityRenderer(TGEntities.FRAG_GRENADE.get(),techguns.client.render.entities.projectiles.RenderExplosive::new);
        event.registerEntityRenderer(TGEntities.GRENADE_40MM.get(),techguns.client.render.entities.projectiles.RenderExplosive::new);
        event.registerEntityRenderer(TGEntities.FLAME.get(),NoopRenderer::new);
        event.registerEntityRenderer(TGEntities.HAMMER.get(),NoopRenderer::new);event.registerEntityRenderer(TGEntities.CHAINSAW.get(),NoopRenderer::new);
        event.registerEntityRenderer(TGEntities.BIO.get(),techguns.client.render.entities.projectiles.RenderBioProjectile::new);
    }
    @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers event){
        for(String skin:event.getSkins()){net.minecraft.client.renderer.entity.player.PlayerRenderer renderer=event.getSkin(skin);renderer.addLayer(new techguns.client.render.entities.AccessoryLayer(renderer,skin.equals("slim")));}
    }
    @SubscribeEvent public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event){event.registerLayerDefinition(techguns.client.render.entities.npcs.NpcRenderer.LAYER,techguns.client.render.entities.npcs.NpcRenderer::layer);}
    @SubscribeEvent public static void extraModels(net.minecraftforge.client.event.ModelEvent.RegisterAdditional event){techguns.client.render.tileentities.LargeDoorRenderer.registerModels(event);}
    @SubscribeEvent public static void reload(net.minecraftforge.client.event.RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) manager -> techguns.client.particle.BulletImpactEffects.reload());
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) manager -> techguns.client.particle.WeaponEffects.reload());
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) techguns.client.render.bbmodel.PlantCustomModels::reload);
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) techguns.client.render.tileentities.PlantAtlas::reload);
    }
    private ClientModEvents() {}
}
