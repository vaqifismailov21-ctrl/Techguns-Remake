package techguns.client.render.tileentities;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import techguns.Techguns;
import techguns.power.plant.PlantKind;

/**
 * Painted atlas of a power plant: every face of every box of its built-in model has its own place in one texture
 * (tools/plant_atlas.py paints it, Blender bakes the shading). Used instead of the repeating textures when the box
 * count still matches the model.
 */
public final class PlantAtlas {
    /** rects[box][face] = {u0,v0,u1,v1} in 0..1, faces in the order -z,+z,-x,+x,+y,-y; null for collision-only boxes. */
    public record Atlas(ResourceLocation texture,int count,float[][][] rects){}
    private static final Map<PlantKind,Atlas> ATLASES=new EnumMap<>(PlantKind.class);
    private PlantAtlas(){}
    public static Atlas get(PlantKind kind){return ATLASES.get(kind);}

    public static void reload(ResourceManager manager){
        ATLASES.clear();
        for(var kind:PlantKind.values()){
            var json=ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"models/plant/"+kind.id()+"_atlas.json");
            var res=manager.getResource(json);if(res.isEmpty())continue;
            try(var in=new InputStreamReader(res.get().open(),StandardCharsets.UTF_8)){
                var o=JsonParser.parseReader(in).getAsJsonObject();
                var size=o.getAsJsonArray("size");float w=size.get(0).getAsFloat(),h=size.get(1).getAsFloat();
                JsonArray list=o.getAsJsonArray("rects");float[][][] rects=new float[list.size()][][];
                for(int i=0;i<list.size();i++){
                    if(list.get(i).isJsonNull())continue;var faces=list.get(i).getAsJsonArray();rects[i]=new float[6][];
                    for(int f=0;f<6;f++){var r=faces.get(f).getAsJsonArray();float x=r.get(0).getAsFloat(),y=r.get(1).getAsFloat();
                        rects[i][f]=new float[]{x/w,y/h,(x+r.get(2).getAsFloat())/w,(y+r.get(3).getAsFloat())/h};}
                }
                ATLASES.put(kind,new Atlas(ResourceLocation.fromNamespaceAndPath(Techguns.MODID,"textures/blocks/plant/atlas/"+kind.id()+".png"),o.get("count").getAsInt(),rects));
            }catch(Exception e){Techguns.LOGGER.warn("Plant atlas {} not loaded: {}",json,e.toString());}
        }
    }
}
