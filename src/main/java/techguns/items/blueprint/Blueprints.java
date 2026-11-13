package techguns.items.blueprint;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** Which energy blocks each blueprint unlocks (the matching crafting recipes are techguns:blueprint_shaped). */
public final class Blueprints {
    public static final String GENERATORS = "generators", POWER_PLANTS = "power_plants", REACTORS = "reactors";
    private static final Map<String, List<String>> UNLOCKS = Map.of(
            GENERATORS, List.of("fuel_generator", "liquid_generator", "solar_panel", "geothermal_generator", "wind_turbine"),
            POWER_PLANTS, List.of("solar_panel_advanced", "solar_panel_industrial", "wind_plant", "geothermal_plant"),
            REACTORS, List.of("fission_reactor", "fusion_reactor", "nuclear_generator"));

    public static List<ResourceLocation> unlocks(String blueprint) {
        return UNLOCKS.getOrDefault(blueprint, List.of()).stream().map(path -> new ResourceLocation("techguns", path)).filter(ForgeRegistries.ITEMS::containsKey).toList();
    }
    /** The blueprint an item's recipe needs, or null. */
    public static @Nullable String required(ResourceLocation item) {
        if (!"techguns".equals(item.getNamespace())) return null;
        for (var entry : UNLOCKS.entrySet()) if (entry.getValue().contains(item.getPath())) return entry.getKey();
        return null;
    }
    private Blueprints() {}

    @Mod.EventBusSubscriber(modid = "techguns", value = Dist.CLIENT)
    public static final class Tooltips {
        @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
            var id = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
            String blueprint = id == null ? null : required(id);
            if (blueprint != null) event.getToolTip().add(Component.translatable("techguns.blueprint.required", Component.translatable("item.techguns.blueprint_" + blueprint)).withStyle(ChatFormatting.GOLD));
        }
        private Tooltips() {}
    }
}
