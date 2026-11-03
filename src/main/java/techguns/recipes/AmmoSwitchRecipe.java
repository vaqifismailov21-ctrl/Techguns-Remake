package techguns.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import techguns.TGRecipeSerializers;
import techguns.items.guns.GenericGun;

/** Original shapeless ammunition switch, preserving the weapon's complete independent NBT. */
public final class AmmoSwitchRecipe extends ShapelessRecipe {
    private AmmoSwitchRecipe(ShapelessRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.getResultItem(), recipe.getIngredients());
    }
    @Override public RecipeSerializer<?> getSerializer() { return TGRecipeSerializers.AMMO_SWITCH.get(); }
    @Override public ItemStack assemble(CraftingContainer input) {
        ItemStack weapon = ItemStack.EMPTY, ammunition = ItemStack.EMPTY;
        for (int slot = 0; slot < input.getContainerSize(); slot++) {
            var stack = input.getItem(slot);
            if (stack.getItem() instanceof GenericGun) weapon = stack;
            else if (!stack.isEmpty()) ammunition = stack;
        }
        if (!(weapon.getItem() instanceof GenericGun gun) || ammunition.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = super.assemble(input);
        if (weapon.hasTag()) result.setTag(weapon.getTag().copy());
        gun.setAmmo(result, gun.getAmmoCount()>1?1:gun.clipsize);
        result.getOrCreateTag().putString("ammovariant", gun.getAmmoType().getAmmoVariantKeyfor(ammunition, 0));
        return result;
    }
    public static final class Serializer implements RecipeSerializer<AmmoSwitchRecipe> {
        private final ShapelessRecipe.Serializer delegate = new ShapelessRecipe.Serializer();
        @Override public AmmoSwitchRecipe fromJson(ResourceLocation id, JsonObject json) { return new AmmoSwitchRecipe(delegate.fromJson(id, json)); }
        @Override public AmmoSwitchRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) { return new AmmoSwitchRecipe(delegate.fromNetwork(id, buffer)); }
        @Override public void toNetwork(FriendlyByteBuf buffer, AmmoSwitchRecipe recipe) { delegate.toNetwork(buffer, recipe); }
    }
}
