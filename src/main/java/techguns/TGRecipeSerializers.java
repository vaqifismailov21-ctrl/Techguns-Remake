package techguns;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import techguns.recipes.AmmoSwitchRecipe;

public final class TGRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Techguns.MODID);
    public static final RegistryObject<RecipeSerializer<AmmoSwitchRecipe>> AMMO_SWITCH = SERIALIZERS.register("ammo_change_crafting", AmmoSwitchRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<techguns.recipes.CopyNbtRecipe>> COPY_NBT = SERIALIZERS.register("copy_nbt", techguns.recipes.CopyNbtRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<techguns.recipes.BlueprintShapedRecipe>> BLUEPRINT_SHAPED = SERIALIZERS.register("blueprint_shaped", techguns.recipes.BlueprintShapedRecipe.Serializer::new);
    private TGRecipeSerializers() {}
    public static final RegistryObject<RecipeSerializer<techguns.recipes.AmmoSumRecipe>> AMMO_SUM=SERIALIZERS.register("ammo_sum_recipe",techguns.recipes.AmmoSumRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<techguns.recipes.MiningHeadRecipe>> MINING_HEAD=SERIALIZERS.register("miningtool_upgrade",techguns.recipes.MiningHeadRecipe.Serializer::new);
}
