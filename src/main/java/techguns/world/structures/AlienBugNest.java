package techguns.world.structures;
import java.util.ArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.core.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.*;
import techguns.TGWorld;
/** Generated from original AlienBugNest; geometry and original ladder cross-product behavior retained. */
public class AlienBugNest {

	static int MAX_RADIUS = 10;
	static int MIN_RADIUS = 3;
	static int MAX_SEGMENTS;
	
	static LegacyNestBlock wallBlock;
	static LegacyNestBlock wallBlock2;
	static LegacyNestBlock innerBlock;
	//static LegacyNestBlock slimeLadder;
	static LegacyNestBlock slimyBlock;
	static LegacyNestBlock spawner;
	static LegacyNestBlock eggsBlock;
	
	static {
        wallBlock = LegacyNestBlock.weighted(new Block[]{TGWorld.HARD_SAND.get(),TGWorld.SLIMY_EGGS.get(),Blocks.SANDSTONE},new int[]{16,1,2});
        wallBlock2 = LegacyNestBlock.weighted(new Block[]{TGWorld.HARD_SAND.get(),Blocks.SANDSTONE},new int[]{12,1});
        innerBlock = LegacyNestBlock.AIR;
        slimyBlock = new MBlockSlime(TGWorld.HARD_SAND.get(),0);
        spawner = LegacyNestBlock.spawner();
        eggsBlock = new LegacyNestBlock(TGWorld.SLIMY_EGGS.get(),0);
    }

	public BugNestSegment mainSegment;
	
	public AlienBugNest(int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, RandomSource rand) {
		
		int segments = 5; //Math.min(Math.max(1, (sizeX*sizeY*sizeZ) / 1000), MAX_SEGMENTS);
		
		boolean largeNest = true;
		
		int startRadius = Math.min(MAX_RADIUS, MIN_RADIUS+(segments/2));
		
		int x = posX + (sizeX/2);
		int y = posY + (sizeY/2);
		int z = posZ + (sizeZ/2);
		
		BugNestSegment entrance = new BugNestSegment(x,y,z, 2, 2);		
		int mainY = y-(startRadius+ 5 +rand.nextInt(7));
		mainSegment = new BugNestSegment(x+rand.nextInt(3),mainY,z+rand.nextInt(3),startRadius, 0);
		mainSegment.neighbors.add(entrance);
		
		float angle = (float) ((Math.PI*2.0) / segments);
		BugNestSegment prev = mainSegment;
		for (int i = 0; i < segments; i++) {
			int radius = Math.max(MIN_RADIUS, startRadius - rand.nextInt(i+1));
			double dist = startRadius + radius + 5.0 + rand.nextDouble()*8.0;
			LegacyNestGeometry.Vec2 xz = LegacyNestGeometry.polarOffsetXZ(x, z, dist, angle*i);
			int segY = mainY + rand.nextInt(startRadius*3)-startRadius*2;
			BugNestSegment seg = new BugNestSegment((int)xz.x, segY, (int)xz.y, radius, 1);
			
			if (prev == mainSegment)  {
				mainSegment.neighbors.add(seg);
			}else {
				if (rand.nextBoolean()) {
					prev.neighbors.add(seg);
					if (rand.nextBoolean()) {
						mainSegment.neighbors.add(seg);
					}
				}else {
					mainSegment.neighbors.add(seg);
				}
			}
			//Add Additional Segment
			if (largeNest && rand.nextBoolean()) {		
				radius = Math.max(MIN_RADIUS, startRadius - rand.nextInt(i+1));
				dist = startRadius + radius + 5.0 + rand.nextDouble()*8.0;
				xz = LegacyNestGeometry.polarOffsetXZ(xz.x, xz.y, dist, (angle*i)+(Math.PI*0.5*(rand.nextDouble()-0.5)));
				segY = mainY + rand.nextInt(startRadius*3)-startRadius*2;
				BugNestSegment seg2 = new BugNestSegment((int)xz.x, segY, (int)xz.y, radius, 1);
				seg.neighbors.add(seg2);
			}
			
			
			prev = seg;
		}		
	}

	
	public void setBlocks(LegacyStructurePlan world) {
		placeSegment(world, mainSegment);
		placeConnections(world, mainSegment);
		placeInterior(world, mainSegment);
	}
	
