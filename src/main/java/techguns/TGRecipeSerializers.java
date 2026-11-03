package techguns;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.recipes.AmmoSwitchRecipe;

public final class TGRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Techguns.MODID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AmmoSwitchRecipe>> AMMO_SWITCH = SERIALIZERS.register("ammo_change_crafting", AmmoSwitchRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<techguns.recipes.CopyNbtRecipe>> COPY_NBT = SERIALIZERS.register("copy_nbt", techguns.recipes.CopyNbtRecipe.Serializer::new);
    private TGRecipeSerializers() {}
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<techguns.recipes.AmmoSumRecipe>> AMMO_SUM=SERIALIZERS.register("ammo_sum_recipe",techguns.recipes.AmmoSumRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<techguns.recipes.MiningHeadRecipe>> MINING_HEAD=SERIALIZERS.register("miningtool_upgrade",techguns.recipes.MiningHeadRecipe.Serializer::new);
}
