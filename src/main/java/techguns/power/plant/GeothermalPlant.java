package techguns.power.plant;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Geothermal power plant: 3x3 casing, two layers high, the controller in the middle of the front of the lower layer.
 * Every lava source in the 5x5x3 space under it gives PER_LAVA FE per tick.
 */
public final class GeothermalPlant extends PlantController {
    public static final int PER_LAVA=15,CAPACITY=200_000,OUTPUT=2000;
    public int lava;
    public GeothermalPlant(BlockPos pos,BlockState state){super(PlantBlocks.GEOTHERMAL_ENTITY.get(),PlantKind.GEOTHERMAL,pos,state,CAPACITY,OUTPUT);}
    @Override protected Map<BlockPos,Need> structure(){
        var map=map();var casing=PlantBlocks.CASING.get();
        for(int y=0;y<=1;y++)for(int r=-1;r<=1;r++)for(int b=0;b<=2;b++)if(!(y==0&&r==0&&b==0))map.put(at(r,y,b),part(casing));
        return map;
    }
    @Override protected void onFormed(){survey();}
    private void survey(){
        lava=0;
        for(int y=-3;y<=-1;y++)for(int r=-2;r<=2;r++)for(int b=-1;b<=3;b++){var f=level.getFluidState(at(r,y,b));if(f.is(FluidTags.LAVA)&&f.isSource())lava++;}
        rate=lava*PER_LAVA;problem=lava==0?1:0;
    }
    @Override protected void work(){
        if(level.getGameTime()%40==0)survey();
        if(energy.getEnergyStored()<energy.getMaxEnergyStored())produce(rate);
    }
    @Override public void effects(RandomSource random){
        if(level==null||random.nextInt(3)!=0)return;var top=at(0,2,1);
        level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,top.getX()+0.5+random.nextGaussian()*0.2,top.getY()+1.8,top.getZ()+0.5+random.nextGaussian()*0.2,0,0.07,0);
    }
    @Override public int value(int i){return i==3?lava:0;}
    @Override protected void saveAdditional(net.minecraft.nbt.CompoundTag tag){super.saveAdditional(tag);tag.putInt("Lava",lava);}
    @Override public void load(net.minecraft.nbt.CompoundTag tag){super.load(tag);lava=tag.getInt("Lava");}
}
