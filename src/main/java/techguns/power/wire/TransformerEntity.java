package techguns.power.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.IEnergyStorage;
import techguns.power.EnergySource;

/** Store of the transformer; every tick it sends energy out over both of its bushings and to the block in front. */
public final class TransformerEntity extends EnergySource {
    public TransformerEntity(BlockPos pos,BlockState state){super(WireBlocks.TRANSFORMER_ENTITY.get(),pos,state,TransformerBlock.CAPACITY,TransformerBlock.RATE);}
    private Direction front(){return getBlockState().getValue(TransformerBlock.FACING);}
    @Override protected boolean gives(Direction side){return side==null||side==front();}
    @Override protected boolean takes(Direction side){return side==null||side!=front()&&side!=Direction.UP;}
    @Override protected int inputLimit(){return TransformerBlock.RATE;}
    public float fill(){return energy.getEnergyStored()/(float)energy.getMaxEnergyStored();}
    private final IEnergyStorage wireIn=new IEnergyStorage(){
        @Override public int receiveEnergy(int max,boolean simulate){int got=energy.receiveEnergy(Math.min(max,TransformerBlock.RATE),simulate);if(got>0&&!simulate)setChanged();return got;}
        @Override public int extractEnergy(int max,boolean simulate){return 0;}
        @Override public int getEnergyStored(){return energy.getEnergyStored();}
        @Override public int getMaxEnergyStored(){return energy.getMaxEnergyStored();}
        @Override public boolean canExtract(){return false;}
        @Override public boolean canReceive(){return energy.getEnergyStored()<energy.getMaxEnergyStored();}
    };
    IEnergyStorage wireInput(){return wireIn;}
    public void tick(){
        if(!(level instanceof ServerLevel server))return;
        distribute();
        var net=WireNetwork.get(server);
        for(int point=0;point<2;point++){
            int stored=energy.getEnergyStored();if(stored<=0)break;
            int sent=net.route(server,new WireNode(worldPosition,point),Math.min(stored,TransformerBlock.RATE),false,fill());
            if(sent>0){energy.extractEnergy(sent,false);setChanged();}
        }
    }
}
