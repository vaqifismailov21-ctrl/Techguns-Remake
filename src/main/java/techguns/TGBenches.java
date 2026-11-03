package techguns;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.*;
import techguns.machines.*;
public final class TGBenches {
    public static final Map<BenchKind,RegistryObject<BenchBlock>> BLOCKS=new EnumMap<>(BenchKind.class);
    public static final Map<BenchKind,RegistryObject<Item>> ITEMS=new EnumMap<>(BenchKind.class);
    static {for(var kind:BenchKind.values()){var block=TGWeaponBlocks.BLOCKS.register(kind.id,()->new BenchBlock(kind));BLOCKS.put(kind,block);ITEMS.put(kind,TGItems.ITEMS.register(kind.id,()->new BlockItem(block.get(),new Item.Properties())));}}
    public static final RegistryObject<BlockEntityType<BenchEntity>> ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("workbench",()->BlockEntityType.Builder.of(BenchEntity::new,BLOCKS.values().stream().map(RegistryObject::get).toArray(net.minecraft.world.level.block.Block[]::new)).build(null));
    public static void init(){}private TGBenches(){}
}
