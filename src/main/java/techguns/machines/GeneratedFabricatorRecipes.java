package techguns.machines;
import java.util.List;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import techguns.*;
public final class GeneratedFabricatorRecipes {public static void add(List<ProcessingRecipe> recipes){
recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{MachineRecipes.tag("forge:ingots/gold"),MachineRecipes.tag("forge:wires/copper"),MachineRecipes.tag("forge:dusts/redstone"),MachineRecipes.tag("forge:sheets/plastic")},new int[]{1,1,3,1},new ItemStack(TGItems.ENERGY_CELL_EMPTY.get(),1),100,80,false));
recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{Ingredient.of(Blocks.SOUL_SAND),Ingredient.of(TGItems.CYBERNETIC_WIRING.get()),MachineRecipes.tag("forge:dusts/redstone"),MachineRecipes.tag("forge:sheets/plastic")},new int[]{1,3,4,2},new ItemStack(TGItems.CYBERNETIC_PARTS.get(),1),100,80,false));
recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{MachineRecipes.tag("forge:ingots/titanium"),MachineRecipes.tag("forge:circuits/basic"),Ingredient.of(TGItems.MECHANICAL_PARTS_CARBON.get()),MachineRecipes.tag("forge:plates/carbon")},new int[]{2,4,1,4},new ItemStack(TGItems.POWER_ARMOR_PLATING.get(),2),100,80,false));
recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{Ingredient.of(TGItems.COIL.get()),MachineRecipes.tag("forge:circuits/elite"),Ingredient.of(TGItems.MECHANICAL_PARTS_CARBON.get()),MachineRecipes.tag("forge:plates/titanium")},new int[]{1,2,1,1},new ItemStack(TGItems.SONIC_EMITTER.get(),1),100,80,false));
recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{Ingredient.of(TGItems.ENRICHED_URANIUM.get()),MachineRecipes.tag("forge:circuits/elite"),Ingredient.of(TGItems.MECHANICAL_PARTS_CARBON.get()),MachineRecipes.tag("forge:plates/lead")},new int[]{1,2,2,2},new ItemStack(TGItems.RAD_EMITTER.get(),1),100,80,false));
recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{MachineRecipes.tag("forge:ingots/steel"),MachineRecipes.tag("forge:circuits/basic"),MachineRecipes.tag("forge:dusts/redstone"),MachineRecipes.tag("forge:plates/lead")},new int[]{1,1,4,2},new ItemStack(TGItems.NUCLEAR_POWERCELL_EMPTY.get(),1),100,80,false));
}}
