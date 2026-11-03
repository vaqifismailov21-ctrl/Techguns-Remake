package techguns.world.dungeon;
import java.util.*;import net.minecraft.core.*;import net.minecraft.util.RandomSource;import net.minecraft.world.level.block.state.BlockState;import techguns.TGWorld;import techguns.world.dungeon.TemplateSegment.SegmentType;import techguns.world.dungeon.MazeDungeonPath.PathSegment;


public interface IDungeonPath {

	public int getNumSegments();

	public void generatePath();

	public void generateSegment(int x, int y, int z, int dir, PathSegment prev);

	public void generateDungeon(DungeonWorld world, int posX, int posY, int posZ, IDungeonPreset preset);

	public void generateNPCSpawners(DungeonWorld world, int posX, int posY, int posZ, IDungeonPreset preset);
	
	public Direction getEntranceRotation();
	
	public BlockPos getStartPos();
}