package techguns.blocks;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.network.NetworkHooks;
import techguns.*;
import techguns.tileentities.MiningChargeEntity;
public final class MiningChargeBlock extends BaseEntityBlock {
    /** Menus open from use() with the block position; the vanilla spectator path opened them without it and the client crashed. */
    @Override public net.minecraft.world.MenuProvider getMenuProvider(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos){return null;}
    public static final DirectionProperty FACING=BlockStateProperties.FACING;public static final BooleanProperty ARMED=BooleanProperty.create("armed");
    public final boolean advanced;
    public MiningChargeBlock(boolean advanced){super(Properties.copy(Blocks.IRON_BLOCK).strength(2).noOcclusion());this.advanced=advanced;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.DOWN).setValue(ARMED,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,ARMED);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getClickedFace().getOpposite());}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new MiningChargeEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return l.isClientSide?null:createTickerHelper(type,TGCharges.ENTITY.get(),(level,pos,state,charge)->charge.tick());}
    @Override public void setPlacedBy(Level l,BlockPos pos,BlockState s,LivingEntity entity,ItemStack stack){if(!l.isClientSide&&l.getBlockEntity(pos) instanceof MiningChargeEntity charge&&entity instanceof Player player){charge.access.place(player);l.playSound(null,pos,(advanced?TGSounds.C4_PLANT:TGSounds.TNT_PLANT).get(),net.minecraft.sounds.SoundSource.BLOCKS,4,1);}}
    @Override public BlockState updateShape(BlockState s,Direction side,BlockState other,LevelAccessor l,BlockPos p,BlockPos otherPos){return side==s.getValue(FACING)&&other.isAir()?Blocks.AIR.defaultBlockState():s;}
    @Override public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){if(!l.isClientSide&&l.getBlockEntity(p) instanceof MiningChargeEntity charge&&charge.access.allows(player))NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer)player,charge,p);return InteractionResult.sidedSuccess(l.isClientSide);}
    @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(FACING)){case DOWN->Block.box(2,0,2,14,5,14);case UP->Block.box(2,11,2,14,16,14);case NORTH->Block.box(2,2,0,14,14,5);case SOUTH->Block.box(2,2,11,14,14,16);case WEST->Block.box(0,2,2,5,14,14);case EAST->Block.box(11,2,2,16,14,14);};}
    @Override public BlockState rotate(BlockState state,Rotation rot){return state.setValue(FACING,rot.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror){return state.setValue(FACING,mirror.mirror(state.getValue(FACING)));}
}
