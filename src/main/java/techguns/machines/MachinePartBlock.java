package techguns.machines;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.network.NetworkHooks;
public final class MachinePartBlock extends BaseEntityBlock {
    public final boolean glass;
    public final boolean reactor;
    public MachinePartBlock(boolean glass){this(glass,false);}
    public MachinePartBlock(boolean glass,boolean reactor){super(Properties.of().strength(4,20).sound(glass?SoundType.GLASS:SoundType.METAL).noOcclusion().dynamicShape());this.glass=glass;this.reactor=reactor;registerDefaultState(stateDefinition.any().setValue(MachineBlock.FORMED,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(MachineBlock.FORMED);}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new MachinePartEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide?null:createTickerHelper(type,techguns.TGFabricator.ENTITY.get(),(l,p,s,part)->{if(l.getGameTime()%20==0)part.validateLink();});}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){if(reactor&&state.getValue(MachineBlock.FORMED)&&level.getBlockEntity(pos) instanceof MachinePartEntity part&&part.master()!=null){var master=part.master();var center=master.getBlockPos().relative(master.getBlockState().getValue(MachineBlock.FACING));int x=pos.getX()-center.getX(),z=pos.getZ()-center.getZ(),y=pos.getY()-center.getY();double margin=y==0?0.25:0.45;return box(x<0?margin*16:0,0,z<0?margin*16:0,x>0?(1-margin)*16:16,y==3?10.4:16,z>0?(1-margin)*16:16);}return glass&&state.getValue(MachineBlock.FORMED)?box(0,0,0,16,14.4,16):super.getShape(state,level,pos,context);}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){if(level.getBlockEntity(pos) instanceof MachinePartEntity part&&part.master()!=null){var master=part.master();if(!level.isClientSide&&player instanceof ServerPlayer server&&master.access.allows(player))NetworkHooks.openScreen(server,master,master.getBlockPos());return InteractionResult.sidedSuccess(level.isClientSide);}return InteractionResult.PASS;}
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){if(state.getBlock()!=next.getBlock()&&!level.isClientSide&&level.getBlockEntity(pos) instanceof MachinePartEntity part&&part.master()!=null)part.master().unformStructure();super.onRemove(state,level,pos,next,moving);}
    /** Like the original machines (isOpaqueCube false): light and sky light pass, no ambient occlusion shadow. */
    @Override public float getShadeBrightness(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){return 1.0f;}
    @Override public boolean propagatesSkylightDown(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){return true;}
    @Override public int getLightBlock(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){return 0;}
}
