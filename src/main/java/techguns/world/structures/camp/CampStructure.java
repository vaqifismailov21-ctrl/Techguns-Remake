package techguns.world.structures.camp;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

/** The source camp helpers use the original range/rotation contract. */
public abstract class CampStructure {
    public int minX,minY,minZ,maxX,maxY,maxZ;public boolean canSwapXZ;public CampLootTier lootTier=CampLootTier.TIER0;
    public CampStructure(int minX,int minY,int minZ,int maxX,int maxY,int maxZ){this.minX=minX;this.minY=minY;this.minZ=minZ;this.maxX=maxX;this.maxY=maxY;this.maxZ=maxZ;}
    public CampStructure setSwapXZ(boolean value){canSwapXZ=value;return this;}
    public static Direction directionToFacing(int direction){return switch(direction){case 0->Direction.WEST;case 2->Direction.EAST;case 3->Direction.SOUTH;default->Direction.NORTH;};}
    public abstract void setBlocks(CampWorld world,int posX,int posY,int posZ,int sizeX,int sizeY,int sizeZ,int direction,BiomeColorType color,RandomSource random);
    public enum BiomeColorType {WOODLAND,SNOW,DESERT,NETHER}
}
