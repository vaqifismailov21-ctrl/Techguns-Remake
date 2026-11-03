package techguns.recipes;
import com.google.gson.*;
import net.minecraft.core.registries.*;
import net.minecraft.tags.TagKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.*;
/** Source ingredient requires electrum when present, falling back to gold in standalone Techguns. */
public final class ElectrumIngredient extends AbstractIngredient {
    public static final ResourceLocation ID=ResourceLocation.fromNamespaceAndPath("techguns","electrum_or_gold");
    public static final Serializer SERIALIZER=new Serializer();
    private static final TagKey<Item> ELECTRUM=TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("forge","ingots/electrum"));
    private static Ingredient selected(){return BuiltInRegistries.ITEM.getTagOrEmpty(ELECTRUM).iterator().hasNext()?Ingredient.of(ELECTRUM):Ingredient.of(Items.GOLD_INGOT);}
    @Override public boolean test(ItemStack stack){return stack!=null&&selected().test(stack);}@Override public ItemStack[] getItems(){return selected().getItems();}@Override public boolean isSimple(){return false;}@Override public IIngredientSerializer<? extends Ingredient> getSerializer(){return SERIALIZER;}
    @Override public JsonElement toJson(){var json=new JsonObject();json.addProperty("type",ID.toString());return json;}
    public static final class Serializer implements IIngredientSerializer<ElectrumIngredient>{@Override public ElectrumIngredient parse(FriendlyByteBuf buffer){return new ElectrumIngredient();}@Override public ElectrumIngredient parse(JsonObject json){return new ElectrumIngredient();}@Override public void write(FriendlyByteBuf buffer,ElectrumIngredient ingredient){}}
}
