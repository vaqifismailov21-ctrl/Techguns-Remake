package techguns.power.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Controller of a power plant: front toward the player; clicking it forms the plant from the parts around it. */
public final class PlantControllerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public static final BooleanProperty FORMED=PlantPartBlock.FORMED;
    public final PlantKind kind;
    public PlantControllerBlock(PlantKind kind){
        super(Properties.of().strength(4,20).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion().isViewBlocking((s,l,p)->false)
                .lightLevel(s->s.getValue(LIT)?kind.light:0));
        this.kind=kind;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(LIT,false).setValue(FORMED,false));
    }
    @Override public net.minecraft.world.MenuProvider getMenuProvider(BlockState state,Level level,BlockPos pos){return null;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,LIT,FORMED);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override public BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public float getShadeBrightness(BlockState state,BlockGetter level,BlockPos pos){return 1;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return kind.create(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){
        return level.isClientSide?(l,p,s,e)->{if(e instanceof PlantController c)techguns.power.PowerBlocks.clientTick.accept(c);}:(l,p,s,e)->{if(e instanceof PlantController c)c.tick();};
    }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!(level.getBlockEntity(pos) instanceof PlantController controller))return InteractionResult.PASS;
        return controller.interact(player,hand,pos,hit.getDirection());
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(state.getBlock()!=next.getBlock()&&!level.isClientSide&&level.getBlockEntity(pos) instanceof PlantController controller){controller.unform();controller.dropContents();}
        super.onRemove(state,level,pos,next,moving);
    }
    @Override public void animateTick(BlockState state,Level level,BlockPos pos,RandomSource random){
        if(state.getValue(FORMED)&&state.getValue(LIT)&&level.getBlockEntity(pos) instanceof PlantController controller)controller.effects(random);
    }
}
