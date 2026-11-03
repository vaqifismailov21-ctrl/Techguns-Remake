package techguns.machines;
import net.minecraft.world.item.*;
public final class MachineBlockItem extends BlockItem {
    public MachineBlockItem(MachineBlock block){super(block,new Properties());}
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer){consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){@Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){return techguns.client.render.item.MachineItemRenderer.get();}});}
}
