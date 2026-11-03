package techguns.world;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.neoforged.neoforge.common.world.*;
import techguns.*;
import techguns.entities.spawn.TGSpawnManager;
public final class NpcSpawnBiomeModifier implements BiomeModifier {
    public static final com.mojang.serialization.MapCodec<NpcSpawnBiomeModifier> CODEC=com.mojang.serialization.MapCodec.unit(NpcSpawnBiomeModifier::new);
    @Override public com.mojang.serialization.MapCodec<? extends BiomeModifier> codec(){return CODEC;}
    @Override public void modify(Holder<Biome> biome,Phase phase,ModifiableBiomeInfo.BiomeInfo.Builder builder){
        if(phase!=Phase.ADD||biome.is(BiomeTags.IS_END)||TGSpawnManager.blacklisted(biome))return;
        int weight=biome.is(BiomeTags.IS_NETHER)?TGConfig.spawnWeightTGNether:TGConfig.spawnWeightTGOverworld;
        if(weight>0)builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,new MobSpawnSettings.SpawnerData(TGWorld.DUMMY.get(),weight,1,3));
    }
}
