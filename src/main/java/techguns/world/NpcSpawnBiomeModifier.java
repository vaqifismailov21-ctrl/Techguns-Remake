package techguns.world;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraftforge.common.world.*;
import techguns.*;
import techguns.entities.spawn.TGSpawnManager;
public final class NpcSpawnBiomeModifier implements BiomeModifier {
    public static final Codec<NpcSpawnBiomeModifier> CODEC=Codec.unit(NpcSpawnBiomeModifier::new);
    @Override public Codec<? extends BiomeModifier> codec(){return CODEC;}
    @Override public void modify(Holder<Biome> biome,Phase phase,ModifiableBiomeInfo.BiomeInfo.Builder builder){
        if(phase!=Phase.ADD||TGSpawnManager.blacklisted(biome))return;
        // the End: only on the outer islands, the dragon fight stays as it is
        if(biome.is(BiomeTags.IS_END)&&biome.is(net.minecraft.world.level.biome.Biomes.THE_END))return;
        int weight=biome.is(BiomeTags.IS_END)?TGConfig.spawnWeightTGEnd:biome.is(BiomeTags.IS_NETHER)?TGConfig.spawnWeightTGNether:TGConfig.spawnWeightTGOverworld;
        if(weight>0)builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,new MobSpawnSettings.SpawnerData(TGWorld.DUMMY.get(),weight,1,3));
    }
}
