package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Wind turbine, built as a column of the same block: the bottom piece is the base (generator), pieces in between are
 * masts, the top piece is the head with the rotor. The part follows from the neighbours, so the column just gets stacked.
 */
public final class WindTurbineBlock extends BaseEntityBlock {
    public enum Part implements StringRepresentable {
        BASE,MAST,HEAD;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    private static final VoxelShape BASE=Shapes.or(Block.box(0,0,0,16,11,16),Block.box(5,11,5,11,16,11));
    private static final VoxelShape MAST=Block.box(5,0,5,11,16,11);
    private static final VoxelShape HEAD=Shapes.or(Block.box(5,0,5,11,4,11),Block.box(4,4,0,12,12,16));
    public WindTurbineBlock(){
        super(Properties.of().strength(3.5f,10).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PART,Part.BASE));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,PART);}
    private Part partAt(LevelAccessor level,BlockPos pos){
        boolean below=level.getBlockState(pos.below()).is(this),above=level.getBlockState(pos.above()).is(this);
        return !below?Part.BASE:above?Part.MAST:Part.HEAD;
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){
        var below=context.getLevel().getBlockState(context.getClickedPos().below());
        var facing=below.is(this)?below.getValue(FACING):context.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACING,facing).setValue(PART,partAt(context.getLevel(),context.getClickedPos()));
    }
    @Override public BlockState updateShape(BlockState state,Direction side,BlockState neighbour,LevelAccessor level,BlockPos pos,BlockPos neighbourPos){
        return side.getAxis()==Direction.Axis.Y?state.setValue(PART,partAt(level,pos)):state;
    }
    @Override public BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
        return switch(state.getValue(PART)){case BASE->BASE;case MAST->MAST;case HEAD->HEAD;};
    }
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new WindTurbineEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return PowerBlocks.ticker(level,type,PowerBlocks.WIND_TURBINE_ENTITY.get(),WindTurbineEntity::tick);}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!level.isClientSide){
            var base=WindTurbineEntity.findBase(level,pos);
            if(base==null)player.displayClientMessage(Component.translatable("techguns.power.wind.no_base"),true);
            else player.displayClientMessage(Component.translatable("techguns.power.wind_status",base.rate,base.energyStored(),base.energyCapacity()).append(" ").append(base.reason()),true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
