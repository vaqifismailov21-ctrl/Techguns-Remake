package techguns.world.dungeon;
import net.minecraft.util.RandomSource;
import techguns.world.dungeon.TemplateSegment.SegmentType;
import techguns.world.dungeon.MazeDungeonPath.PathSegment;
public interface IDungeonPreset {
    IDungeonPreset PRESET_CASTLE=new PresetCastle();
    DungeonSegment getSegment(SegmentType type,int y,int yMin,int yMax,boolean above,boolean below,int seed);
    int getSizeXZ();int getSizeY();
    default float getSpawnDensity(){return .1f;}
    default int getSpawnYOffset(PathSegment segment){return 1;}
    default IDungeonPath getDungeonPath(int x,int y,int z,RandomSource random){return new MazeDungeonPath(x,y,z,random);}
    void initDungeonPath(IDungeonPath path);
    default void initSpawner(DungeonSpawner spawner){spawner.castleGuard();}
}
