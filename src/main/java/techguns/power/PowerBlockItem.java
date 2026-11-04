package techguns.power;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Item of a power block; drawn in hand and inventory with the block's box model. */
public class PowerBlockItem extends BlockItem {
    public PowerBlockItem(Block block){this(block,new Item.Properties());}
    public PowerBlockItem(Block block,Item.Properties properties){super(block,properties);}
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer){
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){return techguns.client.render.item.PowerItemRenderer.get();}
        });
    }
}
