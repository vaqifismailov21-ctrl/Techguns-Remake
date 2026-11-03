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
public class CampProps extends CampStructure {
	
	CampBlock groundBlock = new CampBlock(CampBlocks.state("inline_5"));
	BlockState groundstate = groundBlock.getState();
	
	CampBlock[] crateBlocks = CampBlocks.CRATES; 
	
	CampBlock sandbags = new CampBlock(CampBlocks.state("inline_4"));
	BlockState sandbagsState = sandbags.getState();
	
	BlockState lampstate = CampBlocks.state("yellow_lantern");
	
	int variant = -1;

	public CampProps(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		super(minX, minY, minZ, maxX, maxY, maxZ);
		this.lootTier=CampLootTier.TIER1;
	}
	
	public CampProps(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int variant) {
		this(minX, minY, minZ, maxX, maxY, maxZ);
		this.variant = variant;
		this.lootTier=CampLootTier.TIER1;
	}

	@Override
	public void setBlocks(CampWorld world, int posX, int posY, int posZ, int sizeX,
			int sizeY, int sizeZ, int direction, BiomeColorType colorType, RandomSource rnd) {
		
		CampCamo camoMeta;
		switch (colorType) {
		case DESERT:
			camoMeta = CampCamo.DESERT;
			break;
		case SNOW:
			camoMeta = CampCamo.SNOW;
			break;
		case WOODLAND:
		default:
			camoMeta = CampCamo.WOOD;
			break;	
		}

		if (this.variant == -1) variant = world.random.nextInt(2);
		
		switch (variant) {
			case 0: 
		    default:
				setCrateBlocks(world, posX, posY, posZ, sizeX, sizeY, sizeZ,rnd);
				break;
			case 1: 
				setRoofBlocks(world, posX, posY, posZ, sizeX, sizeY, sizeZ, camoMeta);
				setCrateBlocks(world, posX+1, posY, posZ+1, sizeX-2, sizeY-2, sizeZ-2,rnd);				
				break;
			case 2: 
				setSandbagBlocks(world, posX, posY, posZ, sizeX, sizeY, sizeZ, direction);
				break;
			case 3: 
				setRoofBlocks(world, posX, posY+1, posZ, sizeX, sizeY-1, sizeZ, camoMeta);
				setSandbagBlocks(world, posX, posY, posZ, sizeX, sizeY, sizeZ, direction);
				break;
			case 4: 
				setSandbagRow(world, posX, posY, posZ, sizeX, sizeY, sizeZ, direction);
				break;
			case 5: 
				setRoofBlocks(world, posX, posY+1, posZ, sizeX, sizeY-1, sizeZ, camoMeta);
				setSandbagBlocks(world, posX, posY, posZ, sizeX, sizeY, sizeZ, direction);
				setCrateBlocks(world, posX+1, posY, posZ+1, sizeX-2, sizeY-2, sizeZ-2,rnd);				
				break;
		}
		
	}
	
	private void setSandbagRow(CampWorld world, int px, int py, int pz,
			int sizeX, int sizeY, int sizeZ, int direction) {

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		int x = 0;
		int z = 0;
		int i = 1;
		for (int y = 1; y < sizeY; y++) {
			if (sizeX > sizeZ) {
				z = sizeZ/2;
				for (x = i; x < sizeX-i; x++) {
					world.setBlockState(pos.set(px+x, py+y, pz+z), sandbagsState, 2);
				}
			}else {
				x = sizeX/2;
				for (z = i; z < sizeZ-i; z++) {
					world.setBlockState(pos.set(px+x, py+y, pz+z),sandbagsState, 2);
				}
			}
			i++;
		}
	}

	private void setSandbagBlocks(CampWorld world, int posX, int posY, int posZ,
			int sizeX, int sizeY, int sizeZ, int direction) {
		
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		
	
			for (int x = 0; x < sizeX; x++) {
				for (int z = 0; z < sizeZ; z++ ) {
			
			
			
						if (x== 0 || x == sizeX-1 || z==0 || z == sizeZ-1) {
							world.setBlockState(p.set(posX+x, posY+1, posZ+z), sandbagsState, 2);
						}
			
				}
			}
		
			
			
			int xoffset, zoffset; 
			int dx, dz;
			
			switch (direction) {
				case 0:
					xoffset = 0; zoffset=(sizeZ/2);
					break;
				case 1:
					xoffset = (sizeX/2); zoffset=0;
					break;
				case 2:
					xoffset = sizeX-1; zoffset=(sizeZ/2);
					break;
				case 3:
				default:
					xoffset = (sizeX/2); zoffset=sizeZ-1;
					break;
			}
			
			
			world.setBlockToAir(p.set(posX+xoffset, posY+1, posZ+zoffset));
		
	}

	private void setRoofBlocks(CampWorld world, int posX, int posY, int posZ,
			int sizeX, int sizeY, int sizeZ, CampCamo camo) {
		
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		CampTerrain.fillBlocks(world, p.set(posX, posY+1, posZ), 1, sizeY-2, 1, Blocks.OAK_FENCE.defaultBlockState());
		CampTerrain.fillBlocks(world, p.set(posX+sizeX-1, posY+1, posZ), 1, sizeY-2, 1, Blocks.OAK_FENCE.defaultBlockState());
		CampTerrain.fillBlocks(world, p.set(posX, posY+1, posZ+sizeZ-1), 1, sizeY-2, 1, Blocks.OAK_FENCE.defaultBlockState());
		CampTerrain.fillBlocks(world, p.set(posX+sizeX-1, posY+1, posZ+sizeZ-1), 1, sizeY-2, 1, Blocks.OAK_FENCE.defaultBlockState());
		CampTerrain.fillBlocks(world, p.set(posX, posY+sizeY-1, posZ), sizeX, 1, sizeZ, CampBlocks.camoTop(camo));
	}

	public void setCrateBlocks(CampWorld world, int posX, int posY, int posZ, int sizeX,
			int sizeY, int sizeZ, RandomSource rnd) {

		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		


		
		for (int x = 0; x < sizeX; x++) {
			for (int z=0; z <sizeZ; z++) {
				world.setBlockState(p.set(posX+x, posY, posZ+z), groundstate, 2);
				
				
				float chestroll = rnd.nextFloat();
				Block chest=null;
				
				
				
				
				if (chest==null){
				
					for (int y = 1; y < sizeY; y++) {
						if (!isFreeSpace(world, posX+x, posY+y-1, posZ+z) && rnd.nextFloat() > 0.5f) {
							int index = rnd.nextInt(crateBlocks.length);
	
	
	
	
								world.setBlockState(p.set(posX+x, posY+y, posZ+z), crateBlocks[index].getState(), 2);
							
						}
					}
				} 
			}
		}
		
		if (((sizeX+sizeZ)/2.0f)/6.0f > rnd.nextFloat()) { 
			for (int y = sizeY; y > 1; y--) {
				for (int t = 0; t < 3; t++) {
					int x = rnd.nextInt(sizeX);
					int z = rnd.nextInt(sizeZ);
					if (world.isAirBlock(p.set(posX+x, posY+y, posZ+z)) && !isFreeSpace(world,posX+x,posY+y-1,posZ+z)) {
						world.setBlockState(p, lampstate, 2);
						return;
					}
				}
			}
		}
	}
	
	public boolean isFreeSpace(CampWorld world, int x, int y, int z) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();	
		return world.isAirBlock(p.set(x, y, z)) || world.getBlockState(p).getBlock() == Blocks.SNOW || CampBlocks.isLamp(world.getBlockState(p));
	}
}

