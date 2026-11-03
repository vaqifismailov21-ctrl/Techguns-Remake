package techguns.power;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Shows the charge kept in the item (techguns:energy component copied by the loot table). */
public final class EnergyCellItem extends BlockItem {
    public EnergyCellItem(Block block){super(block,new Item.Properties().stacksTo(1));}
    public static int stored(ItemStack stack){return stack.getOrDefault(techguns.TGDataComponents.ENERGY.get(),0);}
    @Override public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.translatable("techguns.power.cell_item",stored(stack),PowerBlocks.CELL_CAPACITY));}
    @Override public boolean isBarVisible(ItemStack stack){return stored(stack)>0;}
    @Override public int getBarWidth(ItemStack stack){return Math.round(13f*stored(stack)/PowerBlocks.CELL_CAPACITY);}
    @Override public int getBarColor(ItemStack stack){return 0xE02020;}
}
