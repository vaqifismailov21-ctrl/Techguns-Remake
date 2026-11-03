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
public class Bunker extends CampStructure {

	private static final ResourceLocation CHEST_LOOT = ResourceLocation.parse("techguns:chests/militarybase_bunker");
	
	 CampBlock groundBlock = new CampBlock(CampBlocks.state("inline_13"));
	 CampBlock stairsblock = new CampBlock(CampBlocks.state("inline_12"));
	 CampBlock wallBlock = new CampBlock(CampBlocks.state("inline_11"));
	 CampBlock roofBlock = new CampBlock(CampBlocks.state("inline_10"));
	 CampBlock sandbags = new CampBlock(CampBlocks.state("inline_9"));
	 CampBlock ladder = new CampBlock(CampBlocks.state("inline_8"));
	 CampBlock[] crateBlocks = CampBlocks.CRATES; 
	 
	 CampBlock lantern = new CampBlock(CampBlocks.state("inline_7"));
	 CampBlock lamps = new CampBlock(CampBlocks.state("inline_6"));
	 
	 int type = 0;
	
	public Bunker(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int type) {
		super(minX, minY, minZ, maxX, maxY, maxZ);
		this.type = type;
		this.lootTier=CampLootTier.TIER1;
	}

	@Override
	public void setBlocks(CampWorld world, int posX, int posY, int posZ, int sizeX,
			int sizeY, int sizeZ, int direction, BiomeColorType colorType, RandomSource rnd) {
		
			BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		
			
			if ((sizeX <= 4) && (sizeZ >= 5)) {
				if (direction == 0 || direction == 2) {
					direction = (direction+1) % 4;
				}
			}else if ((sizeZ <= 4) && (sizeX >= 5)) {
				if (direction == 1 || direction == 3) {
					direction = (direction+1) % 4;
				}
			}
		
			switch (direction) {
			case 0:
				posX+=2;
				sizeX-=2;
				break;
			case 1:
				posZ+=2;
				sizeZ-=2;
				break;
			case 2:
				sizeX-=2;
				break;
			case 3:
				sizeZ-=2;
				break;
		 }
			
		Direction facing = directionToFacing(direction);

			
		 
		 
	 
		 for (int y = -1; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				for (int z = 0; z < sizeZ; z++) {
					if (y == -1) {
						
						world.setBlockState(p.set(posX+x, posY+y, posZ+z), groundBlock.getState(), 2);
					}else if (y == 0) {
						if (x== 0 || x == sizeX-1 || z==0 || z == sizeZ-1) {
							
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), wallBlock.getState(), 2);
						} else {
							world.setBlockToAir(p.set(posX+x, posY+y, posZ+z));
						}
					}else if (y == 1) {
						if (type == 2 || type == 3) { 
							if (x== 0 || x == sizeX-1 || z==0 || z == sizeZ-1) {
								
								world.setBlockState(p.set(posX+x, posY+y, posZ+z), wallBlock.getState(), 2);
							}				
						}else {
							if (((x== 0 || x == sizeX-1) && (z==0 || z == sizeZ-1 || z % 3 == 0)) ||
									((x== 0 || x == sizeX-1 || x % 3 == 0) && (z==0 || z == sizeZ-1 ))) {
								
								world.setBlockState(p.set(posX+x, posY+y, posZ+z), wallBlock.getState(), 2);
							}
						}
					}else if (y == 2) {
						if (x== 0 || x == sizeX-1 || z==0 || z == sizeZ-1) {
							
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), wallBlock.getState(), 2);
						}else {
							
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), roofBlock.getState(), 2);
						}
					}else if (y == 3 && (type == 1 || type == 3)) { 
						if (x== 0 || x == sizeX-1 || z==0 || z == sizeZ-1) {
							
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), sandbags.getState(), 2);
						}
					}
					
				}
			}
		}
	 
		
		
		
		
		int xoffset, zoffset; 
		int dx, dz;
		
		switch (direction) {
			case 0:
				xoffset = 0; zoffset=sizeZ/2;
				dx = -1; dz =0;
				break;
			case 1:
				xoffset = sizeX/2; zoffset=0;
				dx = 0; dz = -1;
				break;
			case 2:
				xoffset = sizeX-1; zoffset=sizeZ/2;
				dx = 1; dz=0;
				break;
			case 3:
			default:
				xoffset = sizeX/2; zoffset=sizeZ-1;
				dx = 0; dz=1;
				break;
		}
		
		
		world.setBlockToAir(p.set(posX+xoffset, posY+1, posZ+zoffset));
		world.setBlockToAir(p.set(posX+xoffset, posY+0, posZ+zoffset));
		
		
		CampRotator.placeDoor(world, p.set(posX+xoffset, posY+0, posZ+zoffset), facing, TGDoors.BUNKER.get(), true);
		
		
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? 1 : 0), posY+0, posZ+zoffset+((dz == 0) ? 1 : 0)), wallBlock.getState(), 2);
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? 1 : 0), posY+1, posZ+zoffset+((dz == 0) ? 1 : 0)), wallBlock.getState(), 2);
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? -1 : 0), posY+0, posZ+zoffset+((dz == 0) ? -1 : 0)), wallBlock.getState(), 2);
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? -1 : 0), posY+1, posZ+zoffset+((dz == 0) ? -1 : 0)), wallBlock.getState(), 2);
		
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? 1 : dx), posY-1, posZ+zoffset+((dz == 0) ? 1 : dz)), wallBlock.getState(), 2);
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? -1 : dx), posY-1, posZ+zoffset+((dz == 0) ? -1 : dz)), wallBlock.getState(), 2);
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? 1 : dx*2), posY-1, posZ+zoffset+((dz == 0) ? 1 : dz*2)), wallBlock.getState(), 2);
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? -1 : dx*2), posY-1, posZ+zoffset+((dz == 0) ? -1 : dz*2)), wallBlock.getState(), 2);		
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? 1 : dx), posY+0, posZ+zoffset+((dz == 0) ? 1 : dz)), wallBlock.getState(), 2);
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? -1 : dx), posY+0, posZ+zoffset+((dz == 0) ? -1 : dz)), wallBlock.getState(), 2);
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? 1 : dx*2), posY+0, posZ+zoffset+((dz == 0) ? 1 : dz*2)), wallBlock.getState(), 2);
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? -1 : dx*2), posY+0, posZ+zoffset+((dz == 0) ? -1 : dz*2)), wallBlock.getState(), 2);
		
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? 1 : dx), posY+2, posZ+zoffset+((dz == 0) ? 1 : dz)), CampRotator.getWithFacing(lamps.getState(), facing.getOpposite()), 2);
		world.setBlockState(p.set(posX+xoffset+((dx == 0) ? -1 : dx), posY+2, posZ+zoffset+((dz == 0) ? -1 : dz)),CampRotator.getWithFacing(lamps.getState(), facing.getOpposite()), 2);
		
		world.setBlockState(p.set(posX+xoffset, posY-1, posZ+zoffset), groundBlock.getState(), 2);
		world.setBlockState(p.set(posX+xoffset+dx, posY-1, posZ+zoffset+dz), groundBlock.getState(), 2);
		world.setBlockToAir(p.set(posX+xoffset+dx, posY+0, posZ+zoffset+dz));
		world.setBlockToAir(p.set(posX+xoffset+dx, posY+1, posZ+zoffset+dz));
		
		
		world.setBlockState(p.set(posX+xoffset+dx*2, posY+0, posZ+zoffset+dz*2), CampRotator.getWithFacing(stairsblock.getState(), facing), 2);
		world.setBlockToAir(p.set(posX+xoffset+dx*2, posY+1, posZ+zoffset+dz*2));
		
		
		
		
		if (type == 2 || type == 3) {
			
			world.setBlockState(p.set(posX+xoffset-dx, posY+1, posZ+zoffset-dz), CampRotator.getWithFacing(lamps.getState(), Direction.UP), 2);



				setCrateBlocks(world, p, posX+2, posY, posZ+2, sizeX-4, 2, sizeZ-4);
			
			
			
		}else {
			
			world.setBlockState(p.set(posX+(sizeX/2), posY+1, posZ+(sizeZ/2)), CampRotator.getWithFacing(lamps.getState(), Direction.UP), 2);
		
		}
		
		
		
		
		if (type == 1 || type == 3) {
			int meta = 0;
			Direction ladderFacing=Direction.NORTH;
			switch (direction) {
				case 0: 
					xoffset = sizeX-2;
					zoffset = sizeZ-2;
					dx = 1; dz=0;
					meta = 2;
					ladderFacing=Direction.WEST;
					break;
				case 1: 
					zoffset = sizeZ-2;
					xoffset = 1;
					dx = 0; dz = 1;
					meta = 0;
					ladderFacing=Direction.NORTH;
					break;
				case 2: 
					xoffset = 1;
					zoffset = 1;
					dx = -1; dz = 0;
					meta = 3;
					ladderFacing=Direction.EAST;
					break;
				case 3: 
					xoffset = sizeX-2;
					zoffset = 1;
					dx = 0; dz = -1;
					meta = 1;
					ladderFacing=Direction.SOUTH;
					break;
			}
			
			
			world.setBlockToAir(p.set(posX+xoffset, posY+2, posZ+zoffset));
			world.setBlockState(p.set(posX+xoffset+dx, posY+1, posZ+zoffset+dz), wallBlock.getState(), 2);
			
			
			world.setBlockState(p.set(posX+xoffset, posY+0, posZ+zoffset), CampRotator.getWithFacing(ladder.getState(),ladderFacing), 2);
			world.setBlockState(p.set(posX+xoffset, posY+1, posZ+zoffset), CampRotator.getWithFacing(ladder.getState(),ladderFacing), 2);
			world.setBlockState(p.set(posX+xoffset, posY+2, posZ+zoffset), CampRotator.getWithFacing(ladder.getState(),ladderFacing), 2);
		}
		
	}

	private void setCrateBlocks(CampWorld world, BlockPos.MutableBlockPos p, int posX, int posY, int posZ, int sizeX,
			int sizeY, int sizeZ) {
		RandomSource r = world.random;
		for (int x = 0; x < sizeX; x++) {
			for (int z=0; z <sizeZ; z++) {
				
				float chestroll = r.nextFloat();
				Block chest=null;
				
				
				if (chestroll <=0.1f) {
					chest = Blocks.CHEST;
				}
				
				
				if (chest==null){
					
					for (int y = 0; y < sizeY; y++) {
						if (!world.isAirBlock(p.set(posX+x, posY+y-1, posZ+z)) && r.nextFloat() > 0.5) {
							int index = r.nextInt(crateBlocks.length);
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), crateBlocks[index].getState(), 2);
						}
					}
				} else {
					
					int meta = r.nextInt(4);
					
					world.setBlockState(p.set(posX+x, posY, posZ+z), CampRotator.getRotatedHorizontal(chest.defaultBlockState(),meta));
					
					CampTileData tile = world.getTileEntity(p);
					if(tile !=null && tile instanceof CampChestData) {
						((CampChestData)tile).setLootTable(CHEST_LOOT, world.random.nextLong());
					}
					
					
				}
			}
		}
	}

}

