package techguns.power.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Power line pylon: a steel lattice tower HEIGHT blocks tall with two cross-arms, each with an insulator string at
 * both ends for high-voltage (or steel) wires. Placed as one item; the blocks above the base are its invisible parts.
 * The arms run across FACING, so the wires run along it.
 */
public final class PylonBlock extends BaseEntityBlock implements WireHost {
    public static final int HEIGHT=8;
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty PART=IntegerProperty.create("part",0,HEIGHT-1);
    private static final VoxelShape[] SHAPES=new VoxelShape[HEIGHT];
    static{for(int i=0;i<HEIGHT;i++){double h=half(i+0.5)*16;SHAPES[i]=Block.box(8-h,0,8-h,8+h,16,8+h);}}
    /** Half width of the tower at a height above the base, in blocks. */
    public static double half(double y){return 0.44-0.28*Math.min(1,y/(HEIGHT-1.5));}
    public PylonBlock(){
        super(Properties.of().strength(5,20).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion().pushReaction(PushReaction.BLOCK));
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PART,0));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PART);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx){
        var level=ctx.getLevel();var pos=ctx.getClickedPos();
        if(pos.getY()+HEIGHT>level.getMaxBuildHeight())return null;
        for(int i=1;i<HEIGHT;i++)if(!level.getBlockState(pos.above(i)).canBeReplaced(ctx))return null;
        return defaultBlockState().setValue(FACING,ctx.getHorizontalDirection());
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack){
        for(int i=1;i<HEIGHT;i++)level.setBlock(pos.above(i),state.setValue(PART,i),3);
    }
    private static boolean cascading;
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(!state.is(next.getBlock())&&!cascading){
            var base=pos.below(state.getValue(PART));
            cascading=true;
            try{for(int i=0;i<HEIGHT;i++){var p=base.above(i);if(!p.equals(pos)&&level.getBlockState(p).is(this))level.setBlock(p,Blocks.AIR.defaultBlockState(),35);}}
            finally{cascading=false;}
            if(level instanceof ServerLevel server){
                WireCoilItem.dropWires(server,base,WireNetwork.get(server).removeAt(server,base));
                // the base drops the pylon by its loot table; a part taken away drops it here
                if(state.getValue(PART)>0&&!moving)Block.popResource(level,base,new ItemStack(this));
            }
        }
        super.onRemove(state,level,pos,next,moving);
    }
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext ctx){return SHAPES[state.getValue(PART)];}
    @Override public RenderShape getRenderShape(BlockState state){return state.getValue(PART)==0?RenderShape.ENTITYBLOCK_ANIMATED:RenderShape.INVISIBLE;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return state.getValue(PART)==0?new PylonEntity(pos,state):null;}

    /** Ends of the insulator strings: 0,1 the upper arm (left, right), 2,3 the lower arm. */
    @Override public int points(BlockState state){return 4;}
    @Override public Vec3 point(BlockState state,BlockPos pos,int index){
        var across=state.getValue(FACING).getClockWise();
        double reach=index<2?ARM_UPPER:ARM_LOWER,y=(index<2?Y_UPPER:Y_LOWER)-INSULATOR;int side=index%2==0?-1:1;
        return Vec3.atBottomCenterOf(pos).add(across.getStepX()*reach*side,y,across.getStepZ()*reach*side);
    }
    public static final double Y_UPPER=HEIGHT-0.35,Y_LOWER=HEIGHT-1.55,ARM_UPPER=1.55,ARM_LOWER=1.95,INSULATOR=0.55;
    @Override public boolean accepts(BlockState state,int index,WireTier tier){return tier!=WireTier.COPPER;}
}
