package techguns;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.world.*;
/** Original TGOreClusters recipe weights; optional ores resolve from current Forge tags. */
public final class TGOreClusters {
    public record Entry(ItemStack item,FluidStack fluid,int weight){
        public Entry copy(){return new Entry(item.copy(),fluid.copy(),weight);}
    }
    private static void item(List<Entry> entries,ItemLikeValue item,int weight){entries.add(new Entry(item.stack(),FluidStack.EMPTY,weight));}
    @FunctionalInterface private interface ItemLikeValue {ItemStack stack();}
    private static void tag(List<Entry> entries,String id,int weight){
        var values=ForgeRegistries.ITEMS.tags().getTag(TagKey.create(Registries.ITEM,ResourceLocation.parse("forge:ores/"+id)));
        var first=values.iterator();if(first.hasNext()){var stack=new ItemStack(first.next());if(entries.stream().noneMatch(e->ItemStack.isSameItemSameTags(e.item,stack)))entries.add(new Entry(stack,FluidStack.EMPTY,weight));}
    }
    public static List<Entry> entries(ClusterKind kind){
        var entries=new ArrayList<Entry>();
        switch(kind){
            case COAL->{item(entries,()->new ItemStack(Blocks.COAL_ORE),99);item(entries,()->new ItemStack(Items.DIAMOND),1);}
            case COMMON_METAL->{item(entries,()->new ItemStack(Blocks.IRON_ORE),45);item(entries,()->new ItemStack(TGWorld.ORES.get(OreKind.COPPER).get()),30);item(entries,()->new ItemStack(TGWorld.ORES.get(OreKind.TIN).get()),25);}
            case RARE_METAL->{item(entries,()->new ItemStack(TGWorld.ORES.get(OreKind.LEAD).get()),50);tag(entries,"osmium",50);tag(entries,"aluminum",50);}
            case SHINY_METAL->{item(entries,()->new ItemStack(TGWorld.ORES.get(OreKind.TITANIUM).get()),20);item(entries,()->new ItemStack(Blocks.GOLD_ORE),40);tag(entries,"silver",40);}
            case URANIUM->item(entries,()->new ItemStack(TGWorld.ORES.get(OreKind.URANIUM).get()),50);
            case COMMON_GEM->{item(entries,()->new ItemStack(Blocks.REDSTONE_ORE),40);item(entries,()->new ItemStack(Blocks.LAPIS_ORE),20);tag(entries,"certus_quartz",40);tag(entries,"charged_certus_quartz",5);}
            case SHINY_GEM->{item(entries,()->new ItemStack(Blocks.DIAMOND_ORE),40);item(entries,()->new ItemStack(Blocks.EMERALD_ORE),20);}
            case NETHER_CRYSTAL->{item(entries,()->new ItemStack(Blocks.NETHER_QUARTZ_ORE),50);item(entries,()->new ItemStack(Blocks.GLOWSTONE),40);item(entries,()->new ItemStack(Items.BLAZE_ROD),10);}
            case OIL->{
                var oils=new ArrayList<net.minecraft.world.level.material.Fluid>();
                for(String name:TGConfig.fluidListOilWorldspawn)for(var fluid:ForgeRegistries.FLUIDS.getValues())if(fluid.isSource(fluid.defaultFluidState())&&ForgeRegistries.FLUIDS.getKey(fluid).getPath().equals(name)&&fluid!=Fluids.EMPTY)oils.add(fluid);
                var chosen=oils.stream().filter(f->!f.defaultFluidState().createLegacyBlock().isAir()).findFirst().orElse(oils.isEmpty()?null:oils.get(0));
                if(chosen!=null)entries.add(new Entry(ItemStack.EMPTY,new FluidStack(chosen,1000),10));
            }
        }
        return entries;
    }
    public static Entry choose(ClusterKind kind,RandomSource random){
        var entries=entries(kind);int total=entries.stream().mapToInt(Entry::weight).sum();
        if(total==0)return new Entry(new ItemStack(Blocks.COBBLESTONE),FluidStack.EMPTY,1);
        int roll=random.nextInt(total);for(var entry:entries){roll-=entry.weight;if(roll<0)return entry.copy();}throw new IllegalStateException("Invalid cluster weights");
    }
    /** Original OIL_WORLDSPAWN only accepts a configured source fluid with a placeable world block. */
    public static net.minecraft.world.level.material.Fluid worldOil(){
        for(String name:TGConfig.fluidListOilWorldspawn)for(var fluid:ForgeRegistries.FLUIDS.getValues())if(fluid!=Fluids.EMPTY&&fluid.isSource(fluid.defaultFluidState())&&ForgeRegistries.FLUIDS.getKey(fluid).getPath().equals(name)&&!fluid.defaultFluidState().createLegacyBlock().isAir())return fluid;return null;
    }
    public static int powerPerTick(double orePerHour,int radius,double powerMultiplier){return (int)(8*orePerHour*(1+Math.max(radius-1,0)*.2)*powerMultiplier*TGConfig.oreDrillMultiplierPower);}
    private TGOreClusters(){}
}
