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
public class Containers extends CampStructure{

	int containerWidth = 2;
	int containerHeight = 2;
	int containerMinLength = 2;
	BlockState containerBlock=null;
	
	public Containers(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int containerWidth, int containerHeight, int containerMinLength, CampBlock containerBlock) {
		super(minX, minY, minZ, maxX, maxY, maxZ);
		this.containerWidth = containerWidth;
		this.containerHeight = containerHeight;
		this.containerMinLength = containerMinLength;
		this.containerBlock = containerBlock.getState();
	}
	
	public Containers(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, CampBlock containerBlock) {
		super(minX, minY, minZ, maxX, maxY, maxZ);
		this.containerBlock = containerBlock.getState();
	}

	@Override
	public void setBlocks(CampWorld world, int posX, int posY, int posZ, int sizeX,
			int sizeY, int sizeZ, int direction, BiomeColorType colorType, RandomSource rnd) {
		int count; 
		int offset = 0;
		
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		if (sizeX > sizeZ) {
			count = (int)(((float)sizeZ+1.0f)/((float)containerWidth+1.0f));		
			for (int i = 0;  i < count; i++) {
				CampTerrain.fillBlocks(world, pos.set(posX, posY+1, posZ+offset), sizeX, containerHeight, containerWidth, containerBlock);
				offset+=(containerWidth+1);
			}
			
		}else {
			count = (int)(((float)sizeX+1.0f)/((float)containerWidth+1.0f));			
			for (int i = 0;  i < count; i++) {
				CampTerrain.fillBlocks(world, pos.set(posX+offset, posY+1, posZ), containerWidth, containerHeight, sizeZ, containerBlock);
				offset+=(containerWidth+1);
			}
		}
		
		
	}

}
