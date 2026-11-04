package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Floodlight: uses FLOODLIGHT_USE FE per tick and lights a corridor up to FLOODLIGHT_RANGE blocks in front of it. */
public final class FloodlightBlock extends FacingMachineBlock {
    private static final VoxelShape SHAPE=Block.box(3,0,3,13,14,13);
    public FloodlightBlock(){super(15);}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return SHAPE;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new FloodlightEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide?null:createTickerHelper(type,PowerBlocks.FLOODLIGHT_ENTITY.get(),(world,pos,block,entity)->entity.tick());}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!level.isClientSide&&level.getBlockEntity(pos) instanceof FloodlightEntity light)player.displayClientMessage(Component.translatable(state.getValue(LIT)?"techguns.power.floodlight_on":"techguns.power.floodlight_off",light.energyStored(),light.energyCapacity()),true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(!next.is(this)||next.getValue(FACING)!=state.getValue(FACING))FloodlightEntity.clearBeam(level,pos,state.getValue(FACING));
        super.onRemove(state,level,pos,next,moving);
    }
}
