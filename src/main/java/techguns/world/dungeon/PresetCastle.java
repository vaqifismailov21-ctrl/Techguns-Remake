package techguns.world.dungeon;
import techguns.world.dungeon.TemplateSegment.SegmentType;
public final class PresetCastle implements IDungeonPreset {
    public DungeonSegment getSegment(SegmentType type,int y,int yMin,int yMax,boolean above,boolean below,int seed){
        String name=y==-1?"ncroof1":y==yMin?"ncdung1":y<=yMin+1?"nclower1":y<yMax-2?"ncmid1":y<yMax-1?"ncupper1":"nctop1";
        // Original nextInt(1) selects the only template with a local Random(seed), not the world RNG.
        return DungeonTemplate.load(name).segments.get(type);
    }
    public int getSizeXZ(){return DungeonTemplate.load("ncmid1").sizeXZ;}
    public int getSizeY(){return DungeonTemplate.load("ncmid1").sizeY;}
    public void initDungeonPath(IDungeonPath raw){var path=(MazeDungeonPath)raw;path.startHeightLevel=1;path.chanceStraight=.8f;path.chanceRamp=.5f;path.chanceRoom=.25f;path.chanceFork=.4f;path.chanceUp=.65f;path.useFoundations=true;path.usePillars=true;path.useRoof=true;}
}
