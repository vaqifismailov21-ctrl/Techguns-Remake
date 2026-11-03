package techguns.blocks;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
import techguns.TGWeaponBlocks;
import techguns.tileentities.BioBlobBlockEntity;
public final class BioBlobBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.FACING;
    public static final IntegerProperty SIZE=IntegerProperty.create("size",0,2);
    public BioBlobBlock() {super(Properties.of(net.minecraft.world.level.material.Material.CLAY).strength(2).sound(SoundType.SLIME_BLOCK).lightLevel(s->7).noOcclusion());registerDefaultState(stateDefinition.any().setValue(FACING,Direction.DOWN).setValue(SIZE,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {builder.add(FACING,SIZE);}
    @Override public RenderShape getRenderShape(BlockState state) {return RenderShape.MODEL;}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        int size=state.getValue(SIZE),w=size==0?5:size==1?3:1,h=size==0?3:size==1?4:5;
        return switch(state.getValue(FACING)) {
            case DOWN -> box(w,0,w,16-w,h,16-w);
            case UP -> box(w,16-h,w,16-w,16,16-w);
            case WEST -> box(0,w,w,h,16-w,16-w);
            case EAST -> box(16-h,w,w,16,16-w,16-w);
            case NORTH -> box(w,w,0,16-w,16-w,h);
            case SOUTH -> box(w,w,16-h,16-w,16-w,16);
        };
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) {return new BioBlobBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {return level.isClientSide?null:createTickerHelper(type,TGWeaponBlocks.BIOBLOB_ENTITY.get(),BioBlobBlockEntity::tick);}
}
