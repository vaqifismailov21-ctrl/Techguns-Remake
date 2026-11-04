package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Invisible light of a floodlight beam. Behaves like air; removes itself when no lit floodlight points at it. */
public final class FloodlightLightBlock extends Block {
    public FloodlightLightBlock(){super(Properties.of().air().replaceable().noCollission().noLootTable().lightLevel(state->15).pushReaction(PushReaction.DESTROY));}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Shapes.empty();}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    @Override public void onPlace(BlockState state,Level level,BlockPos pos,BlockState old,boolean moving){if(!level.isClientSide)level.scheduleTick(pos,this,40);}
    @Override public void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){if(hasSource(level,pos))level.scheduleTick(pos,this,40);else level.removeBlock(pos,false);}
    public static boolean hasSource(Level level,BlockPos pos){
        for(var dir:Direction.Plane.HORIZONTAL)for(int i=1;i<=PowerBlocks.FLOODLIGHT_RANGE;i++){
            var p=pos.relative(dir,i);var state=level.getBlockState(p);
            if(state.is(PowerBlocks.FLOODLIGHT.get())){if(state.getValue(FacingMachineBlock.LIT)&&state.getValue(FacingMachineBlock.FACING)==dir.getOpposite())return true;break;}
            if(!state.isAir())break;
        }
        return false;
    }
}
