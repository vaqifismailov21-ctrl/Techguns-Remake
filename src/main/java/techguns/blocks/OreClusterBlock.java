package techguns.blocks;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import techguns.world.ClusterKind;
public final class OreClusterBlock extends Block {
    public final ClusterKind kind;
    public OreClusterBlock(ClusterKind kind){super(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).strength(-1,3600000).noLootTable());this.kind=kind;}
    @Override public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext world,List<Component> lines,TooltipFlag flag){
        super.appendHoverText(stack,world,lines,flag);
        lines.add(Component.translatable("techguns.orecluster.mininglevel").append(": "+kind.miningLevel()));
        lines.add(Component.translatable("techguns.orecluster.powermult").append(": x"+String.format(java.util.Locale.ROOT,"%.1f",kind.powerMultiplier())));
        lines.add(Component.translatable("techguns.orecluster.amountmult").append(": x"+String.format(java.util.Locale.ROOT,"%.1f",kind.amountMultiplier())));
    }
}
