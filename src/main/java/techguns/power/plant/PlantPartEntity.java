package techguns.power.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/** Remembers the controller of its plant (also on the client, so clicks with buckets reach it) and hands every capability on to it while the plant is formed. */
public class PlantPartEntity extends BlockEntity {
    private BlockPos master;
    public PlantPartEntity(BlockPos pos,BlockState state){this(PlantBlocks.PART_ENTITY.get(),pos,state);}
    protected PlantPartEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,BlockPos pos,BlockState state){super(type,pos,state);}
    public void setMaster(BlockPos pos){master=pos==null?null:pos.immutable();setChanged();}
    public BlockPos masterPos(){return master;}
    public PlantController master(){
        if(master==null||level==null||!getBlockState().getValue(PlantPartBlock.FORMED))return null;
        return level.getBlockEntity(master) instanceof PlantController c&&c.formed?c:null;
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side){
        var c=master();return c!=null?c.getCapability(capability,side):super.getCapability(capability,side);
    }
    @Override public CompoundTag getUpdateTag(){var tag=super.getUpdateTag();if(master!=null)tag.put("Master",NbtUtils.writeBlockPos(master));return tag;}
    @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);if(master!=null)tag.put("Master",NbtUtils.writeBlockPos(master));}
    @Override public void load(CompoundTag tag){super.load(tag);master=tag.contains("Master")?NbtUtils.readBlockPos(tag.getCompound("Master")):null;}
}
