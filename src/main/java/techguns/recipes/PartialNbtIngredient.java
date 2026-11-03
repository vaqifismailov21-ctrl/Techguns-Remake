package techguns.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import techguns.util.TGItemNbt;

/**
 * Replacement for Forge's {@code forge:partial_nbt}: matches an item whose Techguns data (custom_data)
 * contains at least the given keys and values. JSON: {"type":"techguns:partial_nbt","item":"...","nbt":{...} or "{...}"}.
 */
public record PartialNbtIngredient(Item item, CompoundTag nbt) implements ICustomIngredient {
    /** Like Forge's partial_nbt: a JSON object is written out as SNBT and parsed, so that 1 stays an int (JsonOps would make it a double). */
    private static final Codec<CompoundTag> NBT = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
        if (dynamic.getValue() instanceof CompoundTag tag) return com.mojang.serialization.DataResult.success(tag.copy());
        var json = dynamic.convert(com.mojang.serialization.JsonOps.INSTANCE).getValue();
        if (!(json instanceof com.google.gson.JsonElement element)) return CompoundTag.CODEC.parse(dynamic);
        String text = element.isJsonPrimitive() ? element.getAsString() : element.toString();
        try { return com.mojang.serialization.DataResult.success(TagParser.parseTag(text)); }
        catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) { return com.mojang.serialization.DataResult.error(() -> "Invalid partial nbt " + text + ": " + e.getMessage()); }
    }, tag -> new com.mojang.serialization.Dynamic<>(net.minecraft.nbt.NbtOps.INSTANCE, tag));
    public static final MapCodec<PartialNbtIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(PartialNbtIngredient::item),
            NBT.fieldOf("nbt").forGetter(PartialNbtIngredient::nbt)).apply(i, PartialNbtIngredient::new));
    @Override public boolean test(ItemStack stack) {
        if (stack == null || !stack.is(item)) return false;
        CompoundTag data = TGItemNbt.tag(stack);
        return NbtUtils.compareNbt(nbt, data, true);
    }
    @Override public Stream<ItemStack> getItems() {
        ItemStack stack = new ItemStack(item);
        TGItemNbt.set(stack, nbt.copy());
        return Stream.of(stack);
    }
    @Override public boolean isSimple() { return false; }
    @Override public IngredientType<?> getType() { return TGIngredients.PARTIAL_NBT.get(); }
}