	private void placeSegment(LegacyStructurePlan world, BugNestSegment segment) {
		if (segment.status >= 1)
			return;
		
		if (segment.type != 2) { //Not entrance
		
			LegacyNestGeometry.fillSphere2(world, segment.posX, segment.posY, segment.posZ, segment.radius, 1.0f, LegacyNestBlock.AIR, wallBlock);
			segment.status = 1;
			
			for (BugNestSegment seg : segment.neighbors) {
				placeSegment(world,seg);
				//System.out.println("Placing Segment:"+seg.toString());
			}
		}
	}
	
	private void placeConnections(LegacyStructurePlan world, BugNestSegment segment) {
		if (segment.status >= 2)
			return;
		segment.status = 2;
		
		for (BugNestSegment seg : segment.neighbors) {
			//Get proper distance
			Vec3 pos1 = new Vec3(segment.posX, segment.posY, segment.posZ);
			Vec3 pos2 = new Vec3(seg.posX, seg.posY, seg.posZ);
			Vec3 v2v1 = pos2.subtract(pos1);
			double distance = v2v1.length();
			Vec3 dir = v2v1.normalize();

			double d2 = distance-(seg.radius);
			
			if (seg.type == 2) d2 += 5.0;
			
			int x1 = (int) (segment.posX + dir.x*segment.radius);
			int y1 = (int) (segment.posY + dir.y*segment.radius);
			int z1 = (int) (segment.posZ + dir.z*segment.radius);
			//
			int x2 = (int) (segment.posX + dir.x*d2);
			int y2 = (int) (segment.posY + dir.y*d2);
			int z2 = (int) (segment.posZ + dir.z*d2);
			
			int r = Math.min(segment.radius, Math.min(2+world.random.nextInt(2), seg.radius));
			LegacyNestGeometry.fillCylinder(world, x1, y1, z1, x2, y2, z2, r, LegacyNestBlock.AIR, wallBlock2);
			
			if (seg.type == 2) {
				createSlimeLadder(world, x1, y1, z1, x2, y2, z2, r-1);
			}
			
			//LegacyNestGeometry.fillSphere(world, x1, y1, z1, r-1, new LegacyNestBlock(Blocks.air, 0));
			//LegacyNestGeometry.fillSphere(world, x2, y2, z2, r-1, new LegacyNestBlock(Blocks.air, 0));
			
			placeConnections(world, seg);
		}
	}
	
	private void placeInterior(LegacyStructurePlan world, BugNestSegment segment) {
		if (segment.status >= 3)
			return;
		
		if (segment.type != 2) { //Not entrance
		
			LegacyNestGeometry.fillSphere(world, segment.posX, segment.posY, segment.posZ, segment.radius-1, LegacyNestBlock.AIR);//, innerBlock );
			segment.status = 3;
		
			if (segment.type == 1) { //Regular segment
				placeEggs(world, segment);
			}
			
			
			for (BugNestSegment seg : segment.neighbors) {
				if (segment.type == 0 && seg.type == 2) { //main segment to Entrance
					int k = 1;//+world.random.nextInt(2);
					double angle = world.random.nextDouble()*2.0*Math.PI;
					for (int i = 0; i < k; i++) {
						
						//Get proper distance
						LegacyNestGeometry.Vec2 xz = LegacyNestGeometry.polarOffsetXZ(0, 0, 1.0 + (world.random.nextDouble()*0.66), angle+((Math.PI*2.0)/k)*i);
						int xo = (int) xz.x;
						int zo = (int) xz.y;
						Vec3 pos1 = new Vec3(segment.posX, segment.posY, segment.posZ);
						Vec3 pos2 = new Vec3(seg.posX, seg.posY, seg.posZ);
						Vec3 v2v1 = pos2.subtract(pos1);
						double distance = v2v1.length();
						Vec3 dir = v2v1.normalize();
	
						//double d2 =(segment.radius*2)+(distance-(seg.radius))-4.0;
						double d2 =((segment.radius-1)*2);
						
						
						int x1 = (int) (segment.posX + xo + dir.x*-(segment.radius+1));
						int y1 = (int) (segment.posY + dir.y*-(segment.radius+1));
						int z1 = (int) (segment.posZ + zo + dir.z*-(segment.radius+1));
						//
						int x2 = (int) (segment.posX + xo + dir.x*d2);
						int y2 = (int) (segment.posY + dir.y*d2);
						int z2 = (int) (segment.posZ + zo + dir.z*d2);
						
						LegacyNestGeometry.drawLine(world, x1, y1, z1, x2, y2, z2, slimyBlock);
						//LegacyNestGeometry.fillCylinder(world, x1, y1, z1, x2, y2, z2, 1.0f, new LegacyNestBlock(Blocks.dirt, 0), new LegacyNestBlock(Blocks.dirt, 0));
						
					}
				}
				
				
				placeInterior(world,seg);
			}
		}
	}


//	private void randomOffset(Vec3 pos, int offset, RandomSource rand) {
//		pos.xCoord+= rand.nextInt(offset);
//		pos.yCoord+= rand.nextInt(offset);
//		pos.zCoord+= rand.nextInt(offset);
//	}
	
