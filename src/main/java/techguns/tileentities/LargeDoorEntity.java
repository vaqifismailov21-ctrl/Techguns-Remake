package techguns.tileentities;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import techguns.*;
import techguns.blocks.LargeDoorBlock;
import techguns.blocks.LargeDoorBlock.Phase;
import techguns.machines.*;
/** Source timings expressed as saved game ticks, so pause/reload cannot desynchronise the nine blocks. */
public final class LargeDoorEntity extends BlockEntity implements MenuProvider {
    public int mode,redstone;public boolean autoClose=true;public long changedAt;
    public final MachineSecurity access=new MachineSecurity(this::changed);
    public LargeDoorEntity(BlockPos pos,BlockState state){super(TGDoors.ENTITY.get(),pos,state);}
    public void changed(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    public Phase phase(){return getBlockState().getValue(LargeDoorBlock.PHASE);}
    public int variant(){return ((LargeDoorBlock)getBlockState().getBlock()).variant;}
    public boolean zplane(){return getBlockState().getValue(LargeDoorBlock.ZPLANE);}
    public boolean toggle(boolean neighbours){
        Phase before=phase();if(before.moving())return false;
        transition(before==Phase.CLOSED?Phase.OPENING:Phase.CLOSING);
        if(neighbours&&(variant()==1||variant()==2)){
            // Original hangar groups: three neighbouring sections each side and one opposing row.
            for(int sign:new int[]{-1,1})for(int distance=3;distance<=9;distance+=3)if(!toggleMatching(side(worldPosition,sign*distance),before))break;
            BlockPos opposite=worldPosition.offset(0,variant()==1?-3:3,0);
            if(toggleMatching(opposite,before))for(int sign:new int[]{-1,1})for(int distance=3;distance<=9;distance+=3)if(!toggleMatching(side(opposite,sign*distance),before))break;
        }return true;
    }
    private BlockPos side(BlockPos pos,int amount){return zplane()?pos.offset(0,0,amount):pos.offset(amount,0,0);}
    private boolean toggleMatching(BlockPos pos,Phase previous){if(level.getBlockEntity(pos) instanceof LargeDoorEntity door&&door.zplane()==zplane()&&(door.variant()==1||door.variant()==2)&&door.phase()==previous)return door.toggle(false);return false;}
    private void transition(Phase phase){
        var block=getBlockState().getBlock();for(int c=0;c<3;c++)for(int r=0;r<3;r++){BlockPos pos=LargeDoorBlock.partPos(worldPosition,zplane(),c,r);BlockState state=level.getBlockState(pos);if(state.is(block)&&LargeDoorBlock.masterPos(pos,state).equals(worldPosition))level.setBlock(pos,state.setValue(LargeDoorBlock.PHASE,phase),2);}
        changedAt=level.getGameTime();changed();var sound=phase==Phase.OPENING?TGSounds.TECHDOOR_OPEN:phase==Phase.CLOSING?TGSounds.TECHDOOR_CLOSE:TGSounds.TECHDOOR_STATE_FINISHED;
        level.playSound(null,worldPosition,sound.get(),SoundSource.BLOCKS,.9f+level.random.nextFloat()*.2f,.9f+level.random.nextFloat()*.2f);
    }
    public void tick(){
        long elapsed=level.getGameTime()-changedAt;Phase phase=phase();if(phase.moving()){if(elapsed>=20)transition(phase==Phase.OPENING?Phase.OPENED:Phase.CLOSED);return;}
        if(level.getGameTime()%5!=0)return;
        if(mode==0){if(autoClose&&phase==Phase.OPENED&&elapsed>=100)toggle(true);}
        else if(mode==1&&redstone!=0){boolean powered=false;for(int c=0;c<3;c++)for(int r=0;r<3;r++)powered|=level.hasNeighborSignal(LargeDoorBlock.partPos(worldPosition,zplane(),c,r));boolean shouldOpen=redstone==1?powered:!powered;if(shouldOpen!=(phase==Phase.OPENED))toggle(true);}
        else if(mode==2){boolean nearby=level.getEntitiesOfClass(Player.class,net.minecraft.world.phys.AABB.unitCubeFromLowerCorner(net.minecraft.world.phys.Vec3.atLowerCornerOf(worldPosition)).inflate(2,1,2),p->!p.isSpectator()&&access.allows(p)).size()>0;if(nearby&&phase==Phase.CLOSED||!nearby&&phase==Phase.OPENED&&elapsed>=20)toggle(true);}
    }
    public float openness(float partial){Phase state=phase();if(state==Phase.OPENED)return 1;if(state==Phase.CLOSED)return 0;float t=net.minecraft.util.Mth.clamp((level.getGameTime()-changedAt+partial)/20f,0,1);return state==Phase.OPENING?t:1-t;}
    @Override protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(tag, registries);tag.putInt("mode",mode);tag.putInt("redstone",redstone);tag.putBoolean("autoClose",autoClose);tag.putLong("changedAt",changedAt);access.save(tag);}
    @Override protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(tag, registries);mode=Math.floorMod(tag.getInt("mode"),3);redstone=Math.floorMod(tag.getInt("redstone"),3);autoClose=!tag.contains("autoClose")||tag.getBoolean("autoClose");changedAt=tag.getLong("changedAt");access.load(tag);}
    @Override public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries){return saveWithoutMetadata(techguns.util.TGItemNbt.registries());}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    public AABB getRenderBoundingBox(){return net.minecraft.world.phys.AABB.unitCubeFromLowerCorner(net.minecraft.world.phys.Vec3.atLowerCornerOf(worldPosition)).inflate(4);}
    @Override public Component getDisplayName(){return Component.translatable("block.techguns."+new String[]{"metal_door_3x3","hangar_door_up","hangar_door_down","nether_door_3x3"}[variant()]);}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new DoorMenu(id,inventory,this);}
}
