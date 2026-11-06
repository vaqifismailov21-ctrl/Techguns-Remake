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
    public enum Type{CASING,GLASS,TOWER,COIL}
    public static final BooleanProperty FORMED=BooleanProperty.create("formed");
    public final Type type;
    public PlantPartBlock(Type type){
        super(Properties.of().strength(4,20).sound(type==Type.GLASS?SoundType.GLASS:SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()
                .isViewBlocking((s,l,p)->false).isSuffocating((s,l,p)->type!=Type.GLASS&&!s.getValue(FORMED)).isRedstoneConductor((s,l,p)->false));
        this.type=type;registerDefaultState(stateDefinition.any().setValue(FORMED,false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FORMED);}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new PlantPartEntity(pos,state);}
    @Override public boolean skipRendering(BlockState state,BlockState next,net.minecraft.core.Direction side){return type==Type.GLASS&&next.is(this)||super.skipRendering(state,next,side);}
    @Override public float getShadeBrightness(BlockState state,BlockGetter level,BlockPos pos){return type==Type.GLASS||state.getValue(FORMED)?1:0.2F;}
    @Override public boolean propagatesSkylightDown(BlockState state,BlockGetter level,BlockPos pos){return type==Type.GLASS||state.getValue(FORMED);}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!state.getValue(FORMED)||!(level.getBlockEntity(pos) instanceof PlantPartEntity part))return InteractionResult.PASS;
        var master=part.master();if(master==null)return InteractionResult.PASS;
        return master.interact(player,hand,pos,hit.getDirection());
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(state.getBlock()!=next.getBlock()&&!level.isClientSide&&state.getValue(FORMED)&&level.getBlockEntity(pos) instanceof PlantPartEntity part){var master=part.master();if(master!=null)master.unform();}
        super.onRemove(state,level,pos,next,moving);
    }
}
