package techguns.radiation;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.Techguns;
import techguns.TGRadiationSystem;

@Mod.EventBusSubscriber(modid = Techguns.MODID)
public final class RadiationTagEvents {
    @SubscribeEvent public static void tags(TagsUpdatedEvent event) {
        if (!TGRadiationSystem.isEnabled()) return;
        // ore 1, ingot 2, block 5 radiation per check
        String[] names = {"ores/uranium", "ingots/uranium", "storage_blocks/uranium"};int[] levels = {1, 2, 5};
        for (int i = 0; i < names.length; i++) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", names[i]));
            for (Item item : ForgeRegistries.ITEMS.tags().getTag(tag)) {
                if (ItemRadiationRegistry.getRadiationDataFor(item, (short) 0) == null) ItemRadiationRegistry.addRadiationData(item, 0, levels[i], 100);
            }
        }
    }
    private RadiationTagEvents() {}
}
