package techguns.machines;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
public record ChemicalRecipe(Ingredient[] inputs,int[] amounts,FluidStack fluidInput,FluidStack fluidOutput,ItemStack output,boolean swap,int power) {
    public int[] match(ProcessingMachine machine){if(!fluidInput.isEmpty()&&(!machine.inputTank.getFluid().isFluidEqual(fluidInput)||machine.inputTank.getFluidAmount()<fluidInput.getAmount()))return null;
        if(matches(machine,false))return amounts.clone();if(swap&&matches(machine,true))return new int[]{amounts[1],amounts[0],amounts[2]};return null;}
    private boolean matches(ProcessingMachine machine,boolean swapped){for(int slot=0;slot<3;slot++){int index=swapped&&slot<2?1-slot:slot;var stack=machine.inventory.getStackInSlot(slot);if(amounts[index]==0){if(!stack.isEmpty())return false;}else if(stack.getCount()<amounts[index]||!inputs[index].test(stack))return false;}return true;}
}