	public void placeEggs(LegacyStructurePlan world, BugNestSegment segment) {
		int r = segment.radius-2;
		
		//roll spawner pos
		int ri=r;
		if (r>1){
			ri--;
		}
		int spawnerX = world.random.nextInt(2*ri)-ri;
		int spawnerZ = world.random.nextInt(2*ri)-ri;
		
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		
		for (int i = -r; i < r; i++) {
			for (int j = -r; j < r; j++) {

				
				if ( i==spawnerX && j==spawnerZ){
					int x = segment.posX+i;
					int z = segment.posZ+j;
					int y = segment.posY-(segment.radius-2);
					spawner.setBlock(world, pos.set(x, y, z), 0);
				} else {
					float f1 = (float)(r-Math.abs(i))/(float)(r);
					float f2 = (float)(r-Math.abs(j))/(float)(r);
				//	System.out.println("f1:"+f1+" f2:"+f2);
					if (world.random.nextDouble() < ((f1+f2)/2.0f)) {
						int x = segment.posX+i;
						int z = segment.posZ+j;
						for (int k = 0; k < segment.radius; k++) {
							int y = segment.posY-(segment.radius-1)+k;
							if (world.isAirBlock(pos.set(x, y, z))) {
								eggsBlock.setBlock(world, pos, 0);
							}
							if (world.random.nextDouble() > (f1+f2)/3.0f) {
								break;
							}
						}
					}
				}
			}
		}
	}
	
	
	public static void createSlimeLadder(LegacyStructurePlan world, int x1, int y1, int z1, int x2, int y2, int z2, float radius) {
		Vec3 v1 = new Vec3(x1, y1, z1);
		Vec3 v2 = new Vec3(x2, y2, z2);
		Vec3 v2v1 = v1.subtract(v2);
		double l = v2v1.length();
		//Vec3 dir = v2v1.normalize();
		int tmp = 0;
		if (x1 > x2) { tmp = x2; x2 = x1; x1 = tmp;}
		if (y1 > y2) {tmp = y2; y2 = y1; y1 = tmp;}
		if (z1 > z2) {tmp = z2; z2 = z1; z1 = tmp;}
		
		int r = (int)radius;
		
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int x = x1-r; x < x2+r; x++) {
			for (int y = y1-r; y < y2+r; y++) {
				for (int z = z1-r; z < z2+r; z++) {
					Vec3 v0 = new Vec3(x, y, z);
					Vec3 v1v0 = v1.subtract(v2);
					Vec3 v_ = v2v1.cross(v1v0);
					double distance = v_.length()/l;
					p.set(x,y,z);
					if (distance < radius) world.placeTrail(p,false);
				}
			}
		}
		
	}


	class BugNestSegment {
		int posX;
		int posY;
		int posZ;
		int radius;
		byte type; //0 = main segment, 1 = regular, 2 = entrance
		
		int status = 0; //0 = none, 1 = spheres, 2 = connections.
		
		ArrayList<BugNestSegment> neighbors = new ArrayList<BugNestSegment>(); //one-directional list of connected segments;
		
		public BugNestSegment(int posX, int posY, int posZ, int radius, int type) {
			super();
			this.posX = posX;
			this.posY = posY;
			this.posZ = posZ;
			this.radius = radius;
			this.type = (byte) type;
		}
	
	}
	
	static class MBlockSlime extends LegacyNestBlock {

		public MBlockSlime(Block block, int meta) {
			super(block, meta);
		}
		
		
		@Override
		public void setBlock(LegacyStructurePlan w, BlockPos pos, int rotation) {
			super.setBlock(w, pos, rotation);
			placeSlimeAt(w, pos.mutable().move(Direction.NORTH));
			placeSlimeAt(w, pos.mutable().move(Direction.EAST));
			placeSlimeAt(w, pos.mutable().move(Direction.SOUTH));
			placeSlimeAt(w, pos.mutable().move(Direction.WEST));
		}


		private void placeSlimeAt(LegacyStructurePlan world, BlockPos.MutableBlockPos pos) {
			world.placeTrail(pos,true);
		}

	}
}