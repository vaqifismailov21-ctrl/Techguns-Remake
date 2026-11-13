package techguns.recipes;

import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import techguns.TGRecipeSerializers;
import techguns.items.blueprint.BlueprintKnowledge;

/**
 * A shaped crafting recipe that only works for a player who has studied the named blueprint.
 * The crafting result is decided on the server, so an unknowing player simply gets no result.
 * Crafting without a player behind it (automatic crafters) is not limited.
 */
public final class BlueprintShapedRecipe extends ShapedRecipe {
    public final String blueprint;
    public BlueprintShapedRecipe(ShapedRecipe base, String blueprint) {
        super(base.getId(), base.getGroup(), base.category(), base.getWidth(), base.getHeight(), base.getIngredients(), base.getResultItem(RegistryAccess.EMPTY), base.showNotification());
        this.blueprint = blueprint;
    }
    @Override public boolean matches(CraftingContainer container, Level level) {
        if (!super.matches(container, level)) return false;
        if (level.isClientSide) return true;
        Player player = crafter(container);
        return player == null || player.getAbilities().instabuild || BlueprintKnowledge.knows(level, player.getUUID(), blueprint);
    }
    @Override public RecipeSerializer<?> getSerializer() { return TGRecipeSerializers.BLUEPRINT_SHAPED.get(); }

    private static final Map<Class<?>, Field> FIELDS = new ConcurrentHashMap<>();
    /** The player at the crafting grid: the grid's menu and that menu's player, found by field type (names differ between mappings). */
    static @Nullable Player crafter(CraftingContainer container) {
        Player forge = ForgeHooks.getCraftingPlayer();
        if (forge != null) return forge;
        Object menu = read(container, AbstractContainerMenu.class);
        return menu == null ? null : (Player) read(menu, Player.class);
    }
    private static @Nullable Object read(Object owner, Class<?> type) {
        Field field = FIELDS.computeIfAbsent(owner.getClass(), c -> {
            for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass())
                for (Field f : k.getDeclaredFields()) if (type.isAssignableFrom(f.getType()) && !java.lang.reflect.Modifier.isStatic(f.getModifiers())) { f.setAccessible(true); return f; }
            return NONE;
        });
        if (field == NONE) return null;
        try { return field.get(owner); } catch (IllegalAccessException e) { return null; }
    }
    private static final Field NONE;
    static { try { NONE = BlueprintShapedRecipe.class.getDeclaredField("blueprint"); } catch (NoSuchFieldException e) { throw new ExceptionInInitializerError(e); } }

    public static final class Serializer implements RecipeSerializer<BlueprintShapedRecipe> {
        @Override public BlueprintShapedRecipe fromJson(ResourceLocation id, JsonObject json) { return new BlueprintShapedRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(id, json), GsonHelper.getAsString(json, "blueprint")); }
        @Override public BlueprintShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) { String blueprint = buffer.readUtf(); return new BlueprintShapedRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buffer), blueprint); }
        @Override public void toNetwork(FriendlyByteBuf buffer, BlueprintShapedRecipe recipe) { buffer.writeUtf(recipe.blueprint); RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe); }
    }
}
