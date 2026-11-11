package techguns.power.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Connector logic. A connector works one way at a time: while it is feeding its block it takes nothing from it, and
 * while it takes energy from its block it is not fed, so energy never runs back and forth between two connectors on
 * the same cable network. The direction may change after a second without traffic.
 */
public final class ConnectorEntity extends BlockEntity {
    private static final int HOLD=20;
    private long lastIn=-1000,lastOut=-1000;
    private LazyOptional<IEnergyStorage> input;
    public ConnectorEntity(BlockPos pos,BlockState state){super(WireBlocks.CONNECTOR_ENTITY.get(),pos,state);}
    private Direction back(){return getBlockState().getValue(ConnectorBlock.FACING).getOpposite();}
    private long now(){return level==null?0:level.getGameTime();}

    /** What the block behind gives to the connector. */
    private final IEnergyStorage in=new IEnergyStorage(){
        @Override public int receiveEnergy(int max,boolean simulate){
            if(!(level instanceof ServerLevel server)||now()-lastOut<HOLD)return 0;
            int sent=WireNetwork.get(server).route(server,new WireNode(worldPosition,0),max,simulate,-1);
            if(sent>0&&!simulate)lastIn=now();
            return sent;
        }
        @Override public int extractEnergy(int max,boolean simulate){return 0;}
        @Override public int getEnergyStored(){return 0;}
        @Override public int getMaxEnergyStored(){return 0;}
        @Override public boolean canExtract(){return false;}
        @Override public boolean canReceive(){return true;}
    };
    /** Energy arriving over the wires, handed to the block behind. */
    private final IEnergyStorage out=new IEnergyStorage(){
        @Override public int receiveEnergy(int max,boolean simulate){
            if(level==null||now()-lastIn<HOLD)return 0;
            var behind=level.getBlockEntity(worldPosition.relative(back()));if(behind==null)return 0;
            var target=behind.getCapability(ForgeCapabilities.ENERGY,back().getOpposite()).resolve();
            if(target.isEmpty()||!target.get().canReceive())return 0;
            int got=target.get().receiveEnergy(max,simulate);if(got>0&&!simulate)lastOut=now();return got;
        }
        @Override public int extractEnergy(int max,boolean simulate){return 0;}
        @Override public int getEnergyStored(){return 0;}
        @Override public int getMaxEnergyStored(){return 0;}
        @Override public boolean canExtract(){return false;}
        @Override public boolean canReceive(){return level!=null&&now()-lastIn>=HOLD;}
    };
    IEnergyStorage output(){return out;}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,Direction side){
        if(cap==ForgeCapabilities.ENERGY&&(side==null||side==back())){if(input==null)input=LazyOptional.of(()->in);return input.cast();}
        return super.getCapability(cap,side);
    }
    @Override public void invalidateCaps(){super.invalidateCaps();if(input!=null){input.invalidate();input=null;}}
    @Override public void setBlockState(BlockState state){super.setBlockState(state);if(input!=null){input.invalidate();input=null;}}
}
