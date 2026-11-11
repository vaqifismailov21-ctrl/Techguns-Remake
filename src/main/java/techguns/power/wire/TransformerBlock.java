package techguns.power.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Big transformer, two blocks high. It stores a lot of energy and joins thin wires (copper or steel, on the bushing at
 * its front) with high-voltage wires (on the bushing on top): whatever comes in over one side goes out over the other
 * and to the block in front of it. Cables can feed it from the sides and the back.
 */
public final class TransformerBlock extends BaseEntityBlock implements WireHost {
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final int CAPACITY=20_000_000,RATE=524_288;
    private static final VoxelShape LOWER=Block.box(1,0,1,15,16,15),UPPER=Block.box(2,0,2,14,12,14);
    public TransformerBlock(){
        super(Properties.of().strength(5,20).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion().pushReaction(PushReaction.BLOCK));
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(HALF,DoubleBlockHalf.LOWER));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,HALF);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx){
        var pos=ctx.getClickedPos();
        if(pos.getY()>=ctx.getLevel().getMaxBuildHeight()-1||!ctx.getLevel().getBlockState(pos.above()).canBeReplaced(ctx))return null;
        return defaultBlockState().setValue(FACING,ctx.getHorizontalDirection().getOpposite());
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack){
        level.setBlock(pos.above(),state.setValue(HALF,DoubleBlockHalf.UPPER),3);
    }
    private static boolean cascading;
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(!state.is(next.getBlock())&&!cascading){
            boolean lower=state.getValue(HALF)==DoubleBlockHalf.LOWER;var other=lower?pos.above():pos.below();
            cascading=true;
            try{if(level.getBlockState(other).is(this))level.setBlock(other,Blocks.AIR.defaultBlockState(),35);}finally{cascading=false;}
            if(level instanceof ServerLevel server){
                var main=lower?pos:other;
                WireCoilItem.dropWires(server,main,WireNetwork.get(server).removeAt(server,main));
                // the lower half drops the transformer by its loot table; the upper half taken away drops it here
                if(!lower&&!moving)Block.popResource(level,main,new ItemStack(this));
            }
        }
        super.onRemove(state,level,pos,next,moving);
    }
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext ctx){return state.getValue(HALF)==DoubleBlockHalf.LOWER?LOWER:UPPER;}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return state.getValue(HALF)==DoubleBlockHalf.LOWER?new TransformerEntity(pos,state):null;}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){
        return level.isClientSide?null:createTickerHelper(type,WireBlocks.TRANSFORMER_ENTITY.get(),(l,p,s,e)->e.tick());
    }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!player.getItemInHand(hand).isEmpty())return InteractionResult.PASS;
        var main=state.getValue(HALF)==DoubleBlockHalf.LOWER?pos:pos.below();
        if(!level.isClientSide&&level.getBlockEntity(main) instanceof TransformerEntity t)
            player.displayClientMessage(Component.translatable("techguns.wire.transformer.info",t.energyStored(),CAPACITY),true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** 0: thin-wire bushing on the front of the upper block, 1: high-voltage bushing on top. */
    @Override public int points(BlockState state){return 2;}
    @Override public Vec3 point(BlockState state,BlockPos pos,int index){
        var f=state.getValue(FACING);var c=Vec3.atBottomCenterOf(pos);
        return index==0?c.add(f.getStepX()*0.78,1.5,f.getStepZ()*0.78):c.add(0,2.36,0);
    }
    @Override public boolean accepts(BlockState state,int index,WireTier tier){return index==0?tier!=WireTier.HIGH_VOLTAGE:tier==WireTier.HIGH_VOLTAGE;}
    @Override public IEnergyStorage sink(Level level,BlockPos pos,BlockState state,int index){return level.getBlockEntity(pos) instanceof TransformerEntity t?t.wireInput():null;}
    @Override public float fill(Level level,BlockPos pos){return level.getBlockEntity(pos) instanceof TransformerEntity t?t.fill():-1;}
}
