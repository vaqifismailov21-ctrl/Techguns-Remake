package techguns.oredrill;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import techguns.TGOreDrill;

/** Original OreDrillTileEntSlave/MultiBlockMachineTileEntSlave: master position and slave type (0 = unformed). */
public final class OreDrillSlaveEntity extends BlockEntity {
    private int masterX,masterY,masterZ;
    private boolean hasMaster;
    private byte type;
    public OreDrillSlaveEntity(BlockPos pos,BlockState state){super(TGOreDrill.SLAVE.get(),pos,state);}
    public boolean hasMaster(){return hasMaster;}
    public BlockPos getMasterPos(){return new BlockPos(masterX,masterY,masterZ);}
    public int getSlaveType(){return type;}
    public OreDrillTileEntMaster getMaster(){return hasMaster&&level!=null&&level.getBlockEntity(getMasterPos()) instanceof OreDrillTileEntMaster master?master:null;}
    public void form(BlockPos master,byte type){hasMaster=true;masterX=master.getX();masterY=master.getY();masterZ=master.getZ();this.type=type;}
    public void unform(){hasMaster=false;type=0;}
    /** Breaking any linked part breaks the whole multiblock. */
    public void onBlockBreak(){if(level!=null&&!level.isClientSide&&hasMaster){var master=getMaster();if(master!=null)master.onMultiBlockBreak();}}
    public void needUpdate(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    private void write(CompoundTag tag){tag.putBoolean("hasMaster",hasMaster);if(hasMaster){tag.putInt("masterX",masterX);tag.putInt("masterY",masterY);tag.putInt("masterZ",masterZ);tag.putByte("type",type);}}
    private void read(CompoundTag tag){hasMaster=tag.getBoolean("hasMaster");if(hasMaster){masterX=tag.getInt("masterX");masterY=tag.getInt("masterY");masterZ=tag.getInt("masterZ");type=tag.getByte("type");}else type=0;}
    @Override protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(tag, registries);write(tag);}
    @Override protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(tag, registries);read(tag);}
    @Override public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries){var tag=new CompoundTag();write(tag);return tag;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
