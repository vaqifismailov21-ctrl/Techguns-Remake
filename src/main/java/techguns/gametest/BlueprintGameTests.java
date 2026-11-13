package techguns.gametest;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.items.blueprint.*;
import techguns.recipes.BlueprintShapedRecipe;
@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class BlueprintGameTests {
    @GameTest(template="empty") public static void energyBlocksNeedTheirStudiedBlueprintToCraft(GameTestHelper h){
        var level=h.getLevel();var recipe=level.getRecipeManager().byKey(new ResourceLocation("techguns","fuel_generator")).orElseThrow();
        h.assertTrue(recipe instanceof BlueprintShapedRecipe b&&Blueprints.GENERATORS.equals(b.blueprint),"The fuel generator recipe needs the generator blueprints");
        var shaped=(BlueprintShapedRecipe)recipe;var player=h.makeMockPlayer();
        var grid=new TransientCraftingContainer(new InventoryMenu(player.getInventory(),false,player),3,3);
        for(int y=0;y<shaped.getHeight();y++)for(int x=0;x<shaped.getWidth();x++){var items=shaped.getIngredients().get(x+y*shaped.getWidth()).getItems();if(items.length>0)grid.setItem(x+y*3,items[0].copy());}
        h.assertFalse(shaped.matches(grid,level),"Without the blueprints the grid gives no generator");
        var knowledge=BlueprintKnowledge.get(level.getServer());
        h.assertTrue(knowledge.learn(player.getUUID(),Blueprints.GENERATORS),"Reading the blueprints teaches them once");
        h.assertFalse(knowledge.learn(player.getUUID(),Blueprints.GENERATORS),"A second reading teaches nothing new");
        h.assertTrue(shaped.matches(grid,level),"With the blueprints the generator can be crafted");
        h.assertFalse(BlueprintKnowledge.knows(level,player.getUUID()),"Generator blueprints do not teach cybernetics");
        h.assertTrue(level.getRecipeManager().byKey(new ResourceLocation("techguns","fusion_reactor")).orElseThrow() instanceof BlueprintShapedRecipe r&&Blueprints.REACTORS.equals(r.blueprint),"Reactors need the reactor blueprints");
        h.succeed();
    }
}
