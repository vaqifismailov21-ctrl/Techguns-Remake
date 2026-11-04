package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

/** Nuclear generator: burns Techguns nuclear micro reactors, needs water next to it for cooling. */
public final class NuclearGeneratorBlock extends FacingMachineBlock {
    public NuclearGeneratorBlock(){super(12);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new NuclearGeneratorEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return PowerBlocks.ticker(level,type,PowerBlocks.NUCLEAR_GENERATOR_ENTITY.get(),NuclearGeneratorEntity::tick);}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!level.isClientSide&&player instanceof ServerPlayer server&&level.getBlockEntity(pos) instanceof NuclearGeneratorEntity generator)NetworkHooks.openScreen(server,new GeneratorMenu.Provider(generator),buf->buf.writeBlockPos(pos));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(state.getBlock()!=next.getBlock()&&level.getBlockEntity(pos) instanceof NuclearGeneratorEntity generator)
            for(int i=0;i<generator.slots.getSlots();i++)Containers.dropItemStack(level,pos.getX(),pos.getY(),pos.getZ(),generator.slots.getStackInSlot(i));
        super.onRemove(state,level,pos,next,moving);
    }
}
