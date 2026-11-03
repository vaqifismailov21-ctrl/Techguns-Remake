package techguns.oredrill;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import techguns.TGOreDrill;

/** Original BlockOreDrill/MultiBlockMachine: hardness 4 machine blocks with a FORMED state; the controller owns the multiblock. */
public class OreDrillBlock extends BaseEntityBlock {
    public static final BooleanProperty FORMED=BooleanProperty.create("formed");
    public final OreDrillType type;
    public OreDrillBlock(OreDrillType type){
        super(Properties.of(net.minecraft.world.level.material.Material.METAL).strength(4.0f).sound(SoundType.METAL).noOcclusion());
        this.type=type;registerDefaultState(stateDefinition.any().setValue(FORMED,false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FORMED);}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return type==OreDrillType.CONTROLLER?new OreDrillTileEntMaster(pos,state):new OreDrillSlaveEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> beType){
        return type==OreDrillType.CONTROLLER?createTickerHelper(beType,TGOreDrill.MASTER.get(),(w,p,s,tile)->tile.tick()):null;
    }
    /** A formed scaffold hides its faces towards identical formed scaffold states, as shouldSideBeRendered did. */
    @Override public boolean skipRendering(BlockState state,BlockState adjacent,Direction direction){
        if(type==OreDrillType.SCAFFOLD&&state.getValue(FORMED)&&adjacent==state)return true;
        return super.skipRendering(state,adjacent,direction);
    }
    @Override public float getShadeBrightness(BlockState state,BlockGetter level,BlockPos pos){return type.fullCube?0.2f:1.0f;}
    @Override public boolean propagatesSkylightDown(BlockState state,BlockGetter level,BlockPos pos){return !type.fullCube;}
    @Override public ItemStack getCloneItemStack(BlockState state,HitResult target,BlockGetter level,BlockPos pos,Player player){
        return OreDrillScaffoldBlock.isHidden(state)?ItemStack.EMPTY:super.getCloneItemStack(state,target,level,pos,player);
    }

    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        var tile=level.getBlockEntity(pos);
        if(tile instanceof OreDrillTileEntMaster master){
            if(!master.isUseableByPlayer(player)){player.displayClientMessage(net.minecraft.network.chat.Component.translatable("techguns.container.security.denied"),true);return InteractionResult.sidedSuccess(level.isClientSide);}
            if(!master.isFormed()&&!player.isShiftKeyDown()){
                if(!level.isClientSide&&OreDrillDefinition.checkForm(level,player,pos))OreDrillDefinition.form(level,pos);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            // Wrench: the controller can neither be dismantled nor rotated, the click does nothing.
            if(player.getItemInHand(hand).canPerformAction(techguns.machines.TurretBlock.WRENCH))return InteractionResult.sidedSuccess(level.isClientSide);
            if(!level.isClientSide&&player instanceof ServerPlayer server)NetworkHooks.openScreen(server,master,pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if(tile instanceof OreDrillSlaveEntity slave){
            if(!slave.hasMaster())return InteractionResult.PASS;
            if(!level.isClientSide&&player instanceof ServerPlayer server&&level.getBlockEntity(slave.getMasterPos()) instanceof OreDrillTileEntMaster master)NetworkHooks.openScreen(server,master,master.getBlockPos());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(!level.isClientSide&&state.getBlock()!=next.getBlock()){
            var tile=level.getBlockEntity(pos);
            if(tile instanceof OreDrillTileEntMaster master)master.onBlockBreak();
            else if(tile instanceof OreDrillSlaveEntity slave)slave.onBlockBreak();
        }
        super.onRemove(state,level,pos,next,moving);
    }
}
