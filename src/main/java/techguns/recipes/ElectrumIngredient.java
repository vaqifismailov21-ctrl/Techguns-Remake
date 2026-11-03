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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/** Source ingredient requires electrum when present, falling back to gold in standalone Techguns. */
public final class ElectrumIngredient implements ICustomIngredient {
    public static final ResourceLocation ID=ResourceLocation.fromNamespaceAndPath("techguns","electrum_or_gold");
    public static final ElectrumIngredient INSTANCE=new ElectrumIngredient();
    public static final MapCodec<ElectrumIngredient> CODEC=MapCodec.unit(INSTANCE);
    private static final TagKey<Item> ELECTRUM=TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("c","ingots/electrum"));
    private static Ingredient selected(){return BuiltInRegistries.ITEM.getTagOrEmpty(ELECTRUM).iterator().hasNext()?Ingredient.of(ELECTRUM):Ingredient.of(Items.GOLD_INGOT);}
    @Override public boolean test(ItemStack stack){return stack!=null&&selected().test(stack);}
    @Override public Stream<ItemStack> getItems(){return Arrays.stream(selected().getItems());}
    @Override public boolean isSimple(){return false;}
    @Override public IngredientType<?> getType(){return TGIngredients.ELECTRUM.get();}
}
