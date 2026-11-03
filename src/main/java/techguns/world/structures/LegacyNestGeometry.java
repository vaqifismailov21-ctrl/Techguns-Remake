package techguns.world.structures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
/** Exact sphere, cylinder and line loops extracted from original BlockUtils. */
public final class LegacyNestGeometry {
public static void fillSphere2(LegacyStructurePlan world, int posX, int posY, int posZ, float radius, float thickness, LegacyNestBlock block1, LegacyNestBlock block2) {
		Vec3 center = new Vec3(posX, posY, posZ);
		
		float rsquared = (float) Math.pow(radius, 2.0f);
		float rsquared2 = (float) Math.pow(radius-thickness, 2.0f);
		
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		
		int r = ((int)(radius));
		for(int i = -r; i <= r; i++) {
			for(int j = -r; j <= r; j++) {
				for(int k = -r; k <= r; k++) {
					int x = posX + i;
					int y = posY + j;
					int z = posZ + k;
					Vec3 v = new Vec3(x, y, z);
					float dsquared = (float) center.distanceToSqr(v);
					if (dsquared < rsquared2) {
						block1.setBlock(world, p.set(x, y, z), 0);
					}else if (dsquared < rsquared) {
						block2.setBlock(world, p.set(x, y, z), 0);
					}
				}
			}
		}	
	}
public static void fillCylinder(LegacyStructurePlan world, int x1, int y1, int z1, int x2, int y2, int z2, float radius, LegacyNestBlock block1, LegacyNestBlock block2) {
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
					Vec3 v1v0 = v1.subtract(v0);
					Vec3 v_ = v2v1.cross(v1v0);
					double distance = v_.length()/l;
					
					if (distance < radius-1.0f) {
						block1.setBlock(world, p.set(x, y, z), 0);
					}else if (distance < radius) {
						block2.setBlock(world, p.set(x, y, z), 0);
					}
				}
			}
		}
		
	}
public static void drawLine(LegacyStructurePlan world, int x1, int y1, int z1, int x2, int y2, int z2, LegacyNestBlock block) {

		 BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		 
        int dx = x2 - x1;
        int dy = y2 - y1;
        int dz = z2 - z1;

        int ax = Math.abs(dx) << 1;
        int ay = Math.abs(dy) << 1;
        int az = Math.abs(dz) << 1;

        int signx = (int) Math.signum(dx);
        int signy = (int) Math.signum(dy);
        int signz = (int) Math.signum(dz);

        int x = x1;
        int y = y1;
        int z = z1;

        int deltax, deltay, deltaz;
        if (ax >= Math.max(ay, az)) /* x dominant */ {
            deltay = ay - (ax >> 1);
            deltaz = az - (ax >> 1);
            while (true) {
                block.setBlock(world, p.set(x, y, z), 0);
                if (x == x2) {
                    return;
                }


                if (deltay >= 0) {
                    y += signy;
                    deltay -= ax;
                }

                if (deltaz >= 0) {
                    z += signz;
                    deltaz -= ax;
                }

                x += signx;
                deltay += ay;
                deltaz += az;
            }
        } else if (ay >= Math.max(ax, az)) /* y dominant */ {
            deltax = ax - (ay >> 1);
            deltaz = az - (ay >> 1);
            while (true) {
            	block.setBlock(world, p.set(x, y, z), 0);
                if (y == y2) {
                    return;
                }
                
//	                //Keep 4-way neighborhood when going up/down
//	                if (deltax >= 0 && deltaz >= 0) {
//	                	block.setBlock(world, x+signx, y, z+signz, 0);
//	                }

                if (deltax >= 0) {
                	block.setBlock(world, p.set(x+signx, y, z), 0);
                    x += signx;
                    deltax -= ay;
                }

                if (deltaz >= 0) {
                	block.setBlock(world, p.set(x, y, z+signz), 0);
                    z += signz;
                    deltaz -= ay;
                }

                y += signy;
                deltax += ax;
                deltaz += az;
            }
        } else if (az >= Math.max(ax, ay)) /* z dominant */ {
            deltax = ax - (az >> 1);
            deltay = ay - (az >> 1);
            while (true) {
            	block.setBlock(world, p.set(x, y, z), 0);
                if (z == z2) {
                    return;
                }

                if (deltax >= 0) {
                    x += signx;
                    deltax -= az;
                }

                if (deltay >= 0) {
                    y += signy;
                    deltay -= az;
                }

                z += signz;
                deltax += ax;
                deltay += ay;
            }
        }
	}
public static void fillSphere(LegacyStructurePlan world, int posX, int posY, int posZ, float radius, LegacyNestBlock block) {
		Vec3 center = new Vec3(posX, posY, posZ);
		
		float rsquared = (float) Math.pow(radius, 2.0f);
		
		BlockPos.MutableBlockPos p=new BlockPos.MutableBlockPos();
		
		int r = ((int)(radius));
		for(int i = -r; i <= r; i++) {
			for(int j = -r; j <= r; j++) {
				for(int k = -r; k <= r; k++) {
					int x = posX + i;
					int y = posY + j;
					int z = posZ + k;
					Vec3 v = new Vec3(x, y, z);
					float dsquared = (float) center.distanceToSqr(v);
					if (dsquared < rsquared) {
						block.setBlock(world, p.set(x, y, z), 0);
					}
				}
			}
		}	
	}
public static final class Vec2 { public final double x,y; public Vec2(double x,double y){this.x=x;this.y=y;} }
public static Vec2 polarOffsetXZ(double x,double z,double radius,double angle){return new Vec2(x+radius*Math.cos(angle),z+radius*Math.sin(angle));}
}
