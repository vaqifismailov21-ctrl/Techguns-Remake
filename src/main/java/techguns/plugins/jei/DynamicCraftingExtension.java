package techguns.plugins.jei;

import java.util.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.*;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingRecipe;

/** Ammunition variants and mining heads use the real recipe assembler, including its NBT rules. */
final class DynamicCraftingExtension implements ICraftingCategoryExtension {
    private final CraftingRecipe recipe;
    DynamicCraftingExtension(CraftingRecipe recipe){this.recipe=recipe;}
    private ItemStack output(List<ItemStack> ingredients){
        var menu=new AbstractContainerMenu(null,0){@Override public boolean stillValid(Player player){return true;}@Override public ItemStack quickMoveStack(Player player,int slot){return ItemStack.EMPTY;}};
        var grid=new CraftingContainer(menu,3,3);for(int i=0;i<ingredients.size();i++)grid.setItem(i,ingredients.get(i).copy());
        return recipe.assemble(grid);
    }
    @Override public void setRecipe(IRecipeLayoutBuilder builder,ICraftingGridHelper helper,IFocusGroup focuses){
        var choices=recipe.getIngredients().stream().map(i->Arrays.asList(i.getItems())).toList();helper.createAndSetInputs(builder,choices,0,0);
        var result=output(choices.stream().map(a->a.isEmpty()?ItemStack.EMPTY:a.get(0)).toList());helper.createAndSetOutputs(builder,List.of(result)).setSlotName("techguns-result");builder.setShapeless();
    }
    @Override public void onDisplayedIngredientsUpdate(List<IRecipeSlotDrawable> slots,IFocusGroup focuses){
        var inputs=slots.stream().filter(s->s.getRole()==RecipeIngredientRole.INPUT).map(s->s.getDisplayedItemStack().orElse(ItemStack.EMPTY)).toList();var result=output(inputs);
        for(var slot:slots)if(slot.getRole()==RecipeIngredientRole.OUTPUT)slot.createDisplayOverrides().addItemStack(result);
    }
    @Override public ResourceLocation getRegistryName(){return recipe.getId();}
}
