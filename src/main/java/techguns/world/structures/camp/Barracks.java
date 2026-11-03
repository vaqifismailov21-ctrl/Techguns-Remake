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
public class Barracks extends CampStructure {
	
	private static final ResourceLocation CHEST_LOOT = ResourceLocation.parse("techguns:chests/militarybase_barracks");
	
	CampBlock b_pillar = new CampBlock(CampBlocks.state("BARRACKS_WOOD_PILLAR")); 
	CampBlock b_wall = new CampBlock(CampBlocks.state("BARRACKS_WOOD_WALL")); 
	CampBlock b_floor = new CampBlock(CampBlocks.state("BARRACKS_WOOD_FLOOR")); 
	CampBlock b_scaffold = new CampBlock(CampBlocks.state("BARRACKS_WOOD_SCAFFOLD")); 
	CampBlock b_roof = new CampBlock(CampBlocks.state("BARRACKS_WOOD_ROOF")); 
	CampBlock b_roofSlab = new CampBlock(CampBlocks.state("BARRACKS_WOOD_ROOFSLAB")); 
	CampBlock b_stairs = new CampBlock(CampBlocks.state("WOOD_STAIRS_OAK"));
	CampBlock b_torch = new CampBlock(CampBlocks.state("TGLAMP"));
	CampBlock b_ground = new CampBlock(CampBlocks.state("GRAVEL"));
	CampBlock b_window = new CampBlock(CampBlocks.state("GLASS_PANE"));
	
	
	public Barracks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		super(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public void setBlocks(CampWorld world, int posX, int posY, int posZ, int sizeX,
			int sizeY, int sizeZ, int direction, BiomeColorType colorType, RandomSource rnd) {
		
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		
		Direction facing = directionToFacing(direction);
		
		
		
		
		
		
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
		default:
			sizeZ-=2;
			break;
		}
			
		sizeX-= ((sizeX-1) % 4);
		sizeZ-= ((sizeZ-1) % 4);
				
		
		
		
		
		sizeY = 7;
		
		
		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z < sizeZ; z++) {
				for (int y = 0; y < sizeY; y++) {
					if (y ==0) { 
						world.setBlockState(p.set(posX+x, posY+y, posZ+z), b_ground.getState(), 2);
					}else if (y== 1) { 
						if (x % 4 == 0 && z % 4 == 0) {
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), b_pillar.getState(), 2);
						}
					}else if( y==2) { 
						if (x==0 || z==0 || x== sizeX-1 || z == sizeZ-1) {
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), b_scaffold.getState(), 2);
						}else {
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), b_floor.getState(), 2);		
						}
					}else if (y <= 4){ 
						if (x==0 || z==0 || x== sizeX-1 || z == sizeZ-1) {
							if (x % 4 == 0 && z % 4 == 0) {
								world.setBlockState(p.set(posX+x, posY+y, posZ+z), b_scaffold.getState(), 2);
							}else {
								if (y==4 && (x % 4 ==2 || z % 4 == 2)) { 
									world.setBlockState(p.set(posX+x, posY+y, posZ+z), b_window.getState(), 2);
								}else{
									world.setBlockState(p.set(posX+x, posY+y, posZ+z), b_wall.getState(), 2);
								}
							}
						}
					}else if (y == 5) { 
						if (x==0 || z==0 || x== sizeX-1 || z == sizeZ-1) {
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), b_scaffold.getState(), 2);
						}
					}else if (y == 6) {
						if (x==0 || z==0 || x== sizeX-1 || z == sizeZ-1) {
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), b_roofSlab.getState(), 2);
						}else {
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), b_roof.getState(), 2);		
						}
					}
				}
			}
		}
		
		
		
		
		
		int xoffset, zoffset; 
		int dx, dz;
		
		switch (direction) {
			case 0:
				xoffset = 0; zoffset=((sizeZ/8)*4)+2;
				dx = -1; dz =0;
				break;
			case 1:
				xoffset = ((sizeX/8)*4)+2; zoffset=0;
				dx = 0; dz = -1;
				break;
			case 2:
				xoffset = sizeX-1; zoffset=((sizeZ/8)*4)+2;
				dx = 1; dz=0;
				break;
			case 3:
			default:
				xoffset = ((sizeX/8)*4)+2; zoffset=sizeZ-1;
				dx = 0; dz=1;
				break;
		}
		
		
		world.setBlockToAir(p.set(posX+xoffset, posY+4, posZ+zoffset));
		world.setBlockToAir(p.set(posX+xoffset, posY+3, posZ+zoffset));
		CampRotator.placeDoor(world, p.set(posX+xoffset, posY+3, posZ+zoffset), facing.getOpposite(), Blocks.OAK_DOOR, false);
		

		
		world.setBlockState(p.set(posX+xoffset+dx, posY+5, posZ+zoffset+dz), CampRotator.getWithFacing(b_torch.getState(),facing.getOpposite()), 2);
		
		
		world.setBlockState(p.set(posX+xoffset+dx, posY+2, posZ+zoffset+dz), CampRotator.getWithFacing(b_stairs.getState(),facing.getOpposite()), 2);
		world.setBlockState(p.set(posX+xoffset+dx*2, posY+1, posZ+zoffset+dz*2), CampRotator.getWithFacing(b_stairs.getState(),facing.getOpposite()), 2);
		
		
		
		
		
		int xHalf = sizeX/2;
		int zHalf = sizeZ/2;
		
		for (int x = 1; x < sizeX-1; x++) {
			for (int z = 1; z < sizeZ-1; z++) {
				for (int y = 3; y < 6; y++) {
					if (x==1 || z==1 || x== sizeX-2 || z == sizeZ-2) {
						if (y==5 && (x % 4 ==2 || z % 4 == 2)) { 
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), CampRotator.getWithFacing(b_torch.getState(),Direction.UP), 2);
						}
						if (y == 3 && (x % 4 == 0 || z % 4 == 0) && x!= xoffset && z!= zoffset) {
							
							Direction chestrot = Direction.EAST;
							if (z % 4==0) {
								if(x>xHalf) {
									chestrot =chestrot.getOpposite();
								}
							} else if(x%4==0) {
								chestrot=Direction.SOUTH;
								if(z>zHalf) {
									chestrot =chestrot.getOpposite();
								}
							}
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), CampRotator.getWithFacing(Blocks.CHEST.defaultBlockState(),chestrot), 2);
							
							CampTileData tile = world.getTileEntity(p);
							if(tile !=null && tile instanceof CampChestData) {
								((CampChestData)tile).setLootTable(CHEST_LOOT, world.random.nextLong());
							}
							
						}
					}else{
						if (x==2 || z == 2 || x== sizeX-3 || z== sizeZ-3) {		
							
							if (sizeX / 4 > 1 || sizeZ / 4 > 1) {
								if (y==3 && (x % 4 ==2 && z % 4 == 2) && x!= xoffset && z!= zoffset) {
									if (x==2) {
										world.setBlockState(p.set(posX+x, posY+y, posZ+z), CampBlocks.legacy("bed",1), 3);
										world.setBlockState(p.set(posX+x-1, posY+y, posZ+z), CampBlocks.legacy("bed",1+8), 3);
									}else if (x==sizeX-3) {
										world.setBlockState(p.set(posX+x, posY+y, posZ+z), CampBlocks.legacy("bed",3), 3);
										world.setBlockState(p.set(posX+x+1, posY+y, posZ+z), CampBlocks.legacy("bed",3+8), 3);
									}else if (z==2) {
										world.setBlockState(p.set(posX+x, posY+y, posZ+z), CampBlocks.legacy("bed",2), 3);
										world.setBlockState(p.set(posX+x, posY+y, posZ+z-1), CampBlocks.legacy("bed",2+8), 3);
									}else if (z==sizeZ-3) {
										world.setBlockState(p.set(posX+x, posY+y, posZ+z), CampBlocks.legacy("bed",0), 3);
										world.setBlockState(p.set(posX+x, posY+y, posZ+z+1), CampBlocks.legacy("bed",0+8), 3);
									}					
								}
							}



								
						}
						
						if (y == 5 && x % 8 == 0 && z % 8 == 0) {
							world.setBlockState(p.set(posX+x, posY+y, posZ+z), CampRotator.getWithFacing(b_torch.getState(),Direction.UP), 2);
						}
					}
				}
			}
		}
	}
	

}
