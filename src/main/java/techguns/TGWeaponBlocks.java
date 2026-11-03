package techguns;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.*;
import techguns.blocks.BioBlobBlock;
import techguns.tileentities.BioBlobBlockEntity;
/** Blocks required by weapon mechanics; unrelated machines and world generation are a later stage. */
public final class TGWeaponBlocks {
    public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(Registries.BLOCK,Techguns.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,Techguns.MODID);
    public static final DeferredHolder<net.minecraft.world.level.block.Block, BioBlobBlock> BIOBLOB=BLOCKS.register("bioblob",BioBlobBlock::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BioBlobBlockEntity>> BIOBLOB_ENTITY=BLOCK_ENTITIES.register("bioblob",()->BlockEntityType.Builder.of(BioBlobBlockEntity::new,BIOBLOB.get()).build(null));
    private TGWeaponBlocks() {}
}
