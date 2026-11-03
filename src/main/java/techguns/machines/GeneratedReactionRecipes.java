package techguns.machines;
import java.util.List;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import techguns.*;
public final class GeneratedReactionRecipes {public static void add(List<ReactionRecipe> recipes){
recipes.add(new ReactionRecipe("RC_UV_WHEAT",Ingredient.of(new ItemStack(Items.WHEAT_SEEDS,1)),TGItems.RC_UV_EMITTER.get(),Fluids.WATER,new ItemStack[]{new ItemStack(Items.WHEAT,1),new ItemStack(Items.WHEAT_SEEDS,2)},10,5,3,1,1,1000,0.5f,ReactionRecipe.Risk.EXPLOSION_LOW,50000));
recipes.add(new ReactionRecipe("RC_LASER_FOCUS",MachineRecipes.tag("c:gems/diamond"),TGItems.RC_HEAT_RAY.get(),ReactionRecipe.externalOrLava("redstone"),new ItemStack[]{new ItemStack(TGItems.LASER_FOCUS.get(),1)},10,5,3,1,4,4000,0.5f,ReactionRecipe.Risk.BREAK_ITEM,100000));
recipes.add(new ReactionRecipe("RC_TITANIUM",MachineRecipes.tag("c:ores/ilmenite"),TGItems.RC_HEAT_RAY.get(),TGFluids.ACID.get(),new ItemStack[]{new ItemStack(TGItems.ORE_TITANIUM.get(),2),new ItemStack(Blocks.IRON_ORE,1)},2,1,5,0,3,100,0f,ReactionRecipe.Risk.BREAK_ITEM,25000));
recipes.add(new ReactionRecipe("RC_BLAZEROD",Ingredient.of(new ItemStack(TGItems.QUARTZ_ROD.get(),1)),TGItems.RC_HEAT_RAY.get(),Fluids.LAVA,new ItemStack[]{new ItemStack(Items.BLAZE_ROD,1)},5,3,7,2,4,1000,0.5f,ReactionRecipe.Risk.BREAK_ITEM,250000));
recipes.add(new ReactionRecipe("RC_GLOWSTONE",MachineRecipes.tag("c:storage_blocks/redstone"),TGItems.RC_HEAT_RAY.get(),Fluids.LAVA,new ItemStack[]{new ItemStack(Blocks.GLOWSTONE,1)},5,3,7,2,4,1000,0.5f,ReactionRecipe.Risk.EXPLOSION_MEDIUM,250000));
recipes.add(new ReactionRecipe("RC_ANTIGRAV",Ingredient.of(new ItemStack(Items.NETHER_STAR,1)),TGItems.RC_HEAT_RAY.get(),ReactionRecipe.externalOrLava("ender"),new ItemStack[]{new ItemStack(TGItems.ANTI_GRAV_CORE.get(),1)},10,7,8,2,4,4000,1f,ReactionRecipe.Risk.EXPLOSION_LOW,500000));
recipes.add(new ReactionRecipe("RC_URANIUM",MachineRecipes.tag("c:dusts/uranium"),TGItems.RC_HEAT_RAY.get(),Fluids.WATER,new ItemStack[]{new ItemStack(TGItems.ENRICHED_URANIUM.get(),1)},5,4,7,0,3,1000,0f,ReactionRecipe.Risk.BREAK_ITEM,250000));
}}
