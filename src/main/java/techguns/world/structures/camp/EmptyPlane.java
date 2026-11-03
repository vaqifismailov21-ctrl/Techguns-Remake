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
public class EmptyPlane extends CampStructure {

	BlockState block;
	
	public EmptyPlane(BlockState block) {
		super (1, 1, 1, -1, -1, -1);
		this.block = block;
	}
	
	public EmptyPlane(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState block) {
		super(minX, minY, minZ, maxX, maxY, maxZ);
		this.block = block;
	}

	@Override
	public void setBlocks(CampWorld world, int posX, int posY, int posZ, int sizeX,
			int sizeY, int sizeZ, int direction, BiomeColorType colorType, RandomSource rnd) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int i = 0; i < sizeX; i++) {			
			for (int j = 0; j < sizeZ; j++) {		
				world.setBlockState(p.set(posX+i, posY, posZ+j), block);
			}
		}
	}
}

