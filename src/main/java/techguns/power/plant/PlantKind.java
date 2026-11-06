package techguns.power.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** The four power plants. */
public enum PlantKind {
    WIND(0),GEOTHERMAL(10),FISSION(12),FUSION(15);
    public final int light;
    PlantKind(int light){this.light=light;}
    public PlantController create(BlockPos pos,BlockState state){
        return switch(this){case WIND->new WindPlant(pos,state);case GEOTHERMAL->new GeothermalPlant(pos,state);case FISSION->new FissionReactor(pos,state);case FUSION->new FusionReactor(pos,state);};
    }
    public String id(){return switch(this){case WIND->"wind_plant";case GEOTHERMAL->"geothermal_plant";case FISSION->"fission_reactor";case FUSION->"fusion_reactor";};}
}
