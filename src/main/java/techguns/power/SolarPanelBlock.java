package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A flat panel; right click shows the current output and stored energy. */
public final class SolarPanelBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE=Block.box(0,0,0,16,6,16);
    public SolarPanelBlock(){super(Properties.of().strength(2.5f,6).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return SHAPE;}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new SolarPanelEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide?null:createTickerHelper(type,PowerBlocks.SOLAR_PANEL_ENTITY.get(),(world,pos,block,entity)->entity.tick());}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!level.isClientSide&&level.getBlockEntity(pos) instanceof SolarPanelEntity panel)player.displayClientMessage(Component.translatable("techguns.power.solar_status",panel.currentRate(),panel.energyStored(),panel.energyCapacity()),true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
