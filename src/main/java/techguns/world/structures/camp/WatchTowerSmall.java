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
public class WatchTowerSmall extends CampStructure{
	
	int towerSize = 3;

	public WatchTowerSmall(int minX, int minY, int minZ, int maxX, int maxY,
			int maxZ, int towerSize) {
		super(minX, minY, minZ, maxX, maxY, maxZ);
		this.towerSize = towerSize;
	}

	@Override
	public void setBlocks(CampWorld world, int posX, int posY, int posZ, int sizeX,
			int sizeY, int sizeZ, int direction, BiomeColorType colorType, RandomSource rnd) {
		
		
		if (sizeX >= towerSize+2) {
			posX+= (sizeX-towerSize)/2;
		}
		if (sizeZ >= towerSize+2) {
			posZ+= (sizeZ-towerSize)/2;
		}
		
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		
		
		for (int y = 1; y < sizeY-1; y++) {
			world.setBlockState(p.set(posX, posY+y, posZ), Blocks.OAK_FENCE.defaultBlockState());
			world.setBlockState(p.set(posX+towerSize-1, posY+y, posZ), Blocks.OAK_FENCE.defaultBlockState());
			world.setBlockState(p.set(posX, posY+y, posZ+towerSize-1), Blocks.OAK_FENCE.defaultBlockState());
			world.setBlockState(p.set(posX+towerSize-1, posY+y, posZ+towerSize-1), Blocks.OAK_FENCE.defaultBlockState());
			
			if (y < sizeY-3) {
				for (int x = 1; x < towerSize-1; x++ ) {
					for (int z = 1; z < towerSize-1; z++) {
						world.setBlockState(p.set(posX+x, posY+y, posZ+z), Blocks.OAK_PLANKS.defaultBlockState());
					}
				}
			}
			
		}
		
		CampTerrain.fillBlocksHollow(world, p.set(posX, posY+sizeY-4, posZ), towerSize, 2, towerSize, Blocks.OAK_FENCE.defaultBlockState());
		CampTerrain.fillBlocks(world, p.set(posX, posY+sizeY-1, posZ), towerSize, 1, towerSize, CampBlocks.state("default_CAMONET_TOP"));
		
		
		
		int xoffset = 0; int zoffset = 0;
		int meta = 0;
		switch (direction) {
			case 0:
				xoffset = 0; zoffset=1;
				meta = 4;
				break;
			case 1:
				xoffset = 1; zoffset=0;
				meta = 2;
				break;
			case 2:
				xoffset = towerSize-1; zoffset=towerSize-2;
				meta = 5;
				break;
			case 3:
				xoffset = towerSize-2; zoffset=towerSize-1;
				meta = 3;
				break;
		}
		
		
		
		
		for (int y = 1; y < sizeY-3; y++) {
			world.setBlockState(p.set(posX+xoffset, posY+y, posZ+zoffset), CampBlocks.legacy("ladder",meta), 3);
		}
		world.setBlockState(p.set(posX+xoffset, posY+sizeY-3, posZ+zoffset), CampBlocks.legacy("oak_fence_gate",(direction+1) % 4), 3);
		
		
		
	}
}
