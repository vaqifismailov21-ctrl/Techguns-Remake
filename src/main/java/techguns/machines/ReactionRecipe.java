package techguns.machines;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.*;
public record ReactionRecipe(String id,Ingredient input,Item focus,Fluid fluid,ItemStack[] outputs,int ticks,int requiredCompletion,int preferredIntensity,int margin,int liquidLevel,int liquidConsumption,float instability,Risk risk,int energy){
    public enum Risk {NONE,BREAK_ITEM,EXPLOSION_LOW,EXPLOSION_MEDIUM}
    private static List<ReactionRecipe> recipes;
    public static List<ReactionRecipe> all(){if(recipes==null){recipes=new ArrayList<>();GeneratedReactionRecipes.add(recipes);}return Collections.unmodifiableList(recipes);}
    public static ReactionRecipe byId(String id){return all().stream().filter(r->r.id.equals(id)).findFirst().orElse(null);}
    public static Fluid externalOrLava(String name){return BuiltInRegistries.FLUID.stream().toList().stream().filter(f->BuiltInRegistries.FLUID.getKey(f).getPath().equals(name)).findFirst().orElse(Fluids.LAVA);}
    public boolean matches(ProcessingMachine machine,int intensity,int liquidLevel){return input.test(machine.inventory.getStackInSlot(0))&&machine.inventory.getStackInSlot(1).is(focus)&&machine.inputTank.getFluid().getFluid()==fluid&&machine.inputTank.getFluidAmount()==this.liquidLevel*1000&&this.liquidLevel==liquidLevel&&preferredIntensity==intensity;}
}
