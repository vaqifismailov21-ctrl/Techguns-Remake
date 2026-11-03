package techguns.recipes;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/** Original ore ingredient chooses hardened glass when available, otherwise ordinary glass. */
public final class HardenedGlassIngredient implements ICustomIngredient {
    public static final ResourceLocation ID=ResourceLocation.fromNamespaceAndPath("techguns","hardened_glass_or_glass");
    public static final HardenedGlassIngredient INSTANCE=new HardenedGlassIngredient();
    public static final MapCodec<HardenedGlassIngredient> CODEC=MapCodec.unit(INSTANCE);
    private static final TagKey<Item> HARDENED=TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("c","glass_blocks/hardened"));
    private static final TagKey<Item> GLASS=TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("c","glass_blocks"));
    private static Ingredient selected(){return Ingredient.of(BuiltInRegistries.ITEM.getTagOrEmpty(HARDENED).iterator().hasNext()?HARDENED:GLASS);}
    @Override public boolean test(ItemStack stack){return stack!=null&&selected().test(stack);}
    @Override public Stream<ItemStack> getItems(){return Arrays.stream(selected().getItems());}
    @Override public boolean isSimple(){return false;}
    @Override public IngredientType<?> getType(){return TGIngredients.HARDENED_GLASS.get();}
}
