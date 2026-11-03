package techguns.plugins.jei;

import java.util.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.*;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Ammunition variants and mining heads use the real recipe assembler, including its item data rules. */
final class DynamicCraftingExtension<R extends CraftingRecipe> implements ICraftingCategoryExtension<R> {
    private static ItemStack output(CraftingRecipe recipe,List<ItemStack> ingredients){
        var items=new ArrayList<ItemStack>(9);for(int i=0;i<9;i++)items.add(i<ingredients.size()?ingredients.get(i).copy():ItemStack.EMPTY);
        return recipe.assemble(CraftingInput.of(3,3,items),Minecraft.getInstance().level.registryAccess());
    }
    @Override public void setRecipe(RecipeHolder<R> holder,IRecipeLayoutBuilder builder,ICraftingGridHelper helper,IFocusGroup focuses){
        var recipe=holder.value();
        var choices=recipe.getIngredients().stream().map(i->Arrays.asList(i.getItems())).toList();helper.createAndSetInputs(builder,choices,0,0);
        var result=output(recipe,choices.stream().map(a->a.isEmpty()?ItemStack.EMPTY:a.get(0)).toList());helper.createAndSetOutputs(builder,List.of(result)).setSlotName("techguns-result");builder.setShapeless();
    }
    @Override public void onDisplayedIngredientsUpdate(RecipeHolder<R> holder,List<IRecipeSlotDrawable> slots,IFocusGroup focuses){
        var inputs=slots.stream().filter(s->s.getRole()==RecipeIngredientRole.INPUT).map(s->s.getDisplayedItemStack().orElse(ItemStack.EMPTY)).toList();var result=output(holder.value(),inputs);
        for(var slot:slots)if(slot.getRole()==RecipeIngredientRole.OUTPUT)slot.createDisplayOverrides().addItemStack(result);
    }
    @Override public Optional<ResourceLocation> getRegistryName(RecipeHolder<R> holder){return Optional.of(holder.id());}
}
