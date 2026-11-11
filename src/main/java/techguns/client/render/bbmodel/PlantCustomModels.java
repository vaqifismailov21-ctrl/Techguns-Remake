package techguns.client.render.bbmodel;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.fml.loading.FMLPaths;
import techguns.power.plant.PlantKind;

/**
 * Own models of the formed power plants, made in Blockbench: a file <plant>.bbmodel (wind_plant, geothermal_plant,
 * fission_reactor, fusion_reactor) in config/techguns/models/ of the game folder, or in a resource pack (or the mod)
 * at assets/techguns/models/plant/. The game folder wins; F3+T reloads them. A plant with such a model is drawn from
 * it instead of the built-in model (its collision stays the built-in one).
 */
public final class PlantCustomModels {
    private static final Map<PlantKind,BbModel> MODELS=new EnumMap<>(PlantKind.class);
    private PlantCustomModels(){}
    public static BbModel get(PlantKind kind){return MODELS.get(kind);}
    public static Path folder(){return FMLPaths.CONFIGDIR.get().resolve("techguns").resolve("models");}

    public static void reload(ResourceManager manager){
        MODELS.values().forEach(BbModel::close);MODELS.clear();
        try{Files.createDirectories(folder());}catch(Exception e){}
        for(var kind:PlantKind.values()){
            try{
                var file=folder().resolve(kind.id()+".bbmodel");
                com.google.gson.JsonObject json=null;
                if(Files.isRegularFile(file))json=JsonParser.parseString(Files.readString(file,StandardCharsets.UTF_8)).getAsJsonObject();
                else{
                    var res=manager.getResource(ResourceLocation.fromNamespaceAndPath("techguns","models/plant/"+kind.id()+".bbmodel"));
                    if(res.isPresent())try(var in=new InputStreamReader(res.get().open(),StandardCharsets.UTF_8)){json=JsonParser.parseReader(in).getAsJsonObject();}
                }
                if(json==null)continue;
                // textures not stored in the file: next to it, or in the mod's plant texture folder
                var dir=file.getParent();
                MODELS.put(kind,BbModel.read(json,kind.id(),name->{
                    try{
                        var near=dir.resolve(name);if(Files.isRegularFile(near))try(var in=Files.newInputStream(near)){return NativeImage.read(in);}
                        var res=manager.getResource(ResourceLocation.fromNamespaceAndPath("techguns","textures/blocks/plant/"+(name.endsWith(".png")?name:name+".png")));
                        if(res.isPresent())try(var in=res.get().open()){return NativeImage.read(in);}
                    }catch(Exception e){}
                    return null;
                }));
                techguns.Techguns.LOGGER.info("Techguns: own model of {} loaded",kind.id());
            }catch(Exception e){
                techguns.Techguns.LOGGER.warn("Techguns: could not read the model of {}: {}",kind.id(),e.toString());
            }
        }
    }
}
