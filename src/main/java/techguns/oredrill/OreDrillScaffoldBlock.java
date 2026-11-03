package techguns.oredrill;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Scaffold with the original SCAFFOLD_HIDDEN variant: the formed last scaffold row is invisible (rendered by the drill). */
public final class OreDrillScaffoldBlock extends OreDrillBlock {
    public static final BooleanProperty HIDDEN=BooleanProperty.create("hidden");
    public OreDrillScaffoldBlock(){super(OreDrillType.SCAFFOLD);registerDefaultState(defaultBlockState().setValue(HIDDEN,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){super.createBlockStateDefinition(builder);builder.add(HIDDEN);}
    @Override public RenderShape getRenderShape(BlockState state){return state.getValue(HIDDEN)&&state.getValue(FORMED)?RenderShape.INVISIBLE:RenderShape.MODEL;}
    public static boolean isHidden(BlockState state){return state.getBlock() instanceof OreDrillScaffoldBlock&&state.getValue(HIDDEN);}
}
