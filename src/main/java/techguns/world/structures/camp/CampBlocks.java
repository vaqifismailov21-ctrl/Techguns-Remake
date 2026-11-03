package techguns.world.structures.camp;
import java.util.*;
import com.google.gson.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.world.ScannedStructure;

public final class CampBlocks {
    private static final Map<String,BlockState> STATES=new java.util.concurrent.ConcurrentHashMap<>();private static JsonObject source;
    private static synchronized JsonObject source(){if(source==null)try(var input=CampBlocks.class.getResourceAsStream("/data/techguns/legacy_structures/military-camp-palette.json");var reader=new java.io.InputStreamReader(Objects.requireNonNull(input),java.nio.charset.StandardCharsets.UTF_8)){source=JsonParser.parseReader(reader).getAsJsonObject();}catch(java.io.IOException e){throw new IllegalStateException(e);}return source;}
    public static BlockState state(String id){return STATES.computeIfAbsent(id,key->ScannedStructure.sourceState(Objects.requireNonNull(source().getAsJsonObject(key),"Missing camp palette "+key)));}
    public static final CampBlock[] CRATES=java.util.stream.IntStream.range(0,9).mapToObj(i->new CampBlock(state("crate_"+i))).toArray(CampBlock[]::new);
    public static BlockState camoTop(CampCamo type){return Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("techguns:camonet_top_"+type.name().toLowerCase(java.util.Locale.ROOT)))).defaultBlockState();}
    public static boolean isLamp(BlockState state){return state.getBlock() instanceof techguns.blocks.DecorLampBlock;}
    public static BlockState legacy(String id,int metadata){return switch(id){
        case "ladder"->Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.from3DDataValue(metadata));
        case "oak_fence_gate"->Blocks.OAK_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.from2DDataValue(metadata&3)).setValue(FenceGateBlock.OPEN,(metadata&4)!=0).setValue(FenceGateBlock.POWERED,(metadata&8)!=0);
        case "bed"->Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.from2DDataValue(metadata&3)).setValue(BedBlock.PART,(metadata&8)!=0?BedPart.HEAD:BedPart.FOOT).setValue(BedBlock.OCCUPIED,(metadata&4)!=0);
        default->throw new IllegalArgumentException("Unadapted source camp metadata "+id+":"+metadata);
    };}
    private CampBlocks(){}
}
