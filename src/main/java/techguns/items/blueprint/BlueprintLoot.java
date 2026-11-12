package techguns.items.blueprint;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.TGItems;

/** Nether fortress chests can hold the cybernetics blueprints. */
@Mod.EventBusSubscriber(modid = "techguns")
public final class BlueprintLoot {
    private static final ResourceLocation NETHER_BRIDGE = new ResourceLocation("minecraft", "chests/nether_bridge");

    private BlueprintLoot() {}

    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent event) {
        if (!NETHER_BRIDGE.equals(event.getName())) return;
        event.getTable().addPool(LootPool.lootPool().name("techguns_blueprints")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(TGItems.BLUEPRINTS.get()))
                .when(LootItemRandomChanceCondition.randomChance(0.25F))
                .build());
    }
}
