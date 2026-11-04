package techguns.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.network.NetworkHooks;
import techguns.TGTurrets;
import techguns.tileentities.TurretTileEnt;

/** The original BasicMachine turret variant: an invisible block rendered by its tile renderer, owned and wrenchable. */
public final class TurretBlock extends BaseEntityBlock {
    /** Menus open from use() with the block position; the vanilla spectator path opened them without it and the client crashed. */
    @Override public net.minecraft.world.MenuProvider getMenuProvider(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos){return null;}
    public static final ToolAction WRENCH=ToolAction.get("wrench");
    public TurretBlock(){super(Properties.of().strength(4.0f).sound(SoundType.METAL).noOcclusion());}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new TurretTileEnt(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){
        return level.isClientSide?createTickerHelper(type,TGTurrets.ENTITY.get(),TurretTileEnt::clientTick):createTickerHelper(type,TGTurrets.ENTITY.get(),TurretTileEnt::serverTick);
    }
    /** Original onBlockPlacedByExtended order: owner first, then any dismantled tile data from the item. Facing and spawn follow in TurretBlockItem. */
    @Override public void neighborChanged(BlockState state,Level level,BlockPos pos,net.minecraft.world.level.block.Block block,BlockPos from,boolean moving){
        super.neighborChanged(state,level,pos,block,from,moving);if(level.getBlockEntity(pos) instanceof TurretTileEnt tile)tile.onNeighborBlockChange();
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack){
        super.setPlacedBy(level,pos,state,placer,stack);
        if(level.getBlockEntity(pos) instanceof TurretTileEnt tile){
            if(placer instanceof Player player)tile.setOwner(player);
            tile.onNeighborBlockChange();
            var data=stack.getTagElement("BlockEntityTag");if(data!=null)tile.readNBTfromStackTag(data);
        }
    }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!(level.getBlockEntity(pos) instanceof TurretTileEnt tile))return InteractionResult.PASS;
        if(!tile.isUseableByPlayer(player)){player.displayClientMessage(Component.translatable("techguns.container.security.denied"),true);return InteractionResult.sidedSuccess(level.isClientSide);}
        ItemStack held=player.getItemInHand(hand);
        if(!held.isEmpty()&&held.canPerformAction(WRENCH)){
            if(!level.isClientSide){
                if(player.isShiftKeyDown())dismantle(level,pos,tile);
                else tile.rotateTile(hit.getDirection());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if(!level.isClientSide&&player instanceof ServerPlayer server)NetworkHooks.openScreen(server,tile,pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    /** Original wrench dismantling: the block item keeps inventory and settings (not the owner), the base is emptied and removed. */
    private static void dismantle(Level level,BlockPos pos,TurretTileEnt tile){
        ItemStack item=new ItemStack(TGTurrets.ITEM.get());
        item.getOrCreateTag().put("BlockEntityTag",tile.writeNBTforDismantling());
        tile.emptyContent();
        level.removeBlock(pos,false);
        level.addFreshEntity(new ItemEntity(level,pos.getX()+.5d,pos.getY()+.5d,pos.getZ()+.5d,item));
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(state.getBlock()!=next.getBlock()&&level.getBlockEntity(pos) instanceof TurretTileEnt tile)tile.onBlockBreak();
        super.onRemove(state,level,pos,next,moving);
    }
    /** Only the two original mounting directions exist. */
    public static Direction facingFor(Direction clickedFace){return clickedFace==Direction.DOWN?Direction.DOWN:Direction.UP;}
    public static Block create(){return new TurretBlock();}
    /** Like the original machines (isOpaqueCube false): light and sky light pass, no ambient occlusion shadow. */
    @Override public float getShadeBrightness(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){return 1.0f;}
    @Override public boolean propagatesSkylightDown(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){return true;}
    @Override public int getLightBlock(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){return 0;}
}
