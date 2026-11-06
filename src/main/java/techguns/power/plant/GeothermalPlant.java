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
        if(level==null)return;
        // steam out of the cooling tower, puffs from the separator valve, heat over the intakes
        if(random.nextInt(2)==0){var top=point(-0.55+random.nextGaussian()*0.18,3.72,1.55+random.nextGaussian()*0.18);
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,top.x,top.y,top.z,random.nextGaussian()*0.004,0.06+random.nextDouble()*0.03,random.nextGaussian()*0.004);}
        if(random.nextInt(12)==0){var v=point(0.32,1.82,0.18);for(int i=0;i<4;i++)level.addParticle(ParticleTypes.CLOUD,v.x,v.y,v.z,random.nextGaussian()*0.02,0.05,random.nextGaussian()*0.02);}
        if(random.nextInt(6)==0){var v=point((random.nextBoolean()?1:-1)*(0.6+random.nextDouble()*0.65),0.5+random.nextDouble()*0.3,-0.25);level.addParticle(ParticleTypes.SMALL_FLAME,v.x,v.y,v.z,0,0.01,0);}
    }
    @Override public int value(int i){return i==3?lava:0;}
    @Override protected void saveAdditional(net.minecraft.nbt.CompoundTag tag){super.saveAdditional(tag);tag.putInt("Lava",lava);}
    @Override public void load(net.minecraft.nbt.CompoundTag tag){super.load(tag);lava=tag.getInt("Lava");}
}
