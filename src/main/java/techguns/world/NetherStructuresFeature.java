package techguns.world;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import techguns.TGConfig;

/** Complete active Nether structure table from TGStructureSpawnRegister and its source chunk grid. */
public final class NetherStructuresFeature extends Feature<NoneFeatureConfiguration> {
    private record Spawn(String id,int weight,boolean cluster){}
    private static final List<Spawn> SMALL=List.of(new Spawn("nether_altar_small",10,false),new Spawn("nether_soul_platform",10,false),new Spawn("nether_loot_01",10,false),new Spawn("nether_acid_hole",10,false),new Spawn("nether_orecluster_small",10,true));
    private static final List<Spawn> MEDIUM=List.of(new Spawn("nether_altar_medium",10,false),new Spawn("nether_ghast_spawner",10,false),new Spawn("nether_building",10,false),new Spawn("nether_orecluster_medium",10,true),new Spawn("nether_orecluster_castle",10,true));
    /** Every original Nether structure id of the grid tables. */
    public static final Set<String> IDS=Set.of("nether_altar_small","nether_soul_platform","nether_loot_01","nether_acid_hole","nether_orecluster_small","nether_altar_medium","nether_ghast_spawner","nether_orecluster_castle","nether_building","nether_submarine","nether_orecluster_medium");
    public NetherStructuresFeature(){super(NoneFeatureConfiguration.CODEC);}
    /** Community edition Nether grids: big, then medium, then small; a third of the sites try the lava table (the submarine). */
    public static String choose(int cx,int cz,RandomSource random){
        if(!TGConfig.doWorldspawn)return null;
        int big=Math.max(1,TGConfig.spawnWeightTGStructureBigNether),medium=Math.max(1,TGConfig.spawnWeightTGStructureMediumNether),small=Math.max(1,TGConfig.spawnWeightTGStructureSmallNether);
        List<Spawn> land,lava=List.of();
        if(cx%big==0&&cz%big==0)land=List.of();
        else if(cx%medium==0&&cz%medium==0){land=MEDIUM;lava=LAVA;}
        else if(cx%small==0&&cz%small==0)land=SMALL;
        else return null;
        if(random.nextInt(3)==0){var chosen=pick(lava,random);if(chosen!=null)return chosen;}
        return pick(land,random);
    }
    private static final List<Spawn> LAVA=List.of(new Spawn("nether_submarine",10,false));
    private static String pick(List<Spawn> candidates,RandomSource random){
        int total=candidates.stream().filter(s->!s.cluster||TGConfig.spawnOreClusterStructures).mapToInt(Spawn::weight).sum();if(total==0)return null;
        int roll=random.nextInt(total)+1,sum=0;for(var spawn:candidates)if(!spawn.cluster||TGConfig.spawnOreClusterStructures){sum+=spawn.weight;if(roll<=sum)return spawn.id;}return null;
    }
    /** Community edition lava check: lava with air above at the four corners (from y 31 up), at most 3 apart; the average, or -1. */
    public static int lavaSurface(java.util.function.Function<BlockPos,net.minecraft.world.level.block.state.BlockState> world,int x,int z,int sizeX,int sizeZ){
        int[] h=new int[4];int i=0;
        for(int[] c:new int[][]{{x,z},{x+sizeX,z},{x,z+sizeZ},{x+sizeX,z+sizeZ}}){h[i]=-1;
            for(int y=31;y<100;y++)if(world.apply(new BlockPos(c[0],y,c[1])).is(net.minecraft.world.level.block.Blocks.LAVA)&&world.apply(new BlockPos(c[0],y+1,c[1])).isAir()){h[i]=y;break;}
            if(h[i++]<0)return -1;}
        int min=Math.min(Math.min(h[0],h[1]),Math.min(h[2],h[3])),max=Math.max(Math.max(h[0],h[1]),Math.max(h[2],h[3]));
        return max-min>3?-1:(h[0]+h[1]+h[2]+h[3])/4;
    }
    public static int airHeight(LevelAccessor world,int x,int z){return airHeight(world::getBlockState,x,z);}
    public static int airHeight(java.util.function.Function<BlockPos,net.minecraft.world.level.block.state.BlockState> world,int x,int z){int air=0;for(int y=100;y>20;y--){if(world.apply(new BlockPos(x,y,z)).isAir())++air;else{if(air>=10)return y;air=0;}}return -1;}
    public static int caveHeight(LevelAccessor world,int cx,int cz){return caveHeight(world::getBlockState,cx,cz);}
    public static int caveHeight(java.util.function.Function<BlockPos,net.minecraft.world.level.block.state.BlockState> world,int cx,int cz){
        int[] heights={airHeight(world,cx*16,cz*16),airHeight(world,cx*16+15,cz*16),airHeight(world,cx*16,cz*16+15),airHeight(world,cx*16+15,cz*16+15)};int min=Arrays.stream(heights).min().orElse(-1),max=Arrays.stream(heights).max().orElse(-1);
        return min>=20&&max<=100&&max-min<=10?(int)Math.round(Arrays.stream(heights).average().orElse(-1)):-1;
    }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context){
        var world=context.level();if(!world.getLevel().dimension().equals(Level.NETHER))return false;int cx=context.origin().getX()>>4,cz=context.origin().getZ()>>4;
        var id=choose(cx,cz,context.random());if(id==null)return false;var structure=ScannedStructure.load(id);int turns=context.random().nextInt(4),centerX=structure.sizeX/2,centerZ=structure.sizeZ/2;
        int minX=Integer.MAX_VALUE,minZ=Integer.MAX_VALUE;for(var corner:List.of(new BlockPos(0,0,0),new BlockPos(structure.sizeX,0,0),new BlockPos(0,0,structure.sizeZ),new BlockPos(structure.sizeX,0,structure.sizeZ))){var point=ScannedStructure.rotate(corner,centerX,centerZ,turns);minX=Math.min(minX,point.getX());minZ=Math.min(minZ,point.getZ());}
        int height=caveHeight(world,cx,cz);if(height<0)return false;structure.place(world,new BlockPos(cx*16+minX,height-1,cz*16+minZ),turns,world.getRandom());return true;
    }
}
