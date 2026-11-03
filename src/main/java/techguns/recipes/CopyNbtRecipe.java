package techguns.recipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import techguns.TGRecipeSerializers;
import techguns.items.guns.GenericGun;

/** Shaped recipe that carries all data (ammo, camo, enchantments, damage) of the source gun over to the result. */
public final class CopyNbtRecipe extends ShapedRecipe {
    private CopyNbtRecipe(ShapedRecipe recipe) {
        super(recipe.getGroup(),recipe.category(),recipe.pattern,recipe.getResultItem(RegistryAccess.EMPTY),recipe.showNotification());
    }
    @Override public RecipeSerializer<?> getSerializer() { return TGRecipeSerializers.COPY_NBT.get(); }
    @Override public ItemStack assemble(CraftingInput input,HolderLookup.Provider registries) {
        int source=0;
        for(int i=0;i<input.size();i++) if(input.getItem(i).getItem() instanceof GenericGun) source=i;
        var result=super.assemble(input,registries);
        var from=input.getItem(source);
        if(!from.getComponentsPatch().isEmpty()) result.applyComponents(from.getComponentsPatch());
        return result;
    }
    public static final class Serializer implements RecipeSerializer<CopyNbtRecipe> {
        private final ShapedRecipe.Serializer delegate=new ShapedRecipe.Serializer();
        private final MapCodec<CopyNbtRecipe> codec=delegate.codec().xmap(CopyNbtRecipe::new,r->r);
        private final StreamCodec<RegistryFriendlyByteBuf,CopyNbtRecipe> stream=delegate.streamCodec().map(CopyNbtRecipe::new,r->r);
        @Override public MapCodec<CopyNbtRecipe> codec(){return codec;}
        @Override public StreamCodec<RegistryFriendlyByteBuf,CopyNbtRecipe> streamCodec(){return stream;}
    }
}
