package techguns.radiation;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import techguns.Techguns;

/** Metadata variants are now separate items; durability remains available for API callers. */
public final class ItemRadiationRegistry {
    private static final Map<Item, Map<Short, ItemRadiationData>> REGISTRY = new HashMap<>();
    public static void addRadiationData(ItemStack stack, int amount, int duration) {
        addRadiationData(stack.getItem(), stack.getDamageValue(), amount, duration);
    }
    public static void addRadiationData(Item item, int damage, int amount, int duration) {
        var entry = REGISTRY.computeIfAbsent(item, ignored -> new HashMap<>());
        if (entry.putIfAbsent((short) damage, new ItemRadiationData((short) amount, (short) duration)) != null) {
            Techguns.LOGGER.warn("Radiation data already exists for {} with damage {}", item, damage);
        }
    }
    public static ItemRadiationData getRadiationDataFor(ItemStack stack) {
        return stack.isEmpty() ? null : getRadiationDataFor(stack.getItem(), (short) stack.getDamageValue());
    }
    public static ItemRadiationData getRadiationDataFor(Item item, short damage) {
        var entry = REGISTRY.get(item);
        return entry == null ? null : entry.get(damage);
    }
    private ItemRadiationRegistry() {}
}
