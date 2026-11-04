package techguns.world;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import techguns.TGConfig;
import techguns.TGOreClusters;

/** Original surface grid/table. Entries still being ported retain their weight and never become substitutes. */
public final class SurfaceStructuresFeature extends Feature<NoneFeatureConfiguration> {
    private record Spawn(String id,int weight,boolean cluster,boolean desert,boolean oil){}
    private static Spawn spawn(String id,int weight){return new Spawn(id,weight,false,false,false);}
    private static final List<Spawn> SMALL=List.of(spawn("factory_building_small",10),spawn("small_trainstation",10),spawn("small_mine",10),spawn("gasstation",10));
    private static final List<Spawn> MEDIUM=List.of(new Spawn("alien_bug_nest",20,false,true,false),spawn("policestation",10),spawn("survivor_hideout",10),new Spawn("orecluster_spike",10,true,false,false),new Spawn("orecluster_meteorbase",5,true,false,false),new Spawn("desert_oil_cluster",15,true,true,true));
    /** Buildings this generator places itself; the structure entries for them only mark the site for /locate. */
    public static final Set<String> IMPLEMENTED=Set.of("factory_building_small","small_trainstation","small_mine","gasstation","policestation","survivor_hideout","orecluster_spike","orecluster_meteorbase","desert_oil_cluster");
    /** Every original Overworld structure id of the grid tables. */
    public static final Set<String> IDS=Set.of("aircraftcarrier","military_base","castle","alien_bug_nest","policestation","survivor_hideout","orecluster_spike","orecluster_meteorbase","desert_oil_cluster","factory_building_small","small_trainstation","small_mine","gasstation");
    public SurfaceStructuresFeature(){super(NoneFeatureConfiguration.CODEC);}
    public static RandomSource selectionRandom(long seed,int cx,int cz){var random=new net.minecraft.world.level.levelgen.WorldgenRandom(new net.minecraft.world.level.levelgen.LegacyRandomSource(seed));random.setLargeFeatureSeed(seed,cx,cz);return random;}
    public static String choose(int cx,int cz,boolean ocean,boolean desert,boolean oil,RandomSource random){
        if(!TGConfig.doWorldspawn)return null;
        boolean big=cx%TGConfig.spawnWeightTGStructureBig==0&&cz%TGConfig.spawnWeightTGStructureBig==0;
        var candidates=big?(ocean?List.of(spawn("aircraftcarrier",1)):List.of(spawn("military_base",1),spawn("castle",1))):ocean?List.<Spawn>of():cx%TGConfig.spawnWeightTGStructureMedium==0&&cz%TGConfig.spawnWeightTGStructureMedium==0?MEDIUM:cx%TGConfig.spawnWeightTGStructureSmall==0&&cz%TGConfig.spawnWeightTGStructureSmall==0?SMALL:List.<Spawn>of();
        var eligible=candidates.stream().filter(s->(!s.cluster||TGConfig.spawnOreClusterStructures)&&(!s.desert||desert)&&(!s.oil||oil)).toList();int total=eligible.stream().mapToInt(Spawn::weight).sum();if(total==0)return null;
        int roll=random.nextInt(total)+1,sum=0;for(var spawn:eligible){sum+=spawn.weight;if(roll<=sum)return spawn.id;}return null;
    }
    private static boolean tag(Holder<Biome> biome,String id){return biome.is(TagKey.create(net.minecraft.core.registries.Registries.BIOME,ResourceLocation.parse(id)));}
    public static boolean desert(Holder<Biome> biome){return biome.is(Biomes.DESERT)||tag(biome,"forge:is_sandy")||tag(biome,"forge:is_wasteland");}
    public static int biomeColor(Holder<Biome> biome){
        // Techguns rule (WorldgenStructure.coldTypes): COLD or SNOWY biomes use the snow palette
        if(tag(biome,"forge:is_cold")||tag(biome,"forge:is_cold/overworld")||tag(biome,"forge:is_snowy")||biome.value().getBaseTemperature()<.15f)return 1;
        if(desert(biome)||biome.is(BiomeTags.IS_BEACH)||biome.is(BiomeTags.IS_BADLANDS))return 2;
        return biome.is(BiomeTags.IS_NETHER)?3:0;
    }
    /** Terrain heights and fluids the original surface check reads. */
    public interface HeightProbe{int height(int x,int z);boolean dry(int x,int y,int z);}
    public static int surfaceHeight(WorldGenLevel world,int x,int z,int sizeX,int sizeZ){
        return surfaceHeight(new HeightProbe(){
            @Override public int height(int px,int pz){return world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,px,pz);}
            @Override public boolean dry(int px,int py,int pz){return world.getFluidState(new BlockPos(px,py,pz)).isEmpty();}
        },x,z,sizeX,sizeZ);
    }
    public static int surfaceHeight(HeightProbe probe,int x,int z,int sizeX,int sizeZ){
        int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE,sum=0,count=0;
        // Source samples by four; a non-multiple final edge is not sampled.
        for(int dx=0;dx<=sizeX;dx+=4)for(int dz=0;dz<=sizeZ;dz+=4){int y=probe.height(x+dx,z+dz);if(y<=0||!probe.dry(x+dx,y-1,z+dz))return -1;min=Math.min(min,y);max=Math.max(max,y);sum+=y;count++;}
        return max-min<=3?sum/count:-1;
    }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context){
        var world=context.level();if(!world.getLevel().dimension().equals(Level.OVERWORLD))return false;int cx=context.origin().getX()>>4,cz=context.origin().getZ()>>4;var biome=world.getBiome(new BlockPos(cx*16,64,cz*16));
        var selection=selectionRandom(world.getSeed(),cx,cz);
        String id=choose(cx,cz,biome.is(BiomeTags.IS_OCEAN),desert(biome),TGOreClusters.worldOil()!=null,selection);if(id==null||!IMPLEMENTED.contains(id))return false;
        var scan=ScannedStructure.load(id);int turns=selection.nextInt(4),centerX=scan.sizeX/2,centerZ=scan.sizeZ/2,minX=Integer.MAX_VALUE,minZ=Integer.MAX_VALUE;
        for(var corner:List.of(new BlockPos(0,0,0),new BlockPos(scan.sizeX,0,0),new BlockPos(0,0,scan.sizeZ),new BlockPos(scan.sizeX,0,scan.sizeZ))){var point=ScannedStructure.rotate(corner,centerX,centerZ,turns);minX=Math.min(minX,point.getX());minZ=Math.min(minZ,point.getZ());}
        int x=cx*16+minX,z=cz*16+minZ,y=surfaceHeight(world,x,z,turns%2==0?scan.sizeX:scan.sizeZ,turns%2==0?scan.sizeZ:scan.sizeX);if(y<0)return false;
        scan.place(world,new BlockPos(x,y-1,z),turns,world.getRandom(),context.random(),biomeColor(biome));return true;
    }
}
