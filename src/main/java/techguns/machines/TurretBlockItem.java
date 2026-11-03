package techguns.machines;

import java.util.function.Consumer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import techguns.tileentities.TurretTileEnt;

/** Places the base, then mounts and spawns the turret on the clicked side (bottom face: hanging turret), as the original. */
public final class TurretBlockItem extends BlockItem {
    public TurretBlockItem(Block block){super(block,new Properties());}
    @Override public InteractionResult place(BlockPlaceContext context){
        var result=super.place(context);
        if(result.consumesAction()&&!context.getLevel().isClientSide&&context.getLevel().getBlockEntity(context.getClickedPos()) instanceof TurretTileEnt tile){
            tile.setFacing(TurretBlock.facingFor(context.getClickedFace()));
            if(!tile.turretDeath)tile.spawnTurret(context.getLevel(),context.getClickedPos());
        }
        return result;
    }
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer){
        consumer.accept(new IClientItemExtensions(){@Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){return techguns.client.render.item.TurretItemRenderer.get();}});
    }
}
