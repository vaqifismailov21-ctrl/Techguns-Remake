package techguns.blocks;

import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** Original slimy ladder has no added hardness and cannot attach to power-providing blocks. */
public final class SlimyTrailBlock extends LadderBlock {
    public SlimyTrailBlock(){super(Properties.copy(Blocks.LADDER).strength(0,0).sound(SoundType.SLIME_BLOCK));registerDefaultState(defaultBlockState().setValue(FACING,Direction.SOUTH));}
    @Override public boolean canSurvive(BlockState state,LevelReader world,BlockPos pos){return super.canSurvive(state,world,pos)&&!world.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).isSignalSource();}
}
