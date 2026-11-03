package techguns.util;

import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * 1.21.1 helpers that replace ItemStack NBT of 1.20.1.
 * Techguns item data (ammo, ammo variant, camo, mining head, ...) keeps its old keys inside the
 * {@code minecraft:custom_data} component: vanilla moves leftover 1.20.1 item NBT there when an old world
 * is upgraded, so existing guns keep their state.
 */
public final class TGItemNbt {
    /** Read-only copy of the item's Techguns data, or {@code null} when the stack has none (old {@code getTag()}). */
    public static CompoundTag tag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }
    /** Copy of the item's data, empty when absent. Write it back with {@link #set}. */
    public static CompoundTag tagOrEmpty(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }
    /** Old {@code getTagElement(key)}: a compound child of the item data or {@code null}. */
    public static CompoundTag element(ItemStack stack, String key) { CompoundTag t = tag(stack); return t != null && t.contains(key, net.minecraft.nbt.Tag.TAG_COMPOUND) ? t.getCompound(key) : null; }
    public static boolean has(ItemStack stack) { return stack.has(DataComponents.CUSTOM_DATA); }
    public static void set(ItemStack stack, CompoundTag tag) {
        if (tag == null || tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    /** Modify the data in place (old {@code getOrCreateTag().putX(...)}). */
    public static void update(ItemStack stack, Consumer<CompoundTag> change) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, change);
    }
    public static void putByte(ItemStack stack, String key, byte value) { update(stack, t -> t.putByte(key, value)); }
    public static void putShort(ItemStack stack, String key, short value) { update(stack, t -> t.putShort(key, value)); }
    public static void putInt(ItemStack stack, String key, int value) { update(stack, t -> t.putInt(key, value)); }
    public static void putString(ItemStack stack, String key, String value) { update(stack, t -> t.putString(key, value)); }
    public static void putBoolean(ItemStack stack, String key, boolean value) { update(stack, t -> t.putBoolean(key, value)); }
    public static void put(ItemStack stack, String key, net.minecraft.nbt.Tag value) { update(stack, t -> t.put(key, value)); }
    public static int getInt(ItemStack stack, String key) { CompoundTag t = tag(stack); return t == null ? 0 : t.getInt(key); }
    public static short getShort(ItemStack stack, String key) { CompoundTag t = tag(stack); return t == null ? 0 : t.getShort(key); }
    public static byte getByte(ItemStack stack, String key) { CompoundTag t = tag(stack); return t == null ? 0 : t.getByte(key); }
    public static boolean getBoolean(ItemStack stack, String key) { CompoundTag t = tag(stack); return t != null && t.getBoolean(key); }
    public static String getString(ItemStack stack, String key) { CompoundTag t = tag(stack); return t == null ? "" : t.getString(key); }
    public static boolean contains(ItemStack stack, String key) { CompoundTag t = tag(stack); return t != null && t.contains(key); }

    /** ItemStack -> NBT (empty stack gives an empty tag, like 1.20.1 {@code save(new CompoundTag())} with count 0). */
    public static CompoundTag save(ItemStack stack, HolderLookup.Provider provider) {
        return stack.isEmpty() ? new CompoundTag() : (CompoundTag) stack.save(provider);
    }
    public static ItemStack load(HolderLookup.Provider provider, CompoundTag tag) { return load(tag, provider); }
    public static ItemStack load(CompoundTag tag, HolderLookup.Provider provider) {
        return tag == null || tag.isEmpty() ? ItemStack.EMPTY : ItemStack.parseOptional(provider, tag);
    }
    /** Registry access for code paths without a level at hand (server side, or the client level). */
    public static HolderLookup.Provider registries() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) return server.registryAccess();
        return ClientRegistries.get();
    }
    /** Registry holder of a data-driven enchantment (1.21: Enchantments.X is only a key). */
    public static net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment(net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        return registries().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(key);
    }
    private TGItemNbt() {}

    /** Kept in a nested class so dedicated servers never load client classes. */
    static final class ClientRegistries {
        static HolderLookup.Provider get() {
            if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
                var level = net.minecraft.client.Minecraft.getInstance().level;
                if (level != null) return level.registryAccess();
            }
            return net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
        }
    }
}
