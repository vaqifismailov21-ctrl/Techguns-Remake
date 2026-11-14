package techguns.plugins.jer;

import jeresources.api.IJERAPI;
import jeresources.api.IJERPlugin;
import jeresources.api.JERPlugin;
import jeresources.api.distributions.DistributionSquare;
import jeresources.api.drop.LootDrop;
import jeresources.api.restrictions.Restriction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import techguns.TGWorld;
import techguns.world.OreKind;

/** Just Enough Resources pages: what Techguns structure chests can hold and where Techguns ores generate. Loaded only by JER. */
@JERPlugin
public final class TechgunsJerPlugin implements IJERPlugin {
    /** Every chest loot table the Techguns structures use. */
    static final String[] CHESTS={"aircraftcarrier","castle","end_building","end_floating_ship","factory_building","factory_building_big","gasstation",
            "militarybase_barracks","militarybase_bunker","nether_building_common","nether_building_trap","nether_cluster_secret","nether_submarine_common",
            "nether_submarine_rare","policestation","small_trainstation","survivor_hideout","train_green","train_head","train_orange","train_red"};

    @Override public void receive(IJERAPI api){
        var dungeons=api.getDungeonRegistry();
        for(String chest:CHESTS){
            String category="techguns:chests/"+chest;
            dungeons.registerCategory(category,"techguns.jer.chest."+chest);
            dungeons.registerChest(category,new ResourceLocation("techguns","chests/"+chest));
        }
        var worldGen=api.getWorldGenRegistry();
        for(OreKind kind:OreKind.values()){
            if(!kind.enabled())continue;
            var ore=new ItemStack(TGWorld.ORES.get(kind).get());
            int veinSize=kind.veinBase+(kind.veinRandom-1)/2;
            // LegacyOreFeature: 'attempts' veins per chunk, vein centre uniformly in [minY, maxY), stone and its variants only.
            worldGen.register(ore,new DistributionSquare(kind.attempts,veinSize,kind.minY,kind.maxY-1),Restriction.OVERWORLD,new LootDrop(ore));
        }
    }
}
