package techguns.items;
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
    @Override public int getCurrentCamoIndex(ItemStack stack){return variants().indexOf(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(this).getPath());}
    @Override public Component getCurrentCamoName(ItemStack stack){return stack.getHoverName();}
    @Override public void appendHoverText(ItemStack stack,@javax.annotation.Nullable net.minecraft.world.level.Level level,java.util.List<Component> tooltip,TooltipFlag flag){super.appendHoverText(stack,level,tooltip,flag);if(group.equals("windowframe"))tooltip.add(Component.translatable("techguns.tooltip.windowframe").withStyle(net.minecraft.ChatFormatting.GRAY));}
    @Override public ItemStack cycleStack(ItemStack stack,boolean backwards){var ids=variants();String next=ids.get(Math.floorMod(getCurrentCamoIndex(stack)+(backwards?-1:1),ids.size()));var output=new ItemStack(TGDecorations.ITEMS.get(next).get(),stack.getCount());if(stack.hasTag())output.setTag(stack.getTag().copy());return output;}
}
