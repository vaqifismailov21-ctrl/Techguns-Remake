package techguns.power;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Item of a power block; drawn in hand and inventory with the block's box model. */
public class PowerBlockItem extends BlockItem {
    private final boolean boxModel;
    public PowerBlockItem(Block block){this(block,new Item.Properties());}
    public PowerBlockItem(Block block,Item.Properties properties){this(block,properties,true);}
    public PowerBlockItem(Block block,Item.Properties properties,boolean boxModel){super(block,properties);this.boxModel=boxModel;}
    /** One grey line with what the block does (techguns.power.tooltip.&lt;id&gt;). */
    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,net.minecraft.world.level.Level level,java.util.List<net.minecraft.network.chat.Component> tooltip,net.minecraft.world.item.TooltipFlag flag){
        tooltip.add(net.minecraft.network.chat.Component.translatable("techguns.power.tooltip."+net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(getBlock()).getPath()).withStyle(net.minecraft.ChatFormatting.GRAY));
    }
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer){
        if(boxModel)consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){return techguns.client.render.item.PowerItemRenderer.get();}
        });
    }
}
