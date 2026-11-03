package techguns.radiation;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import techguns.Techguns;
import techguns.TGRadiationSystem;

@EventBusSubscriber(modid = Techguns.MODID)
public final class RadiationTagEvents {
    @SubscribeEvent public static void tags(TagsUpdatedEvent event) {
        if (!TGRadiationSystem.isEnabled()) return;
        for (String name : new String[]{"ores/uranium", "ingots/uranium"}) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name));
            for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) { Item item = holder.value();
                if (ItemRadiationRegistry.getRadiationDataFor(item, (short) 0) == null) ItemRadiationRegistry.addRadiationData(item, 0, 1, 100);
            }
        }
    }
    private RadiationTagEvents() {}
}
