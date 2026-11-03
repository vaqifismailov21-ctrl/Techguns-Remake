package techguns.tileentities;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import techguns.*;
import techguns.blocks.MiningChargeBlock;
import techguns.machines.*;
public final class MiningChargeEntity extends BlockEntity implements MenuProvider {
    public final boolean advanced;public int diameter,length,fuse=100;public boolean armed;
    public final MachineSecurity access=new MachineSecurity(this::changed);
    public MiningChargeEntity(BlockPos pos,BlockState state){super(TGCharges.ENTITY.get(),pos,state);advanced=((MiningChargeBlock)state.getBlock()).advanced;diameter=advanced?5:3;length=advanced?10:3;}
    public void changed(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    public boolean button(Player player,int id){if(!access.allows(player)||armed)return false;
        switch(id){case 0->{armed=true;level.setBlock(worldPosition,getBlockState().setValue(MiningChargeBlock.ARMED,true),3);level.playSound(null,worldPosition,(advanced?TGSounds.C4_INIT:TGSounds.TNT_INIT).get(),SoundSource.BLOCKS,4,1);}case 1->diameter=Math.min(advanced?7:3,diameter+1);case 2->diameter=Math.max(3,diameter-1);case 3->length=Math.min(advanced?10:5,length+1);case 4->length=Math.max(2,length-1);case 5->fuse=Math.min(200,fuse+20);case 6->fuse=Math.max(60,fuse-20);case 7->{return access.toggle(player);}default->{return false;}}
        changed();return true;
    }
    public void tick(){if(!armed)return;fuse--;if(fuse<=0)detonate();else if(fuse%20==0)level.playSound(null,worldPosition,(advanced?TGSounds.C4_TICK:TGSounds.TNT_TICK).get(),SoundSource.BLOCKS,4,1);else if(fuse%5==0)changed();}
    public boolean breakable(float hardness){return hardness>=0&&(advanced?hardness<=TGConfig.explosiveChargeAdvancedMaxBlockHardness:hardness<TGConfig.explosiveChargeMaxBlockHardness);}
    public List<BlockPos> slice(){var direction=getBlockState().getValue(MiningChargeBlock.FACING);Direction a=direction.getAxis()==Direction.Axis.Z?Direction.EAST:Direction.NORTH;Direction b=direction.getAxis()==Direction.Axis.Y?Direction.EAST:Direction.UP;int radius=diameter/2;var result=new ArrayList<BlockPos>();for(int x=-radius;x<=radius;x++)for(int y=-radius;y<=radius;y++)result.add(worldPosition.relative(a,x).relative(b,y));return result;}
    public void detonate(){
        if(level==null||level.isClientSide)return;var direction=getBlockState().getValue(MiningChargeBlock.FACING);var origins=slice();var owner=access.ownerId()==null?null:level.getPlayerByUUID(access.ownerId());
        level.playSound(null,worldPosition,SoundEvents.GENERIC_EXPLODE.value(),SoundSource.BLOCKS,4,(1+(level.random.nextFloat()-level.random.nextFloat())*.2f)*.7f);level.removeBlock(worldPosition,false);
        // Source charges mine an oriented cuboid, with a cancelled or hard block stopping that individual line.
        // The original deliberately requires the placing player to be online for block destruction.
        if(owner==null)return;for(var origin:origins)mineLine(owner,origin,direction);
    }
    public void mineLine(Player owner,BlockPos origin,Direction direction){if(level==null||level.isClientSide)return;for(int distance=1;distance<=length;distance++){var pos=origin.relative(direction,distance);if(!level.hasChunkAt(pos)||!level.getWorldBorder().isWithinBounds(pos))break;var state=level.getBlockState(pos);if(!breakable(state.getDestroySpeed(level,pos)))break;
            var event=NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level,pos,state,owner));if(event.isCanceled())break;
            if(!state.isAir()){level.destroyBlock(pos,true,owner);if(level.random.nextFloat()>.5)TGProjectilePackets.effect(level,"MiningChargeBlockExplosion",Vec3.atCenterOf(pos),0,0);}
        }
    }
    @Override protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(tag, registries);tag.putInt("diameter",diameter);tag.putInt("length",length);tag.putInt("fuse",fuse);tag.putBoolean("armed",armed);access.save(tag);}
    @Override protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(tag, registries);diameter=net.minecraft.util.Mth.clamp(tag.getInt("diameter"),3,advanced?7:3);length=net.minecraft.util.Mth.clamp(tag.getInt("length"),2,advanced?10:5);armed=tag.getBoolean("armed");fuse=net.minecraft.util.Mth.clamp(tag.getInt("fuse"),armed?0:60,200);access.load(tag);}
    @Override public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries){return saveWithoutMetadata(techguns.util.TGItemNbt.registries());}@Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public Component getDisplayName(){return Component.translatable("block.techguns."+(advanced?"advanced_charge":"tnt_charge"));}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new ChargeMenu(id,inventory,this);}
}
