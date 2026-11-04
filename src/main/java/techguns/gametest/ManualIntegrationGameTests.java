package techguns.gametest;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.*;
@GameTestHolder("techguns") @PrefixGameTestTemplate(false)
public final class ManualIntegrationGameTests {
    @GameTest(template="empty") public static void optionalManualRecipesFollowDependencyAndRetainBookIdentity(GameTestHelper h){
        boolean installed=net.minecraftforge.fml.ModList.get().isLoaded("patchouli");
        for(String name:new String[]{"techguns_manual","techguns_manual_alt"}){
            var recipe=h.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("techguns",name));
            h.assertTrue(recipe.isPresent()==installed,"The manual recipe must load exactly when Patchouli is present");
            if(installed){var result=recipe.orElseThrow().getResultItem(h.getLevel().registryAccess());h.assertTrue(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(result.getItem()).toString().equals("patchouli:guide_book")&&result.hasTag()&&result.getTag().getString("patchouli:book").equals("techguns:techguns_manual"),"The result must open the actual Techguns manual");}
        }
        h.succeed();
    }
}
