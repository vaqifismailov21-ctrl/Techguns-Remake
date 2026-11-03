package techguns.world.structures.camp;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import techguns.*;
import techguns.world.structures.camp.CampStructure.BiomeColorType;
/** Mechanically adapted from the original camp source; layout, dimensions and random-call order retained. */
public final class CampTerrain {
public static final float[][] FILTER_GAUSSIAN_5x5 = new float[][] {
		{.00325f, .01375f, .0235f, .01375f, .00325f},
        {.01375f, .059f, .097f, .059f, .01375f},
        {.0235f, .097f, .159f, .097f, .0235f},
        {.01375f, .059f, .097f, .059f, .01375f},
        {.00325f, .01375f, .0235f, .01375f, .00325f}
	};
public static void fillBlocks(CampWorld world, BlockPos.MutableBlockPos start, int sizeX, int sizeY, int sizeZ, BlockState block) {
		int x = start.getX();
		int y = start.getY();
		int z = start.getZ();
		for (int i = 0; i < sizeX; i++) {			
			for (int j = 0; j < sizeZ; j++) {
				for (int k = 0; k < sizeY; k++) {
					world.setBlockState(start.set(x+i, y+k, z+j), block);
				}	
			}
		}
	}
public static void fillBlocksHollow(CampWorld world, BlockPos.MutableBlockPos start, int sizeX, int sizeY, int sizeZ, BlockState block) {
		int x = start.getX();
		int y = start.getY();
		int z = start.getZ();
		
		for (int i = 0; i < sizeX; i++) {			
			for (int j = 0; j < sizeZ; j++) {
				for (int k = 0; k < sizeY; k++) {
					if (j == 0 || j == sizeZ-1 || i == 0 || i == sizeX-1) {
						world.setBlockState(start.set(x+i, y+k, z+j), block);
					}
				}	
			}
		}
	}
public static void flattenArea(CampWorld world, int posX, int posZ, int sizeX, int sizeZ, int maxHeightDiff) {
		int posY;
		int heightSum = 0;
		int count = (sizeX*sizeZ);
		int min = -1;
		int max = 0;
		ArrayList<Integer> heights = new ArrayList<Integer>();
		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z <sizeZ; z++) {
				int y = getHeightValueNoTrees(world,posX+x, posZ+z)-1;
				
				heights.add(Integer.valueOf(y));
				heightSum+=y;
				if (y > max) max = y;
				if (y < min || min == -1) min = y;
			}
		}
		
		int span = max-min;
		
		float median=0;
		Collections.sort(heights);
		if (heights.size()>2){
			if (heights.size() % 2 ==0){
				median = (heights.get(((int)Math.floor(heights.size()/2))-1) + heights.get(((int)Math.ceil(heights.size()/2))-1)) /2;
			} else {
				median = heights.get((heights.size()/2)-1);
			}
		}
		
		
		float averageHeight = median; 
		float f = averageHeight - ((float)maxHeightDiff/2.0f);
		
		
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z <sizeZ; z++) {
				int y =getHeightValueNoTrees(world, posX+x, posZ+z)-1;
				int newY;
				if (span <= maxHeightDiff) newY = y;
				else newY = Math.round(((((float)(y-min)/(float)span)*(float)maxHeightDiff) + f));
				
				adjustHeightAtPos(world, posX+x, posZ+z, y, p, newY);

			}
		}
	}
private static void adjustHeightAtPos(CampWorld world, int x, int z, int y, BlockPos.MutableBlockPos p, int newY) {
		if (newY  > y) {
			BlockState b = world.getBlockState(p.set(x, y, z));
			if (!isGroundBlock(b)) {
				b = Blocks.DIRT.defaultBlockState();
			}
			for (int i = y+1; i <= newY; i++) {
				world.setBlockState(p.set(x, i, z), b, 2);
			}
		}else if (newY < y) {
			for (int i = y; i > newY; i--) {
				world.setBlockToAir(p.set(x, i, z));
			}
		}
	}
public static void apply2DHeightmapFilter(CampWorld world, int posX, int posZ, int sizeX, int sizeZ, float[][] filter) {
		int xoffset = -filter.length/2;
		int zoffset = -filter[0].length/2;
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z <sizeZ; z++) {
				int y = getHeightValueNoTrees(world,posX+x, posZ+z)-1;
				float newY = 0.0f;
				for (int i = 0; i < filter.length; i++) {
					for (int j = 0; j < filter[i].length; j++){
						newY += (getHeightValueNoTrees(world,posX+x+xoffset+i, posZ+z+zoffset+j)-1.0f) * filter[i][j];
					}
				}			
				adjustHeightAtPos(world, posX+x, posZ+z, y, p, Math.round(newY));						
			}
		}
	}
