package techguns.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import com.mojang.serialization.MapCodec;
import techguns.TGRecipeSerializers;
import techguns.items.guns.GenericGun;
import techguns.util.TGItemNbt;

/** Original shapeless ammunition switch, preserving the weapon's complete independent data. */
public final class AmmoSwitchRecipe extends ShapelessRecipe {
    private AmmoSwitchRecipe(ShapelessRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.getResultItem(RegistryAccess.EMPTY), recipe.getIngredients());
    }
    @Override public RecipeSerializer<?> getSerializer() { return TGRecipeSerializers.AMMO_SWITCH.get(); }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack weapon = ItemStack.EMPTY, ammunition = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            var stack = input.getItem(slot);
            if (stack.getItem() instanceof GenericGun) weapon = stack;
            else if (!stack.isEmpty()) ammunition = stack;
        }
        if (!(weapon.getItem() instanceof GenericGun gun) || ammunition.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = super.assemble(input, registries);
        result.applyComponents(weapon.getComponentsPatch());
        gun.setAmmo(result, gun.getAmmoCount()>1?1:gun.clipsize);
        final ItemStack ammo = ammunition;
        TGItemNbt.update(result, tag -> tag.putString("ammovariant", gun.getAmmoType().getAmmoVariantKeyfor(ammo, 0)));
        return result;
    }
    public static final class Serializer implements RecipeSerializer<AmmoSwitchRecipe> {
        private final ShapelessRecipe.Serializer delegate = new ShapelessRecipe.Serializer();
        private final MapCodec<AmmoSwitchRecipe> codec = delegate.codec().xmap(AmmoSwitchRecipe::new, r -> r);
        private final StreamCodec<RegistryFriendlyByteBuf, AmmoSwitchRecipe> stream = delegate.streamCodec().map(AmmoSwitchRecipe::new, r -> r);
        @Override public MapCodec<AmmoSwitchRecipe> codec() { return codec; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, AmmoSwitchRecipe> streamCodec() { return stream; }
    }
}
