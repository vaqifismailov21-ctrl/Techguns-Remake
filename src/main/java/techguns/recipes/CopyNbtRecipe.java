package techguns.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import techguns.TGRecipeSerializers;
import techguns.items.guns.GenericGun;

/** Original ShapedOreRecipeCopyNBTFactory: copy the last gun's whole tag, without reloading it. */
public final class CopyNbtRecipe extends ShapedRecipe {
    private CopyNbtRecipe(ShapedRecipe recipe) {
        super(recipe.getId(),recipe.getGroup(),recipe.category(),recipe.getWidth(),recipe.getHeight(),recipe.getIngredients(),recipe.getResultItem(RegistryAccess.EMPTY),recipe.showNotification());
    }
    @Override public RecipeSerializer<?> getSerializer() { return TGRecipeSerializers.COPY_NBT.get(); }
    @Override public ItemStack assemble(CraftingContainer input,RegistryAccess registries) {
        int source=0;
        for(int i=0;i<input.getContainerSize();i++) if(input.getItem(i).getItem() instanceof GenericGun) source=i;
        var result=super.assemble(input,registries);
        if(input.getItem(source).hasTag()) result.setTag(input.getItem(source).getTag().copy());
        return result;
    }
    public static final class Serializer implements RecipeSerializer<CopyNbtRecipe> {
        private final ShapedRecipe.Serializer delegate=new ShapedRecipe.Serializer();
        @Override public CopyNbtRecipe fromJson(ResourceLocation id,JsonObject json) { return new CopyNbtRecipe(delegate.fromJson(id,json)); }
        @Override public CopyNbtRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf buffer) { return new CopyNbtRecipe(delegate.fromNetwork(id,buffer)); }
        @Override public void toNetwork(FriendlyByteBuf buffer,CopyNbtRecipe recipe) { delegate.toNetwork(buffer,recipe); }
    }
}
