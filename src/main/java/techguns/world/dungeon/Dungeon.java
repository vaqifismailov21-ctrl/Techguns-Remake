package techguns.world.dungeon;
import java.util.*;import net.minecraft.core.*;import net.minecraft.util.RandomSource;import net.minecraft.world.level.block.state.BlockState;import techguns.TGWorld;import techguns.world.dungeon.TemplateSegment.SegmentType;import techguns.world.dungeon.MazeDungeonPath.PathSegment;


public class Dungeon {
	
	private static final int GENERATE_ATTEMPTS = 5;
public IDungeonPath selectedPath;public final List<Integer> attemptSizes=new ArrayList<>();
	
	IDungeonPreset preset;
	
	
	int maxSizeX;
	int maxSizeY;
	int maxSizeZ;
	
	
	int sX;
	int sY;
	int sZ;

	public Direction startRotation;
	public BlockPos startPos;
	
	public Dungeon(IDungeonPreset preset) { 
		this.preset = preset;
	}
	
	public void generate(DungeonWorld world, int posX, int posY, int posZ, int maxSizeX, int maxSizeY, int maxSizeZ) {
		
		this.maxSizeX = maxSizeX;
		this.maxSizeY = maxSizeY;
		this.maxSizeZ = maxSizeZ;
		
		this.sX = maxSizeX / preset.getSizeXZ();
		this.sY = maxSizeY / preset.getSizeY();
		this.sZ = maxSizeZ / preset.getSizeXZ();
		
		
		
	
		
		IDungeonPath path = null;
		for (int i = 0; i < GENERATE_ATTEMPTS; i++) {			
			IDungeonPath p = preset.getDungeonPath(sX, sY, sZ, world.rand);
			preset.initDungeonPath(p);
			p.generatePath();attemptSizes.add(p.getNumSegments());
			if (path == null || p.getNumSegments() > path.getNumSegments()) {
				path = p;
			}
		}
	
		
		
		selectedPath=path;
		path.generateDungeon(world, posX, posY, posZ, preset);
	
		
		path.generateNPCSpawners(world, posX, posY, posZ, preset);
	
		
		world.resolveLegacyShapes();
		this.startPos = path.getStartPos();
		this.startRotation = path.getEntranceRotation();
	
	
		
	}

}
