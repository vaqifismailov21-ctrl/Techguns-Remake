package techguns.power.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Building block of the power plants. Once the plant is formed it hides (the controller draws the whole plant) and forwards clicks, energy, fluids and items to it. */
public final class PlantPartBlock extends BaseEntityBlock {
    /** SHELL: invisible block a formed plant puts where its model reaches out of its own blocks, so that part collides too. */
    public enum Type{CASING,GLASS,TOWER,COIL,SHELL}
    public static final BooleanProperty FORMED=BooleanProperty.create("formed");
    public final Type type;
    public PlantPartBlock(Type type){
        super(properties(type));
        this.type=type;registerDefaultState(stateDefinition.any().setValue(FORMED,false));
    }
    private static Properties properties(Type type){
        var p=Properties.of().strength(4,20).sound(type==Type.GLASS?SoundType.GLASS:SoundType.METAL).noOcclusion().dynamicShape()
                .isViewBlocking((s,l,pos)->false).isSuffocating((s,l,pos)->type!=Type.GLASS&&type!=Type.SHELL&&!s.getValue(FORMED)).isRedstoneConductor((s,l,pos)->false);
        return type==Type.SHELL?p.noLootTable().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK):p.requiresCorrectToolForDrops();
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FORMED);}
    @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){
        return state.getValue(FORMED)?PlantController.formedShape(level,pos):net.minecraft.world.phys.shapes.Shapes.block();
    }
    @Override public RenderShape getRenderShape(BlockState state){return type==Type.SHELL?RenderShape.INVISIBLE:RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new PlantPartEntity(pos,state);}
    @Override public boolean skipRendering(BlockState state,BlockState next,net.minecraft.core.Direction side){return type==Type.GLASS&&next.is(this)||super.skipRendering(state,next,side);}
    @Override public float getShadeBrightness(BlockState state,BlockGetter level,BlockPos pos){return type==Type.GLASS||state.getValue(FORMED)?1:0.2F;}
    @Override public boolean propagatesSkylightDown(BlockState state,BlockGetter level,BlockPos pos){return type==Type.GLASS||state.getValue(FORMED);}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!state.getValue(FORMED)){
            // a full bucket on a plant that is not assembled yet would just pour the water out next to it
            if(net.minecraftforge.fluids.FluidUtil.getFluidContained(player.getItemInHand(hand)).isPresent()){
                if(!level.isClientSide)player.displayClientMessage(net.minecraft.network.chat.Component.translatable("techguns.plant.fill_not_formed"),true);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }
        var master=level.getBlockEntity(pos) instanceof PlantPartEntity part?part.master():null;
        if(master==null)return level.isClientSide?InteractionResult.SUCCESS:InteractionResult.PASS;
        return master.interact(player,hand,pos,hit.getDirection());
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(state.getBlock()!=next.getBlock()&&!level.isClientSide&&state.getValue(FORMED)&&level.getBlockEntity(pos) instanceof PlantPartEntity part){var master=part.master();if(master!=null)master.unform();}
        super.onRemove(state,level,pos,next,moving);
    }
}
