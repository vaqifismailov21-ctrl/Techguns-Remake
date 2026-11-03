package techguns.oredrill;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import techguns.TGOreDrill;

/** Original ItemBlockOreDrill placement rules for rods (next to a free cluster or another rod) and controllers (not next to a controller). */
public final class OreDrillBlockItem extends BlockItem {
    private final OreDrillBlock drillBlock;
    public OreDrillBlockItem(OreDrillBlock block){super(block,new Properties());drillBlock=block;}
    @Override public InteractionResult place(BlockPlaceContext context){
        Level world=context.getLevel();BlockPos pos=context.getClickedPos();var player=context.getPlayer();
        if(drillBlock.type==OreDrillType.ROD){
            BlockPos cluster=checkForOreClusterInNeighbourhood(world,pos);
            if(cluster!=null){
                if(getHasConnectedDrillRod(world,cluster,new HashSet<>(),world.getBlockState(cluster),TGOreDrill.BLOCKS.get(OreDrillType.ROD).get().defaultBlockState())){
                    if(world.isClientSide&&player!=null)player.displayClientMessage(Component.translatable("techguns.msg.error.alreadyrod"),true);
                    return InteractionResult.FAIL;
                }
            }else if(!checkForRodInNeighbourhood(world,pos)){
                if(player!=null)player.displayClientMessage(Component.translatable("techguns.msg.error.rodplacement"),true);
                return InteractionResult.FAIL;
            }
        }else if(drillBlock.type==OreDrillType.CONTROLLER&&checkForControllerInNeighbourhood(world,pos)){
            if(world.isClientSide&&player!=null)player.displayClientMessage(Component.translatable("techguns.msg.error.oredrill.controllernext"),true);
            return InteractionResult.FAIL;
        }
        var result=super.place(context);
        if(result.consumesAction()&&!world.isClientSide&&player!=null&&world.getBlockEntity(pos) instanceof OreDrillTileEntMaster master)master.setOwner(player);
        return result;
    }
    public static BlockPos checkForOreClusterInNeighbourhood(Level w,BlockPos p){for(Direction f:Direction.values())if(OreDrillDefinition.isOreCluster(w,p.relative(f)))return p.relative(f);return null;}
    static boolean checkForRodInNeighbourhood(Level w,BlockPos p){var rod=TGOreDrill.BLOCKS.get(OreDrillType.ROD).get().defaultBlockState();for(Direction f:Direction.values())if(w.getBlockState(p.relative(f))==rod)return true;return false;}
    /** Original quirk kept: the first neighbouring drill block decides, controller or not. */
    static boolean checkForControllerInNeighbourhood(Level w,BlockPos p){
        for(Direction f:Direction.values()){var bs=w.getBlockState(p.relative(f));if(bs.getBlock() instanceof OreDrillBlock block)return block.type==OreDrillType.CONTROLLER;}
        return false;
    }
    /** Original recursion order: down, up, north, south, west, east; any face-connected cluster block touching an unformed rod. */
    static boolean getHasConnectedDrillRod(Level world,BlockPos p,Set<BlockPos> visited,BlockState clusterblock,BlockState rod){
        BlockState target=world.getBlockState(p);
        if(visited.contains(p))return false;
        if(target==clusterblock){
            visited.add(p);
            boolean b=false;
            for(Direction d:new Direction[]{Direction.DOWN,Direction.UP,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST})b|=getHasConnectedDrillRod(world,p.relative(d),visited,clusterblock,rod);
            return b;
        }
        return target==rod;
    }
}
