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

/**
 * Just Enough Resources pages: what Techguns structure chests can hold and where Techguns ores generate.
 * JER 1.4.0 for Forge looks its plugins up by the IJERPlugin interface instead of the JERPlugin annotation and so never finds them;
 * client setup therefore also hands the plugin JER's API directly. Whichever path runs first registers, the other is skipped.
 */
@JERPlugin
public final class TechgunsJerPlugin implements IJERPlugin {
    /** Every chest loot table the Techguns structures use. JER prints the name beside the chest in about 100 pixels, so the names are short. */
    static final String[] CHESTS={"aircraftcarrier","castle","end_building","end_floating_ship","factory_building","factory_building_big","gasstation",
            "militarybase_barracks","militarybase_bunker","nether_building_common","nether_building_trap","nether_cluster_secret","nether_submarine_common",
            "nether_submarine_rare","policestation","small_trainstation","survivor_hideout","train_green","train_head","train_orange","train_red"};
    /** Military crates found in the structures: breaking one drops its block loot table. */
    static final String[] CRATES={"military_crate_ammo","military_crate_armor","military_crate_explosives","military_crate_generic","military_crate_gun","military_crate_medical"};

    private static boolean registered;

    /** Called from client setup only when JER is loaded. */
    public static void registerDirectly(){
        try{
            var api=(IJERAPI)Class.forName("jeresources.compatibility.api.JERAPI").getMethod("getInstance").invoke(null);
            if(api!=null)new TechgunsJerPlugin().receive(api);
        }catch(ReflectiveOperationException|LinkageError e){
            techguns.Techguns.LOGGER.warn("Just Enough Resources found, but its API could not be reached: {}",e.toString());
        }
    }

    @Override public synchronized void receive(IJERAPI api){
        if(registered)return;
        registered=true;
        var dungeons=api.getDungeonRegistry();
        for(String chest:CHESTS){
            String category="techguns:chests/"+chest;
            dungeons.registerCategory(category,"techguns.jer.chest."+chest);
            dungeons.registerChest(category,new ResourceLocation("techguns","chests/"+chest));
        }
        for(String crate:CRATES){
            String category="techguns:blocks/"+crate;
            dungeons.registerCategory(category,"techguns.jer.crate."+crate);
            dungeons.registerChest(category,new ResourceLocation("techguns","blocks/"+crate));
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
