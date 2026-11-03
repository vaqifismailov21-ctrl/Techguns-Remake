package techguns.recipes;

import java.util.function.Supplier;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import techguns.Techguns;

/** Custom recipe ingredient types (1.21.1: registry instead of CraftingHelper.register). */
public final class TGIngredients {
    public static final DeferredRegister<IngredientType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Techguns.MODID);
    public static final Supplier<IngredientType<HardenedGlassIngredient>> HARDENED_GLASS = TYPES.register("hardened_glass_or_glass", () -> new IngredientType<>(HardenedGlassIngredient.CODEC));
    public static final Supplier<IngredientType<ElectrumIngredient>> ELECTRUM = TYPES.register("electrum_or_gold", () -> new IngredientType<>(ElectrumIngredient.CODEC));
    public static final Supplier<IngredientType<PartialNbtIngredient>> PARTIAL_NBT = TYPES.register("partial_nbt", () -> new IngredientType<>(PartialNbtIngredient.CODEC));
    private TGIngredients() {}
}
