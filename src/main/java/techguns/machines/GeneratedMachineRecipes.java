package techguns.machines;
import java.util.List;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.Ingredient;
import techguns.TGItems;
import static techguns.machines.MachineRecipes.tag;
/** Generated from the original TGMachineRecipes declarations. */
public final class GeneratedMachineRecipes {public static void add(List<ProcessingRecipe> recipes){
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:ingots/tin"),tag("forge:ingots/tin")},new int[]{1,1},new ItemStack(TGItems.PLATE_TIN.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:ingots/copper"),tag("forge:ingots/copper")},new int[]{1,1},new ItemStack(TGItems.PLATE_COPPER.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:ingots/bronze"),tag("forge:ingots/bronze")},new int[]{1,1},new ItemStack(TGItems.PLATE_BRONZE.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:ingots/iron"),tag("forge:ingots/iron")},new int[]{1,1},new ItemStack(TGItems.PLATE_IRON.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:ingots/steel"),tag("forge:ingots/steel")},new int[]{1,1},new ItemStack(TGItems.PLATE_STEEL.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:ingots/lead"),tag("forge:ingots/lead")},new int[]{1,1},new ItemStack(TGItems.PLATE_LEAD.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:plates/iron"),Ingredient.of(new ItemStack(Items.FLINT,1))},new int[]{1,1},new ItemStack(TGItems.MECHANICAL_PARTS_IRON.get(),1),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:plates/obsidian_steel"),tag("forge:gems/quartz")},new int[]{1,1},new ItemStack(TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get(),1),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:plates/carbon"),Ingredient.of(new ItemStack(Items.BLAZE_ROD,1))},new int[]{1,1},new ItemStack(TGItems.MECHANICAL_PARTS_CARBON.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:plates/copper"),tag("forge:plates/copper")},new int[]{1,1},new ItemStack(TGItems.WIRE_COPPER.get(),8),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:fibers/carbon"),tag("forge:fibers/carbon")},new int[]{1,1},new ItemStack(TGItems.PLATE_CARBON.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:plates/obsidian_steel"),Ingredient.of(new ItemStack(TGItems.TGX.get(),1))},new int[]{1,1},new ItemStack(TGItems.ADVANCED_ROUNDS.get(),16),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:ingots/obsidian_steel"),tag("forge:ingots/obsidian_steel")},new int[]{1,1},new ItemStack(TGItems.PLATE_OBSIDIAN_STEEL.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:plates/steel"),tag("forge:plates/bronze")},new int[]{1,1},new ItemStack(TGItems.STEAMARMOR_PLATE.get(),1),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:ingots/gold"),tag("forge:ingots/gold")},new int[]{1,1},new ItemStack(TGItems.WIRE_GOLD.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:plates/iron"),Ingredient.of(new ItemStack(Blocks.TNT,1))},new int[]{1,1},new ItemStack(TGItems.GRENADE_40MM.get(),16),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:ingots/titanium"),tag("forge:ingots/titanium")},new int[]{1,1},new ItemStack(TGItems.PLATE_TITANIUM.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("forge:plates/obsidian_steel"),tag("forge:plates/titanium")},new int[]{1,1},new ItemStack(TGItems.GAUSSRIFLE_SLUGS.get(),4),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{Ingredient.of(new ItemStack(TGItems.SNIPER_ROUNDS_INCENDIARY.get(),1)),Ingredient.of(new ItemStack(TGItems.TGX.get(),1))},new int[]{1,1},new ItemStack(TGItems.SNIPER_ROUNDS_EXPLOSIVE.get(),1),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.BLAST_FURNACE,new Ingredient[]{Ingredient.of(new ItemStack(Items.IRON_INGOT,4)),Ingredient.of(new ItemStack(Items.COAL,1))},new int[]{4,1},new ItemStack(TGItems.INGOT_STEEL.get(),4),800,10,false));
        recipes.add(new ProcessingRecipe(MachineKind.BLAST_FURNACE,new Ingredient[]{Ingredient.of(new ItemStack(Items.IRON_INGOT,4)),Ingredient.of(new ItemStack(Items.CHARCOAL,1))},new int[]{4,1},new ItemStack(TGItems.INGOT_STEEL.get(),4),800,10,false));
        recipes.add(new ProcessingRecipe(MachineKind.BLAST_FURNACE,new Ingredient[]{tag("forge:ingots/steel"),Ingredient.of(new ItemStack(Blocks.OBSIDIAN,1))},new int[]{1,1},new ItemStack(TGItems.INGOT_OBSIDIAN_STEEL.get(),1),200,10,false));
        recipes.add(new ProcessingRecipe(MachineKind.BLAST_FURNACE,new Ingredient[]{tag("forge:ingots/copper"),tag("forge:ingots/tin")},new int[]{3,1},new ItemStack(TGItems.INGOT_BRONZE.get(),4),100,10,false));
        recipes.add(new ProcessingRecipe(MachineKind.CHARGING_STATION,new Ingredient[]{Ingredient.of(TGItems.ENERGY_CELL_EMPTY.get())},new int[]{1},new ItemStack(TGItems.ENERGY_CELL.get()),62,800,false));
        recipes.add(new ProcessingRecipe(MachineKind.CHARGING_STATION,new Ingredient[]{Ingredient.of(TGItems.REDSTONE_BATTERY_EMPTY.get())},new int[]{1},new ItemStack(TGItems.REDSTONE_BATTERY.get()),25,800,false));
}}
