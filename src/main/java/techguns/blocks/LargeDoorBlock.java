package techguns.blocks;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import techguns.network.NetworkHooks;
import techguns.*;
import techguns.tileentities.LargeDoorEntity;
public final class LargeDoorBlock extends BaseEntityBlock {
    /** 1.21.1 block codec (blocks are never decoded from data; the codec returns this instance). */
    @Override protected com.mojang.serialization.MapCodec<LargeDoorBlock> codec(){return com.mojang.serialization.MapCodec.unit(this);}
    public enum Phase implements StringRepresentable {CLOSED,OPENED,OPENING,CLOSING;public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}public boolean open(){return this==OPENED||this==OPENING;}public boolean moving(){return this==OPENING||this==CLOSING;}}
    public static final BooleanProperty ZPLANE=BooleanProperty.create("zplane");
    public static final IntegerProperty ROW=IntegerProperty.create("row",0,2),COLUMN=IntegerProperty.create("column",0,2);
    public static final EnumProperty<Phase> PHASE=EnumProperty.create("phase",Phase.class);
    private static final Set<BlockPos> REMOVING=new HashSet<>(),CREATIVE=new HashSet<>();
    public final int variant;
    public LargeDoorBlock(int variant){super(Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(6).noOcclusion().pushReaction(PushReaction.BLOCK));this.variant=variant;registerDefaultState(stateDefinition.any().setValue(ZPLANE,false).setValue(ROW,1).setValue(COLUMN,1).setValue(PHASE,Phase.CLOSED));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(ZPLANE,ROW,COLUMN,PHASE);}
    public static boolean master(BlockState state){return state.getValue(ROW)==1&&state.getValue(COLUMN)==1;}
    public static BlockPos masterPos(BlockPos pos,BlockState state){return state.getValue(ZPLANE)?pos.offset(0,1-state.getValue(ROW),1-state.getValue(COLUMN)):pos.offset(1-state.getValue(COLUMN),1-state.getValue(ROW),0);}
    public static BlockPos partPos(BlockPos center,boolean zplane,int column,int row){return zplane?center.offset(0,row-1,column-1):center.offset(column-1,row-1,0);}
    public boolean place(Level level,BlockPos bottom,boolean zplane,Player player){
        BlockPos center=bottom.above();for(int c=0;c<3;c++)for(int r=0;r<3;r++){BlockPos p=partPos(center,zplane,c,r);if(!level.isInWorldBounds(p)||!level.getBlockState(p).canBeReplaced()||!level.getWorldBorder().isWithinBounds(p)||player!=null&&!player.mayUseItemAt(p,Direction.UP,player.getMainHandItem()))return false;}
        if(level.isClientSide)return true;
        for(int c=0;c<3;c++)for(int r=0;r<3;r++)level.setBlock(partPos(center,zplane,c,r),defaultBlockState().setValue(ZPLANE,zplane).setValue(COLUMN,c).setValue(ROW,r),2);
        if(level.getBlockEntity(center) instanceof LargeDoorEntity door&&player!=null)door.access.place(player);
        for(int c=0;c<3;c++)for(int r=0;r<3;r++)level.updateNeighborsAt(partPos(center,zplane,c,r),this);
        return true;
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return master(state)?new LargeDoorEntity(pos,state):null;}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return !level.isClientSide&&master(state)?createTickerHelper(type,TGDoors.ENTITY.get(),(l,p,s,door)->door.tick()):null;}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override public VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext context){return s.getValue(PHASE).open()?Shapes.empty():closed(s);}
    private static VoxelShape closed(BlockState s){return s.getValue(ZPLANE)?Block.box(6,0,0,10,16,16):Block.box(0,0,6,16,16,10);}
    @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext context){
        if(!s.getValue(PHASE).open())return closed(s);
        boolean z=s.getValue(ZPLANE);int row=s.getValue(ROW),column=s.getValue(COLUMN);
        if(variant==2)return row==0?(z?Block.box(6,0,0,10,4,16):Block.box(0,0,6,16,4,10)):Shapes.empty();
        if(row==2)return z?Block.box(6,12,0,10,16,16):Block.box(0,12,6,16,16,10);
        if(variant==1||column==1)return Shapes.empty();
        return z?Block.box(6,0,column==0?0:12,10,16,column==0?4:16):Block.box(column==0?0:12,0,6,column==0?4:16,16,10);
    }
    @Override protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack tgHeldStack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){return techguns.util.TGInteraction.item(legacyUse(state,level,pos,player,hand,hit));}
    public InteractionResult legacyUse(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(level.getBlockEntity(masterPos(pos,state)) instanceof LargeDoorEntity door&&door.access.allows(player)){
            if(!level.isClientSide){if(player.isShiftKeyDown())NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer)player,door,door.getBlockPos());else if(door.mode==0)door.toggle(true);}
        }return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level,BlockPos pos,BlockState state){return TGDoors.ITEMS.get(variant).get().getDefaultInstance();}
    @Override public java.util.List<ItemStack> getDrops(BlockState state,net.minecraft.world.level.storage.loot.LootParams.Builder builder){return java.util.List.of();}
    @Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){if(player.isCreative())CREATIVE.add(masterPos(pos,state));return super.playerWillDestroy(level,pos,state,player);}
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(!state.is(next.getBlock())&&!level.isClientSide){BlockPos center=masterPos(pos,state);if(REMOVING.add(center)){boolean creative=CREATIVE.remove(center);try{
            for(int c=0;c<3;c++)for(int r=0;r<3;r++){BlockPos p=partPos(center,state.getValue(ZPLANE),c,r);BlockState part=level.getBlockState(p);if(!p.equals(pos)&&part.is(this)&&masterPos(p,part).equals(center))level.removeBlock(p,false);}
            if(!creative&&level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)&&!level.restoringBlockSnapshots)popResource(level,pos,TGDoors.ITEMS.get(variant).get().getDefaultInstance());
        }finally{REMOVING.remove(center);}}}super.onRemove(state,level,pos,next,moving);
    }
}
