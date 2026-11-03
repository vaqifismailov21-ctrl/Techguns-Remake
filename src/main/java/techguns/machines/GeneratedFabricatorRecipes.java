package techguns.machines;
import java.util.List;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import techguns.*;
public final class GeneratedFabricatorRecipes {public static void add(List<ProcessingRecipe> recipes){
recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{MachineRecipes.tag("c:ingots/gold"),MachineRecipes.tag("c:wires/copper"),MachineRecipes.tag("c:dusts/redstone"),MachineRecipes.tag("c:sheets/plastic")},new int[]{1,1,3,1},new ItemStack(TGItems.ENERGY_CELL_EMPTY.get(),1),100,80,false));
recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{Ingredient.of(Blocks.SOUL_SAND),MachineRecipes.tag("c:wires/gold"),MachineRecipes.tag("c:dusts/redstone"),MachineRecipes.tag("c:sheets/plastic")},new int[]{1,1,1,1},new ItemStack(TGItems.CYBERNETIC_PARTS.get(),1),100,80,false));
recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{MachineRecipes.tag("c:ingots/titanium"),MachineRecipes.tag("c:circuits/basic"),Ingredient.of(TGItems.MECHANICAL_PARTS_CARBON.get()),MachineRecipes.tag("c:plates/carbon")},new int[]{2,4,1,4},new ItemStack(TGItems.POWER_ARMOR_PLATING.get(),2),100,80,false));
recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{Ingredient.of(TGItems.COIL.get()),MachineRecipes.tag("c:circuits/elite"),Ingredient.of(TGItems.MECHANICAL_PARTS_CARBON.get()),MachineRecipes.tag("c:plates/titanium")},new int[]{1,2,1,1},new ItemStack(TGItems.SONIC_EMITTER.get(),1),100,80,false));
recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{Ingredient.of(TGItems.ENRICHED_URANIUM.get()),MachineRecipes.tag("c:circuits/elite"),Ingredient.of(TGItems.MECHANICAL_PARTS_CARBON.get()),MachineRecipes.tag("c:plates/lead")},new int[]{1,2,2,2},new ItemStack(TGItems.RAD_EMITTER.get(),1),100,80,false));
recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{MachineRecipes.tag("c:ingots/steel"),MachineRecipes.tag("c:circuits/basic"),MachineRecipes.tag("c:dusts/redstone"),MachineRecipes.tag("c:plates/lead")},new int[]{1,1,4,2},new ItemStack(TGItems.NUCLEAR_POWERCELL_EMPTY.get(),1),100,80,false));
}}
