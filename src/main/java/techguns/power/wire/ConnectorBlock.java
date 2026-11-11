package techguns.power.wire;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Wire connector: an insulator put on the side of a block (machine, cable, generator, energy cell). Energy the block
 * gives to it goes out over the wires; energy coming over the wires goes into the block. A connector on nothing is a
 * relay. Each kind of wire has its own connector.
 */
public final class ConnectorBlock extends BaseEntityBlock implements WireHost {
    public static final DirectionProperty FACING=BlockStateProperties.FACING;
    public final WireTier tier;
    private final Map<Direction,VoxelShape> shapes=new EnumMap<>(Direction.class);
    /** Length of the insulator from the block face, in pixels. */
    private final float length;
    public ConnectorBlock(WireTier tier){
        super(Properties.of().strength(1.5F,6).sound(SoundType.METAL).noOcclusion().pushReaction(PushReaction.DESTROY));
        this.tier=tier;length=tier==WireTier.COPPER?7:tier==WireTier.STEEL?8:11;
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.UP));
        float r=tier==WireTier.HIGH_VOLTAGE?4:3;
        for(var d:Direction.values()){
            double lo=8-r,hi=8+r,l=length;
            shapes.put(d,switch(d){
                case UP->Block.box(lo,0,lo,hi,l,hi);case DOWN->Block.box(lo,16-l,lo,hi,16,hi);
                case NORTH->Block.box(lo,lo,16-l,hi,hi,16);case SOUTH->Block.box(lo,lo,0,hi,hi,l);
                case WEST->Block.box(16-l,lo,lo,16,hi,hi);case EAST->Block.box(0,lo,lo,l,hi,hi);});
        }
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx){return defaultBlockState().setValue(FACING,ctx.getClickedFace());}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext ctx){return shapes.get(state.getValue(FACING));}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new ConnectorEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return null;}

    @Override public int points(BlockState state){return 1;}
    @Override public Vec3 point(BlockState state,BlockPos pos,int index){
        var f=state.getValue(FACING);double out=(length-1.5)/16-0.5;
        return Vec3.atCenterOf(pos).add(f.getStepX()*out,f.getStepY()*out,f.getStepZ()*out);
    }
    @Override public boolean accepts(BlockState state,int index,WireTier t){return t==tier;}
    @Override public IEnergyStorage sink(Level level,BlockPos pos,BlockState state,int index){
        return level.getBlockEntity(pos) instanceof ConnectorEntity c?c.output():null;
    }

    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!player.getItemInHand(hand).isEmpty())return InteractionResult.PASS;
        if(!level.isClientSide){
            int n=WireNetwork.get((ServerLevel)level).at(pos).size();
            var behind=level.getBlockEntity(pos.relative(state.getValue(FACING).getOpposite()));
            if(tier==WireTier.HIGH_VOLTAGE&&(!techguns.power.HighVoltage.equipment(behind)||behind instanceof ConnectorEntity)){player.displayClientMessage(Component.translatable("techguns.wire.hv_only").withStyle(net.minecraft.ChatFormatting.RED),true);return InteractionResult.SUCCESS;}
            player.displayClientMessage(Component.translatable("techguns.wire.connector.info",Component.translatable("techguns.wire."+tier.id),n,tier.rate),true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(!state.is(next.getBlock())&&level instanceof ServerLevel server)WireCoilItem.dropWires(server,pos,WireNetwork.get(server).removeAt(server,pos));
        super.onRemove(state,level,pos,next,moving);
    }
    public static VoxelShape empty(){return Shapes.empty();}
}
