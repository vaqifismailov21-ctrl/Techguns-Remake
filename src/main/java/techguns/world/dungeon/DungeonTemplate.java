package techguns.world.dungeon;
import java.util.*;
import com.google.gson.*;
import techguns.world.dungeon.TemplateSegment.SegmentType;
public final class DungeonTemplate {
    private static final Map<String,DungeonTemplate> TEMPLATES=new HashMap<>();
    public final int sizeXZ,sizeY;public final String name;public final Map<SegmentType,DungeonSegment> segments=new EnumMap<>(SegmentType.class);
    private DungeonTemplate(String name,JsonObject json){this.name=name;sizeXZ=json.get("sizeXZ").getAsInt();sizeY=json.get("sizeY").getAsInt();for(var entry:json.getAsJsonObject("segments").entrySet()){var type=SegmentType.valueOf(entry.getKey());segments.put(type,new DungeonSegment(this,type,entry.getValue().getAsJsonObject()));}}
    public static synchronized DungeonTemplate load(String name){if(TEMPLATES.isEmpty())try(var input=DungeonTemplate.class.getResourceAsStream("/data/techguns/legacy_structures/castle-templates.json");var reader=new java.io.InputStreamReader(Objects.requireNonNull(input),java.nio.charset.StandardCharsets.UTF_8)){for(var entry:JsonParser.parseReader(reader).getAsJsonObject().entrySet())TEMPLATES.put(entry.getKey(),new DungeonTemplate(entry.getKey(),entry.getValue().getAsJsonObject()));}catch(Exception error){throw new IllegalStateException("Original castle template load failed",error);}return Objects.requireNonNull(TEMPLATES.get(name),name);}
}