public static int getValidSpawnYArea(CampWorld world, int x, int z, int sizeX, int sizeZ, int heightDiffLimit, int step){
 		
 		int count=0;
 		int min=255;
 		int max=0;
 		int sum=0;
 				
 		int ix=0;
 		while(ix<=sizeX){
 		
 			int iz=0;
 			while(iz<=sizeZ){
 	 			
 				int h=getHeightValueNoTreesIgnoreLiquid(world, x+ix, z+iz); 
 				
 				if(h<0){
 					return -1;
 				}
 				
 				if (h<min){
 					min=h;
 				}
 				if(h>max){
 					max=h;
 				}
 				sum+=h;
 				count++;	
 				
 				if(iz<=sizeZ+step){
 					iz+=step;
 				} else if(iz<sizeZ && iz+step > sizeZ){
 					iz=sizeZ;
 				}
 				
 	 		}
 			
 			if(ix<=sizeX+step){
				ix+=step;
			} else if(ix<sizeX && ix+step > sizeX){
				ix=sizeX;
			}
 		}
 		
 		
 		
 		if((max-min)>heightDiffLimit){
 			return -1;
 		}
 		
 		return sum/count;
 		
 	}
public static BiomeColorType getBiomeType(CampWorld world,int x,int z){return world.color;}
public static int getHeightValueNoTrees(CampWorld world,int x,int z){int y=world.rawHeight(x,z)-1;while(y>0){var p=new BlockPos(x,y,z);var state=world.getBlockState(p);if((state.getMaterial().blocksMotion()&&!state.is(net.minecraft.tags.BlockTags.LEAVES)&&!foliage(state))||!state.getFluidState().isEmpty())break;y--;}y++;while(y>0){var state=world.getBlockState(new BlockPos(x,y,z));if(!state.is(net.minecraft.tags.BlockTags.LEAVES)&&!state.is(net.minecraft.tags.BlockTags.LOGS)&&!foliage(state))break;y--;}return y;}
public static int getHeightValueNoTreesIgnoreLiquid(CampWorld world,int x,int z){int y=getHeightValueNoTrees(world,x,z);return world.getBlockState(new BlockPos(x,y,z)).getFluidState().isEmpty()?y:-1;}
private static boolean foliage(BlockState state){return state.getBlock() instanceof BushBlock||state.getBlock() instanceof VineBlock;}
public static boolean isGroundBlock(BlockState state){var id=net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.getBlock());String name=id.getPath();if(id.getNamespace().equals("techguns"))return name.startsWith("concrete_")||name.startsWith("ore_")||name.equals("sand_hard");return state.is(net.minecraft.tags.BlockTags.DIRT)||state.is(net.minecraft.tags.BlockTags.SAND)||state.is(net.minecraft.tags.BlockTags.TERRACOTTA)||state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD)||state.is(Blocks.CLAY)||state.is(Blocks.GRAVEL)||state.is(Blocks.MYCELIUM)||state.is(Blocks.MUD)||name.endsWith("sandstone")||name.endsWith("cobblestone")||name.endsWith("stone_bricks")||name.endsWith("_ore")||name.endsWith("_concrete")||state.is(Blocks.STONE)||state.is(Blocks.NETHERRACK);}
public static void removeJunkInArea(CampWorld world,int posX,int posZ,int sizeX,int sizeZ){for(int x=0;x<sizeX;x++)for(int z=0;z<sizeZ;z++){world.markCleanup(posX+x,posZ+z);int y=world.rawHeight(posX+x,posZ+z);for(;y>0;y--){var pos=new BlockPos(posX+x,y,posZ+z);if(isGroundBlock(world.getBlockState(pos)))break;world.setBlockToAir(pos);}}}
public static void cleanupNativeColumn(net.minecraft.world.level.WorldGenLevel world,int x,int z){int y=world.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,x,z);for(;y>0;y--){var pos=new BlockPos(x,y,z);if(isGroundBlock(world.getBlockState(pos)))break;world.setBlock(pos,Blocks.AIR.defaultBlockState(),18);}}
private CampTerrain(){}
}
