package techguns.machines;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
/** Immutable source operation; consumed stacks and result are copied into the saved in-flight transaction. */
public record ProcessingRecipe(MachineKind kind,Ingredient[] ingredients,int[] amounts,ItemStack result,int ticks,int power,boolean swap) {
    public int[] match(net.minecraftforge.items.IItemHandler inventory){
        if(matches(inventory,false))return amounts.clone();
        if(swap&&ingredients.length==2&&matches(inventory,true))return new int[]{amounts[1],amounts[0]};return null;
    }
    private boolean matches(net.minecraftforge.items.IItemHandler inventory,boolean reversed){
        for(int i=0;i<ingredients.length;i++){int j=reversed?1-i:i;var stack=inventory.getStackInSlot(i);if(stack.getCount()<amounts[j]||!ingredients[j].test(stack))return false;}return true;
    }
}
