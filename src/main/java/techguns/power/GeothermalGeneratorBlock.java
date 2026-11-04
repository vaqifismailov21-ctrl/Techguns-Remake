package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Geothermal generator: works from lava sources below and beside it without using them up. */
public final class GeothermalGeneratorBlock extends FacingMachineBlock {
    public GeothermalGeneratorBlock(){super(10);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new GeothermalGeneratorEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return PowerBlocks.ticker(level,type,PowerBlocks.GEOTHERMAL_GENERATOR_ENTITY.get(),GeothermalGeneratorEntity::tick);}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!level.isClientSide&&level.getBlockEntity(pos) instanceof GeothermalGeneratorEntity generator)
            player.displayClientMessage(Component.translatable("techguns.power.geothermal_status",generator.rate,generator.lava,generator.energyStored(),generator.energyCapacity()),true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
