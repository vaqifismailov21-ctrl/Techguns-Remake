package techguns.world.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.world.ModifiableStructureInfo;
import net.minecraftforge.common.world.StructureModifier;
import techguns.TGWorld;

/**
 * Structures the current config can never generate get no biomes at server start, so the generator never plans them and
 * /locate answers "not found" at once instead of searching every grid site (which froze the server for a long time, e.g.
 * desert_oil_cluster without a configured oil fluid).
 */
public final class ConfigStructureModifier implements StructureModifier {
    public static final ConfigStructureModifier INSTANCE=new ConfigStructureModifier();
    public static final Codec<ConfigStructureModifier> CODEC=Codec.unit(INSTANCE);
    @Override public void modify(Holder<Structure> structure,Phase phase,ModifiableStructureInfo.StructureInfo.Builder builder){
        if(phase==Phase.REMOVE&&structure.value() instanceof LegacySurfaceStructure legacy&&!legacy.canEverGenerate())builder.getStructureSettings().setBiomes(HolderSet.direct());
    }
    @Override public Codec<? extends StructureModifier> codec(){return TGWorld.CONFIG_STRUCTURE_MODIFIER.get();}
}
