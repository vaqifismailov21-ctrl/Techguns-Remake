package techguns.blocks;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import techguns.TGSounds;
/** Original iron bunker door: hand operation and matching double-door leaf. */
public final class BunkerDoorBlock extends DoorBlock {
    public BunkerDoorBlock(){super(Properties.copy(Blocks.IRON_DOOR).strength(8).pushReaction(PushReaction.BLOCK),BlockSetType.IRON);}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(level.isClientSide)return InteractionResult.SUCCESS;
        BlockPos bottom=state.getValue(HALF)==DoubleBlockHalf.UPPER?pos.below():pos;
        BlockState lower=level.getBlockState(bottom);if(!lower.is(this))return InteractionResult.PASS;
        boolean opened=lower.getValue(OPEN);Direction side=lower.getValue(HINGE)==DoorHingeSide.LEFT?lower.getValue(FACING).getClockWise():lower.getValue(FACING).getCounterClockWise();
        BlockPos adjacent=bottom.relative(side);BlockState other=level.getBlockState(adjacent);
        toggle(level,bottom,lower,!opened);
        // only a real double door opens together: same facing, mirrored hinge, its own partner side pointing back here
        if(other.is(this)&&other.getValue(HALF)==DoubleBlockHalf.LOWER&&other.getValue(FACING)==lower.getValue(FACING)&&other.getValue(HINGE)!=lower.getValue(HINGE)&&other.getValue(OPEN)==opened)toggle(level,adjacent,other,!opened);
        level.playSound(null,bottom,TGSounds.BUNKER_DOOR_OPEN.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,.9f+level.random.nextFloat()*.1f);
        level.gameEvent(player,!opened?net.minecraft.world.level.gameevent.GameEvent.BLOCK_OPEN:net.minecraft.world.level.gameevent.GameEvent.BLOCK_CLOSE,bottom);
        return InteractionResult.CONSUME;
    }
    /** Both halves are written, so the leaf never shows half open. */
    private void toggle(Level level,BlockPos bottom,BlockState lower,boolean open){
        var state=lower.setValue(OPEN,open);level.setBlock(bottom,state,10);
        var top=bottom.above();var upper=level.getBlockState(top);
        if(upper.is(this)&&upper.getValue(HALF)==DoubleBlockHalf.UPPER)level.setBlock(top,upper.setValue(OPEN,open),10);
    }
}
