package techguns.client;
import net.neoforged.fml.common.EventBusSubscriber;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import techguns.TGMenuTypes;
import techguns.Techguns;
import techguns.TGEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.renderer.entity.NoopRenderer;
import techguns.client.gui.TGPlayerInventoryScreen;

@EventBusSubscriber(modid = Techguns.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    public static final KeyMapping NIGHT_VISION = key("toggleNightvision", GLFW.GLFW_KEY_N);
    public static final KeyMapping SAFE_MODE = key("toggleSafemode", GLFW.GLFW_KEY_B);
    public static final KeyMapping STEP_ASSIST = key("toggleStepassist", GLFW.GLFW_KEY_V);
    public static final KeyMapping JETPACK = key("toggleJetpack", GLFW.GLFW_KEY_J);
    public static final KeyMapping RELOAD = key("forceReload", GLFW.GLFW_KEY_R);
    private static KeyMapping key(String name, int code) { return new KeyMapping("techguns.key." + name, InputConstants.Type.KEYSYM, code, "techguns.key.categories.techguns"); }
    @SubscribeEvent public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(NIGHT_VISION); event.register(SAFE_MODE); event.register(STEP_ASSIST); event.register(JETPACK);
        event.register(RELOAD);
    }
    @SubscribeEvent public static void screens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(TGMenuTypes.PLAYER_INVENTORY.get(), TGPlayerInventoryScreen::new);
        event.register(TGMenuTypes.MACHINE.get(),techguns.client.gui.MachineScreen::new);
        event.register(techguns.power.PowerBlocks.GENERATOR_MENU.get(),techguns.client.gui.GeneratorScreen::new);
        event.register(TGMenuTypes.TURRET.get(),techguns.client.gui.TurretScreen::new);
        event.register(TGMenuTypes.ORE_DRILL.get(),techguns.client.gui.OreDrillScreen::new);
        event.register(TGMenuTypes.BENCH.get(),techguns.client.gui.BenchScreen::new);
        event.register(TGMenuTypes.DOOR.get(),techguns.client.gui.DoorScreen::new);
        event.register(TGMenuTypes.CHARGE.get(),techguns.client.gui.ChargeScreen::new);
    }
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) { event.enqueueWork(() -> {
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
        event.registerBlockEntityRenderer(techguns.TGMachines.ENTITY.get(),techguns.client.render.tileentities.ProcessingMachineRenderer::new);
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
        for(var skin:event.getSkins()){net.minecraft.client.renderer.entity.player.PlayerRenderer renderer=event.getSkin(skin);if(renderer!=null)renderer.addLayer(new techguns.client.render.entities.AccessoryLayer(renderer,skin==net.minecraft.client.resources.PlayerSkin.Model.SLIM));}
    }
    @SubscribeEvent public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event){event.registerLayerDefinition(techguns.client.render.entities.npcs.NpcRenderer.LAYER,techguns.client.render.entities.npcs.NpcRenderer::layer);}
    @SubscribeEvent public static void extraModels(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event){techguns.client.render.tileentities.LargeDoorRenderer.registerModels(event);}
    @SubscribeEvent public static void reload(net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) manager -> techguns.client.particle.BulletImpactEffects.reload());
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) manager -> techguns.client.particle.WeaponEffects.reload());
    }
    private ClientModEvents() {}
}
