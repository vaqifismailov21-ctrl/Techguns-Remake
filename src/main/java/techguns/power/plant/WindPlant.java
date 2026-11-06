package techguns.power.plant;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wind power plant: controller on the ground, a tower of MIN_TOWER..MAX_TOWER sections on top of it, a casing
 * (the nacelle) on the tower and a rotor hub with all its blades in front of the nacelle. The rotor in front of the nacelle needs a free disc of radius ROTOR; output grows with
 * the height of the nacelle (full at Y 150), every block in the rotor disc takes 5 % off, rain and thunder add wind.
 */
public final class WindPlant extends PlantController {
    public static final int RATE=1000,CAPACITY=200_000,OUTPUT=2000,MIN_TOWER=8,MAX_TOWER=24,ROTOR=4;
    public int towers,obstacles;
    public WindPlant(BlockPos pos,BlockState state){super(PlantBlocks.WIND_ENTITY.get(),PlantKind.WIND,pos,state,CAPACITY,OUTPUT);}
    private int countTowers(){
        if(level==null)return 0;int n=0;var tower=PlantBlocks.TOWER.get();
        while(n<=MAX_TOWER&&level.getBlockState(at(0,n+1,0)).is(tower))n++;
        return n;
    }
    @Override protected Map<BlockPos,Need> structure(){
        var map=map();int count=countTowers();int n=Math.min(Math.max(count,MIN_TOWER),MAX_TOWER);
        for(int i=1;i<=n;i++)map.put(at(0,i,0),part(PlantBlocks.TOWER.get()));
        map.put(at(0,n+1,0),part(PlantBlocks.CASING.get()));
        var facing=facing();
        map.put(at(0,n+1,-1),new Need(s->s.is(PlantBlocks.HUB.get())&&s.getValue(RotorHubBlock.BLADES)==RotorHubBlock.MAX_BLADES&&s.getValue(RotorHubBlock.FACING)==facing,PlantBlocks.HUB.get()));
        return map;
    }
    @Override protected void onFormed(){towers=countTowers();survey();}
    private void survey(){
        if(level==null)return;
        var hub=at(0,towers+1,-1);var right=facing().getClockWise();obstacles=0;
        for(int dx=-ROTOR;dx<=ROTOR;dx++)for(int dy=-ROTOR;dy<=ROTOR;dy++){
            if(dx*dx+dy*dy>ROTOR*ROTOR||dx==0&&dy==0)continue;var p=hub.offset(right.getStepX()*dx,dy,right.getStepZ()*dx);
            if(!level.getBlockState(p).isAir())obstacles++;
        }
        double height=Math.max(0.15,Math.min(1,(hub.getY()-60)/90.0));
        double free=Math.max(0,1-obstacles*0.05);
        double weather=level.isThundering()?1.5:level.isRaining()?1.25:1;
        rate=(int)Math.round(Math.min(RATE*1.5,RATE*height*free*weather));
        problem=free<=0?2:height<0.5?1:obstacles>0?3:0;
    }
    @Override protected void work(){
        if(level.getGameTime()%40==0)survey();
        if(energy.getEnergyStored()<energy.getMaxEnergyStored())produce(rate);
    }
    @Override public int value(int i){return i==3?towers:i==2?obstacles:0;}
    @Override protected void saveAdditional(net.minecraft.nbt.CompoundTag tag){super.saveAdditional(tag);tag.putInt("Towers",towers);}
    @Override public void load(net.minecraft.nbt.CompoundTag tag){super.load(tag);towers=tag.getInt("Towers");}
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(){return new net.minecraft.world.phys.AABB(worldPosition).inflate(ROTOR+2,0,ROTOR+2).expandTowards(0,towers+ROTOR+3,0);}
}
