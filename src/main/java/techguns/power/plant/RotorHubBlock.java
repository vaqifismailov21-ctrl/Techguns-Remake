package techguns.power.plant;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Rotor hub of the wind power plant. It goes on the front of the nacelle, takes MAX_BLADES turbine blades one by one
 * (right click with a blade, sneak with an empty hand takes one off) and, once full, a click on it assembles the plant.
 */
public final class RotorHubBlock extends BaseEntityBlock {
    public static final int MAX_BLADES=3;
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty BLADES=IntegerProperty.create("blades",0,MAX_BLADES);
    private static final Map<Direction,VoxelShape> SHAPES=Map.of(
            Direction.NORTH,Block.box(4,3,6,12,11,16),Direction.SOUTH,Block.box(4,3,0,12,11,10),
            Direction.WEST,Block.box(6,3,4,16,11,12),Direction.EAST,Block.box(0,3,4,10,11,12));
    public RotorHubBlock(){
        super(Properties.of().strength(4,20).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(BLADES,0).setValue(PlantPartBlock.FORMED,false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,BLADES,PlantPartBlock.FORMED);}
    /** Faces away from the block it is put on, so it sits on the front of the nacelle. */
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){
        var face=context.getClickedFace();
        return defaultBlockState().setValue(FACING,face.getAxis().isHorizontal()?face:context.getHorizontalDirection().getOpposite());
    }
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return SHAPES.get(state.getValue(FACING));}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new RotorHubEntity(pos,state);}
    @Override public float getShadeBrightness(BlockState state,BlockGetter level,BlockPos pos){return 1;}
    @Override public boolean propagatesSkylightDown(BlockState state,BlockGetter level,BlockPos pos){return true;}

    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(state.getValue(PlantPartBlock.FORMED)){
            var master=level.getBlockEntity(pos) instanceof PlantPartEntity part?part.master():null;
            return master==null?InteractionResult.PASS:master.interact(player,hand,pos,hit.getDirection());
        }
        ItemStack held=player.getItemInHand(hand);int blades=state.getValue(BLADES);
        if(held.is(PlantBlocks.BLADE_ITEM.get())){
            if(blades>=MAX_BLADES)return InteractionResult.PASS;
            if(!level.isClientSide){
                level.setBlock(pos,state.setValue(BLADES,blades+1),3);if(!player.getAbilities().instabuild)held.shrink(1);
                level.playSound(null,pos,SoundEvents.ANVIL_PLACE,SoundSource.BLOCKS,0.4F,1.6F);
                player.displayClientMessage(Component.translatable("techguns.plant.rotor_hub.blades",blades+1,MAX_BLADES),true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if(!held.isEmpty())return InteractionResult.PASS;
        if(player.isShiftKeyDown()){
            if(blades==0)return InteractionResult.PASS;
            if(!level.isClientSide){
                level.setBlock(pos,state.setValue(BLADES,blades-1),3);
                if(!player.getAbilities().instabuild&&!player.getInventory().add(new ItemStack(PlantBlocks.BLADE_ITEM.get())))popResource(level,pos,new ItemStack(PlantBlocks.BLADE_ITEM.get()));
                level.playSound(null,pos,SoundEvents.CHAIN_BREAK,SoundSource.BLOCKS,0.6F,1.2F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if(level.isClientSide)return InteractionResult.SUCCESS;
        if(blades<MAX_BLADES){player.displayClientMessage(Component.translatable("techguns.plant.rotor_hub.need",MAX_BLADES-blades),true);return InteractionResult.CONSUME;}
        var plant=controller(level,pos,state.getValue(FACING));
        if(plant==null){player.displayClientMessage(Component.translatable("techguns.plant.rotor_hub.no_tower"),true);return InteractionResult.CONSUME;}
        return plant.interact(player,hand,pos,hit.getDirection());
    }
    /** Wind plant whose nacelle this hub sits on: behind the hub, then down the tower to the controller. */
    public static WindPlant controller(Level level,BlockPos pos,Direction facing){
        var p=pos.relative(facing.getOpposite());
        if(!level.getBlockState(p).is(PlantBlocks.CASING.get()))return null;
        p=p.below();int n=0;
        while(n<=WindPlant.MAX_TOWER&&level.getBlockState(p).is(PlantBlocks.TOWER.get())){p=p.below();n++;}
        return level.getBlockEntity(p) instanceof WindPlant plant?plant:null;
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(state.getBlock()!=next.getBlock()&&!level.isClientSide){
            if(state.getValue(PlantPartBlock.FORMED)&&level.getBlockEntity(pos) instanceof PlantPartEntity part){var master=part.master();if(master!=null)master.unform();}
            int blades=state.getValue(BLADES);if(blades>0)popResource(level,pos,new ItemStack(PlantBlocks.BLADE_ITEM.get(),blades));
        }
        super.onRemove(state,level,pos,next,moving);
    }
}
