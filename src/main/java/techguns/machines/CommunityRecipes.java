package techguns.machines;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import techguns.TGItems;

/**
 * Machine recipes for the parts of the late armours (Power Armor Mk2, Nether Combat Armor) and the UV emitter, which
 * the original 1.12 mod never let anyone craft; the progression follows the Techguns community edition:
 * titanium + UV light + lava in the reaction chamber gives hellish ingots, the fabricator turns them into armour plates.
 */
public final class CommunityRecipes {
    private CommunityRecipes(){}
    public static void fabricator(List<ProcessingRecipe> recipes){
        recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{Ingredient.of(TGItems.POWER_ARMOR_PLATING.get()),MachineRecipes.tag("forge:circuits/elite"),Ingredient.of(TGItems.MECHANICAL_PARTS_CARBON.get()),Ingredient.of(TGItems.INGOT_HELLISH.get())},
            new int[]{2,4,2,8},new ItemStack(TGItems.MK2_POWER_PLATE.get(),2),100,80,false));
        recipes.add(new ProcessingRecipe(MachineKind.FABRICATOR,new Ingredient[]{Ingredient.of(TGItems.INGOT_HELLISH.get()),Ingredient.of(TGItems.CARBON_FIBERS.get()),Ingredient.of(TGItems.MECHANICAL_PARTS_CARBON.get()),MachineRecipes.tag("forge:plates/titanium")},
            new int[]{4,4,2,2},new ItemStack(TGItems.DOOM_PLATE.get(),2),100,80,false));
    }
    public static void reaction(List<ReactionRecipe> recipes){
        recipes.add(new ReactionRecipe("RC_HELLISHINGOT",MachineRecipes.tag("forge:ingots/titanium"),TGItems.RC_UV_EMITTER.get(),Fluids.LAVA,
            new ItemStack[]{new ItemStack(TGItems.INGOT_HELLISH.get(),1)},5,3,6,0,7,2000,0f,ReactionRecipe.Risk.EXPLOSION_MEDIUM,100000));
    }
}
