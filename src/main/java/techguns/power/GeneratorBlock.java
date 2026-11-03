package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.network.NetworkHooks;

/** Fuel (solid) or liquid generator: a furnace-like block with a front that glows while burning. */
public final class GeneratorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public final boolean liquid;
    public GeneratorBlock(boolean liquid){
        super(Properties.of(net.minecraft.world.level.material.Material.METAL).strength(3.5f,10).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(state->state.getValue(LIT)?13:0));this.liquid=liquid;
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(LIT,false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,LIT);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override public BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return liquid?new LiquidGeneratorEntity(pos,state):new FuelGeneratorEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){
        if(level.isClientSide)return null;
        return liquid?createTickerHelper(type,PowerBlocks.LIQUID_GENERATOR_ENTITY.get(),(world,pos,block,entity)->entity.tick()):createTickerHelper(type,PowerBlocks.FUEL_GENERATOR_ENTITY.get(),(world,pos,block,entity)->entity.tick());
    }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        var entity=level.getBlockEntity(pos);if(entity==null)return InteractionResult.PASS;
        // Buckets and fluid containers pour straight into the liquid generator.
        if(entity instanceof LiquidGeneratorEntity generator&&FluidUtil.interactWithFluidHandler(player,hand,generator.tank))return InteractionResult.sidedSuccess(level.isClientSide);
        if(!level.isClientSide&&player instanceof ServerPlayer server&&entity instanceof EnergySource source)NetworkHooks.openScreen(server,new GeneratorMenu.Provider(source),buf->buf.writeBlockPos(pos));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(state.getBlock()!=next.getBlock()){var entity=level.getBlockEntity(pos);
            if(entity instanceof FuelGeneratorEntity generator)Containers.dropItemStack(level,pos.getX(),pos.getY(),pos.getZ(),generator.fuel.getStackInSlot(0));
            if(entity instanceof LiquidGeneratorEntity generator)Containers.dropItemStack(level,pos.getX(),pos.getY(),pos.getZ(),generator.container.getStackInSlot(0));}
        super.onRemove(state,level,pos,next,moving);
    }
}
