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
public class Helipad extends CampStructure{


	public Helipad(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		super(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public void setBlocks(CampWorld world, int posX, int posY, int posZ, int sizeX,
			int sizeY, int sizeZ, int direction, BiomeColorType colorType, RandomSource rnd) {
		if (sizeX < 7 || sizeZ < 7) {
			return;
		}
		if (sizeX > 9) {
			int offset = (sizeX-9)/2;
			posX+=offset;
			sizeX=9;
		}
		if (sizeZ > 9) {
			int offset = (sizeZ-9)/2;
			posZ+=offset;
			sizeZ=9;
		}
		
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		
		
		if (sizeX==9 && sizeZ == 9) {
			for (int z = 1; z<sizeZ-1; z++) {
				world.setBlockState(p.set(posX, posY, posZ+z), new CampBlock(CampBlocks.state("HELIPAD_WIREFRAME")).getState(), 2);
				world.setBlockState(p.set(posX+sizeX-1, posY, posZ+z), new CampBlock(CampBlocks.state("HELIPAD_WIREFRAME")).getState(), 2);
			}
			posX++;
			sizeX=7;		
			for (int x = 0; x<sizeX; x++) {
				world.setBlockState(p.set(posX+x, posY, posZ), new CampBlock(CampBlocks.state("HELIPAD_WIREFRAME")).getState(), 2);
				world.setBlockState(p.set(posX+x, posY, posZ+sizeZ-1), new CampBlock(CampBlocks.state("HELIPAD_WIREFRAME")).getState(), 2);
			}
			posZ++;
			sizeZ=7;
		}else {
			sizeX=7;
			sizeZ=7;
		}
		
		
		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z < sizeZ; z++) {
				if (x==0 || z==0 || x==sizeX-1 || z==sizeZ-1) {
					world.setBlockState(p.set(posX+x, posY+1, posZ+z), new CampBlock(CampBlocks.state("HELIPAD_HAZARDBLOCK")).getState(), 2);
				}else {
					world.setBlockState(p.set(posX+x, posY+1, posZ+z), new CampBlock(CampBlocks.state("HELIPAD_CONCRETE")).getState(), 2);
				}
			}
		}
		
		
		world.setBlockState(p.set(posX+2, posY+1, posZ+2), new CampBlock(CampBlocks.state("HELIPAD_GLOWBLOCK")).getState(), 2);
		world.setBlockState(p.set(posX+3, posY+1, posZ+2), new CampBlock(CampBlocks.state("HELIPAD_GLOWBLOCK")).getState(), 2);
		world.setBlockState(p.set(posX+4, posY+1, posZ+2), new CampBlock(CampBlocks.state("HELIPAD_GLOWBLOCK")).getState(), 2);
		world.setBlockState(p.set(posX+3, posY+1, posZ+3), new CampBlock(CampBlocks.state("HELIPAD_GLOWBLOCK")).getState(), 2);
		world.setBlockState(p.set(posX+2, posY+1, posZ+4), new CampBlock(CampBlocks.state("HELIPAD_GLOWBLOCK")).getState(), 2);
		world.setBlockState(p.set(posX+3, posY+1, posZ+4), new CampBlock(CampBlocks.state("HELIPAD_GLOWBLOCK")).getState(), 2);
		world.setBlockState(p.set(posX+4, posY+1, posZ+4), new CampBlock(CampBlocks.state("HELIPAD_GLOWBLOCK")).getState(), 2);
	}

}

