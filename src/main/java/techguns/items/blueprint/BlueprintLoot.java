package techguns.items.blueprint;

import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import techguns.TGItems;

/**
 * Where blueprints are found: the cybernetics in nether fortress chests; the energy blueprints in Techguns structure chests,
 * the simple generators in towns and camps, the power plants in factories and bases, the reactors in the Nether and the End.
 */
@Mod.EventBusSubscriber(modid = "techguns")
public final class BlueprintLoot {
    private static final ResourceLocation NETHER_BRIDGE = new ResourceLocation("minecraft", "chests/nether_bridge");
    private record Drop(RegistryObject<? extends Item> item, float chance) {}
    private static final Map<String, Drop[]> CHESTS = Map.ofEntries(
            Map.entry("gasstation", generators(0.11F)), Map.entry("survivor_hideout", generators(0.14F)), Map.entry("small_trainstation", generators(0.11F)),
            Map.entry("policestation", generators(0.08F)), Map.entry("train_green", generators(0.08F)), Map.entry("train_orange", generators(0.08F)),
            Map.entry("train_red", generators(0.08F)), Map.entry("train_head", new Drop[]{new Drop(TGItems.BLUEPRINT_GENERATORS, 0.14F), new Drop(TGItems.BLUEPRINT_POWER_PLANTS, 0.06F)}),
            Map.entry("factory_building", new Drop[]{new Drop(TGItems.BLUEPRINT_GENERATORS, 0.14F), new Drop(TGItems.BLUEPRINT_POWER_PLANTS, 0.08F)}),
            Map.entry("factory_building_big", new Drop[]{new Drop(TGItems.BLUEPRINT_POWER_PLANTS, 0.17F), new Drop(TGItems.BLUEPRINT_REACTORS, 0.03F)}),
            Map.entry("militarybase_barracks", plants(0.07F)), Map.entry("militarybase_bunker", new Drop[]{new Drop(TGItems.BLUEPRINT_POWER_PLANTS, 0.11F), new Drop(TGItems.BLUEPRINT_REACTORS, 0.04F)}),
            Map.entry("aircraftcarrier", new Drop[]{new Drop(TGItems.BLUEPRINT_POWER_PLANTS, 0.11F), new Drop(TGItems.BLUEPRINT_REACTORS, 0.06F)}),
            Map.entry("castle", plants(0.08F)),
            Map.entry("nether_building_common", reactors(0.07F)), Map.entry("nether_submarine_common", reactors(0.06F)), Map.entry("nether_submarine_rare", reactors(0.17F)),
            Map.entry("nether_cluster_secret", reactors(0.19F)), Map.entry("end_building", reactors(0.14F)), Map.entry("end_floating_ship", reactors(0.22F)));
    private static Drop[] generators(float chance) { return new Drop[]{new Drop(TGItems.BLUEPRINT_GENERATORS, chance)}; }
    private static Drop[] plants(float chance) { return new Drop[]{new Drop(TGItems.BLUEPRINT_POWER_PLANTS, chance)}; }
    private static Drop[] reactors(float chance) { return new Drop[]{new Drop(TGItems.BLUEPRINT_REACTORS, chance)}; }
    /** Chest tables that get energy blueprints. */
    public static Set<String> chests() { return CHESTS.keySet(); }

    private BlueprintLoot() {}

    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent event) {
        var name = event.getName();
        if (NETHER_BRIDGE.equals(name)) {
            event.getTable().addPool(LootPool.lootPool().name("techguns_blueprints")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(TGItems.BLUEPRINTS.get()))
                    .when(LootItemRandomChanceCondition.randomChance(0.25F))
                    .build());
            return;
        }
        if (!"techguns".equals(name.getNamespace()) || !name.getPath().startsWith("chests/")) return;
        var drops = CHESTS.get(name.getPath().substring("chests/".length()));
        if (drops == null) return;
        for (var drop : drops)
            event.getTable().addPool(LootPool.lootPool().name("techguns_" + drop.item().getId().getPath())
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(drop.item().get()))
                    .when(LootItemRandomChanceCondition.randomChance(drop.chance()))
                    .build());
    }
}
