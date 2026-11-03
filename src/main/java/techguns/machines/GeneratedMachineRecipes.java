package techguns.machines;
import java.util.List;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.Ingredient;
import techguns.TGItems;
import static techguns.machines.MachineRecipes.tag;
/** Generated from the original TGMachineRecipes declarations. */
public final class GeneratedMachineRecipes {public static void add(List<ProcessingRecipe> recipes){
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:ingots/tin"),tag("c:ingots/tin")},new int[]{1,1},new ItemStack(TGItems.PLATE_TIN.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:ingots/copper"),tag("c:ingots/copper")},new int[]{1,1},new ItemStack(TGItems.PLATE_COPPER.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:ingots/bronze"),tag("c:ingots/bronze")},new int[]{1,1},new ItemStack(TGItems.PLATE_BRONZE.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:ingots/iron"),tag("c:ingots/iron")},new int[]{1,1},new ItemStack(TGItems.PLATE_IRON.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:ingots/steel"),tag("c:ingots/steel")},new int[]{1,1},new ItemStack(TGItems.PLATE_STEEL.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:ingots/lead"),tag("c:ingots/lead")},new int[]{1,1},new ItemStack(TGItems.PLATE_LEAD.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:plates/iron"),Ingredient.of(new ItemStack(Items.FLINT,1))},new int[]{1,1},new ItemStack(TGItems.MECHANICAL_PARTS_IRON.get(),1),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:plates/obsidian_steel"),tag("c:gems/quartz")},new int[]{1,1},new ItemStack(TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get(),1),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:plates/carbon"),Ingredient.of(new ItemStack(Items.BLAZE_ROD,1))},new int[]{1,1},new ItemStack(TGItems.MECHANICAL_PARTS_CARBON.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:plates/copper"),tag("c:plates/copper")},new int[]{1,1},new ItemStack(TGItems.WIRE_COPPER.get(),8),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:fibers/carbon"),tag("c:fibers/carbon")},new int[]{1,1},new ItemStack(TGItems.PLATE_CARBON.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:plates/obsidian_steel"),Ingredient.of(new ItemStack(TGItems.TGX.get(),1))},new int[]{1,1},new ItemStack(TGItems.ADVANCED_ROUNDS.get(),16),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:ingots/obsidian_steel"),tag("c:ingots/obsidian_steel")},new int[]{1,1},new ItemStack(TGItems.PLATE_OBSIDIAN_STEEL.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:plates/steel"),tag("c:plates/bronze")},new int[]{1,1},new ItemStack(TGItems.STEAMARMOR_PLATE.get(),1),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:ingots/gold"),tag("c:ingots/gold")},new int[]{1,1},new ItemStack(TGItems.WIRE_GOLD.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:plates/iron"),Ingredient.of(new ItemStack(Blocks.TNT,1))},new int[]{1,1},new ItemStack(TGItems.GRENADE_40MM.get(),16),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:ingots/titanium"),tag("c:ingots/titanium")},new int[]{1,1},new ItemStack(TGItems.PLATE_TITANIUM.get(),2),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{tag("c:plates/obsidian_steel"),tag("c:plates/titanium")},new int[]{1,1},new ItemStack(TGItems.GAUSSRIFLE_SLUGS.get(),4),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.METAL_PRESS,new Ingredient[]{Ingredient.of(new ItemStack(TGItems.SNIPER_ROUNDS_INCENDIARY.get(),1)),Ingredient.of(new ItemStack(TGItems.TGX.get(),1))},new int[]{1,1},new ItemStack(TGItems.SNIPER_ROUNDS_EXPLOSIVE.get(),1),100,20,true));
        recipes.add(new ProcessingRecipe(MachineKind.BLAST_FURNACE,new Ingredient[]{Ingredient.of(new ItemStack(Items.IRON_INGOT,4)),Ingredient.of(new ItemStack(Items.COAL,1))},new int[]{4,1},new ItemStack(TGItems.INGOT_STEEL.get(),4),800,10,false));
        recipes.add(new ProcessingRecipe(MachineKind.BLAST_FURNACE,new Ingredient[]{Ingredient.of(new ItemStack(Items.IRON_INGOT,4)),Ingredient.of(new ItemStack(Items.CHARCOAL,1))},new int[]{4,1},new ItemStack(TGItems.INGOT_STEEL.get(),4),800,10,false));
        recipes.add(new ProcessingRecipe(MachineKind.BLAST_FURNACE,new Ingredient[]{tag("c:ingots/steel"),Ingredient.of(new ItemStack(Blocks.OBSIDIAN,1))},new int[]{1,1},new ItemStack(TGItems.INGOT_OBSIDIAN_STEEL.get(),1),200,10,false));
        recipes.add(new ProcessingRecipe(MachineKind.BLAST_FURNACE,new Ingredient[]{tag("c:ingots/copper"),tag("c:ingots/tin")},new int[]{3,1},new ItemStack(TGItems.INGOT_BRONZE.get(),4),100,10,false));
        recipes.add(new ProcessingRecipe(MachineKind.CHARGING_STATION,new Ingredient[]{Ingredient.of(TGItems.ENERGY_CELL_EMPTY.get())},new int[]{1},new ItemStack(TGItems.ENERGY_CELL.get()),62,800,false));
        recipes.add(new ProcessingRecipe(MachineKind.CHARGING_STATION,new Ingredient[]{Ingredient.of(TGItems.REDSTONE_BATTERY_EMPTY.get())},new int[]{1},new ItemStack(TGItems.REDSTONE_BATTERY.get()),25,800,false));
}}
