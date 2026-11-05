package techguns.client;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;

/**
 * Connected textures are built into the mod (techguns.client.ctm), so the CTM, Athena and Fusion packs are no longer added;
 * those mods joined stair and slab faces wrongly. CTM still reads the pillar metadata of the drill rod.
 */
@Mod.EventBusSubscriber(modid = Techguns.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ConnectedTexturesCompat {
    private ConnectedTexturesCompat() {}
    private static final boolean BUILT_IN = true;

    @SubscribeEvent
    public static void addPacks(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES || BUILT_IN) return;
        // Fusion works on the texture itself, so it also covers stairs; prefer it when both are present.
        if (ModList.get().isLoaded("fusion")) add(event, "techguns_fusion", "Techguns + Fusion");
        else if (ModList.get().isLoaded("athena")) add(event, "techguns_athena", "Techguns + Athena");
    }

    private static void add(AddPackFindersEvent event, String folder, String title) {
        var file = ModList.get().getModFileById(Techguns.MODID).getFile();
        var path = file.findResource("resourcepacks/" + folder);
        Pack pack = Pack.readMetaAndCreate(Techguns.MODID + ":" + folder, Component.literal(title), true,
                id -> new PathPackResources(id, path, true), PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
        if (pack != null) event.addRepositorySource(consumer -> consumer.accept(pack));
    }
}
