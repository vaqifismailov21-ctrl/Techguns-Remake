package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Energy cell (or the bigger energy bank): its front (the side facing the player when placed) gives energy, all other
 * sides take it. Keeps its charge when broken. CHARGE (0..4) drives the charge bars of the model.
 */
public final class EnergyCellBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.FACING;
    public static final IntegerProperty CHARGE=IntegerProperty.create("charge",0,4);
    public final boolean bank;
    public EnergyCellBlock(boolean bank){
        super(Properties.of().strength(3.5f,10).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());this.bank=bank;
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(CHARGE,0));
    }
    public int capacity(){return bank?PowerBlocks.BANK_CAPACITY:PowerBlocks.CELL_CAPACITY;}
    public int transfer(){return bank?PowerBlocks.BANK_TRANSFER:PowerBlocks.CELL_TRANSFER;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,CHARGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getNearestLookingDirection().getOpposite());}
    @Override public BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return net.minecraft.world.phys.shapes.Shapes.block();}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new EnergyCellEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide?null:createTickerHelper(type,PowerBlocks.ENERGY_CELL_ENTITY.get(),(world,pos,block,entity)->entity.tick());}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!level.isClientSide&&level.getBlockEntity(pos) instanceof EnergyCellEntity cell)player.displayClientMessage(Component.translatable(bank?"techguns.power.bank_status":"techguns.power.cell_status",cell.energyStored(),cell.energyCapacity()),true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    /** Charge bars shown for a stored amount: 0 only when empty. */
    public static int chargeLevel(int energy,int capacity){return energy<=0?0:Math.max(1,Math.min(4,(int)Math.ceil(energy*4.0/capacity)));}
}
