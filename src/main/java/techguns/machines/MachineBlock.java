package techguns.machines;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import techguns.TGMachines;
public final class MachineBlock extends BaseEntityBlock {
    /** Menus open from use() with the block position; the vanilla spectator path opened them without it and the client crashed. */
    @Override public net.minecraft.world.MenuProvider getMenuProvider(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos){return null;}
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED=BooleanProperty.create("formed");
    public final MachineKind kind;
    public MachineBlock(MachineKind kind){super(Properties.of().strength(4,20).sound(SoundType.METAL).noOcclusion().isViewBlocking((st,l,p)->false).isSuffocating((st,l,p)->false));this.kind=kind;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(FORMED,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,FORMED);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override public BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new ProcessingMachine(pos,state);}
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,net.minecraft.world.entity.LivingEntity entity,net.minecraft.world.item.ItemStack stack){super.setPlacedBy(level,pos,state,entity,stack);if(entity instanceof Player player&&level.getBlockEntity(pos) instanceof ProcessingMachine machine)machine.access.place(player);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide?null:createTickerHelper(type,TGMachines.ENTITY.get(),(world,pos,block,machine)->machine.tick());}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){if(!(level.getBlockEntity(pos) instanceof ProcessingMachine machine)||!machine.access.allows(player))return InteractionResult.FAIL;if(kind.multiblock()&&!machine.formed&&!player.isShiftKeyDown()){if(!level.isClientSide&&!machine.formStructure(hit.getDirection())){for(var bad:machine.invalidParts(hit.getDirection()))techguns.oredrill.OreDrillDefinition.sendErrorPing(player,bad);player.displayClientMessage(net.minecraft.network.chat.Component.translatable("techguns.msg.error.multiblockinvalid"),true);};return InteractionResult.sidedSuccess(level.isClientSide);}if((kind==MachineKind.CHEM_LAB||kind==MachineKind.REACTION)&&net.minecraftforge.fluids.FluidUtil.interactWithFluidHandler(player,hand,level,pos,hit.getDirection()))return InteractionResult.sidedSuccess(level.isClientSide);if(!level.isClientSide&&player instanceof ServerPlayer server)NetworkHooks.openScreen(server,machine,pos);return InteractionResult.sidedSuccess(level.isClientSide);}
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){if(state.getBlock()!=next.getBlock()&&level.getBlockEntity(pos) instanceof ProcessingMachine machine){machine.unformStructure();machine.dropContents();level.updateNeighbourForOutputSignal(pos,this);}super.onRemove(state,level,pos,next,moving);}
    /** Like the original machines (isOpaqueCube false): light and sky light pass, no ambient occlusion shadow. */
    @Override public float getShadeBrightness(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){return 1.0f;}
    @Override public boolean propagatesSkylightDown(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){return true;}
    @Override public int getLightBlock(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){return 0;}
}
