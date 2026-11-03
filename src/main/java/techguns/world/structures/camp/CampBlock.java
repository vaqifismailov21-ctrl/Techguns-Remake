package techguns.world.structures.camp;
import net.minecraft.world.level.block.state.BlockState;
/** Source MBlock.getState is its stored first state; random crate choices are made by the source helper. */
public record CampBlock(BlockState state) {public BlockState getState(){return state;}}
