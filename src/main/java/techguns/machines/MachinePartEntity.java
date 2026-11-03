package techguns.machines;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import techguns.capabilities.compat.*;
import techguns.capabilities.compat.LazyOptional;
import techguns.TGFabricator;
public final class MachinePartEntity extends BlockEntity implements techguns.capabilities.compat.ICapabilityHost {
    private BlockPos masterPosition;
    private final Map<Capability<?>,LazyOptional<?>> handles=new HashMap<>();
    public MachinePartEntity(BlockPos pos,BlockState state){super(TGFabricator.ENTITY.get(),pos,state);}
    public ProcessingMachine master(){return level!=null&&masterPosition!=null&&level.hasChunkAt(masterPosition)&&level.getBlockEntity(masterPosition) instanceof ProcessingMachine machine&&machine.formed?machine:null;}
    public boolean linked(){return masterPosition!=null;}
    public boolean linkedTo(BlockPos pos){return pos.equals(masterPosition);}
    public void validateLink(){if(masterPosition==null||level==null||!level.hasChunkAt(masterPosition))return;var master=master();if(master==null||!master.structureParts(master.getBlockState().getValue(MachineBlock.FACING)).containsKey(worldPosition))link(null);}
    public void link(BlockPos pos){clearHandles();masterPosition=pos==null?null:pos.immutable();if(level!=null&&getBlockState().getBlock() instanceof MachinePartBlock)level.setBlock(worldPosition,getBlockState().setValue(MachineBlock.FORMED,pos!=null),3);setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    private void clearHandles(){for(var handle:handles.values())handle.invalidate();handles.clear();}
    private boolean connector(ProcessingMachine master,Capability<?> capability){var part=(MachinePartBlock)getBlockState().getBlock();if(part.glass)return false;if(!part.reactor)return capability==Capabilities.ITEM_HANDLER||capability==Capabilities.ENERGY;var direction=master.getBlockState().getValue(MachineBlock.FACING);var center=master.getBlockPos().relative(direction);if(worldPosition.equals(center.above(3)))return capability==Capabilities.ENERGY;boolean sidePort=worldPosition.equals(center.relative(direction))||worldPosition.equals(center.relative(direction.getClockWise()))||worldPosition.equals(center.relative(direction.getCounterClockWise()));return sidePort&&(capability==Capabilities.ITEM_HANDLER||capability==Capabilities.FLUID_HANDLER);}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){var master=master();if(master!=null&&connector(master,capability)){
        var known=handles.get(capability);if(known!=null&&known.isPresent())return known.cast();handles.remove(capability);var source=master.getCapability(capability,side);if(!source.isPresent())return LazyOptional.empty();var handle=LazyOptional.of(()->source.orElseThrow(IllegalStateException::new));source.addListener(ignored->handle.invalidate());handles.put(capability,handle);return handle;
    }return LazyOptional.empty();}
    @Override public void invalidateCaps(){clearHandles();invalidateCapabilities();}
    @Override protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(tag, registries);if(masterPosition!=null)tag.putLong("master",masterPosition.asLong());}
    @Override protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(tag, registries);clearHandles();masterPosition=tag.contains("master")?BlockPos.of(tag.getLong("master")):null;}
    @Override public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries){return saveWithoutMetadata(techguns.util.TGItemNbt.registries());}@Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
