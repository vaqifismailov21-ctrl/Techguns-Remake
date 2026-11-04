package techguns.power;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Shows the charge kept in the item (block entity data copied by the loot table). Used by the energy cell and the energy bank. */
public final class EnergyCellItem extends PowerBlockItem {
    public final int capacity;
    public EnergyCellItem(Block block,int capacity){super(block,new Item.Properties().stacksTo(1));this.capacity=capacity;}
    public static int stored(ItemStack stack){var tag=stack.getTagElement("BlockEntityTag");return tag==null?0:tag.getInt("Energy");}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.translatable("techguns.power.cell_item",stored(stack),capacity));}
    @Override public boolean isBarVisible(ItemStack stack){return stored(stack)>0;}
    @Override public int getBarWidth(ItemStack stack){return Math.round(13f*Math.min(stored(stack),capacity)/capacity);}
    @Override public int getBarColor(ItemStack stack){return 0xE02020;}
}
