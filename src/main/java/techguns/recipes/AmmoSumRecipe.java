package techguns.recipes;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import techguns.TGRecipeSerializers;
import techguns.items.guns.GenericGun;
/** Four missile launchers contribute their remaining missiles to the original Grim Reaper recipe. */
public final class AmmoSumRecipe extends ShapedRecipe {
    private AmmoSumRecipe(ShapedRecipe source){super(source.getId(),source.getGroup(),source.category(),source.getWidth(),source.getHeight(),source.getIngredients(),source.getResultItem(RegistryAccess.EMPTY),source.showNotification());}
    @Override public ItemStack assemble(CraftingContainer input,RegistryAccess registries) {
        int total=0;for(int i=0;i<input.getContainerSize();i++){var stack=input.getItem(i);if(stack.getItem() instanceof GenericGun gun)total+=gun.getCurrentAmmo(stack);}
        var result=super.assemble(input,registries);if(result.getItem() instanceof GenericGun gun)gun.setAmmo(result,total);return result;
    }
    @Override public RecipeSerializer<?> getSerializer(){return TGRecipeSerializers.AMMO_SUM.get();}
    public static final class Serializer implements RecipeSerializer<AmmoSumRecipe> {
        private final ShapedRecipe.Serializer delegate=new ShapedRecipe.Serializer();
        @Override public AmmoSumRecipe fromJson(ResourceLocation id,JsonObject json){return new AmmoSumRecipe(delegate.fromJson(id,json));}
        @Override public AmmoSumRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf b){return new AmmoSumRecipe(delegate.fromNetwork(id,b));}
        @Override public void toNetwork(FriendlyByteBuf b,AmmoSumRecipe recipe){delegate.toNetwork(b,recipe);}
    }
}
