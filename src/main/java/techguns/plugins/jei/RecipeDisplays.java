package techguns.plugins.jei;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import techguns.*;
import techguns.items.armors.*;
import techguns.machines.*;

/** Read-only views of the same recipes and operations used by the server. */
public final class RecipeDisplays {
    public enum Kind {
        AMMO(MachineKind.AMMO_PRESS),METAL(MachineKind.METAL_PRESS),CHEM(MachineKind.CHEM_LAB),FABRICATOR(MachineKind.FABRICATOR),CHARGING(MachineKind.CHARGING_STATION),REACTION(MachineKind.REACTION),BLAST(MachineKind.BLAST_FURNACE),GRINDER(MachineKind.GRINDER),CAMO(BenchKind.CAMO),UPGRADE(BenchKind.UPGRADE),REPAIR(BenchKind.REPAIR),ORE_DRILL;
        public final MachineKind machine;public final BenchKind bench;
        Kind(MachineKind value){machine=value;bench=null;}Kind(BenchKind value){machine=null;bench=value;}Kind(){machine=null;bench=null;}
        public String id(){return machine!=null?machine.id:bench!=null?bench.id:"oredrill";}
        public Component title(){return Component.translatable(machine!=null?machine.titleKey():bench!=null?bench.titleKey():"techguns.container.oredrill");}
        public ItemStack icon(){return (machine!=null?TGMachines.ITEMS.get(machine):bench!=null?TGBenches.ITEMS.get(bench):TGOreDrill.ITEMS.get(techguns.oredrill.OreDrillType.CONTROLLER)).get().getDefaultInstance();}
        public ResourceLocation texture(){return ResourceLocation.fromNamespaceAndPath("techguns","textures/gui/"+(machine!=null?machine.texture():bench!=null?bench.texture():"ore_drill")+"_gui.png");}
        static Kind machine(MachineKind kind){return Arrays.stream(values()).filter(k->k.machine==kind).findFirst().orElseThrow();}
    }
    public enum Role {INPUT,OUTPUT,CATALYST}
    public record Slot(Role role,int x,int y,List<ItemStack> items,FluidStack fluid,Component note){
        public boolean available(){return !items.isEmpty()||!fluid.isEmpty();}
    }
    public record Display(String id,Kind kind,List<Slot> slots,int ticks,int power,List<Component> details){
        public boolean available(){return slots.stream().allMatch(Slot::available);}
    }
    private static Slot item(Role role,int x,int y,List<ItemStack> items,Component note){return new Slot(role,x-7,y-15,List.copyOf(items),FluidStack.EMPTY,note);}
    private static Slot input(int x,int y,Ingredient ingredient,int amount){return item(Role.INPUT,x,y,Arrays.stream(ingredient.getItems()).filter(s->!s.isEmpty()).map(s->s.copyWithCount(amount)).toList(),null);}
    private static Slot output(int x,int y,ItemStack stack){return item(Role.OUTPUT,x,y,List.of(stack.copy()),null);}
    private static Slot fluid(Role role,int x,int y,FluidStack stack,Component note){return new Slot(role,x-7,y-15,List.of(),stack.copy(),note);}
    /** Techguns CE JEI layouts (jei/*.png backgrounds): exact positions inside the recipe area. */
    private static Slot at(Role role,int x,int y,List<ItemStack> items,Component note){return new Slot(role,x,y,List.copyOf(items),FluidStack.EMPTY,note);}
    private static Slot inputAt(int x,int y,Ingredient ingredient,int amount){return at(Role.INPUT,x,y,Arrays.stream(ingredient.getItems()).filter(s->!s.isEmpty()).map(s->s.copyWithCount(amount)).toList(),null);}
    private static Slot fluidAt(Role role,int x,int y,FluidStack stack,Component note){return new Slot(role,x,y,List.of(),stack.copy(),note);}
    private static final int[][] AMMO_JEI={{22,13},{42,13},{62,13},{42,56}},METAL_JEI={{36,13},{56,13},{46,56}},FABRICATOR_JEI={{6,6},{52,6},{98,6},{144,6},{75,74}},CHEM_JEI={{36,6},{36,27},{36,48}},REACTION_JEI_OUT={{117,62},{135,44},{135,62},{135,80}};
    private static int[][] ceLayout(MachineKind kind){return switch(kind){case AMMO_PRESS->AMMO_JEI;case METAL_PRESS->METAL_JEI;case FABRICATOR->FABRICATOR_JEI;default->null;};}
    public static Map<Kind,List<Display>> all(){
        var result=new EnumMap<Kind,List<Display>>(Kind.class);for(var kind:Kind.values())result.put(kind,new ArrayList<>());
        var processing=new ArrayList<>(MachineRecipes.all());for(int plan=0;plan<4;plan++)processing.add(MachineRecipes.ammo(plan));int index=0;
        for(var recipe:processing){var slots=new ArrayList<Slot>();var ce=ceLayout(recipe.kind());for(int i=0;i<recipe.ingredients().length;i++){if(ce!=null)slots.add(inputAt(ce[i][0],ce[i][1],recipe.ingredients()[i],recipe.amounts()[i]));else{int[] p=recipe.kind().slots[i];slots.add(input(p[0],p[1],recipe.ingredients()[i],recipe.amounts()[i]));}}if(ce!=null)slots.add(at(Role.OUTPUT,ce[ce.length-1][0],ce[ce.length-1][1],List.of(recipe.result().copy()),null));else{int[] out=recipe.kind().slots[recipe.kind().output];slots.add(output(out[0],out[1],recipe.result()));}var steam=MachineRecipes.steam(recipe);add(result,new Display("processing_"+index++,Kind.machine(recipe.kind()),slots,recipe.ticks(),recipe.power(),steam!=null?List.of(Component.translatable("item.techguns.machine_upgrade_steam"),Component.translatable("techguns.jei.steam",steam[0],steam[1])):recipe.kind()==MachineKind.FABRICATOR&&recipe.result().is(techguns.TGItems.CYBERNETIC_PARTS.get())?List.of(Component.translatable("techguns.tooltip.needblueprints")):List.of()));}
        index=0;for(var recipe:ChemicalRecipes.all()){
            var slots=new ArrayList<Slot>();for(int i=0;i<recipe.inputs().length;i++)if(recipe.amounts()[i]>0)slots.add(inputAt(CHEM_JEI[i][0],CHEM_JEI[i][1],recipe.inputs()[i],recipe.amounts()[i]));
            if(!recipe.output().isEmpty())slots.add(at(Role.OUTPUT,132,27,List.of(recipe.output().copy()),null));if(!recipe.fluidInput().isEmpty())slots.add(fluidAt(Role.INPUT,17,7,recipe.fluidInput(),null));if(!recipe.fluidOutput().isEmpty())slots.add(fluidAt(Role.OUTPUT,157,7,recipe.fluidOutput(),null));
            if(ChemicalRecipes.needsSteamUpgrade(recipe))slots.add(at(Role.CATALYST,132,48,List.of(techguns.TGItems.MACHINE_UPGRADE_STEAM.get().getDefaultInstance()),Component.translatable("techguns.jei.recipe_requires",Component.translatable("item.techguns.machine_upgrade_steam"))));
            add(result,new Display("chemical_"+index++,Kind.CHEM,slots,100,recipe.power(),List.of()));
        }
        for(var recipe:ReactionRecipe.all()){
            var slots=new ArrayList<Slot>();slots.add(inputAt(27,62,recipe.input(),1));slots.add(at(Role.CATALYST,72,10,List.of(recipe.focus().getDefaultInstance()),Component.translatable("techguns.jei.catalyst")));
            slots.add(fluidAt(Role.INPUT,5,5,new FluidStack(recipe.fluid(),recipe.liquidLevel()*1000),Component.translatable("techguns.jei.fluid_used",recipe.liquidConsumption())));
            for(int i=0;i<recipe.outputs().length&&i<4;i++)slots.add(at(Role.OUTPUT,REACTION_JEI_OUT[i][0],REACTION_JEI_OUT[i][1],List.of(recipe.outputs()[i].copy()),null));
            add(result,new Display(recipe.id(),Kind.REACTION,slots,recipe.ticks()*60,recipe.energy(),List.of(Component.translatable("techguns.jei.intensity",recipe.preferredIntensity(),recipe.liquidLevel()*10),Component.translatable("techguns.jei.reaction_steps",recipe.requiredCompletion(),recipe.ticks()),Component.translatable("techguns.jei.risk").append(": ").append(Component.translatable("techguns.jei.risk."+recipe.risk().name().toLowerCase(Locale.ROOT))))));
        }
        var grinding=new ArrayList<>(GrindingRecipe.all());for(var entry:TGEquipment.ARMOR.values()){var recipe=GrindingRecipe.find(entry.get().getDefaultInstance());if(recipe!=null)grinding.add(recipe);}
        index=0;for(var recipe:grinding){var slots=new ArrayList<Slot>();slots.add(at(Role.INPUT,15,32,List.of(recipe.input().getDefaultInstance()),null));for(int i=0;i<recipe.outputs().length;i++){
            var stack=recipe.outputs()[i];double expected=stack.getCount()*(recipe.chances()==null?1:recipe.chances()[i]);if(expected<=0)continue;slots.add(at(Role.OUTPUT,115+(i%3)*18,14+(i/3)*18,List.of(stack.copyWithCount((int)Math.ceil(expected))),recipe.chances()==null?null:Component.translatable("techguns.jei.average",number(expected))));
        }add(result,new Display("grinding_"+index++,Kind.GRINDER,slots,100,5,recipe.input() instanceof TGSuitArmor?List.of(Component.translatable("techguns.jei.wear_yield")):List.of()));}
        index=0;for(var upgrade:ArmorUpgrades.all())for(var registered:TGEquipment.ARMOR.values()){
            var armor=registered.get().getDefaultInstance();var applied=ArmorUpgrades.result(armor,upgrade.ingredient().getDefaultInstance());if(applied.stack().isEmpty())continue;
            add(result,new Display("upgrade_"+index++,Kind.UPGRADE,List.of(item(Role.INPUT,17,47,List.of(armor),null),item(Role.INPUT,53,47,List.of(upgrade.ingredient().getDefaultInstance()),null),output(116,47,applied.stack())),0,0,List.of(Component.translatable("techguns.gui.xpcost").append(": "+applied.xp()),Component.translatable("techguns.jei.upgrade_discount"))));
        }
        index=0;for(var registered:TGItems.ITEMS.getEntries()){
            Item item=registered.get();if(item instanceof ICamoChangeable camo&&camo.getCamoCount()>1){var stack=item.getDefaultInstance();for(int i=0;i<camo.getCamoCount();i++){var before=stack.copy();var after=new ItemStack[]{stack.copy()};BenchMenu.change(after[0],false,s->after[0]=s);if(!ItemStack.isSameItemSameTags(before,after[0]))add(result,new Display("camo_"+index++,Kind.CAMO,List.of(item(Role.INPUT,17,18,List.of(before),null),output(130,18,after[0])),0,0,List.of(Component.translatable("techguns.jei.camo"))));stack=after[0];}}
            if(item instanceof TGSuitArmor||item instanceof GenericShield){var damaged=item.getDefaultInstance();damaged.setDamageValue(damaged.getMaxDamage()-1);var materials=item instanceof TGSuitArmor a?a.repairMaterials(damaged):((GenericShield)item).getRepairMats(damaged);if(materials.isEmpty())continue;var slots=new ArrayList<Slot>();slots.add(item(Role.INPUT,8,18,List.of(damaged),null));for(int i=0;i<materials.size();i++)slots.add(item(Role.INPUT,8+i*18,57,List.of(materials.get(i)),null));slots.add(output(116,18,item.getDefaultInstance()));add(result,new Display("repair_"+BuiltInRegistries.ITEM.getKey(item).getPath(),Kind.REPAIR,slots,0,0,List.of(Component.translatable("techguns.jei.repair_cost"))));}
        }
        for(String family:new String[]{"wool","concrete","concrete_powder","terracotta","stained_glass","stained_glass_pane","carpet"})for(var color:DyeColor.values()){
            var item=BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(color.getName()+"_"+family));var before=item.getDefaultInstance();var after=new ItemStack[]{before.copy()};BenchMenu.change(after[0],false,s->after[0]=s);add(result,new Display("dye_"+color.getName()+"_"+family,Kind.CAMO,List.of(item(Role.INPUT,17,18,List.of(before),null),output(130,18,after[0])),0,0,List.of(Component.translatable("techguns.jei.camo"))));
        }
        oreDrill(result);
        return result;
    }
    /** Original OreDrillJeiRecipe/OreDrillMachineRecipe: one display per cluster entry with its weight chance and power per ore. */
    private static void oreDrill(Map<Kind,List<Display>> result){
        var heads=java.util.stream.Stream.of(TGItems.OREDRILLHEAD_STEEL,TGItems.OREDRILLHEAD_OBSIDIANSTEEL,TGItems.OREDRILLHEAD_CARBON,TGItems.OREDRILLHEAD_MEDIUM_STEEL,TGItems.OREDRILLHEAD_MEDIUM_OBSIDIANSTEEL,TGItems.OREDRILLHEAD_MEDIUM_CARBON,TGItems.OREDRILLHEAD_LARGE_STEEL,TGItems.OREDRILLHEAD_LARGE_OBSIDIANSTEEL,TGItems.OREDRILLHEAD_LARGE_CARBON).map(r->r.get().getDefaultInstance()).toList();
        var fuels=BuiltInRegistries.ITEM.stream().map(Item::getDefaultInstance).filter(stack->!stack.isEmpty()&&net.minecraftforge.common.ForgeHooks.getBurnTime(stack,null)>0).toList();
        FluidStack fuelFluid=FluidStack.EMPTY;
        for(String name:TGConfig.fluidListFuel){for(var fluid:net.minecraftforge.registries.ForgeRegistries.FLUIDS.getValues()){var id=net.minecraftforge.registries.ForgeRegistries.FLUIDS.getKey(fluid);if(id!=null&&id.getPath().equals(name)&&fluid.isSource(fluid.defaultFluidState())){fuelFluid=new FluidStack(fluid,16000);break;}}if(!fuelFluid.isEmpty())break;}
        int index=0;
        for(var cluster:techguns.world.ClusterKind.values()){
            var entries=TGOreClusters.entries(cluster);int total=entries.stream().mapToInt(TGOreClusters.Entry::weight).sum();
            double orePerHour=4.5*cluster.amountMultiplier()*TGConfig.oreDrillMultiplierOres;
            int powerTick=(int)(8*orePerHour*cluster.powerMultiplier()*TGConfig.oreDrillMultiplierPower);
            for(var entry:entries){
                var chance=Component.translatable("techguns.oredrill.jei.chance").append(": "+String.format(Locale.ROOT,"%.2f",entry.weight()*100f/total)+"%");
                var slots=new ArrayList<Slot>();
                slots.add(item(Role.CATALYST,30,17,heads,null));
                slots.add(item(Role.INPUT,77,56,List.of(TGWorld.CLUSTERS.get(cluster).get().asItem().getDefaultInstance()),null));
                slots.add(item(Role.INPUT,30,51,fuels,null));
                if(!fuelFluid.isEmpty())slots.add(fluid(Role.INPUT,16,17,fuelFluid,null));
                if(!entry.item().isEmpty())slots.add(item(Role.OUTPUT,102,17,List.of(entry.item().copyWithCount(1)),chance));
                if(!entry.fluid().isEmpty())slots.add(fluid(Role.OUTPUT,158,17,entry.fluid(),chance));
                add(result,new Display("oredrill_"+index++,Kind.ORE_DRILL,slots,0,0,List.of(Component.translatable("techguns.oredrill.powerperore").append(": "+(int)((powerTick*20*60*60)/orePerHour)+" FE"))));
            }
        }
    }
    private static void add(Map<Kind,List<Display>> map,Display display){map.get(display.kind()).add(display);}
    public static String number(double value){return new java.text.DecimalFormat("0.##",java.text.DecimalFormatSymbols.getInstance(Locale.ROOT)).format(value);}
    private RecipeDisplays(){}
}
