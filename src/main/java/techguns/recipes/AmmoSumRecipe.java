package techguns.recipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import techguns.TGRecipeSerializers;
import techguns.items.guns.GenericGun;

/** Four missile launchers contribute their remaining missiles to the original Grim Reaper recipe. */
public final class AmmoSumRecipe extends ShapedRecipe {
    private AmmoSumRecipe(ShapedRecipe source){super(source.getGroup(),source.category(),source.pattern,source.getResultItem(RegistryAccess.EMPTY),source.showNotification());}
    @Override public ItemStack assemble(CraftingInput input,HolderLookup.Provider registries) {
        int total=0;for(int i=0;i<input.size();i++){var stack=input.getItem(i);if(stack.getItem() instanceof GenericGun gun)total+=gun.getCurrentAmmo(stack);}
        var result=super.assemble(input,registries);if(result.getItem() instanceof GenericGun gun)gun.setAmmo(result,total);return result;
    }
    @Override public RecipeSerializer<?> getSerializer(){return TGRecipeSerializers.AMMO_SUM.get();}
    public static final class Serializer implements RecipeSerializer<AmmoSumRecipe> {
        private final ShapedRecipe.Serializer delegate=new ShapedRecipe.Serializer();
        private final MapCodec<AmmoSumRecipe> codec=delegate.codec().xmap(AmmoSumRecipe::new,r->r);
        private final StreamCodec<RegistryFriendlyByteBuf,AmmoSumRecipe> stream=delegate.streamCodec().map(AmmoSumRecipe::new,r->r);
        @Override public MapCodec<AmmoSumRecipe> codec(){return codec;}
        @Override public StreamCodec<RegistryFriendlyByteBuf,AmmoSumRecipe> streamCodec(){return stream;}
    }
}
