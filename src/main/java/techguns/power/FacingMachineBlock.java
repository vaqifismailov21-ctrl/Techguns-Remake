package techguns.power;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/** Base of the new power blocks: horizontal front toward the player, LIT while working, drawn by PowerRenderer. */
public abstract class FacingMachineBlock extends BaseEntityBlock {
    /** Menus open from use() with the block position; the vanilla spectator path opened them without it and the client crashed. */
    @Override public net.minecraft.world.MenuProvider getMenuProvider(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos){return null;}
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    protected FacingMachineBlock(int light){
        super(Properties.of().strength(3.5f,10).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion().lightLevel(state->state.getValue(LIT)?light:0));
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(LIT,false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,LIT);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override public BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    /** World position of a point given in model block coordinates (front at z = 0) for a block facing {@code facing}. */
    public static net.minecraft.world.phys.Vec3 local(net.minecraft.core.BlockPos pos,Direction facing,double x,double y,double z){
        var right=facing.getClockWise();double forward=(8-z)/16,side=(x-8)/16;
        return new net.minecraft.world.phys.Vec3(pos.getX()+.5+facing.getStepX()*forward+right.getStepX()*side,pos.getY()+y/16,pos.getZ()+.5+facing.getStepZ()*forward+right.getStepZ()*side);
    }
    /** Sets LIT when it changed. */
    public static void setLit(net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,BlockState state,boolean lit){if(state.getValue(LIT)!=lit)level.setBlock(pos,state.setValue(LIT,lit),3);}
    /** Like the original machines (isOpaqueCube false): light and sky light pass, no ambient occlusion shadow. */
    @Override public float getShadeBrightness(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){return 1.0f;}
    @Override public boolean propagatesSkylightDown(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){return true;}
    @Override public int getLightBlock(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){return 0;}
}
