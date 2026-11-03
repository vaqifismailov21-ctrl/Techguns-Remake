package techguns.recipes;
import com.google.gson.*;
import net.minecraft.core.registries.*;
import net.minecraft.tags.TagKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.*;
/** Original ore ingredient chooses hardened glass when available, otherwise ordinary glass. */
public final class HardenedGlassIngredient extends AbstractIngredient {
    public static final ResourceLocation ID=ResourceLocation.fromNamespaceAndPath("techguns","hardened_glass_or_glass");
    public static final Serializer SERIALIZER=new Serializer();
    private static final TagKey<Item> HARDENED=TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("forge","glass/hardened"));
    private static final TagKey<Item> GLASS=TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("forge","glass"));
    private static Ingredient selected(){return Ingredient.of(BuiltInRegistries.ITEM.getTagOrEmpty(HARDENED).iterator().hasNext()?HARDENED:GLASS);}
    @Override public boolean test(ItemStack stack){return stack!=null&&selected().test(stack);}
    @Override public ItemStack[] getItems(){return selected().getItems();}
    @Override public boolean isSimple(){return false;}
    @Override public IIngredientSerializer<? extends Ingredient> getSerializer(){return SERIALIZER;}
    @Override public JsonElement toJson(){var json=new JsonObject();json.addProperty("type",ID.toString());return json;}
    public static final class Serializer implements IIngredientSerializer<HardenedGlassIngredient> {
        @Override public HardenedGlassIngredient parse(FriendlyByteBuf b){return new HardenedGlassIngredient();}
        @Override public HardenedGlassIngredient parse(JsonObject json){return new HardenedGlassIngredient();}
        @Override public void write(FriendlyByteBuf b,HardenedGlassIngredient ingredient){}
    }
}
