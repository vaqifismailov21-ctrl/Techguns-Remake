package techguns;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.*;
import techguns.machines.*;
public final class TGMachines {
    public static final Map<MachineKind,RegistryObject<MachineBlock>> BLOCKS=new EnumMap<>(MachineKind.class);
    public static final Map<MachineKind,RegistryObject<Item>> ITEMS=new EnumMap<>(MachineKind.class);
    static {for(var kind:MachineKind.values()){var block=TGWeaponBlocks.BLOCKS.register(kind.id,()->new MachineBlock(kind));BLOCKS.put(kind,block);ITEMS.put(kind,TGItems.ITEMS.register(kind.id,()->new MachineBlockItem(block.get())));}}
    public static final RegistryObject<BlockEntityType<ProcessingMachine>> ENTITY=TGWeaponBlocks.BLOCK_ENTITIES.register("processing_machine",()->BlockEntityType.Builder.of(ProcessingMachine::new,BLOCKS.values().stream().map(RegistryObject::get).toArray(net.minecraft.world.level.block.Block[]::new)).build(null));
    public static void init(){}private TGMachines(){}
}
