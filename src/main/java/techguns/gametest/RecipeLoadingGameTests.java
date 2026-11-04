package techguns.gametest;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.gametest.*;
import techguns.*;
@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class RecipeLoadingGameTests {
    @GameTest(template="empty") public static void standaloneCraftingIngredientsResolveToRealRegisteredItems(GameTestHelper h){try(var input=RecipeLoadingGameTests.class.getResourceAsStream("/data/techguns/port_recipe_index.json");var reader=new java.io.InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8)){var missing=new java.util.ArrayList<String>();for(var id:com.google.gson.JsonParser.parseReader(reader).getAsJsonArray()){var recipe=h.getLevel().getRecipeManager().byKey(ResourceLocation.parse(id.getAsString())).orElseThrow();for(var ingredient:recipe.getIngredients())if(!ingredient.isEmpty()&&ingredient.getItems().length==0)missing.add(id.getAsString()+": "+ingredient.toJson());}h.assertTrue(missing.isEmpty(),"Standalone recipes cannot depend on empty ingredient tags: "+missing);h.succeed();}catch(java.io.IOException error){throw new IllegalStateException(error);}}
    @GameTest(template="empty") public static void everyConvertedCraftingRecipeActuallyLoadsInRecipeManager(GameTestHelper h){
        try(var input=RecipeLoadingGameTests.class.getResourceAsStream("/data/techguns/port_recipe_index.json");var reader=new java.io.InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8)){
            var ids=com.google.gson.JsonParser.parseReader(reader).getAsJsonArray();for(var id:ids){var location=ResourceLocation.parse(id.getAsString());h.assertTrue(h.getLevel().getRecipeManager().byKey(location).isPresent(),"Converted recipe must load successfully, rather than disappear after a parse error: "+location);}h.assertTrue(ids.size()>=308,"Recipe manifest must include the currently ported crafting catalog");h.succeed();
        }catch(java.io.IOException error){throw new IllegalStateException(error);}
    }
    @GameTest(template="empty") public static void furnaceRecipesRetainMultipleResultCounts(GameTestHelper h){var id=ResourceLocation.fromNamespaceAndPath("techguns","smelt_techguns_ironbarrel");var recipe=h.getLevel().getRecipeManager().byKey(id).orElseThrow();h.assertTrue(recipe instanceof SmeltingRecipe&&recipe.getResultItem(h.getLevel().registryAccess()).getCount()==6,"Original iron barrel smelts into six ingots, using Forge's ItemStack JSON result support");h.succeed();}
}
