package techguns;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.damagesystem.ShieldStats;
import techguns.items.armors.GenericShield;

/** Shield registrations from TGArmors; wearable armor registrations are still pending. */
public final class TGArmors {
    public static final Map<String, DeferredHolder<net.minecraft.world.item.Item, GenericShield>> SHIELDS = new LinkedHashMap<>();
    public static final DeferredHolder<net.minecraft.world.item.Item, GenericShield> riot_shield = shield("riot_shield", 1200, 4, "ingots/steel", ShieldStats.RIOT_SHIELD_STATS);
    public static final DeferredHolder<net.minecraft.world.item.Item, GenericShield> ballistic_shield = shield("ballistic_shield", 2400, 4, "plates/obsidian_steel", ShieldStats.BALLISTIC_SHIELD_STATS);
    public static final DeferredHolder<net.minecraft.world.item.Item, GenericShield> advanced_shield = shield("advanced_shield", 3200, 3, "plates/carbon", ShieldStats.ADVANCED_SHIELD_STATS);

    private static DeferredHolder<net.minecraft.world.item.Item, GenericShield> shield(String id, int durability, int camos, String tag, ShieldStats stats) {
        var item = TGItems.ITEMS.register(id, () -> new GenericShield(id, durability, camos,
                ResourceLocation.fromNamespaceAndPath("c", tag), 3, stats));
        SHIELDS.put(id, item);
        return item;
    }
    public static void init() {}
    public static void setup() {
        ShieldStats.SHIELD_STATS.put(net.minecraft.world.item.Items.SHIELD, ShieldStats.VANILLA_SHIELD);
        SHIELDS.values().forEach(item -> ShieldStats.SHIELD_STATS.put(item.get(), item.get().getShieldStats()));
    }
}
