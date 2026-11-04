package techguns.client;

import java.util.ArrayList;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.TGItems;
import techguns.Techguns;

/** Opt-in development check for actually baked models, complementing the filesystem content audit. */
public final class ClientResourceVerifier {
    public static void verify(ModelEvent.BakingCompleted event) {
        if (!Boolean.getBoolean("techguns.verifyClientResources")) return;
        var errors = new ArrayList<String>();
        int checked = 0;
        verifyTranslations(errors);
        for (var item : TGItems.ITEMS.getEntries()) {
            var id = ForgeRegistries.ITEMS.getKey(item.get());
            var location = new ModelResourceLocation(id, "inventory");
            var model = event.getModels().get(location);
            if (model == null) errors.add("No baked item model: " + id);
            else if (model.getParticleIcon().contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
                errors.add("Missing atlas texture in baked item model: " + id);
            }
            if (model != null && model.isCustomRenderer()) {
                var renderer = net.minecraftforge.client.extensions.common.IClientItemExtensions.of(item.get()).getCustomRenderer();
                if (renderer == null) errors.add("No custom item renderer: " + id);
            }
            checked++;
        }
        for(var block:techguns.TGWeaponBlocks.BLOCKS.getEntries())for(var state:block.get().getStateDefinition().getPossibleStates()){
            var model=event.getModels().get(net.minecraft.client.renderer.block.BlockModelShaper.stateToModelLocation(state));
            if(model==null||model==event.getModelManager().getMissingModel())errors.add("Missing baked block state: "+state);
        }
        for(String part:techguns.client.render.tileentities.LargeDoorRenderer.PARTS){var id=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("techguns","block/"+part);if(event.getModels().get(id)==null||event.getModels().get(id)==event.getModelManager().getMissingModel())errors.add("Missing original animated door mesh: "+part);}
        if (!errors.isEmpty()) throw new IllegalStateException("Techguns client resource verification failed ("+errors.size()+" errors): " + errors.stream().limit(20).toList());
        var effects = techguns.client.particle.BulletImpactEffects.definitions();
        var resources = net.minecraft.client.Minecraft.getInstance().getResourceManager();
        var weaponEffects=techguns.client.particle.WeaponEffects.definitions();
        var particleTextures=new java.util.HashSet<net.minecraft.resources.ResourceLocation>();
        for(var definition:weaponEffects.values()) {
            if(resources.getResource(definition.texture).isEmpty())throw new IllegalStateException("Missing original weapon effect texture "+definition.texture);
            particleTextures.add(definition.texture);
            definition.sample(new java.util.Random(0),0,0);
        }
        for(var definition:effects.values())particleTextures.add(definition.texture);
        particleTextures.add(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("techguns","textures/entity/bullet_blue.png"));
        var textureManager=net.minecraft.client.Minecraft.getInstance().getTextureManager();
        for(var path:particleTextures){
            try(var stream=resources.open(path);var pixels=com.mojang.blaze3d.platform.NativeImage.read(stream)){
                if(pixels.getWidth()<1||pixels.getHeight()<1)throw new IllegalStateException("Empty particle sheet "+path);
            }catch(java.io.IOException error){throw new IllegalStateException("Cannot decode particle sheet "+path,error);}
            var loaded=textureManager.getTexture(path);
            if(loaded==MissingTextureAtlasSprite.getTexture())throw new IllegalStateException("Particle texture resolved to the missing-texture fallback: "+path);
            loaded.getId();
        }
        Techguns.LOGGER.info("TECHGUNS_PARTICLE_TEXTURES_PASSED: {} unique original texture sheets decoded and loaded by the real texture manager",particleTextures.size());
        for(var gun:techguns.TGuns.guns())if(!techguns.client.render.item.GunItemRenderer.get().hasModel(gun.get()))throw new IllegalStateException("No distinct model for "+gun.getId());
        techguns.client.render.item.GunItemRenderer.get().verifyResources(resources);
        for(var registered:techguns.TGEquipment.ARMOR.values()){var armor=registered.get();for(int camo=0;camo<armor.getCamoCount();camo++){var stack=armor.getDefaultInstance();stack.getOrCreateTag().putByte("camo",(byte)camo);var path=net.minecraft.resources.ResourceLocation.parse(armor.getArmorTexture(stack,null,armor.slot,null));if(resources.getResource(path).isEmpty())throw new IllegalStateException("Missing original wearable armor texture "+path);}}
        for(var kind:techguns.machines.MachineKind.values()){var path=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/"+kind.texture()+"_gui.png");if(resources.getResource(path).isEmpty())throw new IllegalStateException("Missing original machine interface "+path);}
        for(var kind:techguns.machines.BenchKind.values()){var path=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/"+kind.texture()+"_gui.png");if(resources.getResource(path).isEmpty())throw new IllegalStateException("Missing original workbench interface "+path);}
        Techguns.LOGGER.info("TECHGUNS_NONENERGY_EFFECTS_PASSED: {} original systems parsed and textures present",weaponEffects.size());
        for (var definition : effects.values()) if (resources.getResource(definition.texture).isEmpty())
            throw new IllegalStateException("Missing original bullet effect texture " + definition.texture);
        if (resources.getResource(techguns.client.render.fx.PistolMuzzleFlash.TEXTURE).isEmpty())
            throw new IllegalStateException("Missing original pistol muzzle texture");
        Techguns.LOGGER.info("TECHGUNS_BULLET_EFFECTS_PASSED: {} original impact systems and pistol flash texture loaded", effects.size());
        Techguns.LOGGER.info("TECHGUNS_CLIENT_RESOURCES_PASSED: {} registered item models baked with available atlas textures", checked);
    }
    private ClientResourceVerifier() {}
    private static void verifyTranslations(java.util.List<String> errors){
        var resources=net.minecraft.client.Minecraft.getInstance().getResourceManager();
        for(String language:new String[]{"ru_ru","en_us"}){
            var path=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("techguns","lang/"+language+".json");
            try(var reader=new java.io.InputStreamReader(resources.open(path),java.nio.charset.StandardCharsets.UTF_8)){
                var translations=com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                var keys=new java.util.HashSet<String>();
                for(var item:techguns.TGItems.ITEMS.getEntries())keys.add(item.get().getDescriptionId());
                for(var kind:techguns.machines.MachineKind.values())keys.add(kind.titleKey());
                for(var kind:techguns.machines.BenchKind.values())keys.add(kind.titleKey());
                for(var key:keys)if(!translations.has(key)||translations.get(key).getAsString().isBlank())errors.add("Missing "+language+" runtime translation: "+key);
            }catch(java.io.IOException error){errors.add("Cannot read runtime language "+language+": "+error);}
        }
    }
}
