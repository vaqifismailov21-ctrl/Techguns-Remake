package techguns.items;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import techguns.TGDecorations;
import techguns.items.armors.ICamoChangeable;
public final class DecorBlockItem extends BlockItem implements ICamoChangeable {
    public final String group;public final int variant;
    public DecorBlockItem(Block block,String group,int variant){super(block,new Item.Properties());this.group=group;this.variant=variant;}
    private List<String> variants(){return TGDecorations.CAMOS.get(group);}
    @Override public int getCamoCount(){return variants().size();}
    @Override public int getCurrentCamoIndex(ItemStack stack){return variants().indexOf(BuiltInRegistries.ITEM.getKey(this).getPath());}
    @Override public Component getCurrentCamoName(ItemStack stack){return stack.getHoverName();}
    @Override public ItemStack cycleStack(ItemStack stack,boolean backwards){var ids=variants();String next=ids.get(Math.floorMod(getCurrentCamoIndex(stack)+(backwards?-1:1),ids.size()));var output=new ItemStack(TGDecorations.ITEMS.get(next).get(),stack.getCount());if(techguns.util.TGItemNbt.has(stack))output.applyComponents(stack.getComponentsPatch());return output;}
}
