package techguns;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.*;
import techguns.blocks.BioBlobBlock;
import techguns.tileentities.BioBlobBlockEntity;
/** Blocks required by weapon mechanics; unrelated machines and world generation are a later stage. */
public final class TGWeaponBlocks {
    public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,Techguns.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,Techguns.MODID);
    public static final RegistryObject<BioBlobBlock> BIOBLOB=BLOCKS.register("bioblob",BioBlobBlock::new);
    public static final RegistryObject<BlockEntityType<BioBlobBlockEntity>> BIOBLOB_ENTITY=BLOCK_ENTITIES.register("bioblob",()->BlockEntityType.Builder.of(BioBlobBlockEntity::new,BIOBLOB.get()).build(null));
    private TGWeaponBlocks() {}
}
